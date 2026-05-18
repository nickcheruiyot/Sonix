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

    val infiniteTransition = rememberInfiniteTransition(label = "anim")

    // Breathing glow
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.50f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = ComposeRepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Rotating dashed ring offset
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = ComposeRepeatMode.Restart
        ),
        label = "ring"
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Black100.copy(alpha = 0.86f))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(LimeDim.copy(alpha = 0.08f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(8.dp))
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

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(230.dp)
                ) {
                    // Outermost soft glow halo
                    Box(
                        modifier = Modifier
                            .size(230.dp)
                            .shadow(
                                elevation = if (state.isPlaying) 56.dp else 16.dp,
                                shape = CircleShape,
                                ambientColor = Lime.copy(
                                    alpha = if (state.isPlaying) pulseAlpha * 0.6f
                                    else 0.04f
                                ),
                                spotColor = Lime.copy(
                                    alpha = if (state.isPlaying) pulseAlpha
                                    else 0.04f
                                )
                            )
                            .clip(CircleShape)
                            .background(Color.Transparent)
                    )

                    // Rotating dashed outer ring — visible only when playing
                    if (state.isPlaying) {
                        Box(
                            modifier = Modifier
                                .size(218.dp)
                                .drawBehind {
                                    val r = size.minDimension / 2f
                                    val dashCount = 24
                                    val dashAngle = 360f / dashCount
                                    for (i in 0 until dashCount) {
                                        val startAngle = ringRotation + i * dashAngle
                                        val alpha = if (i % 2 == 0) 0.55f else 0.15f
                                        drawArc(
                                            color = Lime.copy(alpha = alpha),
                                            startAngle = startAngle,
                                            sweepAngle = dashAngle * 0.55f,
                                            useCenter = false,
                                            style = Stroke(width = 1.5.dp.toPx()),
                                            topLeft = Offset(
                                                center.x - r,
                                                center.y - r
                                            ),
                                            size = androidx.compose.ui.geometry.Size(
                                                r * 2, r * 2
                                            )
                                        )
                                    }
                                }
                        )
                    }

                    // Static thin ring border
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .drawBehind {
                                drawCircle(
                                    color = Lime.copy(
                                        alpha = if (state.isPlaying) 0.35f else 0.12f
                                    ),
                                    radius = size.minDimension / 2f,
                                    style = Stroke(width = 1.dp.toPx())
                                )
                            }
                            .clip(CircleShape)
                            .background(Color.Transparent)
                    )

                    // Main frosted glass disc
                    Box(
                        modifier = Modifier
                            .size(192.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        // Centre brighter when playing
                                        if (state.isPlaying)
                                            Black60.copy(alpha = 0.80f)
                                        else
                                            Black60.copy(alpha = 0.95f),
                                        Black40.copy(alpha = 0.97f),
                                        Black100.copy(alpha = 0.85f)
                                    ),
                                    radius = 300f
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Lime tinted inner glow circle behind icon
                        if (state.isPlaying) {
                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Lime.copy(alpha = pulseAlpha * 0.18f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }

                        // Headphone icon
                        Icon(
                            imageVector = Icons.Rounded.Headphones,
                            contentDescription = null,
                            tint = if (state.isPlaying)
                                Lime
                            else
                                Lime.copy(alpha = 0.40f),
                            modifier = Modifier.size(92.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Song title
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
                Spacer(Modifier.height(4.dp))
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

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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

                Spacer(Modifier.height(28.dp))

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
                                    Icons.Rounded.Pause
                                else
                                    Icons.Rounded.PlayArrow,
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

                Spacer(Modifier.height(36.dp))
            }
        }
    }
}