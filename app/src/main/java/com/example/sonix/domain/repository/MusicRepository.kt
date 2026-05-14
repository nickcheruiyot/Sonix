package com.example.sonix.domain.repository

import com.example.sonix.data.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun getAllSongs(): Flow<List<Song>>
    fun searchSongs(query: String): Flow<List<Song>>
    fun getAllArtists(): Flow<List<String>>
    fun getAllAlbums(): Flow<List<String>>
    fun getSongsByArtist(artist: String): Flow<List<Song>>
    fun getSongsByAlbum(album: String): Flow<List<Song>>
    suspend fun syncWithMediaStore()
}