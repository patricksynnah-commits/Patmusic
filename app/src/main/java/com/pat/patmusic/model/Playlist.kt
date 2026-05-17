package com.pat.patmusic.model

data class Playlist(
    val id: Long,
    val name: String,
    val songs: MutableList<Song> = mutableListOf(),
    val createdAt: Long = System.currentTimeMillis()
) {
    val songCount: Int get() = songs.size
}
