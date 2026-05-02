package com.example.btl.Adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.btl.Model.CartItem
import com.example.btl.R
import com.example.btl.databinding.BillingProductsRvItemBinding

class BillingProductAdapter(
    private var cartItems: List<CartItem> = emptyList()
) : RecyclerView.Adapter<BillingProductAdapter.BillingProductViewHolder>() {

    companion object {
        private const val TAG = "BillingProductAdapter"
    }

    inner class BillingProductViewHolder(private val binding: BillingProductsRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItem) {
            binding.apply {
                // Use helper methods to support both old and new formats
                val productName = cartItem.getProductName()
                val price = cartItem.getPrice()
                val imageUrl = cartItem.getImageUrl()

                tvProductCartName.text = productName
                tvProductCartPrice.text = "${String.format("%,d", price.toInt())} VND"
                tvBillingProductQuantity.text = "x${cartItem.quantity}"

                Log.d(TAG, "Binding billing product: $productName, quantity=${cartItem.quantity}")

                // Set color circle
                try {
                    val colorValue = cartItem.getColorValue()
                    val colorInt = getColorFromName(colorValue)
                    imageCartProductColor.setImageDrawable(
                        android.graphics.drawable.ColorDrawable(colorInt)
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting color: ${e.message}")
                }

                // Load product image
                val finalImageUrl = loadImageUrl(imageUrl)
                Log.d(TAG, "Loading billing image: $finalImageUrl")

                if (finalImageUrl.isNotEmpty()) {
                    Glide.with(itemView.context)
                        .load(finalImageUrl)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(imageCartProduct)
                }

                // Set size if available
                val sizeValue = cartItem.getSizeValue()
                tvCartProductSize.text = sizeValue
            }
        }

        private fun loadImageUrl(originalUrl: String): String {
            return when {
                originalUrl.isEmpty() -> ""
                originalUrl.contains("res.cloudinary.com") -> originalUrl
                originalUrl.contains("localhost") -> originalUrl.replace("localhost", "10.0.2.2")
                originalUrl.startsWith("/uploads/") -> "http://10.0.2.2:3000$originalUrl"
                originalUrl.startsWith("http") -> originalUrl
                else -> "http://10.0.2.2:3000/uploads/$originalUrl"
            }
        }

        private fun getColorFromName(colorName: String): Int {
            return when (colorName.lowercase()) {
                "đen", "black" -> 0xFF000000.toInt()
                "trắng", "white" -> 0xFFFFFFFF.toInt()
                "xám", "gray", "grey" -> 0xFF808080.toInt()
                "đỏ", "red" -> 0xFFFF0000.toInt()
                "xanh", "blue" -> 0xFF0000FF.toInt()
                "vàng", "yellow" -> 0xFFFFFF00.toInt()
                "xanh lá", "green" -> 0xFF00FF00.toInt()
                else -> 0xFF0000FF.toInt() // Default blue
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillingProductViewHolder {
        val binding = BillingProductsRvItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BillingProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BillingProductViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateProducts(newItems: List<CartItem>) {
        Log.d(TAG, "updateProducts: Updating with ${newItems.size} items")
        cartItems = newItems
        notifyDataSetChanged()
    }
}

