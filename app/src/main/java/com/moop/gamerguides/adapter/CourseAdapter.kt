package com.moop.gamerguides.adapter

import android.app.AlertDialog
import android.content.DialogInterface
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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.moop.gamerguides.EditCourse
import com.moop.gamerguides.R
import com.moop.gamerguides.adapter.model.Courses
import com.moop.gamerguides.helper.FirebaseUtil
import com.moop.gamerguides.helper.ListToMutableList
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
        // initialize Firebase Storage
        val firebaseStorage: FirebaseStorage = Firebase.storage(FirebaseUtil.firebaseStorageURL)

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
                // get course id for intent
                intent.putExtra("courseID", model.id)
                // go to edit course activity with the course id
                holder.courseEditButton.context.startActivity(intent)
            }

            // add video button onclick function
            holder.courseAddVideoButton.setOnClickListener {
                Toast.makeText(holder.courseEditButton.context, "Add Video", Toast.LENGTH_SHORT)
                    .show()
            }

            // delete course button onclick function
            holder.courseDeleteButton.setOnClickListener {
                // make alert dialog box (Yes/No) listener
                val dialogClickListener =
                    DialogInterface.OnClickListener { _, which ->
                        when (which) {
                            DialogInterface.BUTTON_POSITIVE -> {
                                // delete course from user database
                                var userCourseList: List<String> = emptyList()
                                var removedCourseList: List<String?>
                                firebaseDatabase.reference
                                    .child("users")
                                    .child(model.uid!!)
                                    .child("courses")
                                    .addListenerForSingleValueEvent(object : ValueEventListener{
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val t = object : GenericTypeIndicator<List<String>>() {}
                                            if (snapshot.exists()) {
                                                // get courses in course directory
                                                userCourseList = snapshot.getValue(t)!!
                                            }
                                            // add a new course id into course list
                                            removedCourseList = ListToMutableList.removeElement(userCourseList, model.id!!)

                                            // input new course into the course directory in the user database
                                            firebaseDatabase.reference
                                                .child("users")
                                                .child(firebaseAuth.currentUser!!.uid)
                                                .child("courses")
                                                .setValue(removedCourseList)
                                        }

                                        override fun onCancelled(error: DatabaseError) {}

                                    })

                                // delete course from firebase storage
                                firebaseStorage.reference
                                    .child("courses")
                                    .child(model.id!!)
                                    .child("image")
                                    .child("course_image")
                                    .delete()

                                // delete course from course database
                                firebaseDatabase.reference
                                    .child("courses")
                                    .child(model.id!!)
                                    .removeValue()
                            }
                            DialogInterface.BUTTON_NEGATIVE -> {}
                        }
                    }

                // make alert dialog box (Yes/No)
                val builder: AlertDialog.Builder = AlertDialog.Builder(holder.courseDeleteButton.context)
                builder.setMessage("Apakah anda yakin ingin menghapus kursus ini?").setPositiveButton("Ya", dialogClickListener)
                    .setNegativeButton("Tidak", dialogClickListener).show()
            }
        }
    }

}