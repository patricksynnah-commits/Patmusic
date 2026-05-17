package com.pat.patmusic.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.pat.patmusic.adapter.SongAdapter
import com.pat.patmusic.databinding.FragmentSongsBinding
import com.pat.patmusic.model.Song

class SongsFragment : Fragment() {

    private var _binding: FragmentSongsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MusicViewModel
    private lateinit var songAdapter: SongAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSongsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MusicViewModel::class.java]

        setupRecyclerView()
        setupSearch()
        observeSongs()
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            onSongClick = { song, index, songs ->
                playSong(song, index, songs)
            },
            onFavoriteClick = { song ->
                viewModel.toggleFavorite(song)
            }
        )
        binding.recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = songAdapter
        binding.recyclerView.setHasFixedSize(true)
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.isEmpty()) {
                    songAdapter.submitList(viewModel.songs.value ?: emptyList())
                } else {
                    songAdapter.submitList(viewModel.searchSongs(query))
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeSongs() {
        viewModel.songs.observe(viewLifecycleOwner) { songs ->
            songAdapter.submitList(songs)
            binding.tvSongCount.text = "${songs.size} songs"
            binding.emptyState.visibility = if (songs.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.favorites.observe(viewLifecycleOwner) {
            songAdapter.notifyDataSetChanged()
        }
    }

    private fun playSong(song: Song, index: Int, allSongs: List<Song>) {
        val activity = requireActivity() as MainActivity
        activity.musicService?.let { service ->
            service.songList = allSongs
            service.playSong(index)
            viewModel.setCurrentSong(song)
            startActivity(Intent(requireContext(), PlayerActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
