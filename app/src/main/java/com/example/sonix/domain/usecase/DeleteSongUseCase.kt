package com.example.sonix.domain.usecase
import com.example.sonix.data.model.Song
import com.example.sonix.domain.repository.DeleteResult
import com.example.sonix.domain.repository.MusicRepository

class DeleteSongUseCase(private val repository: MusicRepository) {
    suspend operator fun invoke(song: Song): DeleteResult =
        repository.deleteSong(song)
}