@file:Suppress("DEPRECATION")

package com.moop.gamerguides

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.firebase.ui.database.FirebaseListAdapter
import com.firebase.ui.database.FirebaseListOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.moop.gamerguides.adapter.model.Courses
import com.moop.gamerguides.adapter.model.Games
import com.moop.gamerguides.helper.FirebaseUtil
import java.io.IOException

class EditCourse : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firebaseListAdapter: FirebaseListAdapter<Games>
    private var courseImagePath: Uri? = null
    private lateinit var selectedGameCategory: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_course)

        // initialize Firebase auth
        firebaseAuth = Firebase.auth
        // initialize Firebase Database
        firebaseDatabase = Firebase.database(FirebaseUtil.firebaseDatabaseURL)
        // initialize Firebase Storage
        firebaseStorage = Firebase.storage(FirebaseUtil.firebaseStorageURL)

        // get course data from user input
        val courseTitle: EditText = findViewById(R.id.course_title_input)
        val courseThumbnail: RelativeLayout = findViewById(R.id.course_thumbnail_input)
        val courseDescription: EditText = findViewById(R.id.course_description_input)
        val gameCategoryDropdown: Spinner = findViewById(R.id.course_game_category_input)
        val saveCourseButton: Button = findViewById(R.id.save_course_button)

        // set spinner list with firebase database data
        // set options for adapter
        val options = FirebaseListOptions.Builder<Games>()
            .setQuery(firebaseDatabase.reference.child("games"), Games::class.java)
            .setLayout(android.R.layout.simple_spinner_dropdown_item)
            .build()
        // set adapter from firebase data
        firebaseListAdapter = object : FirebaseListAdapter<Games>(options) {
            override fun populateView(v: View, model: Games, position: Int) {
                val gameTitle: TextView = v.findViewById(android.R.id.text1)
                gameTitle.text = model.title
            }
        }
        gameCategoryDropdown.adapter = firebaseListAdapter

        // course thumbnail input onclick function
        // open gallery for user to upload course thumbnail
        courseThumbnail.setOnClickListener {
            // choose course thumbnail
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            // requestCode == 100 for profile picture image gallery upload
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 100)
        }

        // get game category from spinner
        gameCategoryDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                val courseTitleView: TextView = adapterView.findViewById(android.R.id.text1)
                selectedGameCategory = courseTitleView.text.toString()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }

        // save course button onclick function
        // send data to firebase database
        saveCourseButton.setOnClickListener {
            // upload all course data to firebase
            uploadDataToFirebase(courseImagePath, courseTitle.text.toString(), courseDescription.text.toString())
        }
    }

    private fun uploadDataToFirebase(courseImagePath: Uri?, titleText: String, descriptionText: String) {
        // get course ID
        val courseID = intent.extras!!.getString("courseID")

        var courseTitleText = titleText
        var courseDescriptionText = descriptionText
        var imagePath = courseImagePath.toString()

        // get previous course title
        firebaseDatabase.reference
            .child("courses")
            .child(courseID!!)
            .child("title")
            .get()
            .addOnSuccessListener {
                if (courseTitleText == "") {
                    courseTitleText = it.value.toString()
                }
            }

        // get previous course description
        firebaseDatabase.reference
            .child("courses")
            .child(courseID)
            .child("description")
            .get()
            .addOnSuccessListener {
                if (courseDescriptionText == "") {
                    courseDescriptionText = it.value.toString()
                }
            }

        // get previous course image
        if (courseImagePath.toString() == "null") {
            imagePath = ""
        }
        firebaseDatabase.reference
            .child("courses")
            .child(courseID)
            .child("image")
            .get()
            .addOnSuccessListener {
                if (it.value.toString() != "") {
                    imagePath = it.value.toString()
                }
            }


        if (courseImagePath != null) {
            // create progress dialog
            val progressDialog = ProgressDialog(this@EditCourse)
            progressDialog.setTitle("Uploading Picture")
            progressDialog.show()
            // upload course image to firebase storage
            firebaseStorage.reference
                .child("courses")
                .child(courseID)
                .child("image")
                .child("course_image")
                .putFile(courseImagePath)

                // upload progress number for ProgressDialog
                .addOnProgressListener {
                    val uploadProgress: Double = (100.0 * it.bytesTransferred / it.totalByteCount)
                    progressDialog.setMessage("Uploaded: ${uploadProgress.toInt()}%")
                }

                // if course image upload failed
                .addOnFailureListener {

                    // remove progress dialog
                    progressDialog.dismiss()
                    Toast.makeText(applicationContext, "Failed to add course", Toast.LENGTH_SHORT)
                        .show()
                    onBackPressed()

                }
                // if course image upload succeed
                .addOnCompleteListener {

                    // remove progress dialog
                    progressDialog.dismiss()

                    // get course image path with string data type from firebase storage
                    firebaseStorage.reference
                        .child("courses")
                        .child(courseID)
                        .child("image")
                        .child("course_image")
                        .downloadUrl
                        .addOnSuccessListener {

                            // send course data to firebase database
                            firebaseDatabase.reference
                                .child("courses")
                                .child(courseID)
                                .setValue(
                                    Courses(courseTitleText, it.toString(), courseDescriptionText, selectedGameCategory, firebaseAuth.currentUser!!.uid, courseID)
                                )
                        }
                    onBackPressed()
                }
        }
        else {
            // send course data to firebase database
            firebaseDatabase.reference
                .child("courses")
                .child(courseID)
                .setValue(
                    Courses(courseTitleText, imagePath, courseDescriptionText, selectedGameCategory, firebaseAuth.currentUser!!.uid, courseID)
                )
            onBackPressed()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // requestCode == 100 for profile picture image gallery upload
        if (resultCode == RESULT_OK && requestCode == 100 && data != null && data.data != null) {
            val fileImagePath = data.data!!
            try {
                courseImagePath = fileImagePath
                // show chosen course thumbnail
                val courseThumbnailResult: ImageView = findViewById(R.id.course_thumbnail_result)
                courseThumbnailResult.setImageURI(courseImagePath)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        firebaseListAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        firebaseListAdapter.stopListening()
    }
}