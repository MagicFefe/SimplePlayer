package com.app.simpleplayer.presentation.utils

import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

fun MediaSessionCompat.setState(state: Int, position: Long) {
    setPlaybackState(
        PlaybackStateCompat.Builder()
            .setState(
                state,
                position,
                1f
            )
            .build()
    )
}
