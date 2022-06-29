package com.moop.gamerguides.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.imageview.ShapeableImageView
import com.moop.gamerguides.CourseDetails
import com.moop.gamerguides.R
import com.moop.gamerguides.adapter.model.Courses
import com.squareup.picasso.Picasso

class AllCourseListAdapter: FirebaseRecyclerAdapter<Courses, AllCourseListAdapter.ViewHolder> {

    var gameTitle: String?

    constructor(options: FirebaseRecyclerOptions<Courses>, gameTitle: String) : super(options) {
        this.gameTitle = gameTitle
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseImage: ShapeableImageView = itemView.findViewById(R.id.course_image)
        val courseTitle: TextView = itemView.findViewById(R.id.course_title)
        val courseLayout: RelativeLayout = itemView.findViewById(R.id.course_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_course_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Courses) {
        // set course title
        holder.courseTitle.text = model.title

        // set course image
        if (model.image == "") {
            holder.courseImage.setImageResource(R.drawable.gamerguides_logo)
        }
        else {
            Picasso.get()
                .load(model.image)
                .into(holder.courseImage)
        }

        // set course layout button onclick function
        holder.courseLayout.setOnClickListener {
            val intent = Intent(it.context, CourseDetails::class.java)
            // get course id for intent
            intent.putExtra("courseID", model.id)
            // go to course details activity with the course id
            it.context.startActivity(intent)
        }
    }

}