package com.app.simpleplayer.presentation.screens.home

import android.support.v4.media.session.MediaControllerCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object SimplePlayerConnection {
    var mediaController: MediaControllerCompat? = null

    var isPlaying by mutableStateOf(false)

    var currentSongTitle by mutableStateOf("")

    var currentSongDurationMs by mutableStateOf(0f)
}
