package com.example.btl.Adapters

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.btl.Model.Product
import com.example.btl.R
import com.example.btl.databinding.BestDealItemBinding

class BestDealAdapter(
    private var products: List<Product>
) : RecyclerView.Adapter<BestDealAdapter.BestDealViewHolder>() {
    companion object {
        private const val TAG = "BestDealAdapter"
    }

    inner class BestDealViewHolder(private val binding: BestDealItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.apply {
                tvDealProductName.text = product.name
                val price = product.getPrimaryPrice()
                val oldPrice = (price * 1.2).toInt()
                tvNewPrice.text = "${String.format("%,d", price.toInt())} VND"
                tvOldPrice.text = "${String.format("%,d", oldPrice)} VND"

                // Log product info
                Log.d(TAG, "Binding BestDeal: id=${product.product_id}, name=${product.name}")
                Log.d(TAG, "  Media count: ${product.media.size}")
                Log.d(TAG, "  Variants count: ${product.variants.size}")

                // Get primary image URL (handles Cloudinary + local paths)
                val imageUrl = product.getPrimaryImageUrl()
                Log.d(TAG, "  Primary Image URL: ${imageUrl.take(60)}...")

                // Check if image_url is valid
                if (imageUrl.isNullOrEmpty()) {
                    Log.w(TAG, "  ⚠️ WARNING: No image URL found for product ${product.product_id}")
                    imgBestDeal.setImageResource(R.drawable.ic_image_placeholder)
                } else {
                    // Handle different URL formats
                    val finalImageUrl = when {
                        // If URL is Cloudinary (https://res.cloudinary.com/...), use as-is
                        imageUrl.contains("res.cloudinary.com") -> {
                            Log.d(TAG, "  ✅ Cloudinary URL found: ${imageUrl.take(50)}...")
                            imageUrl
                        }
                        // If URL contains localhost, convert to Azure backend domain
                        imageUrl.contains("localhost") || imageUrl.contains("10.0.2.2") -> {
                            val fullUrl = imageUrl
                                .replace("http://localhost:3000", "https://giang-backend-fdfkdfb4fycff2hs.malaysiawest-01.azurewebsites.net")
                                .replace("https://localhost:3000", "https://giang-backend-fdfkdfb4fycff2hs.malaysiawest-01.azurewebsites.net")
                                .replace("http://10.0.2.2:3000", "https://giang-backend-fdfkdfb4fycff2hs.malaysiawest-01.azurewebsites.net")
                            Log.d(TAG, "  🔄 Converting local host URL to Azure: $imageUrl → $fullUrl")
                            fullUrl
                        }
                        // If URL starts with /uploads/, convert to full URL with Azure backend
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
                        .into(imgBestDeal)
                }

                binding.root.setOnClickListener {
                    // Navigate to product detail
                    Log.d(TAG, "BestDeal product clicked: ${product.name}")

                    try {
                        val activity = itemView.context as? androidx.fragment.app.FragmentActivity
                        if (activity != null) {
                            val navHostFragment = activity.supportFragmentManager
                                .findFragmentById(R.id.nav_host_fragment)
                                as? androidx.navigation.fragment.NavHostFragment

                            // Create arguments Bundle with product data
                            val args = Bundle().apply {
                                putInt("product_id", product.product_id)
                                putString("product_name", product.name)
                                putString("product_description", product.description)
                            }

                            // Try action_home_to_detail_product first (for HomeFragment), fallback to action_shop_to_detail_product
                            try {
                                navHostFragment?.navController?.navigate(
                                    R.id.action_home_to_detail_product,
                                    args
                                )
                                Log.d(TAG, "Navigation triggered to detail product using action_home_to_detail_product")
                            } catch (e: Exception) {
                                Log.d(TAG, "action_home_to_detail_product not available, trying action_shop_to_detail_product: ${e.message}")
                                try {
                                    navHostFragment?.navController?.navigate(
                                        R.id.action_shop_to_detail_product,
                                        args
                                    )
                                    Log.d(TAG, "Navigation triggered to detail product using action_shop_to_detail_product")
                                } catch (e2: Exception) {
                                    Log.e(TAG, "Both navigation actions failed: ${e2.message}")
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BestDealViewHolder {
        val binding = BestDealItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BestDealViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BestDealViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<Product>) {
        Log.d(TAG, "updateProducts: Updating ${newProducts.size} BestDeal products")
        newProducts.forEachIndexed { index, product ->
            Log.d(TAG, "  BestDeal[$index]: id=${product.product_id}, name=${product.name}, image_url=${product.image_url}")
        }
        products = newProducts
        notifyDataSetChanged()
        Log.d(TAG, "updateProducts: BestDeal RecyclerView notified")
    }
}

