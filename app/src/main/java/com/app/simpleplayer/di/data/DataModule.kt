package com.app.simpleplayer.di.data

import com.app.simpleplayer.di.data.repository.RepositoryModule
import com.app.simpleplayer.di.data.storage.StorageAccessModule
import dagger.Module

@Module(
    includes = [
        StorageAccessModule::class, RepositoryModule::class
    ]
)
class DataModule
