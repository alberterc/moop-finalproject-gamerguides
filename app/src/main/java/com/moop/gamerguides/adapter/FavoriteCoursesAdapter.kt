package com.moop.gamerguides.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.moop.gamerguides.CourseDetails
import com.moop.gamerguides.R
import com.moop.gamerguides.helper.FirebaseUtil
import com.moop.gamerguides.helper.ListToMutableList
import com.squareup.picasso.Picasso

class FavoriteCoursesAdapter: RecyclerView.Adapter<FavoriteCoursesAdapter.ViewHolder> {
    var list: List<String> = emptyList()
    var context: Context?

    constructor(list: List<String>, context: Context?) {
        this.list = list
        this.context = context
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(courseID: String?) {
            // initialize Firebase auth
            val firebaseAuth: FirebaseAuth = Firebase.auth
            // initialize Firebase Database
            val firebaseDatabase: FirebaseDatabase = Firebase.database(FirebaseUtil.firebaseDatabaseURL)

            Log.e("FAVORITE COURSE ADAPTER", courseID!!)

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
                                    .child("favorites")
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val t = object : GenericTypeIndicator<List<String>>() {}
                                            if (snapshot.exists()) {
                                                // get favorite courses in course directory
                                                userCourseList = snapshot.getValue(t)!!
                                            }
                                            // remove the course id from course list
                                            removedCourseList =
                                                ListToMutableList.removeElement(userCourseList, courseID)

                                            // input new favorite courses into the favorites directory in the user database
                                            firebaseDatabase.reference
                                                .child("users")
                                                .child(firebaseAuth.currentUser!!.uid)
                                                .child("favorites")
                                                .setValue(removedCourseList)
                                        }

                                        override fun onCancelled(error: DatabaseError) {}

                                    })
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
                val intent = Intent(it.context, CourseDetails::class.java)
                // get course id for intent
                intent.putExtra("courseID", courseID)
                // go to my course details activity with the course id
                it.context.startActivity(intent)
            }
        }

        private val courseImage: ShapeableImageView = itemView.findViewById(R.id.course_image)
        private val courseTitle: TextView = itemView.findViewById(R.id.course_title)
        private val courseVideoCount: TextView = itemView.findViewById(R.id.course_count)
        private val courseDeleteButton: ImageView = itemView.findViewById(R.id.delete_course_button)
        private val courseLayout: CardView = itemView.findViewById(R.id.item_course_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite_course, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }
}