package com.moop.gamerguides.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.moop.gamerguides.R

class SignUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Login text onclick function
        val loginText: TextView = findViewById(R.id.login_text_button)
        loginText.setOnClickListener {
            // go to SignIn activity
            startActivity(Intent(this, SignIn::class.java))
            finish()
        }
    }
}