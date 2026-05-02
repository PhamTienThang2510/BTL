package com.example.btl.Fragments.Shopping

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.btl.Adapters.MockupTemplateAdapter
import com.example.btl.Model.MockupTemplateModel
import com.example.btl.R
import com.example.btl.Repository.MockupRepository
import com.example.btl.databinding.FragmentMockupTemplateSelectionBinding
import kotlinx.coroutines.launch

class MockupTemplateSelectionFragment : Fragment() {

    private lateinit var binding: FragmentMockupTemplateSelectionBinding
    private lateinit var templateAdapter: MockupTemplateAdapter
    private lateinit var mockupRepository: MockupRepository
    private val TAG = "MockupTemplateSelection"

    private var selectedVariantId: Int = 0
    private var selectedProductId: Int = 0
    private var selectedProductName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMockupTemplateSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Initializing template selection")

        selectedVariantId = arguments?.getInt("variant_id", 0) ?: 0
        selectedProductId = arguments?.getInt("product_id", 0) ?: 0
        selectedProductName = arguments?.getString("product_name", "") ?: ""

        mockupRepository = MockupRepository(requireContext())

        setupRecyclerView()
        loadTemplates()
        setupCloseButton()
    }

    private fun setupRecyclerView() {
        templateAdapter = MockupTemplateAdapter(emptyList()) { selectedTemplate ->
            onTemplateSelected(selectedTemplate)
        }

        binding.rvTemplates.apply {
            adapter = templateAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun loadTemplates() {
        val variantId = selectedVariantId.takeIf { it > 0 }
        Log.d(TAG, "loadTemplates: variantId=$variantId")
        viewLifecycleOwner.lifecycleScope.launch {
            mockupRepository.fetchTemplates(variantId, activeOnly = true)
                .onSuccess { templates ->
                    templateAdapter.updateTemplates(templates)
                    if (templates.isEmpty()) {
                        Toast.makeText(requireContext(), "Không có template phù hợp", Toast.LENGTH_SHORT).show()
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "loadTemplates: ${error.message}", error)
                    Toast.makeText(requireContext(), "Tải template thất bại", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun onTemplateSelected(template: MockupTemplateModel) {
        Log.d(TAG, "onTemplateSelected: Template ${template.template_id} - ${template.name}")

        // Navigate to ProductMockupFragment with selected template
        val bundle = Bundle().apply {
            putInt("template_id", template.template_id)
            putString("template_name", template.name)
            putInt("variant_id", selectedVariantId)
            putInt("product_id", selectedProductId)
            putString("product_name", selectedProductName)
        }

        findNavController().navigate(
            R.id.action_mockupTemplateSelection_to_productMockup,
            bundle
        )
    }

    private fun setupCloseButton() {
        binding.ivClose.setOnClickListener {
            Log.d(TAG, "Close button clicked")
            findNavController().popBackStack()
        }
    }
}
