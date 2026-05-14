package com.example.sonix.presentation.state
import com.example.sonix.data.model.Song

data class MusicUiState(
    val allSongs: List<Song> = emptyList(),
    val filteredSongs: List<Song> = emptyList(),
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val shuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val selectedTab: HomeTab = HomeTab.SONGS
)

sealed class RepeatMode {
    object OFF : RepeatMode()
    object ONE : RepeatMode()
    object ALL : RepeatMode()
}

enum class HomeTab { SONGS, ARTISTS, ALBUMS }