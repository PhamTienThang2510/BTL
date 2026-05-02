package com.example.btl.Adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.btl.Model.CartItem
import com.example.btl.R
import com.example.btl.databinding.CartProductItemBinding

class CartAdapter(
    private var cartItems: List<CartItem> = emptyList(),
    private val onQuantityIncrement: (CartItem) -> Unit = {},
    private val onQuantityDecrement: (CartItem) -> Unit = {},
    private val onDeleteClick: (CartItem) -> Unit = {}
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    companion object {
        private const val TAG = "CartAdapter"
        private const val IMAGE_BASE_URL = "https://giang-backend-fdfkdfb4fycff2hs.malaysiawest-01.azurewebsites.net"
    }

    inner class CartViewHolder(private val binding: CartProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItem) {
            binding.apply {
                // ✅ Use helper methods to support both old and new formats
                val productName = cartItem.getProductName()
                val price = cartItem.getPrice()
                val sizeValue = cartItem.getSizeValue()
                val colorValue = cartItem.getColorValue()
                val imageUrl = cartItem.getImageUrl()

                tvProductCartName.text = productName
                tvProductCartPrice.text = "${String.format("%,d", price.toInt())} VND"
                tvCartProductQuantity.text = cartItem.quantity.toString()
                tvCartProductSize.text = sizeValue

                Log.d(TAG, "Binding cart item: $productName, quantity=${cartItem.quantity}")

                // Set color circle
                try {
                    val colorInt = getColorFromName(colorValue)
                    imageCartProductColor.setImageDrawable(
                        android.graphics.drawable.ColorDrawable(colorInt)
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting color: ${e.message}")
                }

                // Load product image
                val finalImageUrl = loadImageUrl(imageUrl)
                Log.d(TAG, "Loading cart image: $finalImageUrl")

                if (finalImageUrl.isNotEmpty()) {
                    Glide.with(itemView.context)
                        .load(finalImageUrl)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(imageCartProduct)
                }

                // Handle quantity buttons
                imagePlus.setOnClickListener {
                    Log.d(TAG, "Plus clicked for ${cartItem.getProductName()}")
                    onQuantityIncrement(cartItem)
                }

                imageMinus.setOnClickListener {
                    Log.d(TAG, "Minus clicked for ${cartItem.getProductName()}")
                    onQuantityDecrement(cartItem)
                }

                // Long press to delete
                binding.root.setOnLongClickListener {
                    Log.d(TAG, "Delete (long press) for ${cartItem.getProductName()}")
                    onDeleteClick(cartItem)
                    true
                }
            }
        }

        private fun loadImageUrl(originalUrl: String): String {
            return when {
                originalUrl.isEmpty() -> ""
                originalUrl.contains("res.cloudinary.com") -> originalUrl
                originalUrl.contains("localhost") -> originalUrl
                    .replace("http://localhost:3000", IMAGE_BASE_URL)
                    .replace("https://localhost:3000", IMAGE_BASE_URL)
                    .replace("localhost:3000", IMAGE_BASE_URL)
                originalUrl.contains("10.0.2.2:3000") -> originalUrl.replace("http://10.0.2.2:3000", IMAGE_BASE_URL)
                originalUrl.startsWith("/uploads/") -> "$IMAGE_BASE_URL$originalUrl"
                originalUrl.startsWith("http") -> originalUrl
                else -> "$IMAGE_BASE_URL/uploads/$originalUrl"
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartProductItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateCartItems(newItems: List<CartItem>) {
        Log.d(TAG, "updateCartItems: Updating with ${newItems.size} items")
        cartItems = newItems
        notifyDataSetChanged()
    }

    fun addItem(item: CartItem) {
        Log.d(TAG, "addItem: Adding ${item.product_name}")
        val newList = cartItems.toMutableList()
        newList.add(item)
        cartItems = newList
        notifyItemInserted(cartItems.size - 1)
    }

    fun removeItem(position: Int) {
        Log.d(TAG, "removeItem: Removing item at position $position")
        val newList = cartItems.toMutableList()
        newList.removeAt(position)
        cartItems = newList
        notifyItemRemoved(position)
    }
}

