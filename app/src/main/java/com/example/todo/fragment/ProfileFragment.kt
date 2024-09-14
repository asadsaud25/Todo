package com.example.todo.fragment

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.example.todo.databinding.FragmentProfileBinding
import com.example.todo.utils.ProfileData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private lateinit var binding: FragmentProfileBinding
    private lateinit var db: FirebaseFirestore
    private var username: String? = null
    private var email: String? = null
    private var birthday: Date? = null
    private var profilePicUri: String? = null
    private val REQUEST_CODE_IMAGE_PICK = 100
    private val PERMISSION_REQUEST_CODE = 101

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        getDataFromFirebase()

        binding.savebtn.setOnClickListener {
            val updatedUsername = binding.textInput1.text.toString()
            val updatedEmail = binding.textInput2.text.toString()
            val updatedBirthday = binding.textInput3.text.toString()
            val parsedBirthday = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(updatedBirthday)
            val updatedProfileData = ProfileData(updatedUsername, updatedEmail, profilePicUri, parsedBirthday)

            update(updatedProfileData)
        }
        binding.pic.setOnClickListener {
            openGallery()
        }
    }

    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        navController = Navigation.findNavController(view)
        db = FirebaseFirestore.getInstance()

    }

    private fun getDataFromFirebase() {
        val uid = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(uid)
        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    username = document.getString("username")
                    email = document.getString("email")
                    birthday = document.getDate("birthday")
                    profilePicUri = document.getString("profile_pic")
                    setData(username, email, profilePicUri, birthday)
                }
            }
            .addOnFailureListener {
                Log.e("Firebase", "Error fetching user data: $it")
            }

    }

    private fun setData(username: String?, email: String?, profilePicUrl: String?, birthday: Date?) {
        binding.textInput1.setText(username)
        binding.textInput2.setText(email)
        binding.textInput3.setText(birthday?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) })

        // Load the profile picture from the URL using Glide
        if (!profilePicUrl.isNullOrEmpty()) {
            Glide.with(this).load(profilePicUrl).into(binding.pic)  // Using Glide to load image
        }
    }

    private fun update(profileDate: ProfileData) {
        val updateData = mapOf(
            "username" to profileDate.username,
            "email" to profileDate.email,
            "profile_pic" to profilePicUri,
            "birthday" to profileDate.birthday
        )

        db.collection("users").document(auth.currentUser?.uid ?: "")
            .update(updateData)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(context, "Profile Updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }


    }

    private fun openGallery() {
        // Check if permission is granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            // Request permission
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
        } else {
            // Permission already granted, open the gallery
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_IMAGE_PICK)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open the gallery
                openGallery()
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(context, "Permission denied to access external storage", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if(requestCode == REQUEST_CODE_IMAGE_PICK && resultCode == AppCompatActivity.RESULT_OK) {
            val imageUri = data?.data
            if(imageUri != null) {
                binding.pic.setImageURI(imageUri)
                uploadImageToFirebase(imageUri)
            }
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().reference.child("profile_pics/$uid.jpg")

        // Log the path and URI
        Log.d("FirebaseStorage", "Uploading to path: profile_pics/$uid.jpg")
        Log.d("ImageURI", "Selected image URI: $imageUri")


        // Start the upload
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    profilePicUri = uri.toString()  // Save the image URL
                    Log.d("Firebase", "Image uploaded successfully, download URL: $profilePicUri")
                    Toast.makeText(context, "Profile picture uploaded", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    Log.e("Firebase", "Failed to get download URL: ${e.message}")
                    Toast.makeText(context, "Failed to get download URL", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to upload image: ${e.message}")
                Toast.makeText(context, "Failed to upload image: ${e.message}", Toast.LENGTH_LONG).show()
            }

    }

}