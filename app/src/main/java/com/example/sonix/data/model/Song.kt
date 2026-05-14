package com.example.sonix.data.model

data class Song( val id: Long,
                 val title: String,
                 val artist: String,
                 val album: String,
                 val duration: Long,          // milliseconds
                 val uri: String,             // file path
                 val albumArtUri: String?,    // content URI for album art
                 val dateAdded: Long)
