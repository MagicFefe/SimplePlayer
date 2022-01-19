package com.app.simpleplayer.domain.models

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val dateAdded: Int,
    val uri: Uri,
    val duration: Float
)
