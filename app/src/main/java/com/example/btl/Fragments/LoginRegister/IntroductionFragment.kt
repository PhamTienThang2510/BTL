package com.example.btl.Fragments.LoginRegister

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.appcompat.widget.AppCompatButton
import com.example.btl.R

class IntroductionFragment: Fragment(R.layout.fragment_introduction) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val startButton = view.findViewById<AppCompatButton>(R.id.startButton)
        startButton.setOnClickListener {
            val navOptions = androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.introductionFragment, true)
                .build()
            findNavController().navigate(R.id.action_introductionFragment_to_accountFragment, null, navOptions)
        }
    }
}