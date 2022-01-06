package com.app.simpleplayer.presentation.screens.home

sealed class HomeScreenUiState {
    object MusicNotLoaded : HomeScreenUiState()
    class MusicLoaded(var playbackStarted: Boolean = false) : HomeScreenUiState()
}
