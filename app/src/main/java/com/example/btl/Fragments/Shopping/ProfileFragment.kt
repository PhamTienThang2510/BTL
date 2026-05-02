package com.example.btl.Fragments.Shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.btl.DataStore.TokenManager
import com.example.btl.R
import com.example.btl.ViewModel.ProfileViewModel
import com.example.btl.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        viewModel.init(requireContext())  // ← Initialize with context

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
                binding.tvUserName.text = user.username
                binding.tvEmail.text = user.email
                if (user.phone != null && user.phone.isNotEmpty()) {
                    binding.tvPhone.text = user.phone
                } else {
                    binding.tvPhone.text = getString(R.string.no_phone_number)
                }
                
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
            binding.progressbarSettings.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        // Logout button
        binding.linearLogOut.setOnClickListener {
            logout()
        }

        // Edit profile
        binding.constraintProfile.setOnClickListener {
            showEditProfileDialog()
        }

        // View all orders
        binding.linearAllOrders.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_ordersListFragment)
        }
    }

    private fun showEditProfileDialog() {
        val currentUser = viewModel.user.value
        if (currentUser != null) {
            // Navigate to UserAccountFragment
            findNavController().navigate(R.id.action_to_userAccountFragment)
        } else {
            Toast.makeText(requireContext(), getString(R.string.user_data_not_loaded), Toast.LENGTH_SHORT).show()
        }
    }

    private fun logout() {
        tokenManager.clearToken()
        Toast.makeText(requireContext(), getString(R.string.logged_out), Toast.LENGTH_SHORT).show()
        // Navigate to login screen
        requireActivity().finishAffinity()
    }
}


