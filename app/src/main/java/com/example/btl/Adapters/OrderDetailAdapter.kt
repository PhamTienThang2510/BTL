package com.example.btl.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.btl.Model.OrderItem
import com.example.btl.databinding.ItemOrderDetailBinding

class OrderDetailAdapter(
    private var items: List<OrderItem> = emptyList(),
    private var allowReview: Boolean = false,
    private val onReviewClick: (OrderItem) -> Unit
) : RecyclerView.Adapter<OrderDetailAdapter.OrderItemViewHolder>() {

    inner class OrderItemViewHolder(private val binding: ItemOrderDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OrderItem) {
            binding.apply {
                val productName = item.product_name?.takeIf { it.isNotBlank() }
                    ?: item.productVariants?.product?.product_name?.takeIf { it.isNotBlank() }
                val name = productName ?: "Sản phẩm #${item.variant_id}"
                tvOrderItemName.text = name
                tvVariantId.text = "Biến thể #${item.variant_id}"
                tvQuantity.text = "Số lượng: ${item.quantity}"
                tvUnitPrice.text = "Giá: ${String.format("%,.0f", item.unitPrice)} VND"
                tvItemTotal.text = "Tổng: ${String.format("%,.0f", item.unitPrice * item.quantity)} VND"

                val imageUrl = item.image_url?.takeIf { it.isNotBlank() }
                    ?: item.productVariants?.image_url
                if (!imageUrl.isNullOrBlank()) {
                    val fullUrl = when {
                        imageUrl.startsWith("http") -> imageUrl
                        imageUrl.startsWith("/uploads/") -> "http://10.0.2.2:3000$imageUrl"
                        else -> "http://10.0.2.2:3000/uploads/$imageUrl"
                    }
                    Glide.with(itemView)
                        .load(fullUrl)
                        .into(ivOrderItemImage)
                } else {
                    ivOrderItemImage.setImageResource(com.example.btl.R.drawable.ic_image_placeholder)
                }

                val hasProductId = item.product_id != null || item.productVariants?.product_id != null
                buttonReviewItem.isEnabled = allowReview && hasProductId
                buttonReviewItem.alpha = if (buttonReviewItem.isEnabled) 1.0f else 0.5f
                buttonReviewItem.setOnClickListener {
                    onReviewClick(item)
                }

                if (item.created_at != null) {
                    tvCreatedAt.text = "Thêm: ${formatDate(item.created_at)}"
                } else {
                    tvCreatedAt.text = ""
                }
            }
        }

        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                val date = inputFormat.parse(dateString)
                outputFormat.format(date ?: return dateString)
            } catch (e: Exception) {
                dateString
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val binding = ItemOrderDetailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<OrderItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun setReviewEnabled(enabled: Boolean) {
        if (allowReview == enabled) return
        allowReview = enabled
        notifyDataSetChanged()
    }
}
