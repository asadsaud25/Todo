package com.example.todo.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.todo.R
import com.example.todo.databinding.FragmentAccountSettingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AccountSettingFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private lateinit var binding: FragmentAccountSettingBinding
    private lateinit var db: FirebaseFirestore
    var email: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        changePassword()
    }

    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        navController = Navigation.findNavController(view)

        //fetch email from server
        val uid = auth.currentUser?.uid?: return
        val userRef = db.collection("users").document(uid)
        userRef.get()
            .addOnSuccessListener { document ->
                if(document.exists()) {
                    email = document.getString("email")
                }else {
                    Toast.makeText(context, "Error! couldn't load email from server", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error! couldn't load email", Toast.LENGTH_SHORT)
                    .show()
            }

    }


    private fun changePassword() {

        binding.btnChangePassword.setOnClickListener {
            val currentPassword = binding.currentPassword.text.toString().trim()
            val newPassword = binding.newPassword.text.toString().trim()
            val confirmPassword = binding.confirmPassword.text.toString().trim()
            if (currentPassword.isNotEmpty() && newPassword.isNotEmpty() && confirmPassword.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email!!, currentPassword)
                    .addOnSuccessListener {
                        if(newPassword == confirmPassword) {
                            binding.progressBar2.visibility = View.VISIBLE
                            auth.currentUser?.updatePassword(newPassword)
                                ?.addOnSuccessListener {
                                    Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
                                    binding.progressBar2.visibility = View.GONE
                                    auth.signOut()
                                    navController.navigate(R.id.action_accountSettingFragment_to_loginFragment)
                                }
                                ?.addOnFailureListener {
                                    Toast.makeText(
                                        context,
                                        "Password change failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    binding.progressBar2.visibility = View.GONE
                                }
                        }else {
                            Toast.makeText(context, "Password does not match", Toast.LENGTH_SHORT).show()
                        }

                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Wrong Password", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }


}