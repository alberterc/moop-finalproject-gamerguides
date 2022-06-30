@file:Suppress("DEPRECATION")

package com.moop.gamerguides

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.moop.gamerguides.adapter.VideoAdapter
import com.moop.gamerguides.adapter.model.Videos
import com.moop.gamerguides.helper.FirebaseUtil
import com.moop.gamerguides.helper.ListToMutableList
import com.squareup.picasso.Picasso

class CourseDetails : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var adapter: VideoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_details)

        // initialize Firebase auth
        firebaseAuth = Firebase.auth
        // initialize Firebase Database
        firebaseDatabase = Firebase.database(FirebaseUtil.firebaseDatabaseURL)
        // initialize Firebase Storage
        firebaseStorage = Firebase.storage(FirebaseUtil.firebaseStorageURL)

        val courseTitleView: TextView = findViewById(R.id.course_title)
        val courseImageView: ShapeableImageView = findViewById(R.id.course_image)
        val courseDescriptionView: TextView = findViewById(R.id.course_description)
        val userProfileButton: CardView = findViewById(R.id.user_profile_button)
        val addToFavoriteButton: TextView = findViewById(R.id.add_favorite_button)
        val videoList: RecyclerView = findViewById(R.id.video_list)

        // make course description view scrollable
        courseDescriptionView.movementMethod = ScrollingMovementMethod()

        // receive course id from intent
        val courseID = intent.extras!!.getString("courseID")

        // course database reference
        val courseRef: DatabaseReference = firebaseDatabase.reference.child("courses").child(courseID!!)

        // get course title from firebase database
        courseRef.child("title").get()
            .addOnSuccessListener {
                courseTitleView.text = it.value.toString()
            }

        // get course image from firebase database
        courseRef.child("image").get()
            .addOnSuccessListener {
                if (it.value.toString() != "") {
                    Picasso.get()
                        .load(it.value.toString())
                        .into(courseImageView)
                }
                else {
                    courseImageView.setImageResource(R.drawable.gamerguides_logo)
                }
            }

        // get course description from firebase database
        courseRef.child("description").get()
            .addOnSuccessListener {
                courseDescriptionView.text = it.value.toString()
            }

        // get course videos from firebase database
        // set adapter options
        val options = FirebaseRecyclerOptions.Builder<Videos>()
            .setQuery(firebaseDatabase.reference.child("courses").child(courseID).child("videos"), Videos::class.java)
            .build()

        // create adapter
        adapter = VideoAdapter(options, courseID)
        videoList.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        videoList.adapter = adapter

        // user profile card view onclick function

        // add to favorite button onclick function
        addToFavoriteButton.setOnClickListener {
            // store user courses id
            var userCourseList: List<String> = emptyList()
            var addedCourseList: List<String?>
            var removeCourseList: List<String?>

            if (addToFavoriteButton.text == "Add to Favorite") {
                // get all courses from favorites directory
                firebaseDatabase.reference
                    .child("users")
                    .child(firebaseAuth.currentUser!!.uid)
                    .child("favorites")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val t = object : GenericTypeIndicator<List<String>>() {}
                            if (snapshot.exists()) {
                                // get courses in favorites directory
                                userCourseList = snapshot.getValue(t)!!
                            }
                            // add a new course id into course list
                            addedCourseList = ListToMutableList.addElement(userCourseList, courseID)

                            // input new favorite course into the favorites directory in the user database
                            firebaseDatabase.reference
                                .child("users")
                                .child(firebaseAuth.currentUser!!.uid)
                                .child("favorites")
                                .setValue(addedCourseList)
                        }

                        override fun onCancelled(error: DatabaseError) {}

                    })
            }
            else {
                // get all courses from favorites directory
                firebaseDatabase.reference
                    .child("users")
                    .child(firebaseAuth.currentUser!!.uid)
                    .child("favorites")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val t = object : GenericTypeIndicator<List<String>>() {}
                            if (snapshot.exists()) {
                                // get courses in favorites directory
                                userCourseList = snapshot.getValue(t)!!
                            }

                            // remove a course from course list
                            removeCourseList = ListToMutableList.removeElement(userCourseList, courseID)
                            // input new favorite course into the favorites directory in the user database
                            firebaseDatabase.reference
                                .child("users")
                                .child(firebaseAuth.currentUser!!.uid)
                                .child("favorites")
                                .setValue(removeCourseList)
                        }

                        override fun onCancelled(error: DatabaseError) {}

                    })
            }
        }

        // change "Add to favorite" to "Remove from favorite"
        checkIsFavorite(courseID, addToFavoriteButton)

        // check is course favorite every 250 milliseconds
        val refreshHandler = Handler()
        val runnable: Runnable = object : Runnable {
            override fun run() {
                checkIsFavorite(courseID, addToFavoriteButton)
                refreshHandler.postDelayed(this, 250)
            }
        }
        refreshHandler.postDelayed(runnable, 250)
    }

    private fun checkIsFavorite(courseID: String, addToFavoriteButton: TextView) {
        // store user courses id
        var userCourseList: List<String> = emptyList()
        // get all courses from favorites directory
        firebaseDatabase.reference
            .child("users")
            .child(firebaseAuth.currentUser!!.uid)
            .child("favorites")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val t = object : GenericTypeIndicator<List<String>>() {}
                    if (snapshot.exists()) {
                        // get courses in favorites directory
                        userCourseList = snapshot.getValue(t)!!
                    }

                    for (course in userCourseList) {
                        if (course == courseID) {
                            addToFavoriteButton.text = "Remove from Favorite"
                            break
                        }
                        else {
                            addToFavoriteButton.text = "Add to Favorite"
                            break
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}

            })
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