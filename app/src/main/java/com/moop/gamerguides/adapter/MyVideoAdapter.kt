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
import com.moop.gamerguides.CourseDetails
import com.moop.gamerguides.EditVideo
import com.moop.gamerguides.R
import com.moop.gamerguides.adapter.model.Videos
import com.moop.gamerguides.helper.FirebaseUtil
import com.moop.gamerguides.helper.GameTitleUtil
import com.squareup.picasso.Picasso

class MyVideoAdapter :
    FirebaseRecyclerAdapter<Videos, MyVideoAdapter.ViewHolder> {

    private var courseID: String

    constructor(options: FirebaseRecyclerOptions<Videos>, courseID: String) : super(options) {
        this.courseID = courseID
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoTitle: TextView = itemView.findViewById(R.id.video_title)
        val videoImage: ShapeableImageView = itemView.findViewById(R.id.video_image)
        val editVideoButton: Button = itemView.findViewById(R.id.edit_video_button)
        val deleteVideoButton: ImageView = itemView.findViewById(R.id.delete_video_button)
        val videoLayout: CardView = itemView.findViewById(R.id.item_video_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_my_video, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Videos) {
        // initialize Firebase auth
        val firebaseAuth: FirebaseAuth = Firebase.auth
        // initialize Firebase Database
        val firebaseDatabase: FirebaseDatabase = Firebase.database(FirebaseUtil.firebaseDatabaseURL)
        // initialize Firebase storage
        val firebaseStorage: FirebaseStorage = Firebase.storage(FirebaseUtil.firebaseStorageURL)

        // set video title
        holder.videoTitle.text = model.title

        // set video image
        if (model.image == "") {
            holder.videoImage.setImageResource(R.drawable.gamerguides_logo)
        }
        else {
            Picasso.get()
                .load(model.image)
                .into(holder.videoImage)
        }

        // edit video button onclick function
        holder.editVideoButton.setOnClickListener {
            val intent = Intent(it.context, EditVideo::class.java)
            // get course id for intent
            intent.putExtra("courseID", courseID)
            // get video id for intent
            intent.putExtra("videoID", model.id)
            // go to course details activity with the course id
            it.context.startActivity(intent)
        }

        // delete video button onclick function
        holder.deleteVideoButton.setOnClickListener {
            // make alert dialog box (Yes/No) listener
            val dialogClickListener =
                DialogInterface.OnClickListener { _, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            // delete video image from firebase storage
                            firebaseStorage.reference
                                .child("courses")
                                .child(courseID)
                                .child("videos")
                                .child(model.id)
                                .child("image")
                                .child("video_image")
                                .delete()
                                .addOnFailureListener {}

                            // delete video from course database
                            firebaseDatabase.reference
                                .child("courses")
                                .child(courseID)
                                .child("videos")
                                .child(model.id)
                                .removeValue()
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {}
                    }
                }

            // make alert dialog box (Yes/No)
            val builder: AlertDialog.Builder = AlertDialog.Builder(it.context)
            builder.setMessage("Apakah anda yakin ingin menghapus video ini?")
                .setPositiveButton("Ya", dialogClickListener)
                .setNegativeButton("Tidak", dialogClickListener).show()
        }

        // video card layout onclick function
    }
}