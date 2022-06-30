@file:Suppress("DEPRECATION")

package com.moop.gamerguides

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.youtube.player.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.moop.gamerguides.adapter.VideoAdapter
import com.moop.gamerguides.adapter.model.Videos
import com.moop.gamerguides.helper.FirebaseUtil
import com.moop.gamerguides.helper.WrapContentLinearLayoutManager
import com.moop.gamerguides.helper.YouTubeUtil
import com.moop.gamerguides.helper.makeScrollableInScrollView
import com.squareup.picasso.Picasso


class VideoDetails : AppCompatActivity(), YouTubePlayer.OnInitializedListener {
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var courseID: String
    private lateinit var videoID: String
    private lateinit var videoTitle: TextView
    private lateinit var videoDescription: TextView
    private lateinit var videoImage: ShapeableImageView
    private lateinit var videoList: RecyclerView
    private lateinit var videoURL: String
    private lateinit var adapter: VideoAdapter

    // youtube player
    private lateinit var ytPlayerFragment: YouTubePlayerFragment
    private var ytPlayer: YouTubePlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_details)

        // initialize Firebase Database
        firebaseDatabase = Firebase.database(FirebaseUtil.firebaseDatabaseURL)
        // initialize Firebase Storage
        firebaseStorage = Firebase.storage(FirebaseUtil.firebaseStorageURL)
        // get course id
        courseID = intent.extras!!.getString("courseID")!!
        // get video id
        videoID = intent.extras!!.getString("videoID")!!

        videoTitle = findViewById(R.id.video_title)
        videoImage = findViewById(R.id.video_image)
        videoDescription = findViewById(R.id.video_description)
        videoList = findViewById(R.id.video_list)
        ytPlayerFragment = fragmentManager.findFragmentById(R.id.video_youtube_player) as YouTubePlayerFragment

        // set video recycler view layout manager
        videoList.layoutManager = WrapContentLinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        // make video description text view scrollable in scroll view
        videoDescription.makeScrollableInScrollView()
        // initialize YouTube player fragment
        ytPlayerFragment.initialize(YouTubeUtil.youTubeKey, this)

        // get video data from firebase
        initDataFromFirebase()
    }

    override fun onInitializationSuccess(provider: YouTubePlayer.Provider?, player: YouTubePlayer?, wasRestored: Boolean) {
        if (!wasRestored) {
            ytPlayer = player!!

            // enables automatic control of orientation
            player.fullscreenControlFlags = YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION

            // show full screen in landscape mode always
            player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE)

            // system controls will appear automatically
            player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI)

            player.cueVideo(videoURL)
        }
    }

    override fun onInitializationFailure(provider: YouTubePlayer.Provider?, errorReason: YouTubeInitializationResult?) {}

    private fun initDataFromFirebase() {
        // video database reference
        val videoDatabaseRef: DatabaseReference = firebaseDatabase.reference.child("courses")
            .child(courseID).child("videos").child(videoID)

        // get video title
        videoDatabaseRef.child("title").get()
            .addOnSuccessListener {
                videoTitle.text = it.value.toString()
            }

        // get video description
        videoDatabaseRef.child("description").get()
            .addOnSuccessListener {
                videoDescription.text = it.value.toString()
            }

        // get video image
        videoDatabaseRef.child("image").get()
            .addOnSuccessListener {
                if (it.value.toString() == "") {
                    videoImage.setImageResource(R.drawable.gamerguides_logo)
                }
                else {
                    Picasso.get()
                        .load(it.value.toString())
                        .into(videoImage)
                }
            }

        // get video url
        videoDatabaseRef.child("video").get()
            .addOnSuccessListener {
                videoURL = it.value.toString()
            }

        // get video list
        // get course videos from firebase database
        // set adapter options
        val options = FirebaseRecyclerOptions.Builder<Videos>()
            .setQuery(firebaseDatabase.reference.child("courses").child(courseID).child("videos"), Videos::class.java)
            .build()
        adapter = VideoAdapter(options, courseID)
        videoList.adapter = adapter
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