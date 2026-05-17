package com.example.sonix.presentation.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.example.sonix.presentation.state.Artist
import com.example.sonix.presentation.ui.theme.Black40
import com.example.sonix.presentation.ui.theme.Lime
import com.example.sonix.presentation.ui.theme.White60
import com.example.sonix.presentation.ui.theme.White100
@Composable
fun ArtistItem(
    artist: Artist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var imageLoadFailed by remember(artist.name) { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Artist image — circle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clip(CircleShape)
                .background(Black40)
                .then(Modifier.size(80.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (!imageLoadFailed && artist.albumArtUri != null) {
                AsyncImage(
                    model = artist.albumArtUri,
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
            if (imageLoadFailed || artist.albumArtUri == null) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = Lime,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = artist.name,
            style = MaterialTheme.typography.titleMedium,
            color = White100,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Text(
            text = "${artist.songCount} songs",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = White60,
            textAlign = TextAlign.Center
        )
    }
}