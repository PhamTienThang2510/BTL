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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.btl.Adapters.OrderDetailAdapter
import com.example.btl.Api.RetrofitClient
import com.example.btl.DataStore.TokenManager
import com.example.btl.Model.Order
import com.example.btl.R
import com.example.btl.databinding.FragmentOrderDetailBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class OrderDetailFragment : Fragment() {
    private lateinit var binding: FragmentOrderDetailBinding
    private lateinit var orderDetailAdapter: OrderDetailAdapter
    private val TAG = "OrderDetailFragment"
    private var orderId: Int = 0
    private var order: Order? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Initializing OrderDetailFragment")

        orderId = arguments?.getInt("order_id") ?: 0
        Log.d(TAG, "Order ID: $orderId")

        if (orderId == 0) {
            Toast.makeText(requireContext(), "Invalid order ID", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        setupRecyclerView()
        setupClickListeners()
        loadOrderDetail()
    }

    private fun setupRecyclerView() {
        orderDetailAdapter = OrderDetailAdapter(emptyList(), false) { item ->
            val productId = item.product_id ?: item.productVariants?.product_id
            val orderItemId = item.order_item_id
            val customerId = order?.customer_id
            if (productId == null || productId <= 0 || orderItemId == null || orderItemId <= 0) {
                Toast.makeText(requireContext(), "Không tìm thấy sản phẩm để đánh giá", Toast.LENGTH_SHORT).show()
                return@OrderDetailAdapter
            }

            val productName = item.product_name
                ?: item.productVariants?.product?.product_name
                ?: ""
            val bundle = Bundle().apply {
                putInt("product_id", productId)
                putInt("order_item_id", orderItemId)
                putInt("customer_id", customerId ?: 0)
                putString("product_name", productName)
                putString("order_status", order?.order_status)
            }
            findNavController().navigate(R.id.action_orderDetailFragment_to_reviewFragment, bundle)
        }

        binding.rvOrderItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderDetailAdapter
        }
    }

    private fun setupClickListeners() {
        binding.imageCloseDetail.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnReturnOrder.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("order_id", orderId)
            }
            findNavController().navigate(R.id.action_orderDetailFragment_to_refundFragment, bundle)
        }
    }

    private fun loadOrderDetail() {
        Log.d(TAG, "loadOrderDetail: Loading order $orderId from API")
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.contentLayout.visibility = View.GONE

                val response = RetrofitClient.orderApi.getOrder(orderId)

                if (response.isSuccessful && response.body() != null) {
                    order = response.body()!!
                    Log.d(TAG, "Order loaded successfully")
                    displayOrderInfo()
                    binding.contentLayout.visibility = View.VISIBLE
                } else {
                    Log.e(TAG, "Failed to load order: ${response.code()}")
                    Toast.makeText(requireContext(), "Failed to load order", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading order: ${e.message}", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun displayOrderInfo() {
        if (order == null) return

        order!!.apply {
            binding.tvOrderNumber.text = "Đơn hàng #$order_id"
            binding.tvOrderStatus.text = getStatusText(order_status)
            binding.tvOrderStatus.setTextColor(getStatusColor(order_status))
            binding.tvOrderDate.text = "Ngày đặt: ${formatDate(created_at)}"
            binding.tvTotalAmount.text = "${String.format("%,.0f", totalAmount)} VND"
            binding.tvPaymentStatus.text = "Thanh toán: ${getPaymentStatusText(payment_status)}"
            binding.tvPaymentStatus.setTextColor(getPaymentStatusColor(payment_status))

            val allowReview = isOrderCompleted(order_status)
            orderDetailAdapter.setReviewEnabled(allowReview)
            binding.btnReturnOrder.visibility =
                if (allowReview) View.VISIBLE else View.GONE

            // Display order items
            orderDetailAdapter.updateItems(orderItems)

            Log.d(TAG, "Order info displayed: ${orderItems.size} items")
        }
    }

    private fun isOrderCompleted(status: String?): Boolean {
        val normalized = status?.trim()?.lowercase(Locale.getDefault())
        return normalized == "complete" || normalized == "completed"
    }

    private fun getStatusText(status: String?): String {
        return when (status?.lowercase()?.trim()) {
            "pending" -> "Chờ xác nhận"
            "confirmed" -> "Đã xác nhận"
            "shipped" -> "Đang giao"
            "delivered" -> "Đã giao"
            "cancelled" -> "Đã hủy"
            null, "" -> "Chưa cập nhật"
            else -> status
        }
    }

    private fun getStatusColor(status: String?): Int {
        return when (status?.lowercase()?.trim()) {
            "pending" -> requireContext().getColor(R.color.g_orange)
            "confirmed" -> requireContext().getColor(R.color.g_blue)
            "shipped" -> requireContext().getColor(R.color.g_blue)
            "delivered" -> requireContext().getColor(R.color.g_green)
            "cancelled" -> requireContext().getColor(R.color.g_red)
            else -> requireContext().getColor(R.color.g_gray700)
        }
    }

    private fun getPaymentStatusText(status: String?): String {
        return when (status?.lowercase()?.trim()) {
            "pending" -> "Chưa thanh toán"
            "completed" -> "Đã thanh toán"
            "failed" -> "Thanh toán thất bại"
            null, "" -> "Chưa cập nhật"
            else -> status
        }
    }

    private fun getPaymentStatusColor(status: String?): Int {
        return when (status?.lowercase()?.trim()) {
            "pending" -> requireContext().getColor(R.color.g_orange)
            "completed" -> requireContext().getColor(R.color.g_green)
            "failed" -> requireContext().getColor(R.color.g_red)
            else -> requireContext().getColor(R.color.g_gray700)
        }
    }

    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "N/A"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: return dateString)
        } catch (e: Exception) {
            dateString
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: Cleaning up OrderDetailFragment")
    }
}
