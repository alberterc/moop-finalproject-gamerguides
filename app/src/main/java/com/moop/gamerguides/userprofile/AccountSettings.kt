@file:Suppress("DEPRECATION")

package com.moop.gamerguides.userprofile

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.moop.gamerguides.R
import com.moop.gamerguides.helper.FirebaseUtil
import com.moop.gamerguides.login.SignUp
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

class AccountSettings : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseStorage: FirebaseStorage

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        // initialize Firebase auth
        firebaseAuth = Firebase.auth
        val user = firebaseAuth.currentUser
        // initialize Firebase database
        firebaseDatabase = Firebase.database(FirebaseUtil.firebaseDatabaseURL)
        // initialize Firebase storage
        firebaseStorage = Firebase.storage(FirebaseUtil.firebaseStorageURL)

        if (user == null) {
            // go to SignUp activity
            startActivity(Intent(applicationContext, SignUp::class.java))
            finish()
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        // refresh fragment every 1 second
        val refreshHandler = Handler()
        val runnable: Runnable = object : Runnable {
            override fun run() {
                initUserInfoFirebase()
                refreshHandler.postDelayed(this, 1 * 1000)
            }
        }
        refreshHandler.postDelayed(runnable, 1 * 1000)

        // top back button
        val topBackButton: ImageView = findViewById(R.id.back_button)
        topBackButton.setOnClickListener {
            onBackPressed()
        }

        // get user profile's info from firebase and set it to view
        initUserInfoFirebase()

        // change password text onclick function
        val changePassword: TextView = findViewById(R.id.change_password_button)
        changePassword.setOnClickListener {
            firebaseAuth.sendPasswordResetEmail(user!!.email!!)
                .addOnCompleteListener {task ->
                    if (task.isSuccessful) {
                        Toast.makeText(applicationContext, "Password reset email sent", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }

        val usernameInput: EditText = findViewById(R.id.username_input)
        val bioInput: EditText = findViewById(R.id.bio_input)
        val changeProfilePicture: TextView = findViewById(R.id.edit_profile_picture_button)
        val saveInfoButton: TextView = findViewById(R.id.save_info_changes)

        // bio input has focus function
        var bioChangedString = bioInput.text.toString()
        bioInput.setOnFocusChangeListener { _, hasFocus ->
            // bio changed
            if (hasFocus) {
                val bioTitle: TextView = findViewById(R.id.bio_title)
                bioTitle.text = "Bio (updated)"
            }
            else {
                bioChangedString = bioInput.text.toString()
                Log.e("ACCOUNT", bioChangedString)
                Log.e("ACCOUNT", "INPUT: ${bioInput.text}")
            }
        }

        // change user username and bio
        // save info text onclick function
        saveInfoButton.setOnClickListener {
            // remove bioInput focus
            bioInput.clearFocus()

            if (usernameInput.text.matches(Regex("^[^\\s].+[^\\s]\$"))) {
                val profileUpdate = userProfileChangeRequest {
                    // set user account display name
                    displayName = usernameInput.text.toString()
                }
                // update user info
                user!!.updateProfile(profileUpdate)
            }

            // set default user bio
            firebaseDatabase.reference
                .child("users").child(user!!.uid).child("bio")
                .setValue(bioChangedString)

            Toast.makeText(applicationContext, "Info saved", Toast.LENGTH_SHORT)
                .show()
        }

        // change profile picture
        // edit profile picture text onclick function
        changeProfilePicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 100)
        }

    }

    private fun initUserInfoFirebase() {
        val user = firebaseAuth.currentUser
        val bio: EditText = findViewById(R.id.bio_input)
        val username: EditText = findViewById(R.id.username_input)
        val email: TextView = findViewById(R.id.email_input)
        val profilePicture: CircleImageView = findViewById(R.id.user_profile_picture)

        if (user != null) {
            // get data from firebase account info
            username.hint = user.displayName
            email.text = user.email

            firebaseDatabase.reference
                .child("users").child(user.uid).child("profile_picture")
                .get()
                .addOnSuccessListener {
                    Picasso.get()
                        .load(it.value.toString())
                        .into(profilePicture)
                }

            firebaseDatabase.reference
                .child("users")
                .child(user.uid)
                .child("bio")
                .get()
                .addOnSuccessListener {
                    bio.hint = it.value.toString()
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
                uploadImageToFirebase(fileImagePath)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImageToFirebase(fileImagePath: Uri) {
        val user = firebaseAuth.currentUser
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading Picture")
        progressDialog.show()

        firebaseStorage.reference
            .child("users")
            .child(user!!.uid)
            .child("profile_picture")
            .putFile(fileImagePath)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progressDialog.dismiss()
                    Toast.makeText(applicationContext, "Uploaded", Toast.LENGTH_SHORT).show()

                    // set user profile picture in Firebase realtime database
                    // set default user profile picture
                    // get default picture from Firebase cloud storage
                    firebaseStorage.reference
                        .child("users/${user.uid}/profile_picture")
                        .downloadUrl
                        .addOnSuccessListener {
                            // set user profile picture in Firebase realtime database
                            firebaseDatabase.reference
                                .child("users").child(user.uid).child("profile_picture")
                                .setValue(it.toString())
                        }
                }
                else {
                    progressDialog.dismiss()
                    Toast.makeText(applicationContext, "Upload failed", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnProgressListener {
                val uploadProgress: Double = (100.0 * it.bytesTransferred / it.totalByteCount)
                progressDialog.setMessage("Uploaded: ${uploadProgress.toInt()}%")
            }
    }
}