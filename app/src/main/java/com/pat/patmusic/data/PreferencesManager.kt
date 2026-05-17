package com.pat.patmusic.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("patmusic_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_REPEAT_MODE = "repeat_mode"
        private const val KEY_SHUFFLE = "shuffle"
        private const val KEY_LAST_SONG_ID = "last_song_id"
        private const val KEY_LAST_POSITION = "last_position"
        private const val KEY_VOLUME = "volume"
        private const val KEY_EQUALIZER_ENABLED = "eq_enabled"
    }

    fun getFavorites(): Set<Long> {
        val raw = prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
        return raw.mapNotNull { it.toLongOrNull() }.toSet()
    }

    fun addFavorite(songId: Long) {
        val current = prefs.getStringSet(KEY_FAVORITES, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        current.add(songId.toString())
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
    }

    fun removeFavorite(songId: Long) {
        val current = prefs.getStringSet(KEY_FAVORITES, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        current.remove(songId.toString())
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
    }

    fun isFavorite(songId: Long): Boolean {
        return getFavorites().contains(songId)
    }

    fun getRepeatMode(): Int = prefs.getInt(KEY_REPEAT_MODE, 0)
    fun setRepeatMode(mode: Int) = prefs.edit().putInt(KEY_REPEAT_MODE, mode).apply()

    fun getShuffleEnabled(): Boolean = prefs.getBoolean(KEY_SHUFFLE, false)
    fun setShuffleEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SHUFFLE, enabled).apply()

    fun getLastSongId(): Long = prefs.getLong(KEY_LAST_SONG_ID, -1L)
    fun setLastSongId(id: Long) = prefs.edit().putLong(KEY_LAST_SONG_ID, id).apply()

    fun getLastPosition(): Int = prefs.getInt(KEY_LAST_POSITION, 0)
    fun setLastPosition(position: Int) = prefs.edit().putInt(KEY_LAST_POSITION, position).apply()

    fun getVolume(): Float = prefs.getFloat(KEY_VOLUME, 1.0f)
    fun setVolume(volume: Float) = prefs.edit().putFloat(KEY_VOLUME, volume).apply()

    fun getEqualizerEnabled(): Boolean = prefs.getBoolean(KEY_EQUALIZER_ENABLED, false)
    fun setEqualizerEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_EQUALIZER_ENABLED, enabled).apply()
}
