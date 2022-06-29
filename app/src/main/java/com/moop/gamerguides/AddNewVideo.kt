@file:Suppress("DEPRECATION")

package com.moop.gamerguides

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.moop.gamerguides.adapter.model.Courses
import com.moop.gamerguides.adapter.model.Video
import com.moop.gamerguides.helper.FirebaseUtil
import java.io.IOException

class AddNewVideo : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseStorage: FirebaseStorage
    private var videoImagePath: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_video)

        // initialize Firebase auth
        firebaseAuth = Firebase.auth
        // initialize Firebase Database
        firebaseDatabase = Firebase.database(FirebaseUtil.firebaseDatabaseURL)
        // initialize Firebase Storage
        firebaseStorage = Firebase.storage(FirebaseUtil.firebaseStorageURL)

        val videoTitleInput: EditText = findViewById(R.id.video_title_input)
        val videoImage: RelativeLayout = findViewById(R.id.video_thumbnail_input)
        val videoDescriptionInput: EditText = findViewById(R.id.video_description_input)
        val videoURLInput: EditText = findViewById(R.id.video_url_input)
        val addNewVideoButton: Button = findViewById(R.id.add_video_button)
        val videoImageResult: ImageView = findViewById(R.id.video_thumbnail_result)

        // video thumbnail input onclick function
        // open gallery for user to upload video thumbnail
        videoImage.setOnClickListener {
            // choose course thumbnail
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            // requestCode == 100 for profile picture image gallery upload
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 100)
        }

        // add video button onclick function
        // send data to firebase database
        addNewVideoButton.setOnClickListener {
            if (videoURLInput.text.toString() != "") {
                // upload all course data to firebase
                uploadDataToFirebase(
                    videoImagePath,
                    videoTitleInput.text.toString(),
                    videoDescriptionInput.text.toString(),
                    videoURLInput.text.toString()
                )
                videoTitleInput.setText("")
                videoDescriptionInput.setText("")
                videoImageResult.setImageDrawable(null)
                videoURLInput.error = null
                videoURLInput.setText("")
            }
            else {
                videoURLInput.error = "Link video tidak bisa kosong."
            }
        }
    }

    private fun uploadDataToFirebase(
        videoImagePath: Uri?,
        videoTitleText: String,
        videoDescriptionText: String,
        videoURLText: String
    ) {
        // get course ID
        val courseID = intent.extras!!.getString("courseID")
        // get video ID
        val videoID = firebaseDatabase.reference.child("courses").child(courseID!!)
            .child("videos").push().key

        if (videoImagePath != null) {
            // create progress dialog
            val progressDialog = ProgressDialog(this@AddNewVideo)
            progressDialog.setTitle("Uploading Picture")
            progressDialog.show()
            // upload video image to firebase storage
            firebaseStorage.reference
                .child("courses")
                .child(courseID)
                .child("videos")
                .child(videoID!!)
                .child("image")
                .child("video_image")
                .putFile(videoImagePath)

                // upload progress number for ProgressDialog
                .addOnProgressListener {
                    val uploadProgress: Double = (100.0 * it.bytesTransferred / it.totalByteCount)
                    progressDialog.setMessage("Uploaded: ${uploadProgress.toInt()}%")
                }

                // if video image upload failed
                .addOnFailureListener {

                    // remove progress dialog
                    progressDialog.dismiss()
                    Toast.makeText(applicationContext, "Failed to add video", Toast.LENGTH_SHORT)
                        .show()

                }
                // if video image upload succeed
                .addOnCompleteListener {

                    // remove progress dialog
                    progressDialog.dismiss()

                    // get course image path with string data type from firebase storage
                    firebaseStorage.reference
                        .child("courses")
                        .child(courseID)
                        .child("videos")
                        .child(videoID)
                        .child("image")
                        .child("video_image")
                        .downloadUrl
                        .addOnSuccessListener {

                            // send video data to firebase database
                            firebaseDatabase.reference
                                .child("courses")
                                .child(courseID)
                                .child("videos")
                                .child(videoID)
                                .setValue(
                                    Video(videoTitleText, it.toString(), videoDescriptionText, videoURLText, firebaseAuth.currentUser!!.uid, courseID)
                                )
                        }
                }
        }
        else {
            // send video data to firebase database
            firebaseDatabase.reference
                .child("courses")
                .child(courseID)
                .child("videos")
                .child(videoID!!)
                .setValue(
                    Video(videoTitleText, "", videoDescriptionText, videoURLText, firebaseAuth.currentUser!!.uid, courseID)
                )
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // requestCode == 100 for profile picture image gallery upload
        if (resultCode == RESULT_OK && requestCode == 100 && data != null && data.data != null) {
            val fileImagePath = data.data!!
            try {
                videoImagePath = fileImagePath
                // show chosen course thumbnail
                val courseThumbnailResult: ImageView = findViewById(R.id.course_thumbnail_result)
                courseThumbnailResult.setImageURI(videoImagePath)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}