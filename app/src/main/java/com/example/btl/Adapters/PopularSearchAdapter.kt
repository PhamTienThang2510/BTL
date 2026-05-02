package com.example.btl.Adapters

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.btl.Model.Product
import com.example.btl.R
import com.example.btl.databinding.ItemPopularSearchBinding
import java.util.Locale

class PopularSearchAdapter(
    private var products: List<Product>
) : RecyclerView.Adapter<PopularSearchAdapter.PopularSearchViewHolder>() {
    companion object {
        private const val TAG = "PopularSearchAdapter"
    }

    inner class PopularSearchViewHolder(private val binding: ItemPopularSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.apply {
                tvProductName.text = product.name
                
                // Hiển thị giá thay cho text "Hot/Popular"
                val price = product.getPrimaryPrice()
                tvSearchCount.text = String.format(Locale("vi", "VN"), "%,d VND", price.toInt())

                // Load product image
                val imageUrl = product.getPrimaryImageUrl()
                val finalImageUrl = when {
                    imageUrl.contains("res.cloudinary.com") -> imageUrl
                    imageUrl.startsWith("/uploads/") -> "http://10.0.2.2:3000$imageUrl"
                    else -> imageUrl
                }

                Glide.with(itemView.context)
                    .load(finalImageUrl)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivProduct)

                // Click listener to navigate to product detail
                root.setOnClickListener {
                    val activity = itemView.context as? FragmentActivity
                    val navHostFragment = activity?.supportFragmentManager
                        ?.findFragmentById(R.id.nav_host_fragment)
                        as? androidx.navigation.fragment.NavHostFragment

                    val args = Bundle().apply {
                        putInt("product_id", product.product_id)
                        putString("product_name", product.name)
                        putString("product_description", product.description)
                    }

                    try {
                        navHostFragment?.navController?.navigate(R.id.action_search_to_detail_product, args)
                    } catch (e: Exception) {
                        try {
                            navHostFragment?.navController?.navigate(R.id.action_shop_to_detail_product, args)
                        } catch (e2: Exception) {
                            Toast.makeText(itemView.context, "Không thể mở chi tiết sản phẩm", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularSearchViewHolder {
        val binding = ItemPopularSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PopularSearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PopularSearchViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}
