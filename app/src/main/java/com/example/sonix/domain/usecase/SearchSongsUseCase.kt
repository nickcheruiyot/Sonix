package com.example.sonix.domain.usecase
import com.example.sonix.data.model.Song
import com.example.sonix.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
class SearchSongsUseCase(private val repository: MusicRepository) {
    operator fun invoke(query: String): Flow<List<Song>> =
        repository.searchSongs(query)
}