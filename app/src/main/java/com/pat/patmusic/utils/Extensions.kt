package com.pat.patmusic.utils

import android.content.Context
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun RecyclerView.setupLinear(context: Context) {
    layoutManager = LinearLayoutManager(context)
    setHasFixedSize(true)
}

fun Long.toTimeString(): String {
    val seconds = (this / 1000) % 60
    val minutes = (this / 1000) / 60
    return "%d:%02d".format(minutes, seconds)
}

fun Int.toTimeString(): String {
    val seconds = (this / 1000) % 60
    val minutes = (this / 1000) / 60
    return "%d:%02d".format(minutes, seconds)
}
