package com.example.btl.Fragments.LoginRegister

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.appcompat.widget.AppCompatButton
import com.example.btl.Activities.ShoppingActivity
import com.example.btl.R
import com.example.btl.ViewModel.AuthViewModel

class RegisterFragment: Fragment(R.layout.fragment_register) {
    private val authViewModel: AuthViewModel by activityViewModels()

    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: AppCompatButton
    private lateinit var googleSignUpButton: AppCompatButton
    private lateinit var facebookSignUpButton: AppCompatButton

    private var hasNavigated = false  // Prevent multiple navigations

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hasNavigated = false  // Reset when view is created

        usernameEditText = view.findViewById(R.id.usernameEditText)
        emailEditText = view.findViewById(R.id.emailEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText)
        registerButton = view.findViewById(R.id.registerButton)
        googleSignUpButton = view.findViewById(R.id.googleSignUpButton)
        facebookSignUpButton = view.findViewById(R.id.facebookSignUpButton)

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            // Validation
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.please_fill_all_fields_register), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(requireContext(), getString(R.string.password_must_be_at_least_6_characters), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(requireContext(), getString(R.string.passwords_do_not_match_toast), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.register(username, email, password)
        }

        // Handle Google Sign Up
        googleSignUpButton.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.google_sign_up_coming_soon_toast), Toast.LENGTH_SHORT).show()
        }

        // Handle Facebook Sign Up
        facebookSignUpButton.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.facebook_sign_up_coming_soon_toast), Toast.LENGTH_SHORT).show()
        }

        // Observe registration response
        authViewModel.loginResponse.observe(viewLifecycleOwner) { loginResponse ->
            if (loginResponse != null && !hasNavigated) {  // Only navigate once!
                hasNavigated = true
                Toast.makeText(requireContext(), getString(R.string.registration_successful, loginResponse.user.username), Toast.LENGTH_SHORT).show()
                // Direct navigation from context, not Activity
                try {
                    val intent = Intent(requireContext(), ShoppingActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    requireContext().startActivity(intent)
                    // Clear the response to prevent observer from firing again
                    authViewModel.clearLoginResponse()
                } catch (e: Exception) {
                    android.util.Log.e("RegisterFragment", "Navigation error: ${e.message}")
                    e.printStackTrace()
                }
            }
        }

        // Observe error messages
        authViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), getString(R.string.error, error), Toast.LENGTH_LONG).show()
            }
        }

        // Observe loading state
        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            registerButton.isEnabled = !isLoading
            registerButton.text = if (isLoading) getString(R.string.loading) else getString(R.string.register)
        }
    }
}

