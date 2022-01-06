package com.app.simpleplayer.di.presentation

import com.app.simpleplayer.di.presentation.activity.ActivityModule
import com.app.simpleplayer.di.presentation.service.ServiceModule
import com.app.simpleplayer.di.presentation.usecases.UseCaseModule
import com.app.simpleplayer.di.presentation.viewmodel.ViewModelModule
import dagger.Module

@Module(
    includes = [
        ActivityModule::class, ViewModelModule::class,
        UseCaseModule::class, ServiceModule::class
    ]
)
class PresentationModule
