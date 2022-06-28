package com.moop.gamerguides.mainfragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.moop.gamerguides.R
import com.moop.gamerguides.adapter.model.Course

class AddNewCourse : Fragment() {

    val courseTitleInput: EditText
    get() = requireView().findViewById(R.id.course_title_input);

    val courseDescriptionInput: EditText
    get() = requireView().findViewById(R.id.course_description_input);

    val category: Spinner
    get() = requireView().findViewById(R.id.course_game_category_input);

    val addCourseButton: Button
    get() = requireView().findViewById(R.id.add_course_button);

    val database = FirebaseDatabase.getInstance();
    val myRef = database.getReference("courses");

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val categorySpinner = arrayOf("Action", "Adventure", "Casual", "Indie", "Massively Multiplayer", "Racing", "RPG", "Simulation", "Sports", "Strategy")
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categorySpinner)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        category.adapter = categoryAdapter

        return inflater.inflate(R.layout.fragment_add_new_course, container, false)



    }

}