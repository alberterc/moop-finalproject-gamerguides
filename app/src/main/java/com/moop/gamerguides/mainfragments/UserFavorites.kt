package com.moop.gamerguides.mainfragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.moop.gamerguides.R

class UserFavorites : Fragment() {
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val swipeRefreshLayout : SwipeRefreshLayout= view.findViewById(R.id.swipe_refresh)
        swipeRefreshLayout.setOnRefreshListener {
            getFavoriteCourses()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun getFavoriteCourses() {
        Toast.makeText(context, "refreshed", Toast.LENGTH_SHORT)
            .show()

        recyclerView.layoutManager = LinearLayoutManager(context)
    }
}