package com.app.simpleplayer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.app.simpleplayer.di.component.DaggerApplicationComponent
import com.app.simpleplayer.presentation.utils.NOTIFICATION_CHANNEL_ID
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class SimplePlayerApp : DaggerApplication() {

    override fun onCreate() {
        super.onCreate()
            /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.player_notification_channel_name)
            val description = getString(R.string.player_notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                this.description = description
            }
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
             */
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
        DaggerApplicationComponent.factory().create(this)
}
