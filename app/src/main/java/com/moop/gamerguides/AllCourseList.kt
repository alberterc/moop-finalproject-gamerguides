package com.moop.gamerguides

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.moop.gamerguides.adapter.AllCourseListAdapter
import com.moop.gamerguides.adapter.model.Courses
import com.moop.gamerguides.helper.FirebaseUtil
import com.moop.gamerguides.helper.GameTitleUtil
import com.moop.gamerguides.helper.WrapContentGridLayoutManager

class AllCourseList : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var adapter: AllCourseListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_course_list)

        // initialize Firebase auth
        firebaseAuth = Firebase.auth
        // initialize Firebase Database
        firebaseDatabase = Firebase.database(FirebaseUtil.firebaseDatabaseURL)

        val gameTitleView: TextView = findViewById(R.id.game_title)
        val courseListView: RecyclerView = findViewById(R.id.course_list)

        // get game title
        val gameTitle = intent.extras!!.getString("gameTitle")
        // set game title
        gameTitleView.text = gameTitle
        val gameTitleDb = GameTitleUtil.change(gameTitle)

        // set course list layout manager
        courseListView.layoutManager = WrapContentGridLayoutManager(applicationContext, 2)

        // set adapter options
        val options = FirebaseRecyclerOptions.Builder<Courses>()
            .setQuery(firebaseDatabase.reference.child("games").child(gameTitleDb).child("courses"), Courses::class.java)
            .build()

        // create adapter
        adapter = AllCourseListAdapter(options, gameTitleDb)
        courseListView.adapter = adapter

    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

}