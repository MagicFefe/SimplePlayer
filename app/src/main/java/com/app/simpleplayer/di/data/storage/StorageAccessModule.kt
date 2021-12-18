package com.app.simpleplayer.di.data.storage

import android.content.Context
import com.app.simpleplayer.data.MusicManager
import dagger.Module
import dagger.Provides

@Module
class StorageAccessModule {

    @Provides
    fun provideMusicManager(context: Context) = MusicManager(context)
}
