package com.app.simpleplayer.domain.repository

import com.app.simpleplayer.domain.models.Song
import kotlinx.coroutines.flow.Flow

interface Repository {

    fun getMusic(): Flow<List<Song>>
}