package com.example.btl.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.btl.Model.Order
import com.example.btl.R
import com.example.btl.databinding.ItemOrderBinding
import java.text.SimpleDateFormat
import java.util.Locale

class OrdersListAdapter(
    private var orders: MutableList<Order>,
    private val onOrderClick: (Order) -> Unit = {},
    private val onCancelOrderClick: (Order) -> Unit = {}
) : RecyclerView.Adapter<OrdersListAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            binding.apply {
                tvOrderId.text = "Đơn hàng #${order.order_id}"
                tvOrderAmount.text = "${String.format("%,.0f", order.totalAmount)} VND"
                tvOrderStatus.text = getStatusText(order.order_status)
                tvOrderStatus.setTextColor(getStatusColor(order.order_status))
                tvOrderDate.text = formatDate(order.created_at)
                tvPaymentStatus.text = getPaymentStatusText(order.payment_status)
                tvItemCount.text = "${order.orderItems.size} sản phẩm"

                // Show/hide cancel button based on order status
                btnCancelOrder.visibility = if (order.order_status?.lowercase()?.trim() == "pending") {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }

                root.setOnClickListener {
                    onOrderClick(order)
                }

                btnCancelOrder.setOnClickListener {
                    onCancelOrderClick(order)
                }
            }
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
                "pending" -> itemView.context.getColor(R.color.g_orange)
                "confirmed" -> itemView.context.getColor(R.color.g_blue)
                "shipped" -> itemView.context.getColor(R.color.g_blue)
                "delivered" -> itemView.context.getColor(R.color.g_green)
                "cancelled" -> itemView.context.getColor(R.color.g_red)
                else -> itemView.context.getColor(R.color.g_gray700)
            }
        }

        private fun getPaymentStatusText(status: String?): String {
            return when (status?.lowercase()?.trim()) {
                "pending" -> "Thanh toán chưa xong"
                "completed" -> "Đã thanh toán"
                "failed" -> "Thanh toán thất bại"
                null, "" -> "Chưa cập nhật"
                else -> status
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<Order>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }
}
