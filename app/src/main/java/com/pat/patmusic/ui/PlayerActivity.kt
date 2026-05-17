package com.pat.patmusic.ui

import android.content.*
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.pat.patmusic.R
import com.pat.patmusic.databinding.ActivityPlayerBinding
import com.pat.patmusic.service.MusicService

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var viewModel: MusicViewModel
    private var musicService: MusicService? = null
    private var serviceBound = false
    private var progressHandler: Handler? = null
    private var progressRunnable: Runnable? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            serviceBound = true
            updateUI()
            startProgressUpdater()
            setupServiceCallbacks()
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MusicViewModel::class.java]

        supportActionBar?.hide()
        progressHandler = Handler(Looper.getMainLooper())

        bindService(
            Intent(this, MusicService::class.java),
            serviceConnection, Context.BIND_AUTO_CREATE
        )

        setupControls()
        observeViewModel()
    }

    private fun setupControls() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnPlayPause.setOnClickListener {
            musicService?.playOrPause()
        }

        binding.btnNext.setOnClickListener { musicService?.next() }
        binding.btnPrevious.setOnClickListener { musicService?.previous() }

        binding.btnShuffle.setOnClickListener {
            musicService?.let { svc ->
                svc.isShuffleOn = !svc.isShuffleOn
                binding.btnShuffle.alpha = if (svc.isShuffleOn) 1.0f else 0.4f
            }
        }

        binding.btnRepeat.setOnClickListener {
            musicService?.let { svc ->
                svc.repeatMode = (svc.repeatMode + 1) % 3
                updateRepeatIcon(svc.repeatMode)
            }
        }

        binding.btnFavorite.setOnClickListener {
            viewModel.currentSong.value?.let { song ->
                viewModel.toggleFavorite(song)
                updateFavoriteIcon(viewModel.prefs.isFavorite(song.id))
            }
        }

        binding.seekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicService?.seekTo(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
    }

    private fun observeViewModel() {
        viewModel.currentSong.observe(this) { song ->
            song?.let { updateSongInfo(it) }
        }
        viewModel.isPlaying.observe(this) { playing ->
            binding.btnPlayPause.setImageResource(
                if (playing) R.drawable.ic_pause_circle else R.drawable.ic_play_circle
            )
        }
    }

    private fun updateUI() {
        musicService?.currentSong?.let { updateSongInfo(it) }
        val playing = musicService?.isPlaying ?: false
        binding.btnPlayPause.setImageResource(
            if (playing) R.drawable.ic_pause_circle else R.drawable.ic_play_circle
        )
        musicService?.let { svc ->
            binding.btnShuffle.alpha = if (svc.isShuffleOn) 1.0f else 0.4f
            updateRepeatIcon(svc.repeatMode)
        }
    }

    private fun updateSongInfo(song: com.pat.patmusic.model.Song) {
        binding.tvTitle.text = song.title
        binding.tvArtist.text = song.artist
        binding.tvAlbum.text = song.album
        updateFavoriteIcon(viewModel.prefs.isFavorite(song.id))

        Glide.with(this)
            .load(song.albumArtUri)
            .placeholder(R.drawable.default_album_art)
            .error(R.drawable.default_album_art)
            .into(binding.ivAlbumArt)
    }

    private fun updateRepeatIcon(mode: Int) {
        when (mode) {
            MusicService.REPEAT_NONE -> {
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat)
                binding.btnRepeat.alpha = 0.4f
            }
            MusicService.REPEAT_ALL -> {
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat)
                binding.btnRepeat.alpha = 1.0f
            }
            MusicService.REPEAT_ONE -> {
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat_one)
                binding.btnRepeat.alpha = 1.0f
            }
        }
    }

    private fun updateFavoriteIcon(isFavorite: Boolean) {
        binding.btnFavorite.setImageResource(
            if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        )
    }

    private fun startProgressUpdater() {
        progressRunnable = object : Runnable {
            override fun run() {
                musicService?.let { svc ->
                    val current = svc.currentPosition
                    val total = svc.duration
                    if (total > 0) {
                        binding.seekBar.max = total
                        binding.seekBar.progress = current
                        binding.tvCurrentTime.text = formatTime(current)
                        binding.tvTotalTime.text = formatTime(total)
                    }
                }
                progressHandler?.postDelayed(this, 500)
            }
        }
        progressHandler?.post(progressRunnable!!)
    }

    private fun setupServiceCallbacks() {
        musicService?.onSongChangedListener = { song ->
            runOnUiThread {
                viewModel.setCurrentSong(song)
                updateSongInfo(song)
            }
        }
        musicService?.onPlayStateChangedListener = { playing ->
            runOnUiThread {
                viewModel.setPlaying(playing)
                binding.btnPlayPause.setImageResource(
                    if (playing) R.drawable.ic_pause_circle else R.drawable.ic_play_circle
                )
            }
        }
    }

    private fun formatTime(ms: Int): String {
        val minutes = (ms / 1000) / 60
        val seconds = (ms / 1000) % 60
        return "%d:%02d".format(minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        progressRunnable?.let { progressHandler?.removeCallbacks(it) }
        if (serviceBound) {
            unbindService(serviceConnection)
        }
    }
}
