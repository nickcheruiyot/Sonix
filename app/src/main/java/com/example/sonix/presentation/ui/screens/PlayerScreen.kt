package com.example.sonix.presentation.ui.screens
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode as ComposeRepeatMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.sonix.presentation.state.RepeatMode
import com.example.sonix.presentation.ui.theme.Black100
import com.example.sonix.presentation.ui.theme.Black20
import com.example.sonix.presentation.ui.theme.Black40
import com.example.sonix.presentation.ui.theme.Black60
import com.example.sonix.presentation.ui.theme.Lime
import com.example.sonix.presentation.ui.theme.LimeDim
import com.example.sonix.presentation.ui.theme.White100
import com.example.sonix.presentation.ui.theme.White30
import com.example.sonix.presentation.ui.theme.White60
import com.example.sonix.presentation.viewmodel.MusicViewModel
import com.example.sonix.util.toFormattedDuration
@Composable
fun PlayerScreen(
    viewModel: MusicViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val song = state.currentSong ?: return

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = ComposeRepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val arcAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = ComposeRepeatMode.Restart
        ),
        label = "arcAngle"
    )

    val progress = if (state.duration > 0)
        state.currentPosition.toFloat() / state.duration else 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black100)
    ) {
        // Blurred album art background
        AsyncImage(
            model = song.albumArtUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(70.dp)
        )
        // Dark scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Black100.copy(alpha = 0.84f))
        )
        // Top lime wash
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            LimeDim.copy(alpha = 0.09f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Main content — statusBarsPadding at top, navigationBarsPadding
        // at bottom so nothing overlaps system bars on either end
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            //  TOP SECTION
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Spacer(Modifier.height(8.dp))

                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                            contentDescription = "Back",
                            tint = White60,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NOW PLAYING",
                            style = MaterialTheme.typography.labelSmall,
                            color = White30,
                            letterSpacing = 3.sp,
                            fontSize = 9.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = song.album,
                            style = MaterialTheme.typography.labelSmall,
                            color = White60,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(Modifier.size(48.dp))
                }
            }

            //  MIDDLE SECTION — headphone + song info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {

                // Stylish headphone artwork
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(210.dp)
                ) {
                    // Spinning arc ring — only when playing
                    if (state.isPlaying) {
                        Box(
                            modifier = Modifier
                                .size(210.dp)
                                .drawBehind {
                                    val r = size.minDimension / 2f
                                    drawCircle(
                                        color = Lime.copy(alpha = 0.12f),
                                        radius = r,
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                    drawArc(
                                        brush = Brush.sweepGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Lime.copy(alpha = 0.5f),
                                                Lime,
                                                Lime.copy(alpha = 0.5f),
                                                Color.Transparent
                                            ),
                                            center = Offset(r, r)
                                        ),
                                        startAngle = arcAngle,
                                        sweepAngle = 120f,
                                        useCenter = false,
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                }
                        )
                    }

                    // Outer glow
                    Box(
                        modifier = Modifier
                            .size(188.dp)
                            .shadow(
                                elevation = if (state.isPlaying) 36.dp else 12.dp,
                                shape = CircleShape,
                                ambientColor = Lime.copy(
                                    alpha = if (state.isPlaying) pulseAlpha else 0.08f
                                ),
                                spotColor = Lime.copy(
                                    alpha = if (state.isPlaying) pulseAlpha else 0.08f
                                )
                            )
                            .clip(CircleShape)
                            .background(Color.Transparent)
                    )

                    // Frosted glass circle
                    Box(
                        modifier = Modifier
                            .size(178.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Black60.copy(alpha = 0.95f),
                                        Black40.copy(alpha = 0.98f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Inner ring detail
                        Box(
                            modifier = Modifier
                                .size(158.dp)
                                .drawBehind {
                                    drawCircle(
                                        color = Lime.copy(alpha = 0.06f),
                                        radius = size.minDimension / 2f,
                                        style = Stroke(width = 1.dp.toPx())
                                    )
                                }
                        )
                        Icon(
                            imageVector = Icons.Rounded.Headphones,
                            contentDescription = null,
                            tint = Lime,
                            modifier = Modifier.size(88.dp)
                        )
                        if (state.isPlaying) {
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = null,
                                tint = Lime.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .size(18.dp)
                                    .align(Alignment.BottomCenter)
                                    .offset(y = (-16).dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                // Song title — marquee scrolls if too long
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = White100,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .basicMarquee()
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = White60,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .basicMarquee()
                )
            }

            // ── BOTTOM SECTION — seekbar + controls ───────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Seek bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val thumbR = 5.dp
                        val thumbOffset = (maxWidth - thumbR * 2) *
                                progress.coerceIn(0f, 1f)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(Black20)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .height(2.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .align(Alignment.CenterStart)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(LimeDim, Lime)
                                    )
                                )
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .offset(x = thumbOffset)
                                .size(thumbR * 2)
                                .shadow(6.dp, CircleShape, spotColor = Lime)
                                .clip(CircleShape)
                                .background(Lime)
                        )
                        Slider(
                            value = progress,
                            onValueChange = {
                                viewModel.seekTo((it * state.duration).toLong())
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.Transparent,
                                activeTrackColor = Color.Transparent,
                                inactiveTrackColor = Color.Transparent,
                                activeTickColor = Color.Transparent,
                                inactiveTickColor = Color.Transparent
                            )
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = state.currentPosition.toFormattedDuration(),
                            style = MaterialTheme.typography.labelSmall,
                            color = White60,
                            fontSize = 11.sp
                        )
                        Text(
                            text = state.duration.toFormattedDuration(),
                            style = MaterialTheme.typography.labelSmall,
                            color = White30,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = viewModel::toggleShuffle) {
                        Icon(
                            Icons.Rounded.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (state.shuffle) Lime else White30,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = viewModel::skipPrevious,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            Icons.Rounded.SkipPrevious,
                            contentDescription = "Previous",
                            tint = White100,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .shadow(
                                elevation = 24.dp,
                                shape = CircleShape,
                                spotColor = Lime.copy(alpha = 0.7f),
                                ambientColor = Lime.copy(alpha = 0.4f)
                            )
                            .clip(CircleShape)
                            .background(Lime),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = viewModel::togglePlayPause,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = if (state.isPlaying)
                                    Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Black100,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = viewModel::skipNext,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            Icons.Rounded.SkipNext,
                            contentDescription = "Next",
                            tint = White100,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    IconButton(onClick = viewModel::toggleRepeat) {
                        Icon(
                            imageVector = when (state.repeatMode) {
                                RepeatMode.ONE -> Icons.Rounded.RepeatOne
                                else -> Icons.Rounded.Repeat
                            },
                            contentDescription = "Repeat",
                            tint = if (state.repeatMode != RepeatMode.OFF)
                                Lime else White30,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}