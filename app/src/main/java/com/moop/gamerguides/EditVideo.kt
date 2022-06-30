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
import com.moop.gamerguides.adapter.model.Videos
import com.moop.gamerguides.helper.FirebaseUtil
import com.moop.gamerguides.helper.YouTubeUtil

class EditVideo : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseStorage: FirebaseStorage
    private var videoImagePath: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_video)

        // initialize Firebase auth
        firebaseAuth = Firebase.auth
        // initialize Firebase Database
        firebaseDatabase = Firebase.database(FirebaseUtil.firebaseDatabaseURL)
        // initialize Firebase Storage
        firebaseStorage = Firebase.storage(FirebaseUtil.firebaseStorageURL)

        // get video data from user input
        val videoTitleView: EditText = findViewById(R.id.video_title_input)
        val videoThumbnailView: RelativeLayout = findViewById(R.id.video_thumbnail_input)
        val videoDescriptionView: EditText = findViewById(R.id.video_description_input)
        val videoURLView: EditText = findViewById(R.id.video_url_input)
        val saveVideoButton: Button = findViewById(R.id.save_video_button)

        // video thumbnail input onclick function
        // open gallery for user to upload video thumbnail
        videoThumbnailView.setOnClickListener {
            // choose course thumbnail
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            // requestCode == 100 for profile picture image gallery upload
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 100)
        }

        // save video button onclick function
        // send data to firebase database
        saveVideoButton.setOnClickListener {
            // upload all course data to firebase
            uploadDataToFirebase(videoImagePath, videoTitleView.text.toString(), videoDescriptionView.text.toString(), YouTubeUtil.getVideoId(videoURLView.text.toString()))
        }
    }

    private fun uploadDataToFirebase(videoImagePath: Uri?, titleText: String, descriptionText: String, urlText: String) {
        // get course ID
        val courseID = intent.extras!!.getString("courseID")!!
        // get video ID
        val videoID = intent.extras!!.getString("videoID")!!
        // database reference for course
        val videoRef = firebaseDatabase.reference.child("courses").child(courseID).child("videos").child(videoID)

        var videoTitle = titleText
        var videoDescription = descriptionText
        var videoURL = urlText

        // get previous video title
        videoRef.child("title")
            .get()
            .addOnSuccessListener {
                if (videoTitle == "") {
                    videoTitle = it.value.toString()
                }
            }

        // get previous video description
        videoRef.child("description")
            .get()
            .addOnSuccessListener {
                if (videoDescription == "") {
                    videoDescription = it.value.toString()
                }
            }

        // get previous video url
        videoRef.child("video")
            .get()
            .addOnSuccessListener {
                if (videoURL == "") {
                    videoURL = it.value.toString()
                }
            }

        // get previous video image
        if (videoImagePath == null) {
            var imagePath = ""
            videoRef.child("image")
                .get()
                .addOnSuccessListener {
                    if (it.value.toString() != "") {
                        imagePath = it.value.toString()
                        // send video data to firebase database
                        videoRef
                            .setValue(
                                Videos(videoTitle, imagePath, videoDescription, videoURL, firebaseAuth.currentUser!!.uid, videoID)
                            )
                    }
                    else {
                        videoRef
                            .setValue(
                                Videos(videoTitle, imagePath, videoDescription, videoURL, firebaseAuth.currentUser!!.uid, videoID)
                            )
                    }
                    onBackPressed()
                }
        }
        else {
            // create progress dialog
            val progressDialog = ProgressDialog(this@EditVideo)
            progressDialog.setTitle("Uploading Picture")
            progressDialog.show()

            val videoImageStorageRef = firebaseStorage.reference.child("courses").child(courseID)
                .child("videos").child(videoID).child("image").child("video_image")

            videoImageStorageRef
                // upload video image to firebase storage
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
                    Toast.makeText(applicationContext, "Failed to add course", Toast.LENGTH_SHORT)
                        .show()
                    onBackPressed()
                }

                // if video image upload succeed
                .addOnCompleteListener {
                    // remove progress dialog
                    progressDialog.dismiss()

                    // get video image path with string data type from firebase storage
                    videoImageStorageRef
                        .downloadUrl
                        .addOnSuccessListener {
                            videoRef
                                .setValue(
                                    Videos(videoTitle, it.toString(), videoDescription, videoURL, firebaseAuth.currentUser!!.uid, videoID)
                                )
                        }

                    onBackPressed()
                }
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
                val videoThumbnailResult: ImageView = findViewById(R.id.video_thumbnail_result)
                videoThumbnailResult.setImageURI(videoImagePath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}