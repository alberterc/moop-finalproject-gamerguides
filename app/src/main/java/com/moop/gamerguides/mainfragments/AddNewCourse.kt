@file:Suppress("DEPRECATION")

package com.moop.gamerguides.mainfragments

import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.firebase.ui.database.FirebaseListAdapter
import com.firebase.ui.database.FirebaseListOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.moop.gamerguides.R
import com.moop.gamerguides.adapter.model.Courses
import com.moop.gamerguides.adapter.model.Games
import com.moop.gamerguides.helper.FirebaseUtil
import com.moop.gamerguides.helper.GameTitleUtil
import com.moop.gamerguides.helper.ListToMutableList
import java.io.IOException


class AddNewCourse : Fragment() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firebaseListAdapter: FirebaseListAdapter<Games>
    private var courseImagePath: Uri? = null
    private lateinit var selectedGameCategory: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_new_course, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initialize Firebase auth
        firebaseAuth = Firebase.auth
        // initialize Firebase Database
        firebaseDatabase = Firebase.database(FirebaseUtil.firebaseDatabaseURL)
        // initialize Firebase Storage
        firebaseStorage = Firebase.storage(FirebaseUtil.firebaseStorageURL)

        // get course data from user input
        val courseTitle: EditText = view.findViewById(R.id.course_title_input)
        val courseThumbnail: RelativeLayout = view.findViewById(R.id.course_thumbnail_input)
        val courseDescription: EditText = view.findViewById(R.id.course_description_input)
        val gameCategoryDropdown: Spinner = view.findViewById(R.id.course_game_category_input)
        val addCourseButton: Button = view.findViewById(R.id.add_course_button)
        val courseThumbnailResult: ImageView = view.findViewById(R.id.course_thumbnail_result)

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

        // add course button onclick function
        // send data to firebase database
        addCourseButton.setOnClickListener {
            // upload all course data to firebase
            uploadDataToFirebase(courseImagePath, courseTitle.text.toString(), courseDescription.text.toString())
            courseTitle.setText("")
            courseDescription.setText("")
            courseImagePath = null
            courseThumbnailResult.setImageDrawable(null)
        }

    }

    private fun uploadDataToFirebase(imagePath: Uri?, courseTitleText: String, courseDescriptionText: String) {
        // generate a key to use as course ID
        val courseID = firebaseDatabase.reference.child("courses").push().key
        val gameTitle = GameTitleUtil.change(selectedGameCategory)

        if (imagePath != null) {
            // create progress dialog
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Uploading Picture")
            progressDialog.show()
            // upload course image to firebase storage
            firebaseStorage.reference
                .child("courses")
                .child(courseID!!)
                .child("image")
                .child("course_image")
                .putFile(imagePath)

                // upload progress number for ProgressDialog
                .addOnProgressListener {
                    val uploadProgress: Double = (100.0 * it.bytesTransferred / it.totalByteCount)
                    progressDialog.setMessage("Uploaded: ${uploadProgress.toInt()}%")
                }

                // if course image upload failed
                .addOnFailureListener {

                    // remove progress dialog
                    progressDialog.dismiss()
                    Toast.makeText(context, "Failed to add course", Toast.LENGTH_SHORT)
                        .show()

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
                            firebaseDatabase.reference
                                .child("games")
                                .child(gameTitle)
                                .child("courses")
                                .child(courseID)
                                .setValue(
                                    Courses(courseTitleText, it.toString(), courseDescriptionText, selectedGameCategory, firebaseAuth.currentUser!!.uid, courseID)
                                )
                        }
                }
        }
        else {
            // send course data to firebase database
            firebaseDatabase.reference
                .child("courses")
                .child(courseID!!)
                .setValue(
                    Courses(courseTitleText, "", courseDescriptionText, selectedGameCategory, firebaseAuth.currentUser!!.uid, courseID)
                )
            firebaseDatabase.reference
                .child("games")
                .child(gameTitle)
                .child("courses")
                .child(courseID)
                .setValue(
                    Courses(courseTitleText, "", courseDescriptionText, selectedGameCategory, firebaseAuth.currentUser!!.uid, courseID)
                )
        }

        // store user courses id in user database
        var userCourseList: List<String> = emptyList()
        var addedCourseList: List<String?>

        // get all courses from course directory
        firebaseDatabase.reference
            .child("users")
            .child(firebaseAuth.currentUser!!.uid)
            .child("courses")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val t = object : GenericTypeIndicator<List<String>>() {}
                    if (snapshot.exists()) {
                        // get courses in course directory
                        userCourseList = snapshot.getValue(t)!!
                    }
                    // add a new course id into course list
                    addedCourseList = ListToMutableList.addElement(userCourseList, courseID)

                    // input new course into the course directory in the user database
                    firebaseDatabase.reference
                        .child("users")
                        .child(firebaseAuth.currentUser!!.uid)
                        .child("courses")
                        .setValue(addedCourseList)
                }

                override fun onCancelled(error: DatabaseError) {}

            })
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
                val courseThumbnailResult: ImageView = view!!.findViewById(R.id.course_thumbnail_result)
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