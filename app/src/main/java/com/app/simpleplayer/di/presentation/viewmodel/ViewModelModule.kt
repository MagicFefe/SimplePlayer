package com.app.simpleplayer.di.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.app.simpleplayer.presentation.ListSongsScreenViewModelImpl
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ViewModelModule {

    @Binds
    @IntoMap
    @CreateViewModelWithFactory(ListSongsScreenViewModelImpl::class)
    fun bindListSongsScreenViewModel(viewModelImplMain: ListSongsScreenViewModelImpl): ViewModel
}