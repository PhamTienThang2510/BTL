package com.example.btl.Fragments.Shopping

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.btl.Adapters.CartAdapter
import com.example.btl.Model.Cart
import com.example.btl.R
import com.example.btl.ViewModel.CartViewModel
import com.example.btl.databinding.FragmentCartBinding
import kotlinx.coroutines.launch

class CartFragment : Fragment() {
    private lateinit var binding: FragmentCartBinding
    private val viewModel: CartViewModel by viewModels()
    private lateinit var cartAdapter: CartAdapter
    private val TAG = "CartFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Initializing CartFragment")

        // Initialize viewModel with context
        viewModel.init(requireContext())

        setupRecyclerView()
        setupClickListeners()
        setupViewModelObservers()
        loadCart()
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: Setting up adapter and layout")

        cartAdapter = CartAdapter(
            cartItems = emptyList(),
            onQuantityIncrement = { cartItem ->
                Log.d(TAG, "Incrementing quantity for ${cartItem.getProductName()}")
                val newQuantity = cartItem.quantity + 1
                viewModel.updateItemQuantity(cartItem.cart_item_id ?: 0, newQuantity)
            },
            onQuantityDecrement = { cartItem ->
                Log.d(TAG, "Decrementing quantity for ${cartItem.getProductName()}")
                val newQuantity = cartItem.quantity - 1
                if (newQuantity <= 0) {
                    viewModel.deleteItem(cartItem.cart_item_id ?: 0)
                } else {
                    viewModel.updateItemQuantity(cartItem.cart_item_id ?: 0, newQuantity)
                }
            },
            onDeleteClick = { cartItem ->
                Log.d(TAG, "Deleting item ${cartItem.getProductName()}")
                viewModel.deleteItem(cartItem.cart_item_id ?: 0)
            }
        )

        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }

        Log.d(TAG, "setupRecyclerView: ✅ RecyclerView setup complete")
    }

    private fun setupClickListeners() {
        Log.d(TAG, "setupClickListeners: Setting up UI click listeners")


        // Checkout button
        binding.buttonCheckout.setOnClickListener {
            Log.d(TAG, "Checkout button clicked")
            findNavController().navigate(R.id.action_navigation_cart_to_billingFragment)
        }
    }

    private fun setupViewModelObservers() {
        Log.d(TAG, "setupViewModelObservers: Setting up observers")

        viewLifecycleOwner.lifecycleScope.launch {
            // Observe cart state
            viewModel.cart.collect { cart ->
                Log.d(TAG, "Cart updated: ${cart.cart_items.size} items")
                updateUI(cart)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Observe loading state
            viewModel.isLoading.collect { isLoading ->
                Log.d(TAG, "Loading state: $isLoading")
                binding.progressbarCart.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Observe error state
            viewModel.error.collect { error ->
                if (error != null) {
                    Log.e(TAG, "Error: $error")
                    // TODO: Show error message to user (Toast/Snackbar)
                }
            }
        }
    }

    private fun updateUI(cart: Cart) {
        Log.d(TAG, "updateUI: Updating UI with ${cart.cart_items.size} items")

        if (cart.cart_items.isEmpty()) {
            // Show empty cart state
            Log.d(TAG, "updateUI: Cart is empty, showing empty state")
            binding.rvCart.visibility = View.GONE
            binding.layoutCartEmpty.visibility = View.VISIBLE
            binding.totalBoxContainer.visibility = View.GONE
            binding.buttonCheckout.visibility = View.GONE
        } else {
            // Show cart items
            Log.d(TAG, "updateUI: Showing cart items")
            binding.rvCart.visibility = View.VISIBLE
            binding.layoutCartEmpty.visibility = View.GONE
            binding.totalBoxContainer.visibility = View.VISIBLE
            binding.buttonCheckout.visibility = View.VISIBLE

            // Update adapter with new items
            cartAdapter.updateCartItems(cart.cart_items)

            // Update total price
            val totalPrice = cart.calculateTotal()
            binding.tvTotalPrice.text = "${String.format("%,d", totalPrice.toInt())} VND"

            Log.d(TAG, "updateUI: ✅ UI updated - Total: ${String.format("%,d", totalPrice.toInt())} VND")
        }
    }

    private fun loadCart() {
        Log.d(TAG, "loadCart: Loading cart from API")
        viewModel.loadCart(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: Cleaning up CartFragment")
    }
}
