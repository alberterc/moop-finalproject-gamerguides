package com.moop.gamerguides.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.moop.gamerguides.EditCourse
import com.moop.gamerguides.R
import com.moop.gamerguides.adapter.model.Courses
import com.moop.gamerguides.helper.FirebaseUtil
import com.squareup.picasso.Picasso

class CourseAdapter(options: FirebaseRecyclerOptions<Courses>) : FirebaseRecyclerAdapter<Courses, CourseAdapter.CourseViewHolder>(
    options) {

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseImage: ShapeableImageView = itemView.findViewById(R.id.course_image)
        val courseTitle: TextView = itemView.findViewById(R.id.course_title)
        val courseVideoCount: TextView = itemView.findViewById(R.id.course_count)
        val courseEditButton: Button = itemView.findViewById(R.id.edit_course_button)
        val courseAddVideoButton: Button = itemView.findViewById(R.id.add_video_button)
        val courseDeleteButton: ImageView = itemView.findViewById(R.id.delete_course_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_user_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int, model: Courses) {
        // initialize Firebase auth
        val firebaseAuth: FirebaseAuth = Firebase.auth
        // initialize Firebase Database
        val firebaseDatabase: FirebaseDatabase = Firebase.database(FirebaseUtil.firebaseDatabaseURL)

        // check if course in database is made by user
        if (model.uid == firebaseAuth.currentUser!!.uid) {
            // get course image from firebase database
            if (model.image != "") {
                Picasso.get()
                    .load(model.image)
                    .into(holder.courseImage)
            }
            else {
                holder.courseImage.setImageResource(R.drawable.gamerguides_logo)
            }

            // get course title from firebase database
            holder.courseTitle.text = model.title

            // get course video count from firebase database
            firebaseDatabase.reference
                .child("courses")
                .child("videos")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            holder.courseVideoCount.text = snapshot.childrenCount.toString()
                        }
                        else {
                            holder.courseVideoCount.text = "0"
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })

            // edit course button onclick function
            holder.courseEditButton.setOnClickListener {
                val intent = Intent(holder.courseEditButton.context, EditCourse::class.java)
                firebaseDatabase.reference
                    .child("courses")
                    .child(model.id!!)
                    .get()
                    .addOnSuccessListener {
                        // get course id for intent
                        intent.putExtra("courseID", model.id)
                        // go to edit course activity with the course id
                        holder.courseEditButton.context.startActivity(intent)
                    }
            }

            // add video button onclick function
            holder.courseAddVideoButton.setOnClickListener {
                Toast.makeText(holder.courseEditButton.context, "Add Video", Toast.LENGTH_SHORT)
                    .show()
            }

            // delete course button onclick function
            holder.courseDeleteButton.setOnClickListener {
                Toast.makeText(holder.courseEditButton.context, "Delete Course", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

}