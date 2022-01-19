package com.app.simpleplayer.presentation.screens.home

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.simpleplayer.domain.models.Song
import com.app.simpleplayer.presentation.ui.common.SongItem
import com.app.simpleplayer.presentation.utils.collectAsStateDelayed
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.flow.Flow

const val MINI_PLAYER_COLLAPSED_HEIGHT_DP = 70
const val DELAY_BEFORE_COLLECTING_FLOW_MS = 1000L

@OptIn(ExperimentalPermissionsApi::class, ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel,
    onGetPlaybackPositionFlow: () -> Flow<Float>,
    permission: PermissionState = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE),
    onSongClick: (startingSong: Song) -> Unit,
    onMiniPlayerPlayPauseButtonClick: () -> Unit,
    onSkipPreviousButtonClick: () -> Unit,
    onSkipNextButtonClick: () -> Unit,
    onSliderPositionChanged: (Float) -> Unit
) {
    if (permission.hasPermission) {
        val convertedSongs by viewModel.songs.collectAsState()
        Box {
            when (val state = viewModel.screenState) {
                is HomeScreenUiState.MusicNotLoaded -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is HomeScreenUiState.MusicLoaded -> {
                    LazyColumn {
                        items(convertedSongs) { song ->
                            SongItem(
                                album = song.title,
                                song = song,
                                onCLick = { selectedSong ->
                                    viewModel.startPlaying()
                                    onSongClick(selectedSong)
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(70.dp))
                        }
                    }
                    AnimatedVisibility(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        visible = state.playbackStarted,
                        enter = slideInVertically(
                            initialOffsetY = { screenHeight ->
                                screenHeight + MINI_PLAYER_COLLAPSED_HEIGHT_DP
                            },
                            animationSpec = tween(
                                durationMillis = 250
                            )
                        )
                    ) {
                        val playbackPosition by onGetPlaybackPositionFlow().collectAsStateDelayed(
                            initial = 0f,
                            timeMillis = DELAY_BEFORE_COLLECTING_FLOW_MS
                        )
                        var collapsed by rememberSaveable { mutableStateOf(true) }
                        MiniPlayer(
                            onMiniPlayerButtonClick = onMiniPlayerPlayPauseButtonClick,
                            onMiniPlayerClick = { collapsed = !collapsed },
                            onSkipPreviousButtonClick = onSkipPreviousButtonClick,
                            onSkipNextButtonClick = onSkipNextButtonClick,
                            onSliderPositionChanged = onSliderPositionChanged,
                            playbackPosition = playbackPosition,
                            songDuration = viewModel.currentSongDurationMs,
                            collapsed = collapsed,
                            album = viewModel.currentSongTitle,
                            isPlaying = viewModel.isPlaying
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
    onMiniPlayerButtonClick: () -> Unit,
    onMiniPlayerClick: () -> Unit,
    onSkipPreviousButtonClick: () -> Unit,
    onSkipNextButtonClick: () -> Unit,
    onSliderPositionChanged: (Float) -> Unit,
    playbackPosition: Float,
    songDuration: Float,
    collapsed: Boolean,
    album: String,
    isPlaying: Boolean
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(
                interactionSource = MutableInteractionSource(),
                onClick = onMiniPlayerClick,
                indication = null
            ),
        color = MaterialTheme.colors.primary,
        shape = RoundedCornerShape(16.dp)
    ) {
        AnimatedContent(
            targetState = collapsed,
            transitionSpec = {
                ContentTransform(
                    targetContentEnter = fadeIn(
                        animationSpec = tween(150),
                    ),
                    initialContentExit = fadeOut(
                        animationSpec = tween(150)
                    ),
                    sizeTransform = SizeTransform()
                )
            }
        ) { collapsed ->
            if (collapsed) {
                MiniPlayerCollapsed(
                    album = album,
                    isPlaying = isPlaying,
                    onMiniPlayerButtonClick = onMiniPlayerButtonClick
                )
            } else {
                MiniPlayerExpanded(
                    album = album,
                    isPlaying = isPlaying,
                    songDuration = songDuration,
                    playbackPosition = playbackPosition,
                    onMiniPlayerButtonClick = onMiniPlayerButtonClick,
                    onSkipPreviousButtonClick = onSkipPreviousButtonClick,
                    onSkipNextButtonClick = onSkipNextButtonClick,
                    onSliderPositionChanged = onSliderPositionChanged
                )
            }
        }
    }
}

@Composable
fun MiniPlayerCollapsed(
    album: String,
    isPlaying: Boolean,
    onMiniPlayerButtonClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(MINI_PLAYER_COLLAPSED_HEIGHT_DP.dp)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = album,
            maxLines = 1,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onMiniPlayerButtonClick) {
            val icon = if (isPlaying) {
                Icons.Filled.Pause
            } else {
                Icons.Filled.PlayArrow
            }
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        }
    }
}

@Composable
fun MiniPlayerExpanded(
    album: String,
    isPlaying: Boolean,
    songDuration: Float,
    playbackPosition: Float,
    onMiniPlayerButtonClick: () -> Unit,
    onSkipPreviousButtonClick: () -> Unit,
    onSkipNextButtonClick: () -> Unit,
    onSliderPositionChanged: (Float) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .padding(
                top = 200.dp,
                start = 6.dp,
                end = 6.dp
            ),
        color = MaterialTheme.colors.primary
    ) {
        Box {
            Text(
                modifier = Modifier.align(Alignment.TopCenter),
                text = album,
                maxLines = 3,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                textAlign = TextAlign.Center
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 170.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Slider(
                    value = playbackPosition,
                    onValueChange = onSliderPositionChanged,
                    valueRange = 0.0f..songDuration,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White.copy(alpha = 0.4f),
                        activeTickColor = Color.White
                    )
                )
                Row {
                    val positionMin = (playbackPosition / 1000 / 60).toInt()
                    val positionSec = (playbackPosition / 1000 - positionMin * 60).toInt()
                    val durationMin = (songDuration / 1000 / 60).toInt()
                    val durationSec = (songDuration / 1000 - durationMin * 60).toInt()
                    val positionSecString = if (positionSec < 10) {
                        "0$positionSec"
                    } else {
                        positionSec
                    }
                    val durationSecString = if (durationSec < 10) {
                        "0$durationSec"
                    } else {
                        durationSec
                    }
                    Text(text = "$positionMin:$positionSecString")
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = "$durationMin:$durationSecString")
                }
            }
            Row(
                modifier = Modifier
                    .padding(bottom = 70.dp)
                    .align(Alignment.BottomCenter)
            ) {
                IconButton(
                    modifier = Modifier.padding(16.dp),
                    onClick = onSkipPreviousButtonClick
                ) {
                    Icon(
                        modifier = Modifier.size(128.dp),
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = null
                    )
                }
                IconButton(
                    modifier = Modifier.padding(16.dp),
                    onClick = onMiniPlayerButtonClick
                ) {
                    val icon = if (isPlaying) {
                        Icons.Filled.Pause
                    } else {
                        Icons.Filled.PlayArrow
                    }
                    Icon(
                        modifier = Modifier.size(128.dp),
                        imageVector = icon,
                        contentDescription = null
                    )
                }
                IconButton(
                    modifier = Modifier.padding(16.dp),
                    onClick = onSkipNextButtonClick
                ) {
                    Icon(
                        modifier = Modifier.size(128.dp),
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Preview
@Composable
fun MiniPlayer_Preview() {
    Scaffold {
        MiniPlayer(
            onMiniPlayerButtonClick = {
            },
            onMiniPlayerClick = {
            },
            onSkipPreviousButtonClick = {
            },
            onSkipNextButtonClick = {
            },
            onSliderPositionChanged = {
            },
            playbackPosition = 0f,
            songDuration = 0f,
            collapsed = false,
            album = "Smells like teen spirit",
            isPlaying = true
        )
    }
}
