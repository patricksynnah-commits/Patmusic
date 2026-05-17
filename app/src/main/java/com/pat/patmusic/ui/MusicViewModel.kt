package com.pat.patmusic.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pat.patmusic.data.MusicRepository
import com.pat.patmusic.data.PreferencesManager
import com.pat.patmusic.model.Song

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    val repository = MusicRepository(application)
    val prefs = PreferencesManager(application)

    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> = _songs

    private val _currentSong = MutableLiveData<Song?>()
    val currentSong: LiveData<Song?> = _currentSong

    private val _isPlaying = MutableLiveData<Boolean>(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _currentTab = MutableLiveData<Int>(0)
    val currentTab: LiveData<Int> = _currentTab

    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> = _searchQuery

    private val _favorites = MutableLiveData<Set<Long>>()
    val favorites: LiveData<Set<Long>> = _favorites

    init {
        _favorites.value = prefs.getFavorites()
    }

    fun loadSongs() {
        val all = repository.getAllSongs().map { song ->
            song.copy(isFavorite = prefs.isFavorite(song.id))
        }
        _songs.value = all
    }

    fun setCurrentSong(song: Song?) {
        _currentSong.value = song
    }

    fun setPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }

    fun setCurrentTab(tab: Int) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(song: Song) {
        if (prefs.isFavorite(song.id)) {
            prefs.removeFavorite(song.id)
        } else {
            prefs.addFavorite(song.id)
        }
        _favorites.value = prefs.getFavorites()
        loadSongs()
    }

    fun getFavoriteSongs(): List<Song> {
        val favIds = prefs.getFavorites()
        return (_songs.value ?: emptyList()).filter { favIds.contains(it.id) }
    }

    fun getSongsByAlbum(): Map<String, List<Song>> {
        return (_songs.value ?: emptyList()).groupBy { it.album }
    }

    fun getSongsByArtist(): Map<String, List<Song>> {
        return (_songs.value ?: emptyList()).groupBy { it.artist }
    }

    fun searchSongs(query: String): List<Song> {
        val q = query.lowercase()
        return (_songs.value ?: emptyList()).filter {
            it.title.lowercase().contains(q) ||
            it.artist.lowercase().contains(q) ||
            it.album.lowercase().contains(q)
        }
    }
}
