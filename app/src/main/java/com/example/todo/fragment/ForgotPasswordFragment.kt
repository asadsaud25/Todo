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
import com.example.todo.databinding.FragmentForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private lateinit var binding: FragmentForgotPasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        resetPasswordEvent()
    }

    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        navController = Navigation.findNavController(view)
    }

    private fun resetPasswordEvent() {
        binding.loginFragment.setOnClickListener {
            navController.navigate(R.id.action_forgotPasswordFragment_to_loginFragment)
        }

        binding.btnResetpass.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()

            if(email.isNotEmpty()) {
                binding.progressBar.visibility = View.VISIBLE
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Please check your email", Toast.LENGTH_SHORT).show()
                        navController.navigate(R.id.action_forgotPasswordFragment_to_loginFragment)
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                    }
                binding.progressBar.visibility = View.GONE
            } else {
                Toast.makeText(
                    context,
                    "Please enter email",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

}