package com.app.simpleplayer.presentation.utils

import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

fun MediaSessionCompat.setState(builder: PlaybackStateCompat.Builder, state: Int) {
    setPlaybackState(
        builder
            .setState(
                state,
                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                1f
            )
            .build()
    )
}
