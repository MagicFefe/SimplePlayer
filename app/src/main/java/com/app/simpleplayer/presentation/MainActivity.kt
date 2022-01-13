package com.app.simpleplayer.presentation

import android.Manifest
import android.content.*
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import com.app.simpleplayer.presentation.theme.SimplePlayerTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import java.util.*
import javax.inject.Inject

class MainActivity : ComponentActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var viewModel: HomeScreenViewModel

    private lateinit var mediaController: MediaControllerCompat
    private lateinit var mediaBrowser: MediaBrowserCompat

    private val getMusic =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                mediaBrowser = MediaBrowserCompat(
                    this,
                    ComponentName(this, MusicPlayerService::class.java),
                    connectionCallback,
                    null
                ).also { mediaBrowser -> mediaBrowser.connect() }
            }
        }
    private val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaController =
                MediaControllerCompat(this@MainActivity, mediaBrowser.sessionToken)
            MediaControllerCompat.setMediaController(this@MainActivity, mediaController)
            viewModel.registerMediaControllerCallback(mediaController)
        }
    }

    @ExperimentalAnimationApi
    @OptIn(ExperimentalPermissionsApi::class)
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
                        onSongClick = { startingSong: Song, playlist: List<Song> ->
                            mediaController.transportControls.playFromUri(startingSong.uri, null)
                        },
                        onMiniPlayerButtonClick = {
                            if (viewModel.isPlaying) {
                                mediaController.transportControls.pause()
                            } else {
                                mediaController.transportControls.play()
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaBrowser.disconnect()
    }
}
