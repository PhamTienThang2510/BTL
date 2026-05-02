package com.example.btl.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.btl.databinding.ItemBannerBinding

class BannerAdapter(private val bannerImages: List<Int>) : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    class BannerViewHolder(private val binding: ItemBannerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(imageRes: Int) {
            binding.ivBanner.setImageResource(imageRes)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val binding = ItemBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BannerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        // Vì là infinite loop, ta sử dụng modulo
        val actualPosition = position % bannerImages.size
        holder.bind(bannerImages[actualPosition])
    }

    override fun getItemCount(): Int {
        // Trả về số lượng rất lớn để tạo hiệu ứng infinite scroll
        return Int.MAX_VALUE
    }
}

