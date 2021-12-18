package com.app.simpleplayer.di.presentation.usecases

import com.app.simpleplayer.domain.repository.Repository
import com.app.simpleplayer.domain.usecases.GetMusicFromExternalStorageUseCase
import dagger.Module
import dagger.Provides

@Module
class UseCaseModule {
    @Provides
    fun provideGetMusicFromExternalStorageUseCase(repository: Repository) =
        GetMusicFromExternalStorageUseCase(repository)
}