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
import com.app.simpleplayer.presentation.utils.title
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class HomeScreenViewModel @Inject constructor(
    getMusicFromExternalStorageUseCase: GetMusicFromExternalStorageUseCase
) : ViewModel() {

    var isPlaying by mutableStateOf(false)
        private set
    var currentSongTitle by mutableStateOf("")
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
            currentSongTitle = metadata?.title ?: ""
        }
    }

    fun startPlaying() {
        screenState = HomeScreenUiState.MusicLoaded(playbackStarted = true)
    }

    fun registerMediaControllerCallback(mediaController: MediaControllerCompat) {
        mediaController.registerCallback(mediaControllerCallback)
    }
}
