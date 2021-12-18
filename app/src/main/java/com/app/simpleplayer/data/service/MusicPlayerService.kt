package com.app.simpleplayer.data.service

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.*
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.app.simpleplayer.R
import com.app.simpleplayer.domain.usecases.GetMusicFromExternalStorageUseCase
import com.app.simpleplayer.presentation.MainActivity
import com.app.simpleplayer.presentation.PlayerNotificationButtonReceiver
import com.app.simpleplayer.presentation.utils.NOTIFICATION_CHANNEL_ID
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.util.NotificationUtil.IMPORTANCE_DEFAULT
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class MusicPlayerService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var getMusicFromExternalStorageUseCase: GetMusicFromExternalStorageUseCase

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSessionCompat

    private val scope = MainScope()
    private val songs by lazy {
        getMusicFromExternalStorageUseCase()
            .stateIn(scope, SharingStarted.Lazily, listOf())
    }
    private val audioManager by lazy {
        getSystemService(AUDIO_SERVICE) as AudioManager
    }
    private val activityIntent by lazy {
        Intent(this, MainActivity::class.java)
    }
    private val activityPendingIntent by lazy {
        PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE)
    }
    private val description by lazy {
        mediaSession.controller.metadata.description
    }
    private val playerNotificationManager by lazy {
        PlayerNotificationManager.Builder(this, 1, NOTIFICATION_CHANNEL_ID)
            .setChannelNameResourceId(R.string.player_notification_channel_name)
            .setChannelDescriptionResourceId(R.string.player_notification_channel_description)
            .setPlayActionIconResourceId(R.drawable.ic_baseline_play_arrow_24)
            .setPauseActionIconResourceId(R.drawable.ic_baseline_pause_24)
            .setSmallIconResourceId(R.drawable.ic_baseline_music_note_24)
            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {

                override fun getCurrentContentTitle(player: Player): CharSequence =
                    getString(R.string.player_notification_title)

                override fun createCurrentContentIntent(player: Player): PendingIntent? =
                    activityPendingIntent

                override fun getCurrentContentText(player: Player): CharSequence =
                    "Smells Like Teen Spirit"

                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): Bitmap? =
                    BitmapFactory.decodeResource(resources, R.drawable.logo_simpe_music_player)

            })
            .setChannelImportance(IMPORTANCE_DEFAULT)
            .build()
    }
    private fun PlayerNotificationManager.createSimplePlayerNotification() =
        apply {
            setUsePreviousAction(false)
            setUseRewindAction(false)
            setUseFastForwardAction(false)
            setMediaSessionToken(mediaSession.sessionToken)
            setUseNextAction(true)
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setUsePlayPauseActions(true)
        }
    private val notificationBuilder by lazy {

        /*
        NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_music_note_24)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.logo_simpe_music_player
                )
            )
            .setContentTitle("description.title")
            .setContentText(getString(R.string.player_notification_title))
            .setShowWhen(false)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(activityPendingIntent)
            .setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_STOP
                )
            )
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0)
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            this,
                            PlaybackStateCompat.ACTION_STOP
                        )
                    )
            )
            .addAction(
                NotificationCompat.Action(
                    R.drawable.ic_baseline_pause_24,
                    getString(R.string.player_notification_pause_button),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE
                    )
                )
            )

         */
    }
    private val mediaButtonIntent by lazy {
        Intent(Intent.ACTION_MEDIA_BUTTON, null, this, MediaButtonReceiver::class.java)
    }

    private val audioFocusIdentityChangedListener =
        AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    mediaSessionCallback.onPlay()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    mediaSessionCallback.onPause()
                }
                else -> {
                    mediaSessionCallback.onStop()
                }
            }
        }
    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {

        override fun onPrepare() {
            super.onPrepare()
            player.prepare()
        }

        override fun onPlay() {
            super.onPlay()
            player.prepare()
            player.play()
        }

        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            super.onPlayFromUri(uri, extras)
            if (uri == null) return
            player.setMediaItem(MediaItem.fromUri(uri))
            player.prepare()
            playerNotificationManager.createSimplePlayerNotification()
        }

        override fun onPause() {
            super.onPause()
            player.pause()
        }

        override fun onStop() {
            super.onStop()

            player.stop()
            player.clearMediaItems()
        }

        fun play() {
            player.prepare()
        }
    }

    val mediaPlayer = MediaPlayer()

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(audioFocusIdentityChangedListener)
                .setAcceptsDelayedFocusGain(false)
                .setWillPauseWhenDucked(true)
                .setAudioAttributes(audioAttributes)
                .build()

        }

        player = ExoPlayer.Builder(this)
            .build()
        player.playWhenReady = true

        mediaSession = MediaSessionCompat(this, MEDIA_SESSION_TAG).apply {
            setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setActions(
                        PlaybackStateCompat.ACTION_PLAY
                                or PlaybackStateCompat.ACTION_PLAY_PAUSE
                                or PlaybackStateCompat.ACTION_STOP
                    )
                    .build()
            )
            setMediaButtonReceiver(
                PendingIntent.getBroadcast(
                    this@MusicPlayerService,
                    0,
                    mediaButtonIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            setSessionActivity(activityPendingIntent)
            setCallback(mediaSessionCallback)
            setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS)
            setSessionToken(sessionToken)
            isActive = true
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return if (SERVICE_INTERFACE == intent?.action) {
            super.onBind(intent)
        } else {
            ServiceBinder()
        }
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
        val mediaDescriptionCompatBuilder = MediaDescriptionCompat.Builder()
        scope.launch {
            songs.collectLatest { songs ->
                val convertedSongs = songs.map { song ->
                    val description = mediaDescriptionCompatBuilder
                        .setTitle(song.title)
                        .setIconBitmap(
                            BitmapFactory.decodeResource(
                                resources,
                                R.drawable.logo_simpe_music_player
                            )
                        )
                        .setMediaUri(song.uri)
                        .build()
                    MediaBrowserCompat.MediaItem(description, FLAG_PLAYABLE)
                }
                result.sendResult(convertedSongs.toMutableList())
            }
        }
    }

    @Deprecated("Replaced by ExoPlayer and MediaSession API")
    fun showPlayerNotification(
        songTitle: String,
        playPauseIcon: Int = R.drawable.ic_baseline_pause_24
    ) {

        val activityIntent = Intent(this, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val pauseActionIntent = Intent(this, PlayerNotificationButtonReceiver::class.java).apply {
            this.action = Playback.PLAY_PAUSE.name
            putExtra(SONG_TITLE_EXTRA_KEY, songTitle)
        }
        val pauseActionPendingIntent = PendingIntent
            .getBroadcast(
                this,
                0,
                pauseActionIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_music_note_24)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.logo_simpe_music_player
                )
            )
            .setContentIntent(contentPendingIntent)
            .setContentTitle(songTitle)
            .setContentText(getString(R.string.player_notification_title))
            .setShowWhen(false)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
            )
            .addAction(
                playPauseIcon,
                getString(R.string.player_notification_pause_button),
                pauseActionPendingIntent
            )
            .build()

        startForeground(1001, notification)
    }

    inner class ServiceBinder : Binder()

    companion object {
        private const val MEDIA_SESSION_TAG = "SimplePlayerMediaSession"
        const val SONG_TITLE_EXTRA_KEY = "INTENT_EXTRA_KEY"
        var INSTANCE: MusicPlayerService? = null
    }

}

enum class Playback {
    PLAY_PAUSE
}
