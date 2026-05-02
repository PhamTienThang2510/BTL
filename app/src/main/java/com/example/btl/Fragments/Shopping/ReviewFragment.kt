package com.example.btl.Fragments.Shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.btl.Repository.ReviewRepository
import com.example.btl.databinding.FragmentReviewBinding
import kotlinx.coroutines.launch

class ReviewFragment : Fragment() {
    private lateinit var binding: FragmentReviewBinding
    private val reviewRepository = ReviewRepository()

    private var productId: Int = 0
    private var productName: String = ""
    private var orderItemId: Int = 0
    private var customerId: Int = 0
    private var orderStatus: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        productId = arguments?.getInt("product_id") ?: 0
        productName = arguments?.getString("product_name").orEmpty()
        orderItemId = arguments?.getInt("order_item_id") ?: 0
        customerId = arguments?.getInt("customer_id") ?: 0
        orderStatus = arguments?.getString("order_status").orEmpty()

        binding.tvReviewProductName.text = productName

        binding.buttonSubmitReview.setOnClickListener {
            submitReview()
        }
    }

    private fun submitReview() {
        if (productId <= 0 || orderItemId <= 0 || customerId <= 0) {
            Toast.makeText(requireContext(), "Thiếu thông tin đơn hàng", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isOrderCompleted(orderStatus)) {
            Toast.makeText(requireContext(), "Chỉ đánh giá khi đơn hàng hoàn tất", Toast.LENGTH_SHORT).show()
            return
        }

        val rating = binding.ratingReviewInput.rating.toDouble()
        val comment = binding.etReviewComment.text?.toString()?.trim().orEmpty()

        if (rating <= 0) {
            Toast.makeText(requireContext(), "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show()
            return
        }

        if (comment.isBlank()) {
            Toast.makeText(requireContext(), "Vui lòng nhập nội dung đánh giá", Toast.LENGTH_SHORT).show()
            return
        }

        binding.buttonSubmitReview.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            reviewRepository.submitReview(productId, customerId, orderItemId, rating, comment, null)
                .onSuccess {
                    Toast.makeText(requireContext(), "Đã gửi đánh giá", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                .onFailure {
                    Toast.makeText(requireContext(), "Gửi đánh giá thất bại", Toast.LENGTH_SHORT).show()
                }

            binding.buttonSubmitReview.isEnabled = true
        }
    }

    private fun isOrderCompleted(status: String): Boolean {
        val normalized = status.trim().lowercase()
        return normalized == "complete" || normalized == "completed"
    }
}
