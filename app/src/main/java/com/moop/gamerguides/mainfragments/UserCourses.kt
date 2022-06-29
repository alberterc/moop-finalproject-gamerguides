@file:Suppress("DEPRECATION")

package com.moop.gamerguides.mainfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.moop.gamerguides.R
import com.moop.gamerguides.adapter.MyCourseAdapter
import com.moop.gamerguides.helper.FirebaseUtil

class UserCourses : Fragment() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var adapter: MyCourseAdapter
    private lateinit var emptyContainer: RelativeLayout
    private var userCourseCount: Int = 0
    private lateinit var courseList: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_courses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initialize Firebase auth
        firebaseAuth = Firebase.auth
        // initialize Firebase Database
        firebaseDatabase = Firebase.database(FirebaseUtil.firebaseDatabaseURL)

        emptyContainer = view.findViewById(R.id.empty_container)

        // set recyclerview layout manager
        courseList = view.findViewById(R.id.course_list)
        courseList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        // swipe down to refresh
        val swipeRefreshLayout: SwipeRefreshLayout = view.findViewById(R.id.swipe_refresh)
        swipeRefreshLayout.setOnRefreshListener {
            initDataFromFirebase()
            swipeRefreshLayout.isRefreshing = false
        }

    }

    override fun onStart() {
        super.onStart()
        initDataFromFirebase()
    }

    private fun initDataFromFirebase() {
        var courseListU: List<String>
        // get user courses
        firebaseDatabase.reference
            .child("users")
            .child(firebaseAuth.currentUser!!.uid)
            .child("courses")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val t = object : GenericTypeIndicator<List<String>>() {}
                    if (snapshot.exists()) {
                        // get courses in course directory
                        courseListU = snapshot.getValue(t)!!
                        // create adapter
                        adapter = MyCourseAdapter(courseListU, context)
                        courseList.adapter = adapter
                    }
                }

                override fun onCancelled(error: DatabaseError) {}

            })

        // get course id from user database
        firebaseDatabase.reference
            .child("users")
            .child(firebaseAuth.currentUser!!.uid)
            .child("courses")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    // get course count from a specific user
                    userCourseCount = snapshot.childrenCount.toInt()

                    // check if the user has a course
                    // if user has course
                    if (userCourseCount != 0) {
                        emptyContainer.visibility = View.INVISIBLE
                        courseList.visibility = View.VISIBLE
                    }
                    // if user doesn't have course
                    else {
                        emptyContainer.visibility = View.VISIBLE
                        courseList.visibility = View.INVISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {}

            })
    }
}