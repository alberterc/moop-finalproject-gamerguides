@file:Suppress("DEPRECATION")

package com.moop.gamerguides.mainfragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.method.ScrollingMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.moop.gamerguides.R
import com.moop.gamerguides.helper.FirebaseUtil
import com.moop.gamerguides.login.SignUp
import com.moop.gamerguides.userprofile.AccountSettings
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserProfile : Fragment() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initialize Firebase auth
        firebaseAuth = Firebase.auth
        val user = firebaseAuth.currentUser
        // initialize Firebase database
        firebaseDatabase = Firebase.database(FirebaseUtil.firebaseDatabaseURL)

        // get user profile's info from firebase and set it to view
        initUserInfoFirebase(view)

        // refresh fragment every 1 second
        val refreshHandler = Handler()
        val runnable: Runnable = object : Runnable {
            override fun run() {
                initUserInfoFirebase(view)
                refreshHandler.postDelayed(this, 1 * 1000)
            }
        }
        refreshHandler.postDelayed(runnable, 1 * 1000)

        // Pengaturan akun text onclick function
        val accountSettingsButton: RelativeLayout = view.findViewById(R.id.account_settings_button_container)
        accountSettingsButton.setOnClickListener {
            activity!!.startActivity(Intent(activity, AccountSettings::class.java))
        }

        // Logout button onclick function
        val logoutButton: Button = view.findViewById(R.id.logout_button)
        logoutButton.setOnClickListener {
            // sign out currently signed in account
            firebaseAuth.signOut()

            // go to SignUp activity
            activity!!.startActivity(Intent(activity, SignUp::class.java))
            activity!!.finish()
        }

    }

    private fun initUserInfoFirebase(view: View) {
        val user = firebaseAuth.currentUser
        val bio: TextView = view.findViewById(R.id.bio_input)
        val username: TextView = view.findViewById(R.id.user_display_name)
        val email: TextView = view.findViewById(R.id.user_email_text)
        val profilePicture: CircleImageView = view.findViewById(R.id.user_profile_picture)

        // make course description view scrollable
        bio.movementMethod = ScrollingMovementMethod()

        if (user != null) {
            // get data from firebase account info
            username.text = user.displayName
            email.text = user.email

            firebaseDatabase.reference
                .child("users").child(user.uid).child("profile_picture")
                .get()
                .addOnSuccessListener {
                    Picasso.get()
                        .load(it.value.toString())
                        .into(profilePicture)
                }

            // get data from firebase database
            firebaseDatabase.reference
                .child("users")
                .child(user.uid)
                .child("bio")
                .get()
                .addOnSuccessListener {
                    bio.text = it.value.toString()
                }
        }

    }
}