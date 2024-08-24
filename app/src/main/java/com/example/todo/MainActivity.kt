package com.example.todo

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.todo.databinding.ActivityMainBinding
import com.example.todo.fragment.AboutUsFragment
import com.example.todo.fragment.AccountSettingFragment
import com.example.todo.fragment.ProfileFragment
import com.example.todo.fragment.TaskPanelFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var auth: FirebaseAuth
    private lateinit var fragmentManager: FragmentManager
    private lateinit var fragmentTransaction: FragmentTransaction
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var db: FirebaseFirestore
    private var username: String? = null
    private var email: String? = null
    private var profilePic: ImageView? = null

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
                    toggle.setDrawerIndicatorEnabled(false)
                    binding.navView.isVisible = false
                } else -> {
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
            val uid = auth.currentUser?.uid?: return
            val userRef = db.collection("users").document(uid)
            userRef.get()
                .addOnSuccessListener { document ->
                    if(document.exists()) {
                        username = document.getString("username")?.replaceFirstChar { it.uppercase() } ?: ""
                        email = document.getString("email")?.replaceFirstChar { it.uppercase() } ?: ""
                        profilePic = document.get("profile_pic") as ImageView?


                        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.username).text = username
                        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.email).text = email
                        if(profilePic != null) {
                            binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.profile_pic).setImageDrawable(profilePic?.drawable)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error! couldn't load username and email", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }
    private fun openFragment(fragment: Fragment) {
        fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.nav_host_fragment, fragment)
        fragmentTransaction.commit()
    }

    override fun onNavigationItemSelected(Item: MenuItem): Boolean {
        when (Item.itemId) {
            // Drawer menu items
            R.id.nav_home -> openFragment(TaskPanelFragment())
            R.id.nav_settings -> openFragment(AccountSettingFragment())
            R.id.nav_profile -> openFragment(ProfileFragment())
            R.id.nav_about -> openFragment(AboutUsFragment())
            R.id.nav_logout -> {
                auth.signOut()
                navController.navigate(R.id.loginFragment)
            }
            else -> return false
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if(binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }else {
            super.onBackPressedDispatcher.onBackPressed()
        }
        super.onBackPressed()
    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}