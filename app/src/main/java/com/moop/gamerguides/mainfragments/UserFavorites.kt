package com.moop.gamerguides.mainfragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.moop.gamerguides.R
import com.moop.gamerguides.adapter.FavoriteCoursesAdapter
import com.moop.gamerguides.helper.FirebaseUtil

class UserFavorites : Fragment() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var courseList: RecyclerView
    private lateinit var emptyContainer: RelativeLayout
    private lateinit var adapter: FavoriteCoursesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initialize Firebase auth
        firebaseAuth = Firebase.auth
        // initialize Firebase Database
        firebaseDatabase = Firebase.database(FirebaseUtil.firebaseDatabaseURL)

        emptyContainer = view.findViewById(R.id.empty_container)

        // set recyclerview layout manager
        courseList = view.findViewById(R.id.favorite_list)
        courseList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        // swipe down to refresh function
        val swipeRefreshLayout : SwipeRefreshLayout= view.findViewById(R.id.swipe_refresh)
        swipeRefreshLayout.setOnRefreshListener {
            getFavoriteCourses()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onStart() {
        super.onStart()
        getFavoriteCourses()
    }

    private fun getFavoriteCourses() {
        var courseListU: List<String>
        val favoriteCoursesRef = firebaseDatabase.reference.child("users").child(firebaseAuth.currentUser!!.uid)
            .child("favorites")

        // get user courses
        favoriteCoursesRef
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val t = object : GenericTypeIndicator<List<String>>() {}
                    if (snapshot.exists()) {
                        // get favorites in course directory
                        courseListU = snapshot.getValue(t)!!
                        // create adapter
                        adapter = FavoriteCoursesAdapter(courseListU, context)
                        courseList.adapter = adapter
                    }
                }

                override fun onCancelled(error: DatabaseError) {}

            })

        var userFavoriteCourseCount: Int
        // check if user has a favorite course
        favoriteCoursesRef
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // get favorite course count from a specific user
                    userFavoriteCourseCount = snapshot.childrenCount.toInt()

                    // if user has favorite course
                    if (userFavoriteCourseCount != 0) {
                        emptyContainer.visibility = View.INVISIBLE
                        courseList.visibility = View.VISIBLE
                    }
                    // if user doesn't have favorite course
                    else {
                        emptyContainer.visibility = View.VISIBLE
                        courseList.visibility = View.INVISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {}

            })

    }
}