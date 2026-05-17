package com.example.sonix.presentation.ui.screens
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sonix.presentation.state.HomeTab
import com.example.sonix.presentation.state.SortOrder
import com.example.sonix.presentation.ui.components.AlbumItem
import com.example.sonix.presentation.ui.components.ArtistItem
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    onOpenPlayer: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val progress = if (state.duration > 0)
        state.currentPosition.toFloat() / state.duration else 0f
    val listState = rememberLazyListState()
    var showSortMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val tabs = HomeTab.entries
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    // Keep ViewModel tab state in sync when user swipes
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            viewModel.onTabSelected(tabs[page])
        }
    }

    // Keep pager in sync when tab is tapped
    LaunchedEffect(state.selectedTab) {
        val targetPage = tabs.indexOf(state.selectedTab)
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

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

            // ── Header ────────────────────────────────────────────────
            Text(
                text = "Sonix",
                style = MaterialTheme.typography.displayLarge,
                color = Lime,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(16.dp))

            // ── Search with X button ──────────────────────────────────
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Search songs, artists, albums", color = White30) },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = White60)
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Clear search",
                                tint = White60,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
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

            // ── Tabs ──────────────────────────────────────────────────
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Black100,
                contentColor = Lime,
                indicator = {},
                divider = {},
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                tabs.forEachIndexed { index, tab ->
                    val selected = pagerState.currentPage == index
                    Tab(
                        selected = selected,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index) }
                            viewModel.onTabSelected(tab)
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) Lime else Black40)
                    ) {
                        Text(
                            text = tab.name.lowercase()
                                .replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(
                                vertical = 8.dp, horizontal = 4.dp
                            ),
                            color = if (selected) Black100 else White60,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Swipeable pager ───────────────────────────────────────
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Lime)
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = true
                ) { page ->
                    when (tabs[page]) {

                        // ── Songs ─────────────────────────────────────
                        HomeTab.SONGS -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (state.searchQuery.isBlank())
                                            "All Songs" else "Results",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = White100
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${state.filteredSongs.size} songs",
                                            style = MaterialTheme.typography.labelSmall
                                                .copy(fontSize = 12.sp),
                                            color = White60
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Box {
                                            IconButton(
                                                onClick = { showSortMenu = true },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    Icons.Rounded.Sort,
                                                    contentDescription = "Sort",
                                                    tint = if (state.sortOrder != SortOrder.A_TO_Z)
                                                        Lime else White60,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            DropdownMenu(
                                                expanded = showSortMenu,
                                                onDismissRequest = { showSortMenu = false },
                                                modifier = Modifier.background(Black60)
                                            ) {
                                                listOf(
                                                    SortOrder.A_TO_Z to "A → Z",
                                                    SortOrder.Z_TO_A to "Z → A",
                                                    SortOrder.DATE_ADDED_NEWEST to "Newest First",
                                                    SortOrder.DATE_ADDED_OLDEST to "Oldest First"
                                                ).forEach { (order, label) ->
                                                    DropdownMenuItem(
                                                        text = {
                                                            Text(
                                                                text = label,
                                                                color = if (state.sortOrder == order)
                                                                    Lime else White100,
                                                                style = MaterialTheme.typography
                                                                    .bodyMedium
                                                            )
                                                        },
                                                        onClick = {
                                                            viewModel.onSortOrderChange(order)
                                                            showSortMenu = false
                                                        },
                                                        modifier = Modifier.background(
                                                            if (state.sortOrder == order)
                                                                Black40 else Color.Transparent
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                LazyColumn(
                                    state = listState,
                                    contentPadding = PaddingValues(
                                        horizontal = 20.dp, vertical = 4.dp
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(
                                        items = state.filteredSongs,
                                        key = { it.id }
                                    ) { song ->
                                        SongListItem(
                                            song = song,
                                            isActive = state.currentSong?.id == song.id,
                                            onClick = { viewModel.playSong(song) },
                                            onDelete = { viewModel.deleteSong(it) },
                                            onShare = { viewModel.shareSong(it) }
                                        )
                                    }
                                    item { Spacer(Modifier.height(8.dp)) }
                                }
                            }
                        }

                        // ── Artists ───────────────────────────────────
                        HomeTab.ARTISTS -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Artists",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = White100
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "${state.artists.size} artists",
                                        style = MaterialTheme.typography.labelSmall
                                            .copy(fontSize = 12.sp),
                                        color = White60
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    contentPadding = PaddingValues(
                                        horizontal = 20.dp, vertical = 4.dp
                                    ),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(
                                        items = state.artists,
                                        key = { it.name }
                                    ) { artist ->
                                        ArtistItem(
                                            artist = artist,
                                            onClick = { viewModel.playArtist(artist) }
                                        )
                                    }
                                }
                            }
                        }

                        // ── Albums ────────────────────────────────────
                        HomeTab.ALBUMS -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Albums",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = White100
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "${state.albums.size} albums",
                                        style = MaterialTheme.typography.labelSmall
                                            .copy(fontSize = 12.sp),
                                        color = White60
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    contentPadding = PaddingValues(
                                        horizontal = 20.dp, vertical = 4.dp
                                    ),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(
                                        items = state.albums,
                                        key = { it.name }
                                    ) { album ->
                                        AlbumItem(
                                            album = album,
                                            onClick = { viewModel.playAlbum(album) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}