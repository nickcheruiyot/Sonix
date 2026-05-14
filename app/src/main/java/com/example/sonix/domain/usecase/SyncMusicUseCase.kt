package com.example.sonix.domain.usecase

import com.example.sonix.domain.repository.MusicRepository

class SyncMusicUseCase(private val repository: MusicRepository) {
    suspend operator fun invoke() = repository.syncWithMediaStore()
}