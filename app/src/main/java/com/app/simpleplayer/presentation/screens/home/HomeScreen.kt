package com.app.simpleplayer.presentation.screens.home

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.simpleplayer.R
import com.app.simpleplayer.domain.models.Song
import com.app.simpleplayer.presentation.ui.common.SongItem
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.flow.Flow

const val MINI_PLAYER_COLLAPSED_HEIGHT_DP = 70

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
    Box {

        if (permission.hasPermission) {
            val convertedSongs by viewModel.songs.collectAsState()
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
                        val playbackPosition by onGetPlaybackPositionFlow().collectAsState(
                            initial = 0f
                        )
                        var collapsed by rememberSaveable { mutableStateOf(true) }
                        MiniPlayer(
                            onMiniPlayerButtonClick = onMiniPlayerPlayPauseButtonClick,
                            onMiniPlayerExpandCollapseButtonClick = { collapsed = !collapsed },
                            onSkipPreviousButtonClick = onSkipPreviousButtonClick,
                            onSkipNextButtonClick = onSkipNextButtonClick,
                            onSliderPositionChanged = onSliderPositionChanged,
                            playbackPosition = playbackPosition,
                            songDuration = SimplePlayerConnection.currentSongDurationMs,
                            collapsed = collapsed,
                            album = SimplePlayerConnection.currentSongTitle,
                            isPlaying = SimplePlayerConnection.isPlaying
                        )
                    }
                }
            }
        } else {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(R.string.contacting_the_user_if_permission_is_denied),
                fontSize = 30.sp
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
    onMiniPlayerButtonClick: () -> Unit,
    onMiniPlayerExpandCollapseButtonClick: () -> Unit,
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
            .padding(8.dp),
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
                    onMiniPlayerPlayPauseButtonClick = onMiniPlayerButtonClick,
                    onMiniPlayerExpandButtonClick = onMiniPlayerExpandCollapseButtonClick
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
                    onSliderPositionChanged = onSliderPositionChanged,
                    onMiniPlayerCollapseButtonClick = onMiniPlayerExpandCollapseButtonClick
                )
            }
        }
    }
}

@Composable
fun MiniPlayerCollapsed(
    album: String,
    isPlaying: Boolean,
    onMiniPlayerPlayPauseButtonClick: () -> Unit,
    onMiniPlayerExpandButtonClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(MINI_PLAYER_COLLAPSED_HEIGHT_DP.dp)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onMiniPlayerExpandButtonClick
        ) {
            Icon(
                imageVector = Icons.Filled.ExpandLess,
                contentDescription = null
            )
        }
        Text(
            modifier = Modifier.weight(1f),
            text = album,
            maxLines = 1,
            fontWeight = FontWeight.Bold,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(
            onClick = onMiniPlayerPlayPauseButtonClick
        ) {
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
    onSliderPositionChanged: (Float) -> Unit,
    onMiniPlayerCollapseButtonClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .padding(
                top = 6.dp,
                start = 6.dp,
                end = 6.dp
            ),
        color = MaterialTheme.colors.primary
    ) {
        Box {
            IconButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                onClick = onMiniPlayerCollapseButtonClick
            ) {
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = null
                )
            }
            Text(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 200.dp),
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
                        thumbColor = MaterialTheme.colors.onPrimary,
                        activeTrackColor = MaterialTheme.colors.onPrimary.copy(alpha = 0.4f),
                        activeTickColor = MaterialTheme.colors.onPrimary
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
            onMiniPlayerExpandCollapseButtonClick = {
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
