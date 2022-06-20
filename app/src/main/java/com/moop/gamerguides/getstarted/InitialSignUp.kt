package com.moop.gamerguides.getstarted

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.moop.gamerguides.R
import com.moop.gamerguides.login.SignIn
import com.moop.gamerguides.login.SignUp

class InitialSignUp : AppCompatActivity() {
    private lateinit var initialSignUpPref: SharedPreferences
    private lateinit var getStartedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        // check if this activity needs to be showed to the user or not
        // if the user has already reached the sign-up activity
        // this activity will not be showed (skip to SignUp activity)
        initialSignUpPref = getSharedPreferences("InitialSignUpPREF", Context.MODE_PRIVATE)
        getStartedPref = getSharedPreferences("GetStartedPREF", Context.MODE_PRIVATE)
        if (initialSignUpPref.getBoolean("activity_passed", false)) {
            // go to SignUp activity
            startActivity(Intent(this, SignUp::class.java))
            finish()
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial_sign_up)

        // Already have an account text onclick function
        val alreadyHaveAccount: TextView = findViewById(R.id.already_have_account)
        alreadyHaveAccount.setOnClickListener {
            // Update GetStarted and InitialSignUp activity visited state
            changeActivityPref()

            // go to SignIn activity
            startActivity(Intent(this, SignIn::class.java))
            finish()
        }

        // Create Account button onclick function
        val createAccountButton: Button = findViewById(R.id.create_account_button)
        createAccountButton.setOnClickListener {
            // Update GetStarted and InitialSignUp activity visited state
            changeActivityPref()

            // go to SignUp activity
            startActivity(Intent(this, SignUp::class.java))
            finish()

        }
    }

    private fun changeActivityPref() {
        // SignUp or SignIn activity has been reached
        // InitialSignUp activity will never be showed to the user
        val initialSignUpPrefEdit: SharedPreferences.Editor = initialSignUpPref.edit()
        initialSignUpPrefEdit.putBoolean("activity_passed", true)
        initialSignUpPrefEdit.apply()

        // SignUp or SignIn activity has been reached
        // GetStarted activity will never be showed to the user
        val getStartedPrefEdit: SharedPreferences.Editor = getStartedPref.edit()
        getStartedPrefEdit.putBoolean("activity_passed", true)
        getStartedPrefEdit.apply()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(applicationContext, GetStarted::class.java)
        startActivity(intent)
        finish()
    }
}