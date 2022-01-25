package com.app.simpleplayer.presentation.screens.home

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.*
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.net.toUri
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.app.simpleplayer.SimplePlayerApp
import com.app.simpleplayer.domain.usecases.GetMusicFromExternalStorageUseCase
import com.app.simpleplayer.presentation.MainActivity
import com.app.simpleplayer.presentation.utils.setState
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

class MusicPlayerService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var getMusicFromExternalStorageUseCase: GetMusicFromExternalStorageUseCase

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSessionCompat

    private var playlistMetadata = listOf<MediaMetadataCompat>()

    private val scope = MainScope()
    private val audioAttributes =
        AudioAttributes.Builder()
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
    private val stateBuilder = PlaybackStateCompat.Builder()

    private val activityIntent by lazy {
        Intent(this, MainActivity::class.java)
    }
    private val activityPendingIntent by lazy {
        PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE)
    }
    private val playerNotificationManager by lazy {
        SimplePlayerNotificationManager(this, mediaSession, SimplePlayerNotificationListener())
    }
    val playlist by lazy {
        playlistMetadata.map { songMetadata ->
            MediaItem.fromUri(
                songMetadata.getString(MediaMetadataCompat.METADATA_KEY_ART_URI)
                    ?.toUri()
                    ?: Uri.EMPTY
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        (application as SimplePlayerApp).androidInjector().inject(this)
        player = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .setAudioAttributes(audioAttributes, true)
            .build().apply {
                addListener(SimplePlayerListener())
            }
        mediaSession = MediaSessionCompat(this, MEDIA_SESSION_TAG).apply {
            setSessionToken(sessionToken)
            setPlaybackState(stateBuilder.build())
            setMediaButtonReceiver(
                PendingIntent.getBroadcast(
                    this@MusicPlayerService,
                    0,
                    Intent(
                        Intent.ACTION_MEDIA_BUTTON,
                        null,
                        this@MusicPlayerService,
                        MediaButtonReceiver::class.java
                    ),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            setCallback(SimplePlayerMediaSessionCallback())
            setSessionActivity(activityPendingIntent)
            isActive = true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        scope.launch {
            getMusicFromExternalStorageUseCase().collect { songs ->
                val convertedSongs = songs.map { song ->
                    MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                        .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, song.uri.toString())
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration.toLong())
                        .build()
                }
                playlistMetadata = convertedSongs
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot = BrowserRoot("", null)

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
    }

    override fun onDestroy() {
        super.onDestroy()
        release()
    }

    private fun release() {
        if (::player.isInitialized) {
            player.release()
        }
        if (::mediaSession.isInitialized) {
            mediaSession.apply {
                isActive = false
                release()
            }
        }
        if (scope.isActive) {
            scope.cancel()
        }
    }

    private inner class SimplePlayerMediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            super.onPlay()
            play()
        }

        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            super.onPlayFromUri(uri, extras)
            if (uri == null) return
            player.currentMediaItem?.let {
                player.stop()
                player.clearMediaItems()
            }
            player.addMediaItems(listOf(MediaItem.fromUri(uri)) + playlist)
            play()
        }

        override fun onPause() {
            super.onPause()
            mediaSession.setState(PlaybackStateCompat.STATE_PAUSED, player.currentPosition)
            player.pause()
            playerNotificationManager.showNotification(player)
        }

        override fun onStop() {
            super.onStop()
            mediaSession.isActive = false
            playerNotificationManager.hideNotification()
            player.stop()
            player.clearMediaItems()
            this@MusicPlayerService.stopSelf()
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            player.seekToNext()
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            player.seekToPrevious()
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            player.seekTo(pos)
        }

        private fun play() {
            mediaSession.setState(PlaybackStateCompat.STATE_PLAYING, player.currentPosition)
            mediaSession.isActive = true
            playerNotificationManager.showNotification(player)
            player.prepare()
            player.play()
        }
    }

    private inner class SimplePlayerListener : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if (!isPlaying) {
                mediaSession.setState(PlaybackStateCompat.STATE_PAUSED, player.currentPosition)
            } else {
                mediaSession.setState(PlaybackStateCompat.STATE_PLAYING, player.currentPosition)
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            val mediaItemUri = mediaItem?.localConfiguration?.uri
            val songMetadata = playlistMetadata.find { item ->
                item.getString(MediaMetadataCompat.METADATA_KEY_ART_URI) == mediaItemUri.toString()
            }
            mediaSession.setMetadata(songMetadata)
            if (mediaSession.controller.playbackState.state == PlaybackStateCompat.STATE_PAUSED) {
                mediaSession.setState(PlaybackStateCompat.STATE_PAUSED, 0)
            } else {
                mediaSession.setState(PlaybackStateCompat.STATE_PLAYING, 0)
            }
            playerNotificationManager.showNotification(player)
        }
    }

    private inner class SimplePlayerNotificationListener :
        PlayerNotificationManager.NotificationListener {
        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            stopForeground(true)
            stopSelf()
        }
    }

    companion object {
        private const val MEDIA_SESSION_TAG = "SimplePlayerMediaSession"
    }
}
