package com.moop.gamerguides.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.moop.gamerguides.CourseDetails
import com.moop.gamerguides.R
import com.moop.gamerguides.helper.FirebaseUtil
import com.squareup.picasso.Picasso

class UserProfileCoursesAdapter: RecyclerView.Adapter<UserProfileCoursesAdapter.ViewHolder> {

    var list: List<String> = emptyList()
    var context: Context?

    constructor(list: List<String>, context: Context?) {
        this.list = list
        this.context = context
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(courseID: String) {
            // initialize Firebase Database
            val firebaseDatabase: FirebaseDatabase = Firebase.database(FirebaseUtil.firebaseDatabaseURL)
            val courseDatabaseRef = firebaseDatabase.reference.child("courses").child(courseID)

            // set course title
            courseDatabaseRef.child("title")
                .get()
                .addOnSuccessListener {
                    courseTitle.text = it.value.toString()
                }

            // set course image
            courseDatabaseRef.child("image")
                .get()
                .addOnSuccessListener {
                    if (it.value.toString() == "") {
                        courseImage.setImageResource(R.drawable.gamerguides_logo)
                    }
                    else {
                        Picasso.get()
                            .load(it.value.toString())
                            .into(courseImage)
                    }
                }

            // set course video count
            courseDatabaseRef.child("videos")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            courseVideoCount.text = snapshot.childrenCount.toString()
                        }
                        else {
                            courseVideoCount.text = "0"
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}

                })

            // set course onclick function
            courseLayout.setOnClickListener {
                val intent = Intent(courseLayout.context, CourseDetails::class.java)
                // get course id for intent
                intent.putExtra("courseID", courseID)
                // go to course details activity with the course id
                courseLayout.context.startActivity(intent)
                ((courseLayout.context) as Activity).finish()
            }
        }

        private val courseImage: ShapeableImageView = itemView.findViewById(R.id.course_image)
        private val courseTitle: TextView = itemView.findViewById(R.id.course_title)
        private val courseVideoCount: TextView = itemView.findViewById(R.id.course_count)
        private val courseLayout: CardView = itemView.findViewById(R.id.item_course_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_course_user_profile, parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }
}