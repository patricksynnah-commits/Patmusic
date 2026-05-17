package com.pat.patmusic.ui

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import com.pat.patmusic.R
import com.pat.patmusic.databinding.ActivityMainBinding
import com.pat.patmusic.service.MusicService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel: MusicViewModel
    var musicService: MusicService? = null
    var serviceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            serviceBound = true
            setupServiceCallbacks()
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBound = false
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            viewModel.loadSongs()
        } else {
            Toast.makeText(this, "Storage permission required to load music", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MusicViewModel::class.java]

        setupTabs()
        setupBottomPlayer()
        requestPermissions()
        startMusicService()
    }

    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Songs").setIcon(R.drawable.ic_music_note))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Albums").setIcon(R.drawable.ic_album))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Artists").setIcon(R.drawable.ic_artist))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Favorites").setIcon(R.drawable.ic_favorite))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { pos ->
                    viewModel.setCurrentTab(pos)
                    loadFragment(pos)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        loadFragment(0)
    }

    private fun loadFragment(position: Int) {
        val fragment = when (position) {
            0 -> SongsFragment()
            1 -> AlbumsFragment()
            2 -> ArtistsFragment()
            3 -> FavoritesFragment()
            else -> SongsFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun setupBottomPlayer() {
        viewModel.currentSong.observe(this) { song ->
            song?.let {
                binding.bottomPlayer.tvSongTitle.text = it.title
                binding.bottomPlayer.tvArtist.text = it.artist
                binding.bottomPlayer.root.setOnClickListener { _ ->
                    startActivity(Intent(this, PlayerActivity::class.java))
                }
            }
        }

        viewModel.isPlaying.observe(this) { playing ->
            binding.bottomPlayer.btnPlayPause.setImageResource(
                if (playing) R.drawable.ic_pause else R.drawable.ic_play
            )
        }

        binding.bottomPlayer.btnPlayPause.setOnClickListener {
            musicService?.playOrPause()
        }

        binding.bottomPlayer.btnNext.setOnClickListener {
            musicService?.next()
        }
    }

    private fun setupServiceCallbacks() {
        musicService?.onSongChangedListener = { song ->
            runOnUiThread {
                viewModel.setCurrentSong(song)
            }
        }
        musicService?.onPlayStateChangedListener = { playing ->
            runOnUiThread {
                viewModel.setPlaying(playing)
            }
        }
    }

    private fun startMusicService() {
        val intent = Intent(this, MusicService::class.java)
        startService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun requestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            viewModel.loadSongs()
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }
}
