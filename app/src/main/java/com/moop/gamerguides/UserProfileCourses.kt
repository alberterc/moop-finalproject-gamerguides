package com.moop.gamerguides

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.moop.gamerguides.adapter.UserProfileCoursesAdapter
import com.moop.gamerguides.helper.FirebaseUtil
import com.moop.gamerguides.helper.WrapContentLinearLayoutManager
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserProfileCourses : AppCompatActivity() {
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var userImage: CircleImageView
    private lateinit var userName: TextView
    private lateinit var userEmail:  TextView
    private lateinit var userBio: TextView
    private lateinit var userCourseList: RecyclerView
    private lateinit var adapter: UserProfileCoursesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile_courses)

        // initialize Firebase Database
        firebaseDatabase = Firebase.database(FirebaseUtil.firebaseDatabaseURL)

        userImage = findViewById(R.id.user_profile_picture)
        userName = findViewById(R.id.user_display_name)
        userEmail = findViewById(R.id.user_email_text)
        userBio = findViewById(R.id.bio_text)
        userCourseList = findViewById(R.id.course_list)

        // set bio text scroll
        userBio.movementMethod = ScrollingMovementMethod()

        // set recycler view layout manager
        userCourseList.layoutManager = WrapContentLinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)

        // get course id to get user id for this course id
        val courseID = intent.extras!!.getString("courseID")
        var userID = ""

        // get user id
        firebaseDatabase.reference
            .child("courses").child(courseID!!).child("uid")
            .get()
            .addOnSuccessListener {
                userID = it.value.toString()

                getUserDataFromUid(userID)
            }
    }

    private fun getUserDataFromUid(userID: String) {
        val userDatabaseRef: DatabaseReference = firebaseDatabase.reference.child("users").child(userID)

        // get user name
        userDatabaseRef.child("name")
            .get()
            .addOnSuccessListener {
                userName.text = it.value.toString()
            }

        // get user email
        userDatabaseRef.child("email")
            .get()
            .addOnSuccessListener {
                userEmail.text = it.value.toString()
            }

        // get user bio
        userDatabaseRef.child("bio")
            .get()
            .addOnSuccessListener {
                userBio.text = it.value.toString()
            }

        // get user profile picture
        userDatabaseRef.child("profile_picture")
            .get()
            .addOnSuccessListener {
                Picasso.get()
                    .load(it.value.toString())
                    .into(userImage)
            }

        // get user courses
        var courseList: List<String>
        userDatabaseRef.child("courses")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val t = object : GenericTypeIndicator<List<String>>() {}
                    if (snapshot.exists()) {
                        // get courses in course directory
                        courseList = snapshot.getValue(t)!!
                        // create adapter
                        adapter = UserProfileCoursesAdapter(courseList, applicationContext)
                        userCourseList.adapter = adapter
                    }
                }

                override fun onCancelled(error: DatabaseError) {}

            })
    }
}