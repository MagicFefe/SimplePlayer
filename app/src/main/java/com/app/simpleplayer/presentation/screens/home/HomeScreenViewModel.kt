package com.app.simpleplayer.presentation.screens.home

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.simpleplayer.domain.usecases.GetMusicFromExternalStorageUseCase
import com.app.simpleplayer.presentation.utils.duration
import com.app.simpleplayer.presentation.utils.title
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class HomeScreenViewModel @Inject constructor(
    getMusicFromExternalStorageUseCase: GetMusicFromExternalStorageUseCase
) : ViewModel() {

    var isPlaying by mutableStateOf(false)
        private set
    var currentSongTitle by mutableStateOf("")
        private set
    var currentSongDurationMs by mutableStateOf(0f)
        private set
    var screenState: HomeScreenUiState by mutableStateOf(HomeScreenUiState.MusicNotLoaded)
        private set

    @OptIn(FlowPreview::class)
    val songs = getMusicFromExternalStorageUseCase()
        .filter { it.isNotEmpty() }
        .onEach { screenState = HomeScreenUiState.MusicLoaded() }
        .stateIn(viewModelScope, SharingStarted.Lazily, listOf())

    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            isPlaying = state?.state == PlaybackStateCompat.STATE_PLAYING
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            if (metadata == null) return
            currentSongTitle = metadata.title
            currentSongDurationMs = metadata.duration.toFloat()
        }
    }

    fun getPlaybackPosition(mediaController: MediaControllerCompat) = flow {
        emit(mediaController.playbackState.position.toFloat())
    }

    fun startPlaying() {
        screenState = HomeScreenUiState.MusicLoaded(playbackStarted = true)
    }

    fun registerMediaControllerCallback(mediaController: MediaControllerCompat) {
        mediaController.registerCallback(mediaControllerCallback)
    }
}
