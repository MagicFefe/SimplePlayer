package com.app.simpleplayer.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.app.simpleplayer.R
import com.app.simpleplayer.data.service.MusicPlayerService
import com.app.simpleplayer.data.service.Playback

class PlayerNotificationButtonReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val songTitle = intent.getStringExtra(MusicPlayerService.SONG_TITLE_EXTRA_KEY) ?: ""
        MusicPlayerService.INSTANCE?.let { service ->
            when (intent.action) {
                Playback.PLAY_PAUSE.name -> {
                    val player = service.mediaPlayer
                    val icon = if (player.isPlaying) {
                        R.drawable.ic_baseline_play_arrow_24
                    } else {
                        R.drawable.ic_baseline_pause_24
                    }
                    service.showPlayerNotification(songTitle, icon)
                    if (player.isPlaying) {
                        player.pause()
                    } else {
                        player.start()
                    }
                }
            }
        }
    }
}
