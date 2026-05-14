package com.example.sonix.presentation.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sonix.presentation.state.HomeTab
import com.example.sonix.presentation.ui.components.MiniPlayer
import com.example.sonix.presentation.ui.components.SongListItem
import com.example.sonix.presentation.ui.theme.Black100
import com.example.sonix.presentation.ui.theme.Black20
import com.example.sonix.presentation.ui.theme.Black40
import com.example.sonix.presentation.ui.theme.Black60
import com.example.sonix.presentation.ui.theme.Lime
import com.example.sonix.presentation.ui.theme.White100
import com.example.sonix.presentation.ui.theme.White30
import com.example.sonix.presentation.ui.theme.White60
import com.example.sonix.presentation.viewmodel.MusicViewModel

@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    onOpenPlayer: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val progress = if (state.duration > 0) state.currentPosition.toFloat() / state.duration else 0f

    // Persisted scroll state — prevents scroll position reset on recomposition
    val listState = rememberLazyListState()

    Scaffold(
        containerColor = Black100,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                state.currentSong?.let { song ->
                    MiniPlayer(
                        song = song,
                        isPlaying = state.isPlaying,
                        progress = progress,
                        onPlayPause = viewModel::togglePlayPause,
                        onSkipPrevious = viewModel::skipPrevious,
                        onSkipNext = viewModel::skipNext,
                        onExpand = onOpenPlayer
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Black100)
        ) {
            Spacer(Modifier.height(16.dp))

            // Header
            Text(
                text = "Sonix",
                style = MaterialTheme.typography.displayLarge,
                color = Lime,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Search field
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Search songs, artists, albums", color = White30) },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = null,
                        tint = White60
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Black20,
                    focusedBorderColor = Lime,
                    unfocusedContainerColor = Black60,
                    focusedContainerColor = Black60,
                    cursorColor = Lime,
                    unfocusedTextColor = White100,
                    focusedTextColor = White100
                )
            )

            Spacer(Modifier.height(16.dp))

            // Tabs
            TabRow(
                selectedTabIndex = state.selectedTab.ordinal,
                containerColor = Black100,
                contentColor = Lime,
                indicator = {},
                divider = {},
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                HomeTab.entries.forEach { tab ->
                    val selected = state.selectedTab == tab
                    Tab(
                        selected = selected,
                        onClick = { viewModel.onTabSelected(tab) },
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) Lime else Black40)
                    ) {
                        Text(
                            text = tab.name.lowercase()
                                .replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(
                                vertical = 8.dp,
                                horizontal = 4.dp
                            ),
                            color = if (selected) Black100 else White60,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Content
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Lime)
                }
            } else {
                // Song count row — only shown on Songs tab
                if (state.selectedTab == HomeTab.SONGS) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (state.searchQuery.isBlank())
                                "All Songs"
                            else
                                "Results",
                            style = MaterialTheme.typography.titleMedium,
                            color = White100
                        )
                        Text(
                            text = "${state.filteredSongs.size} songs",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                            color = White60
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                }

                LazyColumn(
                    // rememberLazyListState keeps scroll position stable
                    // so fling/fast scroll is not interrupted by recomposition
                    state = listState,
                    contentPadding = PaddingValues(
                        horizontal = 20.dp,
                        vertical = 4.dp
                    ),
                    // spacedBy with no extra modifiers is the fastest spacing approach
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    // flingBehavior default is fine — do NOT override it,
                    // overriding with a custom fling is what causes sluggishness
                ) {
                    items(
                        items = state.filteredSongs,
                        // stable key prevents full list recomposition when
                        // only one item changes (e.g. active song highlight)
                        key = { it.id }
                    ) { song ->
                        SongListItem(
                            song = song,
                            isActive = state.currentSong?.id == song.id,
                            onClick = { viewModel.playSong(song) }
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}