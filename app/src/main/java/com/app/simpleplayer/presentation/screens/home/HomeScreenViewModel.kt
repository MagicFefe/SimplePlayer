package com.app.simpleplayer.presentation.screens.home

import android.support.v4.media.session.MediaControllerCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.simpleplayer.domain.usecases.GetMusicFromExternalStorageUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class HomeScreenViewModel @Inject constructor(
    getMusicFromExternalStorageUseCase: GetMusicFromExternalStorageUseCase
) : ViewModel() {

    var screenState: HomeScreenUiState by mutableStateOf(HomeScreenUiState.MusicNotLoaded)
        private set

    @OptIn(FlowPreview::class)
    val songs = getMusicFromExternalStorageUseCase()
        .filter { it.isNotEmpty() }
        .onEach { screenState = HomeScreenUiState.MusicLoaded() }
        .stateIn(viewModelScope, SharingStarted.Lazily, listOf())

    fun getPlaybackPosition(mediaController: MediaControllerCompat) = flow {
        emit(mediaController.playbackState.position.toFloat())
    }

    fun startPlaying() {
        screenState = HomeScreenUiState.MusicLoaded(playbackStarted = true)
    }
}
