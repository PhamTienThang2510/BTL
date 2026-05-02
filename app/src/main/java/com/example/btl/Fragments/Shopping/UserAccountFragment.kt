package com.example.btl.Fragments.Shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.btl.DataStore.TokenManager
import com.example.btl.R
import com.example.btl.ViewModel.ProfileViewModel
import com.example.btl.databinding.FragmentUserAccountBinding

class UserAccountFragment : Fragment() {
    private lateinit var binding: FragmentUserAccountBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        viewModel.init(requireContext())

        // Setup UI observers
        setupObservers()

        // Load user profile data
        viewModel.loadUserProfile()

        // Setup click listeners
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                // Populate user data into EditTexts
                binding.edFirstName.setText(user.username)
                binding.edEmail.setText(user.email)
                binding.edLastName.setText(user.phone ?: "")

                // Load avatar image
                val imageUrl = user.avatar ?: user.image_url
                if (imageUrl != null && imageUrl.isNotEmpty()) {
                    Glide.with(this)
                        .load(imageUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_user)
                        .error(R.drawable.ic_user)
                        .into(binding.imageUser)
                } else {
                    // Set default placeholder if no image
                    binding.imageUser.setImageResource(R.drawable.ic_user)
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressbarAccount.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        // Close button
        binding.imageCloseUserAccount.setOnClickListener {
            closeFragment()
        }

        // Edit profile image
        binding.imageEdit.setOnClickListener {
            Toast.makeText(requireContext(), "Tính năng thay đổi ảnh sắp có", Toast.LENGTH_SHORT).show()
            // TODO: Implement image picker
        }

        // Save button
        binding.buttonSave.setOnClickListener {
            saveUserProfile()
        }

        // Update password link
        binding.tvUpdatePassword.setOnClickListener {
            Toast.makeText(requireContext(), "Tính năng thay đổi mật khẩu sắp có", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to change password screen
        }
    }

    private fun saveUserProfile() {
        val username = binding.edFirstName.text.toString().trim()
        val phone = binding.edLastName.text.toString().trim()

        if (username.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.username_required), Toast.LENGTH_SHORT).show()
            return
        }

        // Update user profile
        viewModel.updateUserProfile(username, if (phone.isEmpty()) null else phone)

        // Show success message
        Toast.makeText(requireContext(), "Thông tin được lưu thành công", Toast.LENGTH_SHORT).show()
    }

    private fun closeFragment() {
        findNavController().popBackStack()
    }
}

