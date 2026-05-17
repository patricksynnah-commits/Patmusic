package com.pat.patmusic.model

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val uri: Uri,
    val albumArtUri: Uri?,
    val data: String,
    var isFavorite: Boolean = false
) {
    fun getDurationFormatted(): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return "%d:%02d".format(minutes, seconds)
    }
}
