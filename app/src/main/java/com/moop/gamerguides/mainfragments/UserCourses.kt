package com.moop.gamerguides.mainfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.moop.gamerguides.R
import com.moop.gamerguides.adapter.CourseAdapter
import com.moop.gamerguides.adapter.model.Courses
import com.moop.gamerguides.helper.FirebaseUtil
import com.moop.gamerguides.helper.WrapContentLinearLayoutManager

class UserCourses : Fragment() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var adapter: CourseAdapter

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

        val emptyContainer: RelativeLayout = view.findViewById(R.id.empty_container)
        var userCourseCount: Int

        // set recyclerview layout manager
        val courseList: RecyclerView = view.findViewById(R.id.course_list)
        courseList.layoutManager = WrapContentLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        // set adapter options
        val options = FirebaseRecyclerOptions.Builder<Courses>()
            .setQuery(firebaseDatabase.reference.child("courses"), Courses::class.java)
            .build()

        // create adapter
        adapter = CourseAdapter(options)
        courseList.adapter = adapter

        // get course id from user database
        firebaseDatabase.reference
            .child("users")
            .child(firebaseAuth.currentUser!!.uid)
            .child("courses")
            .get()
            .addOnSuccessListener {
                // get course count from a specific user
                userCourseCount = it.childrenCount.toInt()

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


    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

}