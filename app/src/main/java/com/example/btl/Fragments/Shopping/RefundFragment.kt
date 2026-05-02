package com.example.btl.Fragments.Shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.btl.Repository.RefundRepository
import com.example.btl.databinding.FragmentRefundBinding
import kotlinx.coroutines.launch

class RefundFragment : Fragment() {
    private lateinit var binding: FragmentRefundBinding
    private lateinit var refundRepository: RefundRepository
    private var orderId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRefundBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refundRepository = RefundRepository(requireContext())
        orderId = arguments?.getInt("order_id") ?: 0

        binding.imageCloseRefund.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.buttonSubmitRefund.setOnClickListener {
            submitRefund()
        }
    }

    private fun submitRefund() {
        val reason = binding.edRefundReason.text?.toString()?.trim().orEmpty()

        if (orderId <= 0) {
            Toast.makeText(requireContext(), "Đơn hàng không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        if (reason.isEmpty()) {
            binding.edRefundReason.error = "Vui lòng nhập lý do hoàn hàng"
            return
        }

        binding.buttonSubmitRefund.isEnabled = false
        binding.progressRefund.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            refundRepository.createRefund(orderId, reason)
                .onSuccess { message ->
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                .onFailure { error ->
                    Toast.makeText(requireContext(), error.message ?: "Gửi yêu cầu thất bại", Toast.LENGTH_SHORT).show()
                }

            binding.buttonSubmitRefund.isEnabled = true
            binding.progressRefund.visibility = View.GONE
        }
    }
}
