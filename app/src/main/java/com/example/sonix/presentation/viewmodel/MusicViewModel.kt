package com.example.sonix.presentation.viewmodel
import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.sonix.data.model.Song
import com.example.sonix.domain.usecase.GetAllSongsUseCase
import com.example.sonix.domain.usecase.SearchSongsUseCase
import com.example.sonix.domain.usecase.SyncMusicUseCase
import com.example.sonix.presentation.state.HomeTab
import com.example.sonix.presentation.state.MusicUiState
import com.example.sonix.presentation.state.RepeatMode
import com.example.sonix.service.MusicService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MusicViewModel(
    private val getAllSongsUseCase: GetAllSongsUseCase,
    private val searchSongsUseCase: SearchSongsUseCase,
    private val syncMusicUseCase: SyncMusicUseCase,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MusicUiState())
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    private var controller: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var progressJob: Job? = null
    private var playlist: List<Song> = emptyList()
    private var songsJobStarted = false

    init {
        initMediaController()
        // Load whatever is already in the DB immediately (handles relaunch case)
        loadSongsFromDb()
    }

    private fun initMediaController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            controller = controllerFuture?.get()
            controller?.addListener(playerListener)
        }, MoreExecutors.directExecutor())
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) startProgressTracking() else progressJob?.cancel()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val index = controller?.currentMediaItemIndex ?: return
            if (index in playlist.indices) {
                _uiState.update { it.copy(currentSong = playlist[index]) }
            }
        }
    }

    // Called on first launch after permission is granted
    fun syncAfterPermission() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                syncMusicUseCase()   // writes songs from MediaStore into Room
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Could not read music library", isLoading = false) }
            }
            // loadSongsFromDb() is already collecting — Room Flow will emit automatically
            // after upsert, so we just clear the loading flag
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    // Collects the Room Flow — stays active for the lifetime of the ViewModel
    private fun loadSongsFromDb() {
        if (songsJobStarted) return
        songsJobStarted = true
        viewModelScope.launch {
            getAllSongsUseCase().collect { songs ->
                playlist = songs
                _uiState.update {
                    it.copy(
                        allSongs = songs,
                        filteredSongs = songs,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun playSong(song: Song) {
        val index = playlist.indexOfFirst { it.id == song.id }
        if (index == -1) return
        controller?.apply {
            setMediaItems(playlist.map { MediaItem.fromUri(it.uri) }, index, 0L)
            prepare()
            play()
        }
        _uiState.update { it.copy(currentSong = song, isPlaying = true) }
        startProgressTracking()
    }

    fun togglePlayPause() {
        controller?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun skipNext() { controller?.seekToNextMediaItem() }

    fun skipPrevious() {
        controller?.let {
            if (it.currentPosition > 3000L) it.seekTo(0L)
            else it.seekToPreviousMediaItem()
        }
    }

    fun seekTo(position: Long) {
        controller?.seekTo(position)
        _uiState.update { it.copy(currentPosition = position) }
    }

    fun toggleShuffle() {
        val newVal = !_uiState.value.shuffle
        controller?.shuffleModeEnabled = newVal
        _uiState.update { it.copy(shuffle = newVal) }
    }

    fun toggleRepeat() {
        val next = when (_uiState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        controller?.repeatMode = when (next) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
        _uiState.update { it.copy(repeatMode = next) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            if (query.isBlank()) {
                _uiState.update { it.copy(filteredSongs = playlist) }
            } else {
                searchSongsUseCase(query).collect { results ->
                    _uiState.update { it.copy(filteredSongs = results) }
                }
            }
        }
    }

    fun onTabSelected(tab: HomeTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                val pos = controller?.currentPosition ?: 0L
                val dur = controller?.duration?.coerceAtLeast(0L) ?: 0L
                _uiState.update { it.copy(currentPosition = pos, duration = dur) }
                delay(500L)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        progressJob?.cancel()
        controller?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val appContext = context.applicationContext
            val db = com.example.sonix.data.local.SonixDatabase.getInstance(appContext)
            val mediaStoreHelper = com.example.sonix.util.MediaStoreHelper(appContext)
            val repo = com.example.sonix.data.repository.MusicRepositoryImpl(
                db.songDao(), mediaStoreHelper
            )
            @Suppress("UNCHECKED_CAST")
            return MusicViewModel(
                GetAllSongsUseCase(repo),
                SearchSongsUseCase(repo),
                SyncMusicUseCase(repo),
                appContext
            ) as T
        }
    }
}