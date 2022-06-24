package com.moop.gamerguides

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.ashokvarma.bottomnavigation.BottomNavigationBar
import com.ashokvarma.bottomnavigation.BottomNavigationBar.OnTabSelectedListener
import com.ashokvarma.bottomnavigation.BottomNavigationItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.moop.gamerguides.mainfragments.*


class MainActivity : AppCompatActivity() {
    private var lastSelectedPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // default bottom navigation bar with floating action button
        val bottomNavBar: BottomNavigationView = findViewById(R.id.bottom_nav_bar)
        bottomNavBar.background = null
        bottomNavBar.menu.getItem(2).isEnabled = false

        val navController: NavController = Navigation.findNavController(this, R.id.fragment_container_view)
        NavigationUI.setupWithNavController(bottomNavBar, navController)

        val floatingActionButton: FloatingActionButton = findViewById(R.id.fab_bottom_nav_bar)
        floatingActionButton.setOnClickListener {
            navController.navigate(R.id.home_fragment)
            bottomNavBar.selectedItemId = R.id.home_fragment
        }

//        // create custom Bottom navigation bar
//        val bottomNavigationBar: BottomNavigationBar = findViewById(R.id.bottom_nav_bar)
//        bottomNavigationBar
//            .addItem(BottomNavigationItem(R.drawable.ic_baseline_home_24, "Home"))
//            .addItem(BottomNavigationItem(R.drawable.ic_baseline_video_library_24, "Courses"))
//            .addItem(BottomNavigationItem(R.drawable.ic_baseline_add_circle_24, "Add Course"))
//            .addItem(BottomNavigationItem(R.drawable.ic_baseline_favorite_24, "Wishlist"))
//            .addItem(BottomNavigationItem(R.drawable.ic_baseline_account_circle_24, "Profile"))
//            .initialise()
//
//        bottomNavigationBar.setTabSelectedListener(object : OnTabSelectedListener {
//            val fragmentManager: FragmentManager = supportFragmentManager
//            override fun onTabSelected(position: Int) {
//                when (position) {
//                    0 -> {
//                        fragmentManager.beginTransaction().replace(R.id.fragment_container_view, AddNewCourse()).commit()
//                        bottomNavigationBar.setFirstSelectedPosition(position)
//                    }
//                    1 -> {
//                        fragmentManager.beginTransaction().replace(R.id.fragment_container_view, UserCourses()).commit()
//                        bottomNavigationBar.setFirstSelectedPosition(position)
//                    }
//                    2 -> {
//                        fragmentManager.beginTransaction().replace(R.id.fragment_container_view, Home()).commit()
//                        bottomNavigationBar.setFirstSelectedPosition(position)
//                    }
//                    3 -> {
//                        fragmentManager.beginTransaction().replace(R.id.fragment_container_view, UserWishlist()).commit()
//                        bottomNavigationBar.setFirstSelectedPosition(position)
//                    }
//                    4 -> {
//                        fragmentManager.beginTransaction().replace(R.id.fragment_container_view, UserProfile()).commit()
//                        bottomNavigationBar.setFirstSelectedPosition(position)
//                    }
//                }
//            }
//            override fun onTabUnselected(position: Int) {}
//            override fun onTabReselected(position: Int) {}
//        })

    }
}