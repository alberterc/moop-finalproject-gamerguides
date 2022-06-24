package com.moop.gamerguides

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {
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
    }
}