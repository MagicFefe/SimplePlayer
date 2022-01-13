package com.app.simpleplayer.presentation.screens.home

import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.app.simpleplayer.R
import com.app.simpleplayer.presentation.utils.NOTIFICATION_CHANNEL_ID
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.util.NotificationUtil

class SimplePlayerNotificationManager(
    context: Context,
    private val mediaSession: MediaSessionCompat,
    notificationListener: PlayerNotificationManager.NotificationListener
) {

    private val notificationManager = PlayerNotificationManager.Builder(context, 1, NOTIFICATION_CHANNEL_ID)
        .setChannelNameResourceId(R.string.player_notification_channel_name)
        .setChannelDescriptionResourceId(R.string.player_notification_channel_description)
        .setSmallIconResourceId(R.drawable.ic_baseline_music_note_24)
        .setMediaDescriptionAdapter(SimplePlayerMediaDescriptionAdapter(context, mediaSession.controller))
        .setChannelImportance(NotificationUtil.IMPORTANCE_LOW)
        .setNotificationListener(notificationListener)
        .build()

    private val simplePlayerNotification = notificationManager.apply {
        setUsePreviousAction(false)
        setUseRewindAction(false)
        setUseFastForwardAction(false)
        setMediaSessionToken(mediaSession.sessionToken)
        setUseNextAction(true)
        setPriority(NotificationCompat.PRIORITY_DEFAULT)
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        setUsePlayPauseActions(true)
    }

    fun showNotification(player: Player) = simplePlayerNotification.setPlayer(player)

    fun hideNotification() = simplePlayerNotification.setPlayer(null)
}
