package com.example.btl.Fragments.LoginRegister

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.appcompat.widget.AppCompatButton
import com.example.btl.R

class AccountFragment: Fragment(R.layout.fragment_account_optionts) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loginButton = view.findViewById<AppCompatButton>(R.id.loginButton)
        val registerButton = view.findViewById<AppCompatButton>(R.id.registerButton)

        loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_loginFragment)
        }

        registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_registerFragment)
        }
    }
}