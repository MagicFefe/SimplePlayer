package com.app.simpleplayer.presentation.screens.home

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.app.simpleplayer.R
import com.app.simpleplayer.presentation.utils.title
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class SimplePlayerMediaDescriptionAdapter(
    private val context: Context,
    private val mediaController: MediaControllerCompat
) : PlayerNotificationManager.MediaDescriptionAdapter {
    override fun getCurrentContentTitle(player: Player): CharSequence =
        context.getString(R.string.player_notification_title)

    override fun createCurrentContentIntent(player: Player): PendingIntent =
        mediaController.sessionActivity

    override fun getCurrentContentText(player: Player): CharSequence =
        mediaController.metadata.title

    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback
    ): Bitmap? =
        BitmapFactory.decodeResource(context.resources, R.drawable.logo_simpe_music_player)
}
