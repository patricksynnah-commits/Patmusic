package com.pat.patmusic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pat.patmusic.R
import com.pat.patmusic.databinding.ItemAlbumBinding
import com.pat.patmusic.model.Song

class AlbumAdapter(
    private val onAlbumClick: (String, List<Song>) -> Unit
) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    private var albums: List<Pair<String, List<Song>>> = emptyList()

    fun submitData(data: Map<String, List<Song>>) {
        albums = data.entries.sortedBy { it.key }.map { Pair(it.key, it.value) }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(albums[position])
    }

    override fun getItemCount() = albums.size

    inner class AlbumViewHolder(private val binding: ItemAlbumBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pair: Pair<String, List<Song>>) {
            val (albumName, songs) = pair
            binding.tvAlbumName.text = albumName
            binding.tvArtist.text = songs.firstOrNull()?.artist ?: "Unknown Artist"
            binding.tvSongCount.text = "${songs.size} songs"

            Glide.with(binding.root.context)
                .load(songs.firstOrNull()?.albumArtUri)
                .placeholder(R.drawable.default_album_art)
                .error(R.drawable.default_album_art)
                .into(binding.ivAlbumArt)

            binding.root.setOnClickListener { onAlbumClick(albumName, songs) }
        }
    }
}
