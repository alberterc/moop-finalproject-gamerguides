@file:Suppress("DEPRECATION")

package com.moop.gamerguides

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.moop.gamerguides.getstarted.GetStarted

class IntroScreen : AppCompatActivity() {
    private val handler = Handler();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handler.postDelayed({
            startActivity(Intent(applicationContext, GetStarted::class.java))
            finish()
        }, 2000)
    }

    override fun onBackPressed() {
        handler.removeCallbacksAndMessages(null)
        finish()
        super.onBackPressed()
    }
}