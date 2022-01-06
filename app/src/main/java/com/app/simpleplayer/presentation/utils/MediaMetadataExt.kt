package com.app.simpleplayer.presentation.utils

import android.support.v4.media.MediaMetadataCompat

val MediaMetadataCompat.title: String
    get() = getString(MediaMetadataCompat.METADATA_KEY_TITLE) ?: ""
