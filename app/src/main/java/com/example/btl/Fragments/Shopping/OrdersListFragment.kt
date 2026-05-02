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
import com.example.btl.Adapters.OrdersListAdapter
import com.example.btl.Api.RetrofitClient
import com.example.btl.DataStore.TokenManager
import com.example.btl.Model.Order
import com.example.btl.Model.UpdateOrderRequest
import com.example.btl.R
import com.example.btl.databinding.FragmentOrdersListBinding
import kotlinx.coroutines.launch

class OrdersListFragment : Fragment() {
    private lateinit var binding: FragmentOrdersListBinding
    private lateinit var ordersAdapter: OrdersListAdapter
    private val TAG = "OrdersListFragment"
    private val orders = mutableListOf<Order>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrdersListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        loadOrders()
    }

    private fun setupRecyclerView() {
        ordersAdapter = OrdersListAdapter(
            orders,
            onOrderClick = { order ->
                val bundle = Bundle().apply {
                    putInt("order_id", order.order_id ?: 0)
                }
                findNavController().navigate(R.id.action_ordersListFragment_to_orderDetailFragment, bundle)
            },
            onCancelOrderClick = { order ->
                showCancelConfirmation(order)
            }
        )

        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ordersAdapter
        }
    }

    private fun showCancelConfirmation(order: Order) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Hủy đơn hàng")
            .setMessage("Bạn có chắc chắn muốn hủy đơn hàng #${order.order_id}?")
            .setPositiveButton("Xác nhận hủy") { _, _ ->
                cancelOrder(order)
            }
            .setNegativeButton("Quay lại") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun cancelOrder(order: Order) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE

                // Khởi tạo request body đúng theo curl: {"order_status": "CANCELLED"}
                val requestBody = UpdateOrderRequest(order_status = "CANCELLED")
                
                Log.d(TAG, "Canceling order ${order.order_id} with status CANCELLED")
                
                // Gọi API PUT /orders/{id}
                val response = RetrofitClient.orderApi.updateOrder(
                    orderId = order.order_id ?: 0,
                    request = requestBody
                )

                if (response.isSuccessful) {
                    Log.d(TAG, "Order ${order.order_id} cancelled successfully")
                    Toast.makeText(requireContext(), "Đã hủy đơn hàng thành công", Toast.LENGTH_SHORT).show()
                    // Tải lại danh sách để cập nhật trạng thái mới
                    loadOrders()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    Log.e(TAG, "Failed to cancel order: ${response.code()} - $errorMsg")
                    Toast.makeText(requireContext(), "Không thể hủy đơn hàng (Mã: ${response.code()})", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during order cancellation: ${e.message}", e)
                Toast.makeText(requireContext(), "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        binding.imageCloseOrders.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadOrders() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.tvEmptyOrders.visibility = View.GONE

                val response = RetrofitClient.orderApi.getOrders()

                if (response.isSuccessful && response.body() != null) {
                    val fetchedOrders = response.body()!!
                    orders.clear()
                    orders.addAll(fetchedOrders.sortedByDescending { it.order_id }) // Hiện đơn mới nhất lên đầu
                    ordersAdapter.notifyDataSetChanged()

                    binding.tvEmptyOrders.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    binding.tvEmptyOrders.visibility = View.VISIBLE
                    binding.tvEmptyOrders.text = "Không thể tải danh sách đơn hàng"
                }
            } catch (e: Exception) {
                binding.tvEmptyOrders.visibility = View.VISIBLE
                binding.tvEmptyOrders.text = "Lỗi: ${e.message}"
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}
