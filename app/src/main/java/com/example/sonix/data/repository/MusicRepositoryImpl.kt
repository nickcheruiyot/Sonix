package com.example.sonix.data.repository
import com.example.sonix.data.local.dao.SongDao
import com.example.sonix.data.mapper.SongMapper.toDomain
import com.example.sonix.data.mapper.SongMapper.toEntity
import com.example.sonix.data.model.Song
import com.example.sonix.domain.repository.MusicRepository
import com.example.sonix.util.MediaStoreHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MusicRepositoryImpl(
    private val dao: SongDao,
    private val mediaStoreHelper: MediaStoreHelper
) : MusicRepository {

    override fun getAllSongs(): Flow<List<Song>> =
        dao.getAllSongs().map { list -> list.map { it.toDomain() } }

    override fun searchSongs(query: String): Flow<List<Song>> =
        dao.searchSongs(query).map { list -> list.map { it.toDomain() } }

    override fun getAllArtists(): Flow<List<String>> =
        dao.getAllArtists()

    override fun getAllAlbums(): Flow<List<String>> =
        dao.getAllAlbums()

    override fun getSongsByArtist(artist: String): Flow<List<Song>> =
        dao.getSongsByArtist(artist).map { list -> list.map { it.toDomain() } }

    override fun getSongsByAlbum(album: String): Flow<List<Song>> =
        dao.getSongsByAlbum(album).map { list -> list.map { it.toDomain() } }

    override suspend fun syncWithMediaStore() {
        val songs = mediaStoreHelper.fetchAllSongs()
        dao.clearAll()
        dao.upsertAll(songs.map { it.toEntity() })
    }
}