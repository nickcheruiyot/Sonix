package com.example.sonix.presentation.viewmodel
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.sonix.data.model.Song
import com.example.sonix.domain.repository.DeleteResult
import com.example.sonix.domain.usecase.DeleteSongUseCase
import com.example.sonix.domain.usecase.GetAllSongsUseCase
import com.example.sonix.domain.usecase.SearchSongsUseCase
import com.example.sonix.domain.usecase.SyncMusicUseCase
import com.example.sonix.presentation.state.Album
import com.example.sonix.presentation.state.Artist
import com.example.sonix.presentation.state.HomeTab
import com.example.sonix.presentation.state.MusicUiState
import com.example.sonix.presentation.state.RepeatMode
import com.example.sonix.presentation.state.SortOrder
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
import java.io.File

class MusicViewModel(
    private val getAllSongsUseCase: GetAllSongsUseCase,
    private val searchSongsUseCase: SearchSongsUseCase,
    private val syncMusicUseCase: SyncMusicUseCase,
    private val deleteSongUseCase: DeleteSongUseCase,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MusicUiState())
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    private var controller: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var progressJob: Job? = null
    private var playlist: List<Song> = emptyList()
    private var songsJobStarted = false
    private var pendingRestoreIndex: Int = -1
    private var pendingRestoreIsPlaying: Boolean = false

    init {
        initMediaController()
        loadSongsFromDb()
    }

    private fun initMediaController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            val ctrl = controllerFuture?.get() ?: return@addListener
            controller = ctrl
            ctrl.addListener(playerListener)
            restorePlaybackState(ctrl)
        }, MoreExecutors.directExecutor())
    }

    private fun restorePlaybackState(ctrl: MediaController) {
        val index = ctrl.currentMediaItemIndex
        val isPlaying = ctrl.isPlaying || ctrl.playWhenReady

        if (playlist.isNotEmpty() && index in playlist.indices) {
            val song = playlist[index]
            _uiState.update {
                it.copy(
                    currentSong = song,
                    isPlaying = isPlaying,
                    duration = ctrl.duration.coerceAtLeast(0L)
                )
            }
            if (isPlaying) startProgressTracking()
        } else {
            pendingRestoreIndex = index
            pendingRestoreIsPlaying = isPlaying
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) startProgressTracking() else progressJob?.cancel()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            // Keep uiState in sync when player reaches end or buffers
            _uiState.update {
                it.copy(isPlaying = controller?.isPlaying == true)
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val index = controller?.currentMediaItemIndex ?: return
            if (index in playlist.indices) {
                _uiState.update { it.copy(currentSong = playlist[index]) }
            }
        }
    }

    fun syncAfterPermission() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                syncMusicUseCase()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Could not read music library", isLoading = false)
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadSongsFromDb() {
        if (songsJobStarted) return
        songsJobStarted = true
        viewModelScope.launch {
            getAllSongsUseCase().collect { songs ->
                val sorted = applySortOrder(songs, _uiState.value.sortOrder)
                playlist = sorted

                val albums = songs
                    .groupBy { it.album }
                    .map { (name, s) ->
                        Album(name, s.first().artist, s.size, s.first().albumArtUri, s)
                    }
                    .sortedBy { it.name.lowercase() }

                val artists = songs
                    .groupBy { it.artist }
                    .map { (name, s) ->
                        Artist(name, s.size, s.first().albumArtUri, s)
                    }
                    .sortedBy { it.name.lowercase() }

                _uiState.update {
                    it.copy(
                        allSongs = songs,
                        filteredSongs = sorted,
                        albums = albums,
                        artists = artists,
                        isLoading = false
                    )
                }

                if (pendingRestoreIndex >= 0 &&
                    pendingRestoreIndex in playlist.indices
                ) {
                    val song = playlist[pendingRestoreIndex]
                    _uiState.update {
                        it.copy(
                            currentSong = song,
                            isPlaying = pendingRestoreIsPlaying
                        )
                    }
                    if (pendingRestoreIsPlaying) startProgressTracking()
                    pendingRestoreIndex = -1
                    pendingRestoreIsPlaying = false
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

    fun playAlbum(album: Album) {
        if (album.songs.isEmpty()) return
        playlist = album.songs
        controller?.apply {
            setMediaItems(album.songs.map { MediaItem.fromUri(it.uri) }, 0, 0L)
            prepare()
            play()
        }
        _uiState.update { it.copy(currentSong = album.songs.first(), isPlaying = true) }
        startProgressTracking()
    }

    fun playArtist(artist: Artist) {
        if (artist.songs.isEmpty()) return
        playlist = artist.songs
        controller?.apply {
            setMediaItems(artist.songs.map { MediaItem.fromUri(it.uri) }, 0, 0L)
            prepare()
            play()
        }
        _uiState.update { it.copy(currentSong = artist.songs.first(), isPlaying = true) }
        startProgressTracking()
    }

    fun togglePlayPause() {
        val ctrl = controller ?: return
        if (ctrl.isPlaying) {
            ctrl.pause()
        } else {
            // If controller has media items ready — just play
            if (ctrl.mediaItemCount > 0) {
                ctrl.play()
            } else {
                val current = _uiState.value.currentSong
                if (current != null && playlist.isNotEmpty()) {
                    val index = playlist.indexOfFirst { it.id == current.id }
                    val resumeIndex = if (index >= 0) index else 0
                    val position = ctrl.currentPosition.coerceAtLeast(0L)
                    ctrl.setMediaItems(
                        playlist.map { MediaItem.fromUri(it.uri) },
                        resumeIndex,
                        position
                    )
                    ctrl.prepare()
                    ctrl.play()
                }
            }
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
                val sorted = applySortOrder(
                    _uiState.value.allSongs, _uiState.value.sortOrder
                )
                _uiState.update { it.copy(filteredSongs = sorted) }
            } else {
                searchSongsUseCase(query).collect { results ->
                    val sorted = applySortOrder(results, _uiState.value.sortOrder)
                    _uiState.update { it.copy(filteredSongs = sorted) }
                }
            }
        }
    }

    fun onSortOrderChange(order: SortOrder) {
        val sorted = applySortOrder(_uiState.value.allSongs, order)
        playlist = sorted
        _uiState.update { it.copy(sortOrder = order, filteredSongs = sorted) }
    }

    fun onTabSelected(tab: HomeTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch {
            if (_uiState.value.currentSong?.id == song.id) {
                controller?.stop()
                _uiState.update { it.copy(currentSong = null, isPlaying = false) }
            }
            when (val result = deleteSongUseCase(song)) {
                is DeleteResult.Success -> {}
                is DeleteResult.RequiresPermission -> {
                    _uiState.update {
                        it.copy(
                            pendingDeleteIntent = result.pendingIntent,
                            pendingDeleteSong = result.song
                        )
                    }
                }
                is DeleteResult.Failure -> {
                    _uiState.update {
                        it.copy(error = "Could not delete \"${song.title}\"")
                    }
                }
            }
        }
    }

    fun retryPendingDelete() {
        val song = _uiState.value.pendingDeleteSong ?: return
        _uiState.update { it.copy(pendingDeleteIntent = null, pendingDeleteSong = null) }
        deleteSong(song)
    }

    fun clearPendingDelete() {
        _uiState.update { it.copy(pendingDeleteIntent = null, pendingDeleteSong = null) }
    }

    fun shareSong(song: Song) {
        try {
            val file = File(song.uri)
            val uri: Uri = if (file.exists()) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
            } else {
                android.content.ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    song.id
                )
            }
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, song.title)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                Intent.createChooser(intent, "Share ${song.title}").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Could not share \"${song.title}\"") }
        }
    }

    private fun applySortOrder(songs: List<Song>, order: SortOrder): List<Song> =
        when (order) {
            SortOrder.A_TO_Z            -> songs.sortedBy { it.title.lowercase() }
            SortOrder.Z_TO_A            -> songs.sortedByDescending { it.title.lowercase() }
            SortOrder.DATE_ADDED_NEWEST -> songs.sortedByDescending { it.dateAdded }
            SortOrder.DATE_ADDED_OLDEST -> songs.sortedBy { it.dateAdded }
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
                db.songDao(), mediaStoreHelper, appContext
            )
            @Suppress("UNCHECKED_CAST")
            return MusicViewModel(
                GetAllSongsUseCase(repo),
                SearchSongsUseCase(repo),
                SyncMusicUseCase(repo),
                DeleteSongUseCase(repo),
                appContext
            ) as T
        }
    }
}