package com.example.sonix.presentation.state
import android.app.PendingIntent
import com.example.sonix.data.model.Song

enum class SortOrder {
    A_TO_Z,
    Z_TO_A,
    DATE_ADDED_NEWEST,
    DATE_ADDED_OLDEST
}

data class Album(
    val name: String,
    val artist: String,
    val songCount: Int,
    val albumArtUri: String?,
    val songs: List<Song>
)

data class Artist(
    val name: String,
    val songCount: Int,
    val albumArtUri: String?,
    val songs: List<Song>
)

data class MusicUiState(
    val allSongs: List<Song> = emptyList(),
    val filteredSongs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val shuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val selectedTab: HomeTab = HomeTab.SONGS,
    val sortOrder: SortOrder = SortOrder.A_TO_Z,
    val pendingDeleteIntent: PendingIntent? = null,
    val pendingDeleteSong: Song? = null
)

sealed class RepeatMode {
    object OFF : RepeatMode()
    object ONE : RepeatMode()
    object ALL : RepeatMode()
}

enum class HomeTab { SONGS, ARTISTS, ALBUMS }