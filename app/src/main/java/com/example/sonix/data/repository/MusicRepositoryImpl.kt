package com.example.sonix.data.repository
import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.example.sonix.data.local.dao.SongDao
import com.example.sonix.data.mapper.SongMapper.toDomain
import com.example.sonix.data.mapper.SongMapper.toEntity
import com.example.sonix.data.model.Song
import com.example.sonix.domain.repository.DeleteResult
import com.example.sonix.domain.repository.MusicRepository
import com.example.sonix.util.MediaStoreHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MusicRepositoryImpl(
    private val dao: SongDao,
    private val mediaStoreHelper: MediaStoreHelper,
    private val context: Context
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

    override suspend fun deleteSong(song: Song): DeleteResult {
        val uri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            song.id
        )
        return when {
            // ── Android 11+ (API 30+) ─────────────────────────────────
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                try {
                    val deleted = context.contentResolver.delete(uri, null, null)
                    if (deleted > 0) {
                        dao.deleteSongById(song.id)
                        DeleteResult.Success
                    } else {
                        // Need to create a delete request for system confirmation
                        val pendingIntent = MediaStore.createDeleteRequest(
                            context.contentResolver,
                            listOf(uri)
                        )
                        DeleteResult.RequiresPermission(pendingIntent, song)
                    }
                } catch (e: Exception) {
                    try {
                        val pendingIntent = MediaStore.createDeleteRequest(
                            context.contentResolver,
                            listOf(uri)
                        )
                        DeleteResult.RequiresPermission(pendingIntent, song)
                    } catch (ex: Exception) {
                        DeleteResult.Failure
                    }
                }
            }
            // ── Android 10 (API 29) ───────────────────────────────────
            Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> {
                try {
                    val deleted = context.contentResolver.delete(uri, null, null)
                    if (deleted > 0) {
                        dao.deleteSongById(song.id)
                        DeleteResult.Success
                    } else {
                        DeleteResult.Failure
                    }
                } catch (e: android.app.RecoverableSecurityException) {
                    DeleteResult.RequiresPermission(
                        e.userAction.actionIntent,
                        song
                    )
                } catch (e: Exception) {
                    DeleteResult.Failure
                }
            }
            // Android 9 and below (API 28-)
            else -> {
                try {
                    val deleted = context.contentResolver.delete(uri, null, null)
                    if (deleted > 0) {
                        dao.deleteSongById(song.id)
                        DeleteResult.Success
                    } else {
                        DeleteResult.Failure
                    }
                } catch (e: Exception) {
                    DeleteResult.Failure
                }
            }
        }
    }
}