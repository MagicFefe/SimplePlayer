package com.app.simpleplayer.presentation

import android.Manifest
import android.content.*
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.simpleplayer.R
import com.app.simpleplayer.SimplePlayerApp
import com.app.simpleplayer.data.service.MusicPlayerService
import com.app.simpleplayer.di.presentation.viewmodel.ViewModelFactory
import com.app.simpleplayer.domain.models.MediaElement
import com.app.simpleplayer.domain.models.Song
import com.app.simpleplayer.domain.viewmodels.ListSongsScreenViewModel
import com.app.simpleplayer.presentation.theme.SimplePlayerTheme
import com.app.simpleplayer.presentation.ui.common.SongItem
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import java.util.*
import javax.inject.Inject


class MainActivity : ComponentActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var mediaController: MediaControllerCompat
    private lateinit var mediaBrowser: MediaBrowserCompat

    private val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaController =
                MediaControllerCompat(this@MainActivity, mediaBrowser.sessionToken)
            MediaControllerCompat.setMediaController(this@MainActivity, mediaController)
        }
    }
    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {

    }

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as SimplePlayerApp).androidInjector().inject(this)
        volumeControlStream = AudioManager.STREAM_MUSIC
        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, MusicPlayerService::class.java),
            connectionCallback,
            null
        )
        setContent {
            SimplePlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainScreen(
                        viewModel = viewModel(
                            modelClass = ListSongsScreenViewModelImpl::class.java,
                            factory = viewModelFactory
                        ),
                        onSongClick = { startingSong: Song, playlist: List<Song> ->
                            /*
                            var nextSongIndex = playlist.indexOf(startingSong)
                            service.mediaPlayer.apply {
                                playSafety(this@MainActivity, startingSong.uri)
                                service.showPlayerNotification(intent, startingSong.title)
                                setOnCompletionListener {
                                    if (nextSongIndex < playlist.size) {
                                        nextSongIndex++
                                        val nextSong = playlist[nextSongIndex]
                                        playSafety(this@MainActivity, nextSong.uri)
                                        service.showPlayerNotification(intent, nextSong.title)
                                    }
                                }
                            }
                             */
                            /*
                            service.mediaPlayer.playQueueFrom(startingSong, this, { song ->
                                service.showPlayerNotification(song.title)
                            }, *playlist.toTypedArray())

                             */
                            val description = MediaDescriptionCompat.Builder()
                                .setTitle(startingSong.title)
                                .setIconBitmap(
                                    BitmapFactory.decodeResource(
                                        resources,
                                        R.drawable.logo_simpe_music_player
                                    )
                                )
                                .setMediaUri(startingSong.uri)
                                .build()
                            mediaController.addQueueItem(description)
                            //mediaController.transportControls.play()

                            mediaController.transportControls.playFromUri(startingSong.uri, null)
                            /*
                            Toast
                                .makeText(this, mediaController.metadata.getText(MediaMetadataCompat.METADATA_KEY_TITLE), Toast.LENGTH_SHORT)
                                .show()

                             */
                        },
                        onMiniPlayerButtonClick = {

                            /*
                            kotlin.runCatching {
                                val intent = Intent(
                                    this,
                                    PlayerNotificationButtonReceiver::class.java
                                ).apply {
                                    action = Playback.PLAY_PAUSE.name
                                }
                                sendBroadcast(intent)
                                mediaController
                            }

                             */

                            if (mediaController.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                                mediaController.transportControls.pause()
                            } else {
                                mediaController.transportControls.play()
                            }
                        },
                        currentSongMetadata = if (::mediaController.isInitialized) {
                            mediaController.metadata
                        } else {
                            MediaMetadataCompat.Builder().build()
                        }
                    )
                }
            }
        }
        mediaBrowser.connect()
    }

    override fun onDestroy() {
        super.onDestroy()

        //MediaControllerCompat.getMediaController(this)?.unregisterCallback()
        mediaBrowser.disconnect()
    }
}

@Composable
fun ShortToast(text: String) {
    val context = LocalContext.current
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

@Composable
fun LongToast(text: String) {
    val context = LocalContext.current
    Toast.makeText(context, text, Toast.LENGTH_LONG).show()
}

fun MediaPlayer.playSafety(context: Context, uri: Uri) {
    kotlin.runCatching {
        reset()
        setDataSource(context, uri)
        prepare()
        start()
    }
}

fun <T : MediaElement> MediaPlayer.playQueueFrom(
    startingElement: T,
    context: Context,
    block: (T) -> Unit = {},
    vararg queue: T
) {
    var currPLayingElementIndex = queue.indexOf(startingElement) + 1
    playSafety(context, startingElement.uri)
    block(startingElement)
    setOnCompletionListener {
        if (currPLayingElementIndex < queue.size) {
            val element = queue[currPLayingElementIndex]
            playSafety(context, element.uri)
            block(element)
            currPLayingElementIndex++
        }
    }
}

@ExperimentalPermissionsApi
@Composable
fun MainScreen(
    viewModel: ListSongsScreenViewModel,
    permission: PermissionState = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE),
    onSongClick: (startingSong: Song, playlist: List<Song>) -> Unit,
    onMiniPlayerButtonClick: () -> Unit,
    currentSongMetadata: MediaMetadataCompat
) {
    if (!permission.hasPermission) {
        PrerequisitePermissionPopup(
            onButtonClick = {
                permission.launchPermissionRequest()
            }
        )
    } else {
        val convertedSongs by viewModel.songs.collectAsState()
        var isPlaying by rememberSaveable {
            mutableStateOf(false)
        }
        val albumKey = "title"
        var currentPlayingSong by rememberSaveable {
            mutableStateOf(mapOf<String, String>())
        }
        Box {
            when (val state = viewModel.screenState) {
                is SongScreenState.MusicNotLoaded -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is SongScreenState.MusicLoaded -> {
                    LazyColumn {
                        items(convertedSongs) { song ->
                            SongItem(
                                album = song.title,
                                painter = null,
                                song = song,
                                onCLick = { selectedSong ->
                                    viewModel.startPlaying()
                                    onSongClick(selectedSong, convertedSongs)
                                    isPlaying = true
                                    currentPlayingSong = mapOf(
                                        albumKey to selectedSong.title
                                    )
                                }
                            )
                        }
                    }
                    if (state.isPlaying) {
                        MiniPlayer(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            album = currentPlayingSong[albumKey] ?: "",
                            isPlaying = isPlaying,
                            onMiniPlayerButtonClick = {
                                isPlaying = !isPlaying
                                onMiniPlayerButtonClick()
                            }
                        )
                    }
                }
            }
        }
    }

}


@Composable
fun PrerequisitePermissionPopup(
    onButtonClick: () -> Unit
) {
    Dialog(
        onDismissRequest = {
        }
    ) {
        Card(
            elevation = 2.dp,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.padding(
                        top = 12.dp,
                        start = 12.dp,
                        end = 12.dp,
                    ),
                    text = stringResource(id = R.string.application_needs_read_external_storage),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.please_grant_permission_for_it)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onButtonClick
                ) {
                    Text(text = stringResource(id = R.string.ok))
                }
            }
        }
    }
}

sealed class SongScreenState {
    object MusicNotLoaded : SongScreenState()
    class MusicLoaded(var isPlaying: Boolean = false) : SongScreenState()
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
