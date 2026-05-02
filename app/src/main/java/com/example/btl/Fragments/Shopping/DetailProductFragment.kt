package com.example.btl.Fragments.Shopping

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.btl.DataStore.TokenManager
import com.example.btl.Model.Product
import com.example.btl.Model.ProductVariant
import com.example.btl.Model.Review
import com.example.btl.R
import com.example.btl.Repository.MockupRepository
import com.example.btl.Repository.UserRepository
import com.example.btl.ViewModel.DetailProductViewModel
import com.example.btl.databinding.FragmentDetailProductBinding
import kotlinx.coroutines.launch
import java.util.Locale

class DetailProductFragment : Fragment() {
    private lateinit var binding: FragmentDetailProductBinding
    private lateinit var viewModel: DetailProductViewModel
    private lateinit var tokenManager: TokenManager
    private lateinit var userRepository: UserRepository
    private lateinit var mockupRepository: MockupRepository
    private val reviewRepository = com.example.btl.Repository.ReviewRepository()

    private var productId: Int = 0
    private var productName: String = ""
    private var productDescription: String = ""
    private var isDescriptionExpanded = false
    private var showAllReviews = false
    private var cachedReviews: List<Review> = emptyList()

    companion object {
        private const val IMAGE_BASE_URL = "https://giang-backend-fdfkdfb4fycff2hs.malaysiawest-01.azurewebsites.net"
        private const val ARG_PRODUCT_ID = "product_id"
        private const val ARG_PRODUCT_NAME = "product_name"
        private const val ARG_PRODUCT_DESCRIPTION = "product_description"
        private const val TAG = "DetailProductFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            productId = it.getInt(ARG_PRODUCT_ID, 0)
            productName = it.getString(ARG_PRODUCT_NAME, "") ?: ""
            productDescription = it.getString(ARG_PRODUCT_DESCRIPTION, "") ?: ""
        }

        tokenManager = TokenManager(requireContext())
        userRepository = UserRepository(requireContext())
        mockupRepository = MockupRepository(requireContext())

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return DetailProductViewModel(productId) as T
            }
        }

        viewModel = ViewModelProvider(this, factory)[DetailProductViewModel::class.java]

        viewLifecycleOwner.lifecycleScope.launch {
            userRepository.getCurrentUser().onSuccess { user ->
                Log.d(TAG, "Fetched user: ${user.user_id}")
                viewModel.init(user.user_id)
            }.onFailure {
                Log.e(TAG, "Failed to fetch user")
                viewModel.init(null)
            }
        }

        setupUI()
        setupClickListeners()
        setupObservers()
        loadProductDetails()
        loadReviews()
    }

    private fun setupUI() {
        binding.apply {
            tvProductName.text = productName
            tvProductPrice.text = "Đang tải..."
            tvProductDescription.text = productDescription

            tvReadMore.visibility = if (productDescription.length > 100) View.VISIBLE else View.GONE

            rvColors.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            val rvSizes = root.findViewById<RecyclerView>(R.id.rv_sizes)
            rvSizes?.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            rvReviews.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupClickListeners() {
        binding.imageClose.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.buttonAddToCart.setOnClickListener {
            handleAddToCart()
        }

        binding.buttonCreateMockup.setOnClickListener {
            handleCreateMockup()
        }

        binding.tvReadMore.setOnClickListener {
            isDescriptionExpanded = !isDescriptionExpanded
            if (isDescriptionExpanded) {
                binding.tvProductDescription.maxLines = Integer.MAX_VALUE
                binding.tvReadMore.text = "Thu gọn"
            } else {
                binding.tvProductDescription.maxLines = 3
                binding.tvReadMore.text = "Xem thêm"
            }
        }

        binding.buttonSeeMoreReviews.setOnClickListener {
            showAllReviews = !showAllReviews
            renderReviews()
        }
    }

    private fun handleAddToCart() {
        val selectedColor = viewModel.selectedColor.value
        val selectedSize = viewModel.selectedSize.value

        if (selectedColor.isBlank() || selectedSize.isBlank()) {
            Toast.makeText(requireContext(), "Vui lòng chọn màu và kích cỡ", Toast.LENGTH_SHORT).show()
            return
        }

        // Tìm biến thể tương ứng
        val selectedVariant = viewModel.product.value?.variants?.find { 
            it.color.equals(selectedColor, ignoreCase = true) && it.size.equals(selectedSize, ignoreCase = true)
        }

        if (selectedVariant != null) {
            // ✅ KIỂM TRA TỒN KHO
            if (selectedVariant.stock_quantity <= 0) {
                Toast.makeText(requireContext(), "Rất tiếc, sản phẩm này đã hết hàng", Toast.LENGTH_LONG).show()
                return
            }

            viewLifecycleOwner.lifecycleScope.launch {
                val token = tokenManager.getToken()
                if (token != null) {
                    viewModel.addToCart(selectedVariant.variant_id, 1, selectedColor, selectedSize, token)
                }
            }
        } else {
            Toast.makeText(requireContext(), "Biến thể không tồn tại", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleCreateMockup() {
        val selectedVariant = findSelectedVariant()
        if (selectedVariant == null) {
            Toast.makeText(requireContext(), "Vui lòng chọn màu và kích cỡ", Toast.LENGTH_SHORT).show()
            return
        }

        binding.buttonCreateMockup.isEnabled = false
        binding.buttonCreateMockup.alpha = 0.5f

        viewLifecycleOwner.lifecycleScope.launch {
            mockupRepository.fetchTemplates(selectedVariant.variant_id, activeOnly = true)
                .onSuccess { templates ->
                    if (templates.isEmpty()) {
                        Toast.makeText(requireContext(), "Không có mẫu mockup", Toast.LENGTH_SHORT).show()
                        binding.buttonCreateMockup.isEnabled = true
                        binding.buttonCreateMockup.alpha = 1.0f
                        return@onSuccess
                    }

                    val selectedTemplate = templates.first()
                    val bundle = Bundle().apply {
                        putInt("product_id", productId)
                        putInt("variant_id", selectedVariant.variant_id)
                        putInt("template_id", selectedTemplate.template_id)
                        putString("template_name", selectedTemplate.name)
                        putString("product_name", productName)
                    }

                    findNavController().navigate(R.id.productMockupFragment, bundle)
                }
                .onFailure { error ->
                    Log.e(TAG, "loadTemplates: ${error.message}", error)
                    Toast.makeText(requireContext(), "Không tải được mẫu mockup", Toast.LENGTH_SHORT).show()
                    binding.buttonCreateMockup.isEnabled = true
                    binding.buttonCreateMockup.alpha = 1.0f
                }
        }
    }

    private fun findSelectedVariant(): ProductVariant? {
        val selectedColor = viewModel.selectedColor.value
        val selectedSize = viewModel.selectedSize.value
        if (selectedColor.isBlank() || selectedSize.isBlank()) return null

        return viewModel.product.value?.variants?.find {
            it.color.equals(selectedColor, ignoreCase = true) &&
                it.size.equals(selectedSize, ignoreCase = true)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.product.collect { product ->
                if (product != null) {
                    val price = product.getPrimaryPrice()
                    binding.tvProductPrice.text = String.format(Locale("vi", "VN"), "%,d VND", price.toInt())
                    binding.tvProductDescription.text = product.description
                    binding.tvReadMore.visibility = if (product.description.length > 100) View.VISIBLE else View.GONE
                    setupImageGallery(product)
                }
            }
        }

        // Theo dõi sự thay đổi màu sắc/kích cỡ để cập nhật trạng thái nút "Thêm vào giỏ hàng"
        viewLifecycleOwner.lifecycleScope.launch {
            kotlinx.coroutines.flow.combine(viewModel.selectedColor, viewModel.selectedSize) { color, size ->
                val variant = viewModel.product.value?.variants?.find { 
                    it.color.equals(color, ignoreCase = true) && it.size.equals(size, ignoreCase = true)
                }
                variant
            }.collect { variant ->
                if (variant != null) {
                    binding.buttonCreateMockup.isEnabled = true
                    binding.buttonCreateMockup.alpha = 1.0f
                } else {
                    binding.buttonCreateMockup.isEnabled = false
                    binding.buttonCreateMockup.alpha = 0.5f
                }

                if (variant != null) {
                    if (variant.stock_quantity <= 0) {
                        binding.buttonAddToCart.text = "Hết hàng"
                        binding.buttonAddToCart.isEnabled = false
                        binding.buttonAddToCart.alpha = 0.5f
                    } else {
                        binding.buttonAddToCart.text = "Thêm vào giỏ hàng"
                        binding.buttonAddToCart.isEnabled = true
                        binding.buttonAddToCart.alpha = 1.0f
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.colors.collect { colors ->
                if (colors.isNotEmpty()) {
                    binding.rvColors.adapter = VariantAdapter(colors) { viewModel.setSelectedColor(it) }
                    if (viewModel.selectedColor.value.isBlank()) viewModel.setSelectedColor(colors.first())
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.sizes.collect { sizes ->
                if (sizes.isNotEmpty()) {
                    val rvSizes = binding.root.findViewById<RecyclerView>(R.id.rv_sizes)
                    rvSizes?.adapter = VariantAdapter(sizes) { viewModel.setSelectedSize(it) }
                    if (viewModel.selectedSize.value.isBlank()) viewModel.setSelectedSize(sizes.first())
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.addToCartSuccess.collect { isSuccess ->
                if (isSuccess) {
                    Toast.makeText(requireContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                    viewModel.resetAddToCartSuccess()
                }
            }
        }
    }

    private fun setupImageGallery(product: Product) {
        val imageUrls = product.variants.mapNotNull { it.image_url }.distinct().ifEmpty { product.media.map { it.media_url } }
        if (imageUrls.isNotEmpty()) {
            binding.viewPagerProductImages.adapter = ImagePagerAdapter(imageUrls, this)
        }
    }

    private fun loadProductDetails() {
        viewLifecycleOwner.lifecycleScope.launch {
            tokenManager.getToken()?.let { viewModel.loadProductDetails(it) }
        }
    }

    private fun loadReviews() {
        viewLifecycleOwner.lifecycleScope.launch {
            reviewRepository.getReviewsByProduct(productId).onSuccess { reviews ->
                cachedReviews = reviews
                showAllReviews = false

                val average = if (reviews.isNotEmpty()) {
                    reviews.mapNotNull { it.rating }.average()
                } else {
                    0.0
                }

                binding.ratingSummary.rating = average.toFloat()
                binding.tvReviewCount.text = "(${reviews.size} đánh giá)"
                binding.tvNoReviews.visibility = if (reviews.isEmpty()) View.VISIBLE else View.GONE
                renderReviews()
            }.onFailure { exception ->
                Log.e(TAG, "loadReviews: ${exception.message}", exception)
                cachedReviews = emptyList()
                showAllReviews = false
                binding.ratingSummary.rating = 0f
                binding.tvReviewCount.text = "(0 đánh giá)"
                binding.tvNoReviews.visibility = View.VISIBLE
                binding.rvReviews.adapter = com.example.btl.Adapters.ReviewAdapter(emptyList())
                binding.buttonSeeMoreReviews.visibility = View.GONE
            }
        }
    }

    private fun renderReviews() {
        val reviews = if (showAllReviews) {
            cachedReviews
        } else {
            cachedReviews.take(2)
        }

        binding.rvReviews.adapter = com.example.btl.Adapters.ReviewAdapter(reviews)

        if (cachedReviews.size > 2) {
            binding.buttonSeeMoreReviews.visibility = View.VISIBLE
            binding.buttonSeeMoreReviews.text = if (showAllReviews) "Thu gọn" else "Xem thêm"
        } else {
            binding.buttonSeeMoreReviews.visibility = View.GONE
        }
    }

    private class ImagePagerAdapter(val urls: List<String>, fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = urls.size
        override fun createFragment(position: Int): Fragment = ImageFragment.newInstance(urls[position])
    }

    private class VariantAdapter(val items: List<String>, val onClick: (String) -> Unit) : RecyclerView.Adapter<VariantAdapter.ViewHolder>() {
        private var selectedPos = 0
        
        class ViewHolder(val tv: TextView) : RecyclerView.ViewHolder(tv)
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val textView = TextView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            return ViewHolder(textView)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.tv.apply {
                text = items[position]
                setPadding(32, 16, 32, 16)
                
                if (selectedPos == holder.adapterPosition) {
                    setTextColor(android.graphics.Color.WHITE)
                    setBackgroundColor(android.graphics.Color.BLACK)
                } else {
                    setTextColor(android.graphics.Color.BLACK)
                    setBackgroundColor(android.graphics.Color.LTGRAY)
                }
                
                setOnClickListener {
                    val old = selectedPos
                    selectedPos = holder.adapterPosition
                    notifyItemChanged(old)
                    notifyItemChanged(selectedPos)
                    onClick(items[selectedPos])
                }
            }
        }
        override fun getItemCount(): Int = items.size
    }
}

class ImageFragment : Fragment() {
    companion object {
        private const val ARG_IMAGE_URL = "image_url"
        private const val IMAGE_BASE_URL = "https://giang-backend-fdfkdfb4fycff2hs.malaysiawest-01.azurewebsites.net"
        fun newInstance(imageUrl: String): ImageFragment {
            return ImageFragment().apply {
                arguments = Bundle().apply { putString(ARG_IMAGE_URL, imageUrl) }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val imageUrl = arguments?.getString(ARG_IMAGE_URL, "") ?: ""
        return ImageView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            scaleType = ImageView.ScaleType.CENTER_CROP
            
            val finalImageUrl = when {
                imageUrl.contains("res.cloudinary.com") -> imageUrl
                imageUrl.contains("localhost") || imageUrl.contains("10.0.2.2") -> imageUrl
                    .replace("http://localhost:3000", IMAGE_BASE_URL)
                    .replace("https://localhost:3000", IMAGE_BASE_URL)
                    .replace("http://10.0.2.2:3000", IMAGE_BASE_URL)
                imageUrl.startsWith("/uploads/") -> "$IMAGE_BASE_URL$imageUrl"
                imageUrl.startsWith("http") -> imageUrl
                else -> "$IMAGE_BASE_URL/uploads/$imageUrl"
            }

            Glide.with(this@ImageFragment)
                .load(finalImageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(this)
        }
    }
}
