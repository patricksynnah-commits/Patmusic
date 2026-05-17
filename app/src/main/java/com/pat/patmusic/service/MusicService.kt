package com.pat.patmusic.service

import android.app.*
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.pat.patmusic.R
import com.pat.patmusic.model.Song
import com.pat.patmusic.ui.MainActivity

class MusicService : Service(), MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    AudioManager.OnAudioFocusChangeListener {

    companion object {
        const val CHANNEL_ID = "patmusic_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_PLAY = "com.pat.patmusic.PLAY"
        const val ACTION_PAUSE = "com.pat.patmusic.PAUSE"
        const val ACTION_NEXT = "com.pat.patmusic.NEXT"
        const val ACTION_PREV = "com.pat.patmusic.PREV"
        const val ACTION_STOP = "com.pat.patmusic.STOP"

        const val REPEAT_NONE = 0
        const val REPEAT_ALL = 1
        const val REPEAT_ONE = 2
    }

    private val binder = MusicBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var mediaSession: MediaSessionCompat? = null

    var songList: List<Song> = emptyList()
    var currentIndex: Int = 0
    var isShuffleOn: Boolean = false
    var repeatMode: Int = REPEAT_NONE
    private var shuffledIndices: MutableList<Int> = mutableListOf()

    var onSongChangedListener: ((Song) -> Unit)? = null
    var onPlayStateChangedListener: ((Boolean) -> Unit)? = null
    var onProgressChangedListener: ((Int, Int) -> Unit)? = null

    val currentSong: Song? get() = if (songList.isNotEmpty() && currentIndex < songList.size) songList[currentIndex] else null
    val isPlaying: Boolean get() = mediaPlayer?.isPlaying ?: false
    val currentPosition: Int get() = mediaPlayer?.currentPosition ?: 0
    val duration: Int get() = mediaPlayer?.duration ?: 0

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        createNotificationChannel()
        initMediaSession()
    }

    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(this, "PatmusicSession").apply {
            isActive = true
        }
    }

    private fun initMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setOnCompletionListener(this@MusicService)
            setOnPreparedListener(this@MusicService)
            setOnErrorListener(this@MusicService)
        }
    }

    fun playSong(index: Int) {
        if (songList.isEmpty()) return
        currentIndex = index.coerceIn(0, songList.size - 1)
        val song = songList[currentIndex]

        requestAudioFocus()
        initMediaPlayer()
        try {
            mediaPlayer?.apply {
                setDataSource(applicationContext, song.uri)
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        onSongChangedListener?.invoke(song)
        showNotification(song)
    }

    fun playOrPause() {
        if (mediaPlayer == null) {
            playSong(currentIndex)
            return
        }
        if (isPlaying) pause() else resume()
    }

    fun pause() {
        mediaPlayer?.pause()
        onPlayStateChangedListener?.invoke(false)
        currentSong?.let { showNotification(it) }
    }

    fun resume() {
        requestAudioFocus()
        mediaPlayer?.start()
        onPlayStateChangedListener?.invoke(true)
        currentSong?.let { showNotification(it) }
    }

    fun next() {
        val nextIndex = getNextIndex()
        playSong(nextIndex)
    }

    fun previous() {
        if (currentPosition > 3000) {
            seekTo(0)
            return
        }
        val prevIndex = getPreviousIndex()
        playSong(prevIndex)
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    private fun getNextIndex(): Int {
        return if (isShuffleOn) {
            if (shuffledIndices.isEmpty()) buildShuffleList()
            val current = shuffledIndices.indexOf(currentIndex)
            if (current < shuffledIndices.size - 1) shuffledIndices[current + 1]
            else { buildShuffleList(); shuffledIndices[0] }
        } else {
            if (currentIndex < songList.size - 1) currentIndex + 1 else 0
        }
    }

    private fun getPreviousIndex(): Int {
        return if (isShuffleOn) {
            val current = shuffledIndices.indexOf(currentIndex)
            if (current > 0) shuffledIndices[current - 1]
            else shuffledIndices.lastOrNull() ?: 0
        } else {
            if (currentIndex > 0) currentIndex - 1 else songList.size - 1
        }
    }

    private fun buildShuffleList() {
        shuffledIndices = (songList.indices).toMutableList().also { it.shuffle() }
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(this)
                .build()
            audioFocusRequest = request
            audioManager?.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->
                mediaPlayer?.setVolume(0.3f, 0.3f)
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer?.setVolume(1.0f, 1.0f)
                resume()
            }
        }
    }

    override fun onPrepared(mp: MediaPlayer) {
        mp.start()
        onPlayStateChangedListener?.invoke(true)
    }

    override fun onCompletion(mp: MediaPlayer) {
        when (repeatMode) {
            REPEAT_ONE -> playSong(currentIndex)
            REPEAT_ALL -> next()
            else -> {
                if (currentIndex < songList.size - 1) next()
                else {
                    onPlayStateChangedListener?.invoke(false)
                }
            }
        }
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        mp.reset()
        return false
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Patmusic Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music playback controls"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(song: Song) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseAction = if (isPlaying) {
            NotificationCompat.Action(
                R.drawable.ic_pause, "Pause",
                createActionIntent(ACTION_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                R.drawable.ic_play, "Play",
                createActionIntent(ACTION_PLAY)
            )
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setSubText(song.album)
            .setSmallIcon(R.drawable.ic_music_note)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.default_album_art))
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_skip_previous, "Previous", createActionIntent(ACTION_PREV))
            .addAction(playPauseAction)
            .addAction(R.drawable.ic_skip_next, "Next", createActionIntent(ACTION_NEXT))
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
                    .setMediaSession(mediaSession?.sessionToken)
            )
            .setOngoing(isPlaying)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createActionIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply { this.action = action }
        return PendingIntent.getService(
            this, action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> resume()
            ACTION_PAUSE -> pause()
            ACTION_NEXT -> next()
            ACTION_PREV -> previous()
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        mediaSession?.release()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        }
    }
}
