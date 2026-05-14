package com.example.sonix.data.mapper
import com.example.sonix.data.local.entity.SongEntity
import com.example.sonix.data.model.Song

object SongMapper {

    fun SongEntity.toDomain(): Song = Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        uri = uri,
        albumArtUri = albumArtUri,
        dateAdded = dateAdded
    )

    fun Song.toEntity(): SongEntity = SongEntity(
        id = id,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        uri = uri,
        albumArtUri = albumArtUri,
        dateAdded = dateAdded
    )
}