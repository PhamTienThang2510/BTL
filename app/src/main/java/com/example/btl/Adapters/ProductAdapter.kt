package com.example.btl.Adapters

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.btl.Model.Product
import com.example.btl.R
import com.example.btl.databinding.ItemProductBinding

class ProductAdapter(
    private var products: List<Product>,
    private val showMockupButton: Boolean = false,
    private val onCreateMockupClick: ((Product) -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
    companion object {
        private const val TAG = "ProductAdapter"
    }

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.apply {
                tvProductName.text = product.name
                val price = product.getPrimaryPrice()
                tvProductPrice.text = "${String.format("%,d", price.toInt())} VND"
                tvProductDescription.text = product.description

                // Log product info for debugging
                Log.d(TAG, "Binding product: id=${product.product_id}, name=${product.name}")
                Log.d(TAG, "  Media count: ${product.media.size}")
                Log.d(TAG, "  Variants count: ${product.variants.size}")

                // Get primary image URL (handles Cloudinary + local paths)
                val imageUrl = product.getPrimaryImageUrl()
                Log.d(TAG, "  Primary Image URL: ${imageUrl.take(60)}...")

                // Check if image_url is valid
                if (imageUrl.isNullOrEmpty()) {
                    Log.w(TAG, "  ⚠️ WARNING: No image URL found for product ${product.product_id}")
                    ivProductImage.setImageResource(R.drawable.ic_image_placeholder)
                } else {
                    // Handle different URL formats
                    val finalImageUrl = when {
                        // If URL is Cloudinary (https://res.cloudinary.com/...), use as-is
                        imageUrl.contains("res.cloudinary.com") -> {
                            Log.d(TAG, "  ✅ Cloudinary URL found: ${imageUrl.take(50)}...")
                            imageUrl
                        }
                        // If URL contains localhost, point to deployed backend domain
                        imageUrl.contains("localhost") -> {
                            val fullUrl = imageUrl.replace(
                                "http://localhost:3000",
                                "https://giang-backend-fdfkdfb4fycff2hs.malaysiawest-01.azurewebsites.net"
                            ).replace(
                                "https://localhost:3000",
                                "https://giang-backend-fdfkdfb4fycff2hs.malaysiawest-01.azurewebsites.net"
                            )
                            Log.d(TAG, "  🔄 Converting localhost to production: $imageUrl → $fullUrl")
                            fullUrl
                        }
                        // If URL starts with /uploads/, convert to full URL with deployed backend
                        imageUrl.startsWith("/uploads/") -> {
                            val fullUrl = "https://giang-backend-fdfkdfb4fycff2hs.malaysiawest-01.azurewebsites.net$imageUrl"
                            Log.d(TAG, "  🔄 Converting local path to full URL: $imageUrl → $fullUrl")
                            fullUrl
                        }
                        // If URL already starts with http/https, use as-is
                        imageUrl.startsWith("http") -> {
                            Log.d(TAG, "  ✅ Full URL found: ${imageUrl.take(50)}...")
                            imageUrl
                        }
                        // If URL is just filename without /, assume it's in /uploads/
                        else -> {
                            val fullUrl = "https://giang-backend-fdfkdfb4fycff2hs.malaysiawest-01.azurewebsites.net/uploads/$imageUrl"
                            Log.d(TAG, "  🔄 Converting filename to full URL: $imageUrl → $fullUrl")
                            fullUrl
                        }
                    }

                    Log.d(TAG, "  🖼️ Loading image with Glide: $finalImageUrl")
                    Glide.with(itemView.context)
                        .load(finalImageUrl)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(ivProductImage)
                }

                binding.root.setOnClickListener {
                    // Navigate to product detail
                    Log.d(TAG, "Product clicked: ${product.name}")

                    try {
                        // Get the parent Fragment from the itemView's context
                        val activity = itemView.context as? androidx.fragment.app.FragmentActivity
                        if (activity != null) {
                            // Get the primary navigation fragment (current visible fragment)
                            val navHostFragment = activity.supportFragmentManager
                                .findFragmentById(R.id.nav_host_fragment)
                                as? androidx.navigation.fragment.NavHostFragment

                            // Create arguments Bundle with product data
                            val args = Bundle().apply {
                                putInt("product_id", product.product_id)
                                putString("product_name", product.name)
                                putString("product_description", product.description)
                            }

                            // Try both actions with fallback
                            try {
                                // Try home first
                                navHostFragment?.navController?.navigate(
                                    R.id.action_home_to_detail_product,
                                    args
                                )
                                Log.d(TAG, "Navigation triggered (home) to detail product: ${product.name}")
                            } catch (e: Exception) {
                                try {
                                    // Try shop action as fallback
                                    navHostFragment?.navController?.navigate(
                                        R.id.action_shop_to_detail_product,
                                        args
                                    )
                                    Log.d(TAG, "Navigation triggered (shop) to detail product: ${product.name}")
                                } catch (e2: Exception) {
                                    Log.e(TAG, "Both navigation actions failed: ${e2.message}", e2)
                                    Toast.makeText(activity, "Cannot open product details", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Navigation error: ${e.message}", e)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<Product>) {
        Log.d(TAG, "updateProducts: Updating ${newProducts.size} products")
        newProducts.forEachIndexed { index, product ->
            Log.d(TAG, "  Product[$index]: id=${product.product_id}, name=${product.name}, image_url=${product.image_url?.take(50)}...")
        }
        products = newProducts
        notifyDataSetChanged()
        Log.d(TAG, "updateProducts: RecyclerView notified")
    }
}
