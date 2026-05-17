package com.example.sonix.presentation.ui.components
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.example.sonix.data.model.Song
import com.example.sonix.presentation.ui.theme.Black100
import com.example.sonix.presentation.ui.theme.Black20
import com.example.sonix.presentation.ui.theme.Black40
import com.example.sonix.presentation.ui.theme.Black60
import com.example.sonix.presentation.ui.theme.Lime
import com.example.sonix.presentation.ui.theme.White100
import com.example.sonix.presentation.ui.theme.White30
import com.example.sonix.presentation.ui.theme.White60
import com.example.sonix.util.toFormattedDuration

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongListItem(
    song: Song,
    isActive: Boolean,
    onClick: () -> Unit,
    onDelete: (Song) -> Unit,
    onShare: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isActive) Black40 else Black100,
        label = "bg"
    )

    var imageLoadFailed by remember(song.id) { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // ── Delete confirmation dialog ────────────────────────────────────
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Black60,
            title = {
                Text("Delete Song", color = White100,
                    style = MaterialTheme.typography.titleMedium)
            },
            text = {
                Text(
                    "\"${song.title}\" will be permanently deleted from your device.",
                    color = White60,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(song)
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = Color(0xFFFF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = White60)
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showMenu = true }
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Black40),
                contentAlignment = Alignment.Center
            ) {
                if (!imageLoadFailed && song.albumArtUri != null) {
                    AsyncImage(
                        model = song.albumArtUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        onState = { state ->
                            if (state is AsyncImagePainter.State.Error) {
                                imageLoadFailed = true
                            }
                        }
                    )
                }
                if (imageLoadFailed || song.albumArtUri == null) {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = Lime,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isActive) Lime else White100,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = White60,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = song.duration.toFormattedDuration(),
                style = MaterialTheme.typography.labelSmall,
                color = White30
            )
        }

        // ── Context menu (long press) ─────────────────────────────────
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(Black60)
        ) {
            DropdownMenuItem(
                text = {
                    Text("Share", color = White100,
                        style = MaterialTheme.typography.bodyMedium)
                },
                leadingIcon = {
                    Icon(Icons.Rounded.Share, contentDescription = null, tint = White60)
                },
                onClick = {
                    onShare(song)
                    showMenu = false
                }
            )
            DropdownMenuItem(
                text = {
                    Text("Delete", color = Color(0xFFFF4444),
                        style = MaterialTheme.typography.bodyMedium)
                },
                leadingIcon = {
                    Icon(Icons.Rounded.Delete, contentDescription = null,
                        tint = Color(0xFFFF4444))
                },
                onClick = {
                    showMenu = false
                    showDeleteDialog = true
                }
            )
        }
    }
}