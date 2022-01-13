package com.app.simpleplayer.presentation.screens.home

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.*
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.app.simpleplayer.SimplePlayerApp
import com.app.simpleplayer.domain.usecases.GetMusicFromExternalStorageUseCase
import com.app.simpleplayer.presentation.MainActivity
import com.app.simpleplayer.presentation.utils.setState
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
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

    private var playlist = listOf<MediaMetadataCompat>()

    private val scope = MainScope()
    private val audioAttributes =
        AudioAttributes.Builder()
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
    private val stateBuilder = PlaybackStateCompat.Builder()
        .setActions(
            PlaybackStateCompat.ACTION_PLAY
                    or PlaybackStateCompat.ACTION_PLAY_PAUSE
                    or PlaybackStateCompat.ACTION_PAUSE
                    or PlaybackStateCompat.ACTION_STOP
        )

    private val activityIntent by lazy {
        Intent(this, MainActivity::class.java)
    }
    private val activityPendingIntent by lazy {
        PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE)
    }
    private val playerNotificationManager by lazy {
        SimplePlayerNotificationManager(this, mediaSession, SimplePlayerNotificationListener())
    }

    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            super.onPlay()
            play()
        }

        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            super.onPlayFromUri(uri, extras)
            if (uri == null) return
            val songMetadata = playlist.find { item ->
                item.getString(MediaMetadataCompat.METADATA_KEY_ART_URI) == uri.toString()
            }
            mediaSession.setMetadata(songMetadata)
            player.setMediaItem(MediaItem.fromUri(uri))
            play()
        }

        override fun onPause() {
            super.onPause()
            mediaSession.setState(stateBuilder, PlaybackStateCompat.STATE_PAUSED)
            player.pause()
            playerNotificationManager.showNotification(player)
        }

        override fun onStop() {
            super.onStop()
            mediaSession.isActive = false
            playerNotificationManager.hideNotification()
            player.stop()
            player.clearMediaItems()
        }

        private fun play() {
            //TODO: Implement audioFocus request asap
            mediaSession.setState(stateBuilder, PlaybackStateCompat.STATE_PLAYING)
            mediaSession.isActive = true
            playerNotificationManager.showNotification(player)
            player.prepare()
            player.play()
        }
    }
    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if (!isPlaying) {
                mediaSession.setState(stateBuilder, PlaybackStateCompat.STATE_PAUSED)
            } else {
                mediaSession.setState(stateBuilder, PlaybackStateCompat.STATE_PLAYING)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        (application as SimplePlayerApp).androidInjector().inject(this)
        player = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .setAudioAttributes(audioAttributes, true)
            .build().apply {
                addListener(playerListener)
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
            setCallback(mediaSessionCallback)
            setSessionActivity(activityPendingIntent)
            isActive = true
        }
        scope.launch {
            getMusicFromExternalStorageUseCase().collect { songs ->
                val convertedSongs = songs.map { song ->
                    MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                        .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, song.uri.toString())
                        .build()
                }
                playlist = convertedSongs
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
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
