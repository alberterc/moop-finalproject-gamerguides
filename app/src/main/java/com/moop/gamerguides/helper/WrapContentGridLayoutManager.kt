package com.moop.gamerguides.helper

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// fixes recyclerview content inconsistency when getting updates from firebase database
class WrapContentGridLayoutManager : GridLayoutManager {
    constructor(context: Context?, spanCount: Int) : super(context, spanCount) {}

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
//            Log.e("TAG", "found IndexOutOfBoundsException in RecyclerView")
        }
    }
}