@file:Suppress("unused")

package com.app.simpleplayer.di.presentation.activity

import com.app.simpleplayer.presentation.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface ActivityModule {
    @ContributesAndroidInjector
    fun contributeMainActivity(): MainActivity
}
