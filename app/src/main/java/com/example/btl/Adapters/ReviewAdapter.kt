package com.example.btl.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.btl.Model.Review
import com.example.btl.databinding.ItemReviewBinding

class ReviewAdapter(
    private val items: List<Review>
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(private val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(review: Review) {
            val displayName = review.customerName?.takeIf { it.isNotBlank() }
                ?: review.userName?.takeIf { it.isNotBlank() }
                ?: "Ẩn danh"

            binding.tvReviewerName.text = displayName
            binding.tvReviewComment.text = review.comment ?: ""
            binding.ratingReview.rating = (review.rating ?: 0.0).toFloat()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}

