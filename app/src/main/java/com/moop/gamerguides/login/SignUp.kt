package com.moop.gamerguides.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.moop.gamerguides.MainMenu
import com.moop.gamerguides.R
import com.moop.gamerguides.helper.FirebaseUtil


class SignUp : AppCompatActivity() {
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

        // if user is already signed in
        // go to MainMenu activity
        if (user != null) {
            startActivity(Intent(this, MainMenu::class.java))
            finish()
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Login text onclick function
        // already have an account
        val loginText: TextView = findViewById(R.id.login_text_button)
        loginText.setOnClickListener {
            // go to SignIn activity
            startActivity(Intent(this, SignIn::class.java))
            finish()
        }

        val usernameInput: EditText = findViewById(R.id.username_input)
        val emailInput: EditText = findViewById(R.id.email_input)
        val passwordInput: EditText = findViewById(R.id.password_input)
        val signUpButton: Button = findViewById(R.id.signup_button)
        val passwordToggle: ImageView = findViewById(R.id.password_toggle)
        val passwordToggleCondition: TextView = findViewById(R.id.password_toggle_condition)

        // eye password toggle onclick function
        passwordToggle.setOnClickListener {
            if (passwordToggleCondition.text == "hide") {
                passwordInput.transformationMethod = HideReturnsTransformationMethod.getInstance()
                passwordToggle.setImageResource(R.drawable.eye_closed)
                passwordToggleCondition.text = "show"
            }
            else {
                passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
                passwordToggle.setImageResource(R.drawable.eye_opened)
                passwordToggleCondition.text = "hide"
            }
        }

        // check for invalid password
        passwordInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                verifyPassword()
            }
        }

        // user account sign up to Firebase
        signUpButton.setOnClickListener {
            val username = usernameInput.text.toString()
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            if (username != "" && email != "" && verifyPassword()) {
                createAccountFirebase(username, email, password)
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun createAccountFirebase(username: String, email: String, password: String) {
        val emailInput: EditText = findViewById(R.id.email_input)
        val passwordInput: EditText = findViewById(R.id.password_input)
        val errorText: TextView = findViewById(R.id.error_text)

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                // account creation and sign in success
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    // store user info
                    val profileUpdate = userProfileChangeRequest {
                        // set user account display name
                        displayName = username
                    }
                    // update user info
                    user!!.updateProfile(profileUpdate)
                    writeDatabaseNewUser(user.uid, user.email, username)

                    // go to MainMenu activity
                    startActivity(Intent(applicationContext, MainMenu::class.java))
                    finish()
                }
                // account creation and sign in failed
                else {
                    try {
                        when ((task.exception as FirebaseAuthException?)!!.errorCode) {
                            "ERROR_INVALID_EMAIL" -> {
                                emailInput.error = "The email address is badly formatted."
                                emailInput.requestFocus()
                            }
                            "ERROR_EMAIL_ALREADY_IN_USE" -> {
                                emailInput.error = "The email address is already in use by another account."
                                emailInput.requestFocus()
                            }
                            "ERROR_WEAK_PASSWORD" -> {
                                errorText.text = "Needs to have at least 6 characters."
                                passwordInput.requestFocus()
                            }
                            "ERROR_WRONG_PASSWORD" -> {
                                errorText.text = "The password is incorrect."
                                passwordInput.requestFocus()
                                passwordInput.setText("")
                            }
                            "ERROR_USER_DISABLED" -> Toast.makeText(
                                applicationContext,
                                "The user account has been disabled by an administrator.",
                                Toast.LENGTH_LONG
                            ).show()
                            "ERROR_USER_NOT_FOUND" -> Toast.makeText(
                                applicationContext,
                                "Account not found.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            this,
                            "A network error has occurred",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    private fun writeDatabaseNewUser(uid: String?, email: String?, displayName: String?) {
        // default user info
        val userBio = "Hello!"

        if (uid != null) {
            // set default user bio
            firebaseDatabase.reference
                .child("users").child(uid).child("bio")
                .setValue(userBio)

            // store user email in firebase database
            firebaseDatabase.reference
                .child("users").child(uid).child("email")
                .setValue(email)

            // store user name in firebase database
            firebaseDatabase.reference
                .child("users").child(uid).child("name")
                .setValue(displayName)

            // set default user profile picture
            // get default picture from Firebase cloud storage
            firebaseStorage.reference
                .child("default_assets/profile_picture/user_icon.png")
                .downloadUrl
                .addOnSuccessListener {
                    // set user profile picture in Firebase realtime database
                    firebaseDatabase.reference
                        .child("users").child(uid).child("profile_picture")
                        .setValue(it.toString())
                }
        }
    }

    private fun verifyPassword(): Boolean {
        val passwordInput: EditText = findViewById(R.id.password_input)
        val passwordStr: String = passwordInput.text.toString()
        val errorText: TextView = findViewById(R.id.error_text)

        // check for password length
        // needs to have at least 6 characters
        if (passwordStr.length < 6) {
            errorText.text = "Needs to have at least 6 characters."
            passwordInput.setBackgroundResource(R.drawable.user_text_input_failed_verify_background)
        }
        else {
            passwordInput.setBackgroundResource(R.drawable.user_text_input_background)
        }

        // check for password number
        // needs to have at least 1 number
        if (!passwordStr.contains(".*\\d.*".toRegex()) && passwordStr.length > 5 && passwordStr.contains(".*[A-Za-z].*".toRegex())) {
            errorText.text = "Needs to have at least a number."
            passwordInput.setBackgroundResource(R.drawable.user_text_input_failed_verify_background)
        }
        else if (!passwordStr.contains(".*\\d.*".toRegex()) && passwordStr.length < 6  && passwordStr.contains(".*[A-Za-z].*".toRegex())) {
            passwordInput.setBackgroundResource(R.drawable.user_text_input_failed_verify_background)
        }
        else {
            passwordInput.setBackgroundResource(R.drawable.user_text_input_background)
        }

        // check for password letter
        // needs to have at least 1 letter
        if (passwordStr.contains(".*\\d.*".toRegex()) && passwordStr.length > 5 && !passwordStr.contains(".*[A-Za-z].*".toRegex())) {
            errorText.text = "Needs to have at least a letter."
            passwordInput.setBackgroundResource(R.drawable.user_text_input_failed_verify_background)
        }
        else if (passwordStr.contains(".*\\d.*".toRegex()) && passwordStr.length < 6  && !passwordStr.contains(".*[A-Za-z].*".toRegex())) {
            passwordInput.setBackgroundResource(R.drawable.user_text_input_failed_verify_background)
        }
        else {
            passwordInput.setBackgroundResource(R.drawable.user_text_input_background)
        }

        // correct password format
        if (passwordStr.length > 5 && passwordStr.contains(".*\\d.*".toRegex()) && passwordStr.contains(".*[A-Za-z].*".toRegex())) {
            errorText.text = ""
        }

        // only return true if no error is found
        return errorText.text == ""
    }
}