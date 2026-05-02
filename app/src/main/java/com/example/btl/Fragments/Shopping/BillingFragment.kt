package com.example.btl.Fragments.Shopping

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.btl.Adapters.AddressAdapter
import com.example.btl.Adapters.BillingProductAdapter
import com.example.btl.Api.RetrofitClient
import com.example.btl.DataStore.TokenManager
import com.example.btl.Model.Address
import com.example.btl.Model.CreateOrderRequest
import com.example.btl.Model.OrderItemRequest
import com.example.btl.R
import com.example.btl.ViewModel.CartViewModel
import com.example.btl.databinding.FragmentBillingBinding
import kotlinx.coroutines.launch

class BillingFragment : Fragment() {
    private lateinit var binding: FragmentBillingBinding
    private val cartViewModel: CartViewModel by viewModels()
    private lateinit var billingProductAdapter: BillingProductAdapter
    private lateinit var addressAdapter: AddressAdapter
    private var addresses: List<Address> = emptyList()
    private var selectedAddressId: Int? = null
    private var selectedPaymentMethod: String = PAYMENT_CASH
    private var currentCartTotal: Long = 0L
    private var currentCartItemCount: Int = 0
    private var appliedDiscountCode: String? = null
    private var discountPercentage: Int = 0
    private val TAG = "BillingFragment"

    companion object {
        private const val PAYMENT_CASH = "cash"
        private const val PAYMENT_VNPAY = "vnpay"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBillingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Initializing BillingFragment")

        setupCloseBilling()
        setupRecyclerViews()
        setupClickListeners()
        setupObservers()
        loadAddresses()
        loadCartData()
    }

    private fun setupCloseBilling() {
        binding.imageCloseBilling.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerViews() {
        // Product RecyclerView (Horizontal)
        billingProductAdapter = BillingProductAdapter(emptyList())
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = billingProductAdapter
        }

        // Address RecyclerView (Horizontal)
        addressAdapter = AddressAdapter(
            addresses = emptyList(),
            onAddressSelected = { address ->
                selectedAddressId = address.address_id
                addressAdapter.selectAddress(address.address_id ?: 0)
                Log.d(TAG, "Address selected: ${address.address_id} - ${address.receiver_name}")
            }
        )
        binding.rvAddress.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = addressAdapter
        }
    }

    private fun setupClickListeners() {
        binding.radioGroupPayment.setOnCheckedChangeListener { _, checkedId ->
            selectedPaymentMethod = if (checkedId == R.id.radioVnpay) PAYMENT_VNPAY else PAYMENT_CASH
            updatePaymentViews()
            Log.d(TAG, "Payment method changed: $selectedPaymentMethod")
        }

        // Add address button
        binding.imageAddAddress.setOnClickListener {
            Log.d(TAG, "Add address clicked")
            findNavController().navigate(R.id.action_billingFragment_to_addressFragment)
        }

        // Apply discount button
        binding.buttonApplyDiscount.setOnClickListener {
            applyDiscountCode()
        }

        // Place order button
        binding.buttonPlaceOrder.setOnClickListener {
            placeOrder()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            cartViewModel.cart.collect { cart ->
                Log.d(TAG, "Cart updated: ${cart.cart_items.size} items")
                billingProductAdapter.updateProducts(cart.cart_items)
                
                // Update total price
                currentCartTotal = cart.calculateTotal().toLong()
                currentCartItemCount = cart.cart_items.sumOf { it.quantity }
                binding.tvTotalPrice.text = "${String.format("%,d", currentCartTotal)} VND"
                binding.tvOrderItemCount.text = currentCartItemCount.toString()
            }
        }

        val backStackHandle = findNavController().currentBackStackEntry?.savedStateHandle
        backStackHandle?.getLiveData<Boolean>("address_added")?.observe(viewLifecycleOwner) { isAdded ->
            if (isAdded == true) {
                val preferredAddressId = backStackHandle.get<Int>("new_address_id")
                Log.d(TAG, "Address added from AddressFragment, reloading. preferredId=$preferredAddressId")
                loadAddresses(preferredAddressId)
                backStackHandle["address_added"] = false
                backStackHandle.remove<Int>("new_address_id")
            }
        }
    }

    private fun loadCartData() {
        Log.d(TAG, "loadCartData: Loading cart data")
        cartViewModel.init(requireContext())
        cartViewModel.loadCart(requireContext())
    }

    private fun loadAddresses(preferredAddressId: Int? = null) {
        Log.d(TAG, "loadAddresses: Loading addresses from API")
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.progressbarAddress.visibility = View.VISIBLE

                val response = RetrofitClient.addressApi.getAddresses()

                if (response.isSuccessful && response.body() != null) {
                    addresses = response.body()!!
                    Log.d(TAG, "Addresses loaded: ${addresses.size}")
                    
                    addressAdapter.updateAddresses(addresses)
                    
                    // Auto-select preferred address, fallback default address, then first address.
                    val preferredAddress = addresses.find { it.address_id == preferredAddressId }
                    val defaultAddress = addresses.find { it.isDefault }
                    val addressToSelect = preferredAddress ?: defaultAddress ?: addresses.firstOrNull()

                    if (addressToSelect != null) {
                        selectedAddressId = addressToSelect.address_id
                        addressAdapter.selectAddress(addressToSelect.address_id ?: 0)
                    } else if (addresses.isNotEmpty()) {
                        selectedAddressId = addresses[0].address_id
                        addressAdapter.selectAddress(addresses[0].address_id ?: 0)
                    }
                } else {
                    Log.e(TAG, "Failed to load addresses: ${response.code()}")
                    Toast.makeText(requireContext(), "Failed to load addresses", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading addresses: ${e.message}", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressbarAddress.visibility = View.GONE
            }
        }
     }

    private fun applyDiscountCode() {
        val discountCode = binding.etDiscountCode.text.toString().trim()
        
        if (discountCode.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập mã giảm giá", Toast.LENGTH_SHORT).show()
            return
        }

        // Simple discount code validation (in real app, validate with backend)
        // Example: codes like "SAVE10", "SAVE20", "SAVE50" give 10%, 20%, 50% discount
        when {
            discountCode.uppercase().startsWith("SAVE") -> {
                try {
                    val percentStr = discountCode.uppercase().removePrefix("SAVE")
                    discountPercentage = percentStr.toInt()
                    if (discountPercentage in 1..99) {
                        appliedDiscountCode = discountCode
                        val discountAmount = (currentCartTotal * discountPercentage) / 100
                        val discountedTotal = currentCartTotal - discountAmount
                        
                        binding.tvDiscountInfo.text = "Đã giảm ${discountPercentage}%: -${String.format("%,d", discountAmount)} VND"
                        binding.tvDiscountInfo.visibility = View.VISIBLE
                        binding.tvTotalPrice.text = "${String.format("%,d", discountedTotal)} VND"
                        
                        Toast.makeText(requireContext(), "Áp dụng mã giảm giá thành công!", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "Discount code applied: $discountCode, discount: $discountPercentage%")
                    } else {
                        Toast.makeText(requireContext(), "Mã giảm giá không hợp lệ", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Mã giảm giá không hợp lệ", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Toast.makeText(requireContext(), "Mã giảm giá không hợp lệ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun placeOrder() {
        Log.d(TAG, "placeOrder: Placing order")

        if (selectedAddressId == null) {
            Toast.makeText(requireContext(), "Please select a shipping address", Toast.LENGTH_SHORT).show()
            return
        }

        val cart = cartViewModel.cart.value
        if (cart.cart_items.isEmpty()) {
            Toast.makeText(requireContext(), "Cart is empty", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.buttonPlaceOrder.isEnabled = false


                val finalTotal = if (discountPercentage > 0) {
                    currentCartTotal - (currentCartTotal * discountPercentage) / 100
                } else {
                    currentCartTotal
                }

                // Create order items from cart items
                val orderItems = cart.cart_items.map { cartItem ->
                    OrderItemRequest(
                        variant_id = cartItem.getVariantId(),
                        quantity = cartItem.quantity,
                        unit_price = cartItem.getPrice(),
                        renderId = cartItem.render_id
                    )
                }

                val customerId = cart.customer_id
                Log.d(TAG, "Order payload: customer_id=$customerId, address_id=$selectedAddressId, payment_method=$selectedPaymentMethod, total_amount=$finalTotal")
                orderItems.forEachIndexed { index, item ->
                    Log.d(TAG, "Order item[$index]: variant_id=${item.variant_id}, qty=${item.quantity}, unit_price=${item.unit_price}, render_id=${item.renderId}")
                }

                val paymentStatus = if (selectedPaymentMethod == PAYMENT_CASH) {
                    "UNPAID"
                } else {
                    null
                }
                val orderStatus = "PENDING"

                val createOrderRequest = CreateOrderRequest(
                    customer_id = customerId,
                    address_id = selectedAddressId,
                    items = orderItems,
                    paymentMethod = selectedPaymentMethod,
                    totalAmount = finalTotal.toDouble(),
                    paymentStatus = paymentStatus,
                    orderStatus = orderStatus
                )

                Log.d(TAG, "Sending createOrder request...")
                val response = RetrofitClient.orderApi.createOrder(createOrderRequest)

                Log.d(TAG, "createOrder response: code=${response.code()}, success=${response.isSuccessful}")
                if (!response.isSuccessful) {
                    Log.e(TAG, "createOrder error body: ${response.errorBody()?.string()}")
                }

                if (response.isSuccessful && response.body() != null) {
                    val order = response.body()!!
                    val orderId = order.order_id ?: 0
                    if (orderId == 0) {
                        Toast.makeText(requireContext(), "Failed to place order. Please try again.", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    Log.d(TAG, "Order created successfully: $orderId")
                    val args = Bundle().apply {
                        putInt("order_id", orderId)
                        putInt("cart_id", cart.cart_id ?: 0)
                        putInt("customer_id", cart.customer_id ?: 0)
                        putString("payment_method", selectedPaymentMethod)
                    }
                    findNavController().navigate(R.id.action_billingFragment_to_paymentFragment, args)
                } else {
                    Log.e(TAG, "Failed to create order: ${response.code()}")
                    Toast.makeText(requireContext(), "Failed to place order. Please try again.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error placing order: ${e.message}", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.buttonPlaceOrder.isEnabled = true
            }
        }
    }

    private fun updatePaymentViews() {
        binding.tvSelectedPaymentMethod.text = if (selectedPaymentMethod == PAYMENT_VNPAY) {
            getString(R.string.payment_vnpay)
        } else {
            getString(R.string.payment_cash)
        }

        binding.tvPaymentExplanation.text = if (selectedPaymentMethod == PAYMENT_VNPAY) {
            getString(R.string.payment_method_hint_vnpay)
        } else {
            getString(R.string.payment_method_hint)
        }

        binding.buttonPlaceOrder.text = if (selectedPaymentMethod == PAYMENT_VNPAY) {
            getString(R.string.pay_with_vnpay)
        } else {
            getString(R.string.place_order)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: Cleaning up BillingFragment")
    }

    override fun onResume() {
        super.onResume()
        updatePaymentViews()
    }
}
