package com.example.todo

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.todo.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var auth: FirebaseAuth
    private lateinit var fragmentManager: FragmentManager
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var db: FirebaseFirestore
    private var username: String? = null
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setSupportActionBar(binding.toolbar)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.taskPanelFragment,
                R.id.profileFragment,
                R.id.accountSettingFragment,
                R.id.aboutUsFragment,
            ),
            binding.drawerLayout
        )


        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
        binding.navView.setNavigationItemSelectedListener(this)



        navController.addOnDestinationChangedListener {_, destination, _ ->
            when(destination.id) {
                R.id.splashFragment,
                R.id.loginFragment,
                R.id.signupFragment,
                R.id.forgotPasswordFragment -> {
                    toggle.isDrawerIndicatorEnabled = false
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    binding.navView.isVisible = false
                } else -> {
                    toggle.isDrawerIndicatorEnabled = true
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    binding.navView.isVisible = true
                }
            }
        }

        auth.addAuthStateListener {
            getData()
        }
    }


    private fun init() {
        fragmentManager = supportFragmentManager
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.nav_open, R.string.nav_close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navHostFragment = supportFragmentManager.findFragmentById(binding.navHostFragment.id) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun getData() {
        if(auth.currentUser != null) {
            val uid = auth.currentUser?.uid ?: return
            val userRef = db.collection("users").document(uid)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        username = document.getString("username")?.replaceFirstChar { it.uppercase() } ?: ""
                        email = document.getString("email")
                        val profilePicUrl = document.getString("profile_pic") // Get the URL of the profile picture

                        // Set username and email
                        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.username).text = username
                        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.email).text = email

                        // Load the profile picture using Glide
                        if (profilePicUrl != null) {
                            val profileImageView = binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.profile_pic)
                            Glide.with(this)
                                .load(profilePicUrl)
                                .into(profileImageView)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error! couldn't load username and email", Toast.LENGTH_SHORT).show()
                }
        }
    }



    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> navController.navigate(R.id.taskPanelFragment)
            R.id.nav_settings -> navController.navigate(R.id.accountSettingFragment)
            R.id.nav_profile -> navController.navigate(R.id.profileFragment)
            R.id.nav_about -> navController.navigate(R.id.aboutUsFragment)
            R.id.nav_logout -> {
                auth.signOut()
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true) // The second parameter is for inclusive
                    .build()

                navController.navigate(R.id.loginFragment, null, navOptions)
            }
            else -> return false
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            // If the drawer is open, close it
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            // If the drawer is closed, handle the back press as usual
            super.onBackPressed() // Use this instead of calling super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    /*
    * ATTENTION!
    *
    * Hide Keyboard function not working properly....
    *
    * */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus() // Clear focus from EditText
                    hideKeyboard(this) // Hide keyboard
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun hideKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = activity.currentFocus ?: View(activity) // If no view is focused, create a new one
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}