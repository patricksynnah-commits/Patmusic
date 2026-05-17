package com.pat.patmusic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pat.patmusic.R
import com.pat.patmusic.databinding.ItemSongBinding
import com.pat.patmusic.model.Song

class SongAdapter(
    private val onSongClick: (Song, Int, List<Song>) -> Unit,
    private val onFavoriteClick: (Song) -> Unit
) : ListAdapter<Song, SongAdapter.SongViewHolder>(SongDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(getItem(position), position, currentList)
    }

    inner class SongViewHolder(private val binding: ItemSongBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song, position: Int, songs: List<Song>) {
            binding.tvTitle.text = song.title
            binding.tvArtist.text = song.artist
            binding.tvDuration.text = song.getDurationFormatted()

            Glide.with(binding.root.context)
                .load(song.albumArtUri)
                .placeholder(R.drawable.default_album_art)
                .error(R.drawable.default_album_art)
                .into(binding.ivAlbumArt)

            binding.btnFavorite.setImageResource(
                if (song.isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
            )

            binding.root.setOnClickListener { onSongClick(song, position, songs) }
            binding.btnFavorite.setOnClickListener { onFavoriteClick(song) }
        }
    }

    class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Song, newItem: Song) = oldItem == newItem
    }
}
