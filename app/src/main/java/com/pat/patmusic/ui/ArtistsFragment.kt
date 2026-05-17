package com.pat.patmusic.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.pat.patmusic.adapter.ArtistAdapter
import com.pat.patmusic.databinding.FragmentArtistsBinding

class ArtistsFragment : Fragment() {

    private var _binding: FragmentArtistsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MusicViewModel
    private lateinit var artistAdapter: ArtistAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentArtistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MusicViewModel::class.java]

        artistAdapter = ArtistAdapter { _, songs ->
            val activity = requireActivity() as MainActivity
            activity.musicService?.let { service ->
                service.songList = songs
                service.playSong(0)
                viewModel.setCurrentSong(songs[0])
                startActivity(Intent(requireContext(), PlayerActivity::class.java))
            }
        }
        binding.recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = artistAdapter

        viewModel.songs.observe(viewLifecycleOwner) { _ ->
            val artists = viewModel.getSongsByArtist()
            artistAdapter.submitData(artists)
            binding.emptyState.visibility = if (artists.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
