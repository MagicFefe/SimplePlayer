package com.app.simpleplayer.domain.models

import android.net.Uri
import androidx.annotation.DrawableRes
import com.app.simpleplayer.R

data class Song(
    val id: Long,
    val title: String,
    val dateAdded: Int,
    val uri: Uri
)
