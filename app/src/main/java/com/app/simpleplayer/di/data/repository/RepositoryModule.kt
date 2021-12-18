package com.app.simpleplayer.di.data.repository

import com.app.simpleplayer.data.repository.RepositoryImpl
import com.app.simpleplayer.domain.repository.Repository
import dagger.Binds
import dagger.Module

@Module
interface RepositoryModule {

    @Binds
    fun bindRepository(repository: RepositoryImpl): Repository
}