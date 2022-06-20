package com.moop.gamerguides.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.moop.gamerguides.R

class SignIn : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Sign up text onclick function
        val signUpText: TextView = findViewById(R.id.signup_text_button)
        signUpText.setOnClickListener {
            // go to SignUp activity
            startActivity(Intent(this, SignUp::class.java))
            finish()
        }
    }
}