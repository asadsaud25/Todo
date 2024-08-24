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
import com.example.todo.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            navController.navigate(R.id.action_loginFragment_to_taskPanelFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        loginEvent()
    }

    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        navController = Navigation.findNavController(view)
    }

    private fun loginEvent() {
        binding.signupFragment.setOnClickListener {
            navController.navigate(R.id.action_loginFragment_to_signupFragment)
        }
        binding.forgotPasswordFragment.setOnClickListener {
            navController.navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val pass = binding.passEt.text.toString().trim()

            if(email.isNotEmpty() && pass.isNotEmpty()) {
                binding.progressBar.visibility = View.VISIBLE
                auth.signInWithEmailAndPassword(email, pass)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                        binding.progressBar.visibility = View.GONE
                        navController.navigate(R.id.action_loginFragment_to_taskPanelFragment)
                    }
                    .addOnFailureListener {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, "Authentication Failed", Toast.LENGTH_SHORT).show()
                    }


            }else{
                Toast.makeText(context, "Fill all fields please", Toast.LENGTH_SHORT).show()
            }
        }
    }

}