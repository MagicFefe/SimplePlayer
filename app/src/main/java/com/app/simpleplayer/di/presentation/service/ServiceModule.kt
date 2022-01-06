package com.app.simpleplayer.di.presentation.service

import com.app.simpleplayer.presentation.screens.home.MusicPlayerService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface ServiceModule {
    @ContributesAndroidInjector
    fun contributeMusicPlayerService(): MusicPlayerService
}
