package com.app.simpleplayer.data.repository

import com.app.simpleplayer.data.MusicManager
import com.app.simpleplayer.domain.models.Song
import com.app.simpleplayer.domain.repository.Repository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val musicManager: MusicManager
) : Repository {
    override fun getMusic(): Flow<List<Song>> = musicManager.getExternalContent()
}
