package com.app.simpleplayer.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.simpleplayer.domain.usecases.GetMusicFromExternalStorageUseCase
import com.app.simpleplayer.domain.viewmodels.ListSongsScreenViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class ListSongsScreenViewModelImpl @Inject constructor(
    private val getMusicFromExternalStorageUseCase: GetMusicFromExternalStorageUseCase
) : ViewModel(), ListSongsScreenViewModel {

    @OptIn(FlowPreview::class)
    override val songs = getMusicFromExternalStorageUseCase()
        .filter { it.isNotEmpty() }
        .onEach { screenState = SongScreenState.MusicLoaded() }
        .stateIn(viewModelScope, SharingStarted.Lazily, listOf())


    override var screenState: SongScreenState by mutableStateOf(SongScreenState.MusicNotLoaded)
        private set

    override fun startPlaying() {
        screenState = SongScreenState.MusicLoaded(isPlaying = true)
    }
}
