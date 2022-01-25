package com.app.simpleplayer.presentation

import android.Manifest
import android.content.*
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.app.simpleplayer.SimplePlayerApp
import com.app.simpleplayer.di.presentation.viewmodel.ViewModelFactory
import com.app.simpleplayer.domain.models.Song
import com.app.simpleplayer.presentation.screens.home.HomeScreen
import com.app.simpleplayer.presentation.screens.home.HomeScreenViewModel
import com.app.simpleplayer.presentation.screens.home.MusicPlayerService
import com.app.simpleplayer.presentation.screens.home.SimplePlayerConnection
import com.app.simpleplayer.presentation.theme.SimplePlayerTheme
import com.app.simpleplayer.presentation.utils.duration
import com.app.simpleplayer.presentation.utils.title
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import java.util.*
import javax.inject.Inject

class MainActivity : ComponentActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var mediaController: MediaControllerCompat
    private lateinit var mediaBrowser: MediaBrowserCompat

    val viewModel: HomeScreenViewModel by viewModels { viewModelFactory }
    private val getMusic =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                mediaBrowser = MediaBrowserCompat(
                    this,
                    ComponentName(this, MusicPlayerService::class.java),
                    connectionCallback,
                    null
                ).also { mediaBrowser -> mediaBrowser.connect() }
                startService(Intent(this, MusicPlayerService::class.java))
            }
        }
    private val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaController =
                MediaControllerCompat(this@MainActivity, mediaBrowser.sessionToken)
            MediaControllerCompat.setMediaController(this@MainActivity, mediaController)
            mediaController.registerCallback(mediaControllerCallback)
            SimplePlayerConnection.mediaController = mediaController
        }
    }
    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            SimplePlayerConnection.isPlaying = state?.state == PlaybackStateCompat.STATE_PLAYING
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            if (metadata == null) return
            SimplePlayerConnection.currentSongTitle = metadata.title
            SimplePlayerConnection.currentSongDurationMs = metadata.duration.toFloat()
        }

        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
            mediaController.transportControls.stop()
            mediaBrowser.disconnect()
        }
    }

    @OptIn(ExperimentalPermissionsApi::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getMusic.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        (application as SimplePlayerApp).androidInjector().inject(this)
        volumeControlStream = AudioManager.STREAM_MUSIC
        setContent {
            SimplePlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    HomeScreen(
                        viewModel = viewModel,
                        onGetPlaybackPositionFlow = {
                            viewModel.getPlaybackPosition(
                                SimplePlayerConnection.mediaController ?: mediaController
                            )
                        },
                        onSongClick = { startingSong: Song ->
                            mediaController.transportControls.playFromUri(startingSong.uri, null)
                            Log.d("DDDD", "${startingSong.uri}")
                        },
                        onMiniPlayerPlayPauseButtonClick = {
                            if (SimplePlayerConnection.isPlaying) {
                                mediaController.transportControls.pause()
                            } else {
                                mediaController.transportControls.play()
                            }
                        },
                        onSkipPreviousButtonClick = {
                            mediaController.transportControls.skipToPrevious()
                        },
                        onSkipNextButtonClick = {
                            mediaController.transportControls.skipToNext()
                        },
                        onSliderPositionChanged = { newPosition ->
                            mediaController.transportControls.seekTo(newPosition.toLong())
                        }
                    )
                }
            }
        }
    }
}
