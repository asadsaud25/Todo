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
import com.example.todo.databinding.FragmentSignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private lateinit var binding: FragmentSignupBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        init(view)
        registryEvent()
    }

    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        navController = Navigation.findNavController(view)
    }

    private fun registryEvent() {
        binding.loginFragment.setOnClickListener {
            navController.navigate(R.id.action_signupFragment_to_loginFragment)
        }

        binding.btnSignup.setOnClickListener {
            val username = binding.usernameEt.text.toString().trim()
            val email = binding.emailEt.text.toString().trim()
            val pass = binding.passEt.text.toString().trim()
            val verifyPass = binding.verifypassEt.text.toString().trim()
            val profilePic =  null

            if(username.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty() && verifyPass.isNotEmpty()) {

                binding.progressBar.visibility = View.VISIBLE
                if (pass == verifyPass) {
                    auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = auth.currentUser?.uid
                            val userRef =
                                FirebaseFirestore.getInstance().collection("users").document(uid!!)

                            userRef.set(
                                hashMapOf(
                                    "username" to username,
                                    "email" to email,
                                    "profile_pic" to profilePic
                                )
                            )
                                .addOnSuccessListener {
                                    binding.progressBar.visibility = View.GONE
                                    Toast.makeText(
                                        context,
                                        "Registration Successful",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    navController.navigate(R.id.action_signupFragment_to_loginFragment)
                                }
                                .addOnFailureListener {
                                    binding.progressBar.visibility = View.GONE
                                    Toast.makeText(
                                        context,
                                        "Something went wrong try again\nRegistration Failed!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        } else {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(
                                context,
                                "Something went wrong try again\nRegistration Failed!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }else {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        context,
                        "password don't match!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    context,
                    "Please fill all field",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}