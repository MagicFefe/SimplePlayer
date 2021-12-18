package com.app.simpleplayer.domain.usecases

import com.app.simpleplayer.domain.models.Song
import com.app.simpleplayer.domain.repository.Repository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMusicFromExternalStorageUseCase @Inject constructor(
    private val repository: Repository
) {
    operator fun invoke(): Flow<List<Song>> = repository.getMusic()
}
