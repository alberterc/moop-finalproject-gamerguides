package com.moop.gamerguides.login

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.moop.gamerguides.MainMenu
import com.moop.gamerguides.R

class SignIn : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        // initialize Firebase auth
        firebaseAuth = Firebase.auth
        val user = firebaseAuth.currentUser

        // if user is already signed in
        // go to MainMenu activity
        if (user != null) {
            startActivity(Intent(this, MainMenu::class.java))
            finish()
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Sign up text onclick function
        val signUpText: TextView = findViewById(R.id.signup_text_button)
        signUpText.setOnClickListener {
            // go to SignUp activity
            startActivity(Intent(this, SignUp::class.java))
            finish()
        }

        val emailInput: EditText = findViewById(R.id.email_input)
        val passwordInput: EditText = findViewById(R.id.password_input)
        val forgotPassword: TextView = findViewById(R.id.forgot_password)
        val loginButton: Button = findViewById(R.id.login_button)
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

        // forgot password onclick function
        forgotPassword.setOnClickListener {
            if (emailInput.text.toString() != "") {
                firebaseAuth.sendPasswordResetEmail(emailInput.text.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT)
                                .show()
                        }
                        else {
                            try {
                                task.exception as FirebaseAuthInvalidUserException
                                Toast.makeText(this, "Account not found.", Toast.LENGTH_SHORT)
                                    .show()
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
        }

        // Login button onclick function
        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            if (email != "" && password != "") {
                signInAccountFirebase(email, password)
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun signInAccountFirebase(email: String, password: String) {
        val emailInput: EditText = findViewById(R.id.email_input)
        val passwordInput: EditText = findViewById(R.id.password_input)
        val errorText: TextView = findViewById(R.id.error_text)

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                // sign in success, check if email is verified
                if (task.isSuccessful) {
                    // go to MainMenu activity
                    startActivity(Intent(applicationContext, MainMenu::class.java))
                    finish()
                }
                // sign in failed
                else {
                    try {
                        when ((task.exception as FirebaseAuthException?)!!.errorCode) {
                            "ERROR_INVALID_EMAIL" -> {
                                errorText.text = "The email address is badly formatted."
                                emailInput.requestFocus()
                            }
                            "ERROR_WRONG_PASSWORD" -> {
                                errorText.text = "The password is incorrect."
                                passwordInput.requestFocus()
                                passwordInput.setText("")
                            }
                            "ERROR_USER_DISABLED" -> Toast.makeText(
                                applicationContext,
                                "The user account has been disabled by an administrator.",
                                Toast.LENGTH_SHORT
                            ).show()
                            "ERROR_USER_NOT_FOUND" -> Toast.makeText(
                                applicationContext,
                                "Account not found.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: ClassCastException) {
                        Toast.makeText(
                            applicationContext,
                            "A network error has occurred",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }
}