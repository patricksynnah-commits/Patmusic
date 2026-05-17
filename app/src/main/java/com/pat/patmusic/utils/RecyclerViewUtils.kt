package com.pat.patmusic.utils

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

object RecyclerViewUtils {

    fun setupLinearList(recyclerView: RecyclerView, context: Context) {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
    }

    fun setupGridList(recyclerView: RecyclerView, context: Context, spanCount: Int = 2) {
        recyclerView.layoutManager = GridLayoutManager(context, spanCount)
        recyclerView.setHasFixedSize(true)
    }
}
