package com.moop.gamerguides.mainfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.moop.gamerguides.R
import com.moop.gamerguides.adapter.model.GameAdapter
import com.moop.gamerguides.adapter.model.Games
import com.moop.gamerguides.helper.FirebaseUtil


class Home : Fragment() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var adapter: GameAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initialize Firebase auth
        firebaseAuth = Firebase.auth
        // initialize Firebase Database
        firebaseDatabase = Firebase.database(FirebaseUtil.firebaseDatabaseURL)
        // initialize Firebase Storage
        firebaseStorage = Firebase.storage(FirebaseUtil.firebaseStorageURL)

        val gameList: RecyclerView = view.findViewById(R.id.game_list)
        gameList.layoutManager = GridLayoutManager(context, 2)

        // set adapter options
        val options = FirebaseRecyclerOptions.Builder<Games>()
            .setQuery(firebaseDatabase.reference.child("games"), Games::class.java)
            .build()

        // create adapter
        adapter = GameAdapter(options)
        gameList.adapter = adapter
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