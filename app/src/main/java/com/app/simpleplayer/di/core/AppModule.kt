package com.app.simpleplayer.di.core

import android.content.Context
import com.app.simpleplayer.SimplePlayerApp
import com.app.simpleplayer.di.data.DataModule
import com.app.simpleplayer.di.presentation.PresentationModule
import dagger.Module
import dagger.Provides

@Module(
    includes = [
        DataModule::class,
        PresentationModule::class
    ]
)
class AppModule {

    @Provides
    fun provideContext(simplePlayerApp: SimplePlayerApp): Context =
        simplePlayerApp.applicationContext
}
