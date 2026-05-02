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
import com.bumptech.glide.Glide
import com.example.btl.DataStore.DataStoreManager
import com.example.btl.Repository.CartRepository
import com.example.btl.Repository.MockupRepository
import com.example.btl.Repository.UserRepository
import com.example.btl.R
import com.example.btl.databinding.FragmentProductMockupBinding
import kotlinx.coroutines.launch

class ProductMockupFragment : Fragment() {
    private lateinit var binding: FragmentProductMockupBinding
    private val TAG = "ProductMockupFragment"
    private lateinit var mockupRepository: MockupRepository
    private lateinit var cartRepository: CartRepository
    private lateinit var userRepository: UserRepository
    private var lastRenderId: Int? = null
    private var hasRendered = false

    // Template ID for mockup (received from arguments)
    private var selectedTemplateId: Int = 0
    private var templateName: String = "Default Template"
    private var selectedVariantId: Int = 0
    private var selectedProductId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductMockupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Initializing ProductMockupFragment")

        // Get template from arguments if provided
        selectedTemplateId = getPositiveIntArg(arguments, "template_id", 0)
        templateName = arguments?.getString("template_name") ?: getString(R.string.mockup_template_default_name)
        selectedVariantId = getPositiveIntArg(arguments, "variant_id", 0)
        selectedProductId = getPositiveIntArg(arguments, "product_id", 0)
        Log.d(
            TAG,
            "[INPUT] Args: templateId=$selectedTemplateId, templateName=$templateName, variantId=$selectedVariantId, productId=$selectedProductId"
        )

        // Update template name display
        binding.tvTemplateName.text = getString(R.string.mockup_template_format, templateName)

        // Initialize repository
        mockupRepository = MockupRepository(requireContext())
        cartRepository = CartRepository()
        userRepository = UserRepository(requireContext())
        displayInputReferenceImage(null)

        setupClickListeners()
    }

    private fun getPositiveIntArg(bundle: Bundle?, key: String, fallback: Int): Int {
        if (bundle == null || !bundle.containsKey(key)) return fallback
        return bundle.getInt(key, fallback).takeIf { it > 0 } ?: fallback
    }

    private fun setupClickListeners() {
        // Close button
        binding.imageCloseProductMockup.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.buttonCreateMockup.setOnClickListener {
            handlePrimaryAction()
        }
    }

    private fun handlePrimaryAction() {
        if (hasRendered) {
            addRenderedMockupToCart()
        } else {
            uploadAndRenderMockup()
        }
    }

     /**
      * Upload image to API and get rendered mockup back
      */
     private fun uploadAndRenderMockup() {
         Log.d(TAG, "[INPUT] Starting uploadAndRenderMockup")

          if (selectedTemplateId <= 0) {
              Toast.makeText(requireContext(), "Vui lòng chọn template", Toast.LENGTH_SHORT).show()
              return
          }

          if (selectedVariantId <= 0) {
              Toast.makeText(requireContext(), "Thiếu mã biến thể", Toast.LENGTH_SHORT).show()
             return
         }

        val designImageUrl = binding.etDesignImageUrl.text?.toString()?.trim().orEmpty()
        if (designImageUrl.isBlank()) {
            Toast.makeText(requireContext(), "Vui lòng nhập URL ảnh thiết kế", Toast.LENGTH_SHORT).show()
            return
        }

        displayInputReferenceImage(designImageUrl)

         // Show loading progress
         binding.progressBar.visibility = View.VISIBLE
         binding.buttonCreateMockup.isEnabled = false
         Toast.makeText(requireContext(), getString(R.string.mockup_uploading), Toast.LENGTH_SHORT).show()

         // Launch coroutine to upload
         lifecycleScope.launch {
             try {
                 Log.d(
                     TAG,
                     "[INPUT] Render mode=direct-template, designImageUrl=${designImageUrl.take(120)}"
                 )

                  val mockupResponse = mockupRepository.createMockupForSelectedTemplate(
                      variantId = selectedVariantId,
                      templateId = selectedTemplateId,
                      productId = selectedProductId.takeIf { it > 0 },
                      designImageUrl = designImageUrl
                  )

                 binding.progressBar.visibility = View.GONE
                 binding.buttonCreateMockup.isEnabled = true

                 if (mockupResponse != null) {
                     Log.d(TAG, "[OUTPUT] Mockup render successful")
                     Log.d(TAG, "[OUTPUT] renderId=${mockupResponse.render_id}")
                     Log.d(TAG, "[OUTPUT] status=${mockupResponse.status}")
                     Log.d(TAG, "[OUTPUT] templateId=${mockupResponse.template_id}")
                     Log.d(TAG, "[OUTPUT] designImage=${mockupResponse.design_image_url}")
                     Log.d(TAG, "[OUTPUT] renderedImage=${mockupResponse.rendered_image_url}")
                     Log.d(TAG, "[OUTPUT] errorMessage=${mockupResponse.error_message}")

                     // Display the rendered image
                     displayRenderedMockup(mockupResponse.rendered_image_url)
                     setRenderedState(mockupResponse.render_id)

                     Toast.makeText(
                         requireContext(),
                         getString(R.string.mockup_render_success),
                         Toast.LENGTH_SHORT
                     ).show()
                 } else {
                     Log.e(
                         TAG,
                         "[ERROR] Mockup rendering failed (null response). templateId=$selectedTemplateId, variantId=$selectedVariantId, productId=$selectedProductId"
                     )
                     Toast.makeText(
                         requireContext(),
                         getString(R.string.mockup_render_failed),
                         Toast.LENGTH_SHORT
                     ).show()
                 }
             } catch (e: Exception) {
                 Log.e(
                     TAG,
                     "[ERROR] Exception during uploadAndRenderMockup. templateId=$selectedTemplateId, variantId=$selectedVariantId, productId=$selectedProductId",
                     e
                 )
                 binding.progressBar.visibility = View.GONE
                 binding.buttonCreateMockup.isEnabled = true
                 Toast.makeText(
                     requireContext(),
                     getString(R.string.error, e.message ?: ""),
                     Toast.LENGTH_SHORT
                 ).show()
             }
         }
     }

    private fun setRenderedState(renderId: Int?) {
        lastRenderId = renderId
        hasRendered = renderId != null
        binding.buttonCreateMockup.text = if (hasRendered) "Thêm vào giỏ hàng" else getString(R.string.create_mockup)
    }

    private fun addRenderedMockupToCart() {
        val renderId = lastRenderId
        if (selectedVariantId <= 0) {
            Toast.makeText(requireContext(), "Thiếu mã biến thể", Toast.LENGTH_SHORT).show()
            return
        }
        if (renderId == null) {
            Toast.makeText(requireContext(), "Chưa có ảnh mockup", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.buttonCreateMockup.isEnabled = false

        lifecycleScope.launch {
            try {
                val token = DataStoreManager.getToken(requireContext())
                if (token.isNullOrBlank()) {
                    binding.progressBar.visibility = View.GONE
                    binding.buttonCreateMockup.isEnabled = true
                    Toast.makeText(requireContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val customerId = userRepository.getCurrentUser().getOrNull()?.user_id
                val result = cartRepository.addToCart(
                    customerId = customerId,
                    variantId = selectedVariantId,
                    token = token,
                    renderId = renderId
                )

                binding.progressBar.visibility = View.GONE
                binding.buttonCreateMockup.isEnabled = true

                if (result.isSuccess) {
                    Toast.makeText(requireContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Thêm vào giỏ hàng thất bại", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "[ERROR] addRenderedMockupToCart failed", e)
                binding.progressBar.visibility = View.GONE
                binding.buttonCreateMockup.isEnabled = true
                Toast.makeText(requireContext(), "Thêm vào giỏ hàng thất bại", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Display the rendered mockup image from the API on right side
     */
    private fun displayRenderedMockup(renderedImageUrl: String?) {
        if (renderedImageUrl.isNullOrEmpty()) {
            Log.w(TAG, "[ERROR] No rendered image URL provided")
            return
        }
        
        Log.d(TAG, "[OUTPUT] Loading rendered mockup image: ${renderedImageUrl.take(120)}")
        
        // Convert to full URL if needed
        val fullImageUrl = when {
            renderedImageUrl.startsWith("http") -> renderedImageUrl
            renderedImageUrl.startsWith("/uploads/") -> "http://10.0.2.2:3000$renderedImageUrl"
            else -> "http://10.0.2.2:3000/uploads/$renderedImageUrl"
        }
        
        Log.d(TAG, "[OUTPUT] Final preview URL: ${fullImageUrl.take(120)}")
        
        // Use Glide to load and display the rendered image (right side)
        Glide.with(this)
            .load(fullImageUrl)
            .into(binding.ivProductPreview)
        
        binding.ivProductPreview.visibility = View.VISIBLE
        binding.tvRenderPlaceholder.visibility = View.GONE
        
        Log.d(TAG, "Rendered mockup image displayed on right side")
    }
    
    private fun displayInputReferenceImage(imageUrl: String?) {
        val finalUrl = imageUrl?.takeIf { it.isNotBlank() }
        if (finalUrl == null) {
            binding.ivUploadedImage.visibility = View.GONE
            binding.tvUploadPlaceholder.visibility = View.VISIBLE
            return
        }

        Log.d(TAG, "[INPUT] Display input image URL: ${finalUrl.take(120)}")
        Glide.with(this)
            .load(finalUrl)
            .into(binding.ivUploadedImage)
        binding.ivUploadedImage.visibility = View.VISIBLE
        binding.tvUploadPlaceholder.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: Cleaning up ProductMockupFragment")
    }
}
