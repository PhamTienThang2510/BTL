package com.example.btl.Fragments.LoginRegister

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.btl.R
import com.example.btl.ViewModel.AuthViewModel

class ForgotPasswordFragment : Fragment() {

    private lateinit var emailEditText: EditText
    private lateinit var sendButton: AppCompatButton
    private lateinit var backToLoginLink: TextView
    private lateinit var backButton: View
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forgotpassword, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emailEditText = view.findViewById(R.id.emailEditText)
        sendButton = view.findViewById(R.id.sendButton)
        backToLoginLink = view.findViewById(R.id.backToLogin)
        backButton = view.findViewById(R.id.backButton)

        sendButton.setOnClickListener {
            handleSendResetLink()
        }

        backToLoginLink.setOnClickListener {
            navigateBackToLogin()
        }

        backButton.setOnClickListener {
            navigateBackToLogin()
        }

        authViewModel.forgotPasswordSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess == true) {
                Toast.makeText(
                    requireContext(),
                    "Liên kết đặt lại mật khẩu đã được gửi đến ${emailEditText.text.toString().trim()}",
                    Toast.LENGTH_LONG
                ).show()
                authViewModel.resetForgotPasswordStatus()
                navigateBackToLogin()
            }
        }

        authViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrBlank()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            }
        }

        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            sendButton.isEnabled = !isLoading
            sendButton.text = if (isLoading) "Đang gửi..." else getString(R.string.send_reset_link)
        }
    }

    private fun handleSendResetLink() {
        val email = emailEditText.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidEmail(email)) {
            Toast.makeText(requireContext(), "Email không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        authViewModel.forgotPassword(email)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun navigateBackToLogin() {
        findNavController().popBackStack()
    }
}
