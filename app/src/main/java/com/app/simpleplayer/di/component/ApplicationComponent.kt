package com.app.simpleplayer.di.component

import com.app.simpleplayer.SimplePlayerApp
import com.app.simpleplayer.di.core.AppModule
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule

@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        AppModule::class
    ]
)
interface ApplicationComponent : AndroidInjector<SimplePlayerApp> {

    @Component.Factory
    interface Factory : AndroidInjector.Factory<SimplePlayerApp>
}
