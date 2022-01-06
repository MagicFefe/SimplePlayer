package com.app.simpleplayer.di.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.app.simpleplayer.presentation.screens.home.HomeScreenViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ViewModelModule {

    @Binds
    @IntoMap
    @CreateViewModelWithFactory(HomeScreenViewModel::class)
    fun bindListSongsScreenViewModel(viewModelMain: HomeScreenViewModel): ViewModel
}
