package com.example.btl.Fragments.LoginRegister

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.appcompat.widget.AppCompatButton
import androidx.navigation.fragment.findNavController
import com.example.btl.Activities.ShoppingActivity
import com.example.btl.R
import com.example.btl.ViewModel.AuthViewModel

class LoginFragment: Fragment(R.layout.fragment_login) {
    private val authViewModel: AuthViewModel by activityViewModels()

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: AppCompatButton
    private lateinit var forgotPasswordLink: TextView
    private lateinit var googleSignInButton: AppCompatButton
    private lateinit var facebookSignInButton: AppCompatButton

    private var hasNavigated = false  // Prevent multiple navigations
    
    companion object {
        private const val TAG = "LoginFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "✅ onViewCreated called")

        hasNavigated = false  // Reset when view is created

        emailEditText = view.findViewById(R.id.emailEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        loginButton = view.findViewById(R.id.loginButton)
        forgotPasswordLink = view.findViewById(R.id.forgotPasswordLink)
        googleSignInButton = view.findViewById(R.id.googleSignInButton)
        facebookSignInButton = view.findViewById(R.id.facebookSignInButton)

        // Apply underline style to forgot password link
        val spannableString = SpannableString(getString(R.string.forgot_password_question))
        spannableString.setSpan(UnderlineSpan(), 0, spannableString.length, 0)
        forgotPasswordLink.text = spannableString

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.please_fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d(TAG, "🔄 Attempting login with email: $email")
            authViewModel.login(email, password)
        }

        // Handle forgot password link click
        forgotPasswordLink.setOnClickListener {
            Log.d(TAG, "Forgot password link clicked")
            try {
                findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
            } catch (e: Exception) {
                Log.e(TAG, "Navigation to ForgotPasswordFragment failed: ${e.message}")
                Toast.makeText(requireContext(), "Không thể mở màn hình quên mật khẩu", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle Google Sign In
        googleSignInButton.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.google_sign_in_coming_soon_toast), Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Google Sign In clicked")
        }

        // Handle Facebook Sign In
        facebookSignInButton.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.facebook_sign_in_coming_soon_toast), Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Facebook Sign In clicked")
        }

        // Observe login response
        authViewModel.loginResponse.observe(viewLifecycleOwner) { loginResponse ->
            if (loginResponse != null && !hasNavigated) {  // Only navigate once!
                Log.d(TAG, "✅ Login response received")
                hasNavigated = true
                Toast.makeText(requireContext(), getString(R.string.login_successful), Toast.LENGTH_SHORT).show()
                
                // Give DataStore time to persist the token
                Log.d(TAG, "⏳ Waiting for token to be persisted before navigation...")
                view.postDelayed({
                    Log.d(TAG, "🔄 Navigating to ShoppingActivity")
                    // Direct navigation from Activity, not Fragment
                    try {
                        val intent = Intent(requireContext(), ShoppingActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        requireContext().startActivity(intent)
                        // Clear the response to prevent observer from firing again
                        authViewModel.clearLoginResponse()
                        Log.d(TAG, "✅ Navigation completed")
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Navigation error: ${e.message}")
                        e.printStackTrace()
                    }
                }, 500)  // Wait 500ms for DataStore persistence
            }
        }

        // Observe error messages
        authViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Log.e(TAG, "❌ Login error: $error")
                Toast.makeText(requireContext(), getString(R.string.error, error), Toast.LENGTH_LONG).show()
            }
        }

        // Observe loading state
        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            loginButton.isEnabled = !isLoading
            loginButton.text = if (isLoading) getString(R.string.loading) else getString(R.string.login)
            Log.d(TAG, "Loading state: $isLoading")
        }
    }
}