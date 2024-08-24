package com.example.todo.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.todo.R
import com.example.todo.databinding.FragmentAccountSettingBinding
import com.google.firebase.auth.FirebaseAuth

class AccountSettingFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentAccountSettingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAccountSettingBinding.inflate(inflater, container, false)
        return binding.root
    }


}