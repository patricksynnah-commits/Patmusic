package com.pat.patmusic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pat.patmusic.R
import com.pat.patmusic.databinding.ItemArtistBinding
import com.pat.patmusic.model.Song

class ArtistAdapter(
    private val onArtistClick: (String, List<Song>) -> Unit
) : RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {

    private var artists: List<Pair<String, List<Song>>> = emptyList()

    fun submitData(data: Map<String, List<Song>>) {
        artists = data.entries.sortedBy { it.key }.map { Pair(it.key, it.value) }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val binding = ItemArtistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        holder.bind(artists[position])
    }

    override fun getItemCount() = artists.size

    inner class ArtistViewHolder(private val binding: ItemArtistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pair: Pair<String, List<Song>>) {
            val (artistName, songs) = pair
            binding.tvArtistName.text = artistName
            val albumCount = songs.map { it.album }.distinct().size
            binding.tvInfo.text = "$albumCount albums · ${songs.size} songs"

            Glide.with(binding.root.context)
                .load(songs.firstOrNull()?.albumArtUri)
                .placeholder(R.drawable.default_album_art)
                .error(R.drawable.default_album_art)
                .circleCrop()
                .into(binding.ivArtistArt)

            binding.root.setOnClickListener { onArtistClick(artistName, songs) }
        }
    }
}
