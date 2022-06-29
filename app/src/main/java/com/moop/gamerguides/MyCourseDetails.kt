package com.moop.gamerguides

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.moop.gamerguides.helper.FirebaseUtil
import com.squareup.picasso.Picasso

class MyCourseDetails : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseStorage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_course_details)

        // initialize Firebase auth
        firebaseAuth = Firebase.auth
        // initialize Firebase Database
        firebaseDatabase = Firebase.database(FirebaseUtil.firebaseDatabaseURL)
        // initialize Firebase Storage
        firebaseStorage = Firebase.storage(FirebaseUtil.firebaseStorageURL)

        val courseTitleView: TextView = findViewById(R.id.course_title)
        val courseImageView: ShapeableImageView = findViewById(R.id.course_image)
        val courseDescriptionView: TextView = findViewById(R.id.course_description)
        val addNewVideo: CardView = findViewById(R.id.add_video_button)
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


        // add new video button onclick function

    }
}