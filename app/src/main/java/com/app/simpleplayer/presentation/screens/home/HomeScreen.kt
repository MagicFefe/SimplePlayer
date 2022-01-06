package com.app.simpleplayer.presentation.screens.home

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.simpleplayer.R
import com.app.simpleplayer.domain.models.Song
import com.app.simpleplayer.presentation.ui.common.SongItem
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState

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
                    }
                    if (state.playbackStarted) {
                        MiniPlayer(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            album = viewModel.currentSongTitle,
                            isPlaying = viewModel.isPlaying,
                            onMiniPlayerButtonClick = onMiniPlayerButtonClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
    album: String,
    isPlaying: Boolean,
    onMiniPlayerButtonClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(8.dp),
        color = MaterialTheme.colors.primary,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = album,
                maxLines = 1,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onMiniPlayerButtonClick) {
                val drawableResource = if (isPlaying) {
                    R.drawable.ic_baseline_pause_24
                } else {
                    R.drawable.ic_baseline_play_arrow_24
                }
                Icon(
                    painter = painterResource(id = drawableResource),
                    contentDescription = null
                )
            }
        }
    }
}

@Preview
@Composable
fun MiniPlayer_Preview() {
    Scaffold {
        MiniPlayer(
            album = "Smells like teen spirit",
            isPlaying = false
        ) {}
    }
}
