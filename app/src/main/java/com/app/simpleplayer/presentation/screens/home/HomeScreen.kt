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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.simpleplayer.domain.models.Song
import com.app.simpleplayer.presentation.ui.common.SongItem
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState

const val MINI_PLAYER_COLLAPSED_HEIGHT = 70

@ExperimentalAnimationApi
@ExperimentalPermissionsApi
@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel,
    permission: PermissionState = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE),
    onSongClick: (startingSong: Song, playlist: List<Song>) -> Unit,
    onMiniPlayerButtonClick: () -> Unit
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
                                painter = null,
                                song = song,
                                onCLick = { selectedSong ->
                                    viewModel.startPlaying()
                                    onSongClick(selectedSong, convertedSongs)
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
                                screenHeight + MINI_PLAYER_COLLAPSED_HEIGHT
                            },
                            animationSpec = tween(
                                durationMillis = 250
                            )
                        ),
                    ) {
                        var collapsed by remember { mutableStateOf(true) }
                        MiniPlayer(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            onMiniPlayerButtonClick = onMiniPlayerButtonClick,
                            onMiniPlayerClick = { collapsed = !collapsed },
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

@ExperimentalAnimationApi
@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
    onMiniPlayerButtonClick: () -> Unit,
    onMiniPlayerClick: () -> Unit,
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
                    onMiniPlayerButtonClick = onMiniPlayerButtonClick
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
            .height(MINI_PLAYER_COLLAPSED_HEIGHT.dp)
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
    onMiniPlayerButtonClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .padding(8.dp),
        color = MaterialTheme.colors.primary
    ) {
        Box {
            Text(
                modifier = Modifier
                    .padding(top = 300.dp)
                    .align(Alignment.TopCenter),
                text = album,
                maxLines = 5,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp
            )
            IconButton(
                modifier = Modifier
                    .padding(128.dp)
                    .align(Alignment.BottomCenter),
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
        }
    }
}

@ExperimentalAnimationApi
@Preview
@Composable
fun MiniPlayer_Preview() {
    Scaffold {
        MiniPlayer(
            onMiniPlayerButtonClick = { /*TODO*/ },
            onMiniPlayerClick = { /*TODO*/ },
            collapsed = false,
            album = "Smells like teen's spirit",
            isPlaying = true
        )
    }
}
