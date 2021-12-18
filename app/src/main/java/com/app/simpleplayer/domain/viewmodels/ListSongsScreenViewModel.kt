package com.app.simpleplayer.domain.viewmodels

import com.app.simpleplayer.domain.models.Song
import com.app.simpleplayer.presentation.SongScreenState
import kotlinx.coroutines.flow.StateFlow

interface ListSongsScreenViewModel {
    val songs: StateFlow<List<Song>>
    val screenState: SongScreenState

    fun startPlaying()
}