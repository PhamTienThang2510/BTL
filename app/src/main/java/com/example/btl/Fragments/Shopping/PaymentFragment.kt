package com.example.btl.Fragments.Shopping

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.btl.DataStore.TokenManager
import com.example.btl.Repository.CartRepository
import com.example.btl.R
import com.example.btl.databinding.FragmentPaymentBinding
import kotlinx.coroutines.launch

class PaymentFragment : Fragment() {
    private lateinit var binding: FragmentPaymentBinding
    private var orderId: Int = 0
    private var cartId: Int = 0
    private var customerId: Int = 0
    private val cartRepository = CartRepository()
    private val TAG = "PaymentFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        orderId = arguments?.getInt("order_id") ?: 0
        cartId = arguments?.getInt("cart_id") ?: 0
        customerId = arguments?.getInt("customer_id") ?: 0

        if (orderId == 0) {
            Toast.makeText(requireContext(), getString(R.string.payment_order_missing), Toast.LENGTH_SHORT).show()
        }

        clearCartAfterPayment()
        setupClickListeners()
    }

    private fun clearCartAfterPayment() {
        if (cartId <= 0) return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val token = TokenManager(requireContext()).getToken() ?: ""
                val result = cartRepository.clearCart(cartId, customerId.takeIf { it > 0 }, token)
                if (result.isSuccess) {
                    Log.d(TAG, "clearCartAfterPayment: Cart cleared for cartId=$cartId")
                } else {
                    Log.e(TAG, "clearCartAfterPayment: Failed ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "clearCartAfterPayment: Exception ${e.message}", e)
            }
        }
    }

    private fun setupClickListeners() {
        binding.buttonOrderTracking.setOnClickListener {
            if (orderId > 0) {
                val args = Bundle().apply { putInt("order_id", orderId) }
                findNavController().navigate(R.id.action_paymentFragment_to_orderDetailFragment, args)
            } else {
                findNavController().navigate(R.id.ordersListFragment)
            }
        }

        binding.buttonBackHome.setOnClickListener {
            findNavController().navigate(R.id.navigation_home)
        }
    }
}
