package com.moop.gamerguides.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
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
import com.moop.gamerguides.*
import com.moop.gamerguides.R
import com.moop.gamerguides.adapter.model.Courses
import com.moop.gamerguides.helper.FirebaseUtil
import com.moop.gamerguides.helper.GameTitleUtil
import com.moop.gamerguides.helper.ListToMutableList
import com.squareup.picasso.Picasso

class MyCourseAdapter: RecyclerView.Adapter<MyCourseAdapter.CourseViewHolder> {

    var list: List<String> = emptyList()
    var context: Context?

    constructor(list: List<String>, context: Context?) {
        this.list = list
        this.context = context
    }

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(courseID: String?) {
            // initialize Firebase auth
            val firebaseAuth: FirebaseAuth = Firebase.auth
            // initialize Firebase Database
            val firebaseDatabase: FirebaseDatabase = Firebase.database(FirebaseUtil.firebaseDatabaseURL)
            // initialize Firebase storage
            val firebaseStorage: FirebaseStorage = Firebase.storage(FirebaseUtil.firebaseStorageURL)

            // set course title
            firebaseDatabase.reference
                .child("courses")
                .child(courseID!!)
                .child("title")
                .get()
                .addOnSuccessListener {
                    courseTitle.text = it.value.toString()
                }

            // set course image
            firebaseDatabase.reference
                .child("courses")
                .child(courseID)
                .child("image")
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

            // set video count
            firebaseDatabase.reference
                .child("courses")
                .child(courseID)
                .child("videos")
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

            // set edit course button onclick function
            courseEditButton.setOnClickListener {
                val intent = Intent(courseEditButton.context, EditCourse::class.java)
                // get course id for intent
                intent.putExtra("courseID", courseID)
                // go to edit course activity with the course id
                courseEditButton.context.startActivity(intent)
            }

            // set add video button onclick function
            courseAddVideoButton.setOnClickListener {
                val intent = Intent(courseAddVideoButton.context, AddNewVideo::class.java)
                // get course id for intent
                intent.putExtra("courseID", courseID)
                // go to add new video activity with the course id
                courseAddVideoButton.context.startActivity(intent)
            }

            // set course delete button onclick function
            courseDeleteButton.setOnClickListener {
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
                                    .child(firebaseAuth.currentUser!!.uid)
                                    .child("courses")
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val t = object : GenericTypeIndicator<List<String>>() {}
                                            if (snapshot.exists()) {
                                                // get courses in course directory
                                                userCourseList = snapshot.getValue(t)!!
                                            }
                                            // add a new course id into course list
                                            removedCourseList =
                                                ListToMutableList.removeElement(userCourseList, courseID)

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
                                    .child(courseID)
                                    .child("image")
                                    .child("course_image")
                                    .delete()
                                    .addOnFailureListener {}

                                // get game category from course
                                var gameTitle = ""
                                var selectedGameCategory = ""
                                firebaseDatabase.reference
                                    .child("courses")
                                    .child(courseID)
                                    .child("gameCategory")
                                    .get()
                                    .addOnSuccessListener {
                                        selectedGameCategory = it.value.toString()
                                        gameTitle = GameTitleUtil.change(selectedGameCategory)

                                        // delete course from game category
                                        firebaseDatabase.reference
                                            .child("games")
                                            .child(gameTitle)
                                            .child("courses")
                                            .child(courseID)
                                            .removeValue()
                                    }

                                // delete course from course database
                                firebaseDatabase.reference
                                    .child("courses")
                                    .child(courseID)
                                    .removeValue()
                            }
                            DialogInterface.BUTTON_NEGATIVE -> {}
                        }
                    }

                // make alert dialog box (Yes/No)
                val builder: AlertDialog.Builder = AlertDialog.Builder(courseDeleteButton.context)
                builder.setMessage("Apakah anda yakin ingin menghapus kursus ini?")
                    .setPositiveButton("Ya", dialogClickListener)
                    .setNegativeButton("Tidak", dialogClickListener).show()
            }

            // set course card onclick function
            courseLayout.setOnClickListener {
                val intent = Intent(courseEditButton.context, MyCourseDetails::class.java)
                // get course id for intent
                intent.putExtra("courseID", courseID)
                // go to my course details activity with the course id
                courseEditButton.context.startActivity(intent)
            }

        }

        private val courseImage: ShapeableImageView = itemView.findViewById(R.id.course_image)
        private val courseTitle: TextView = itemView.findViewById(R.id.course_title)
        private val courseVideoCount: TextView = itemView.findViewById(R.id.course_count)
        private val courseEditButton: Button = itemView.findViewById(R.id.edit_course_button)
        private val courseAddVideoButton: Button = itemView.findViewById(R.id.add_video_button)
        private val courseDeleteButton: ImageView = itemView.findViewById(R.id.delete_course_button)
        private val courseLayout: CardView = itemView.findViewById(R.id.item_course_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_my_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }
}