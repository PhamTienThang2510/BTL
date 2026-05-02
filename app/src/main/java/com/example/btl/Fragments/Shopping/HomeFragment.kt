package com.example.btl.Fragments.Shopping

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.btl.Adapters.BestDealAdapter
import com.example.btl.Adapters.BannerAdapter
import com.example.btl.Adapters.ProductAdapter
import com.example.btl.DataStore.TokenManager
import com.example.btl.R
import com.example.btl.Repository.ProductRepository
import com.example.btl.Utils.NetworkUtils
import com.example.btl.databinding.FragmentHomeBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var tokenManager: TokenManager
    private var bestDealAdapter: BestDealAdapter? = null
    private var bestSellerAdapter: BestDealAdapter? = null  // NEW: Reuse same adapter for best sellers
    private var hotSellingAdapter: ProductAdapter? = null
    private var bannerAdapter: BannerAdapter? = null
    private var bannerHandler: Handler? = null

    companion object {
        private const val TAG = "HomeFragment"
        private const val BANNER_AUTO_SCROLL_DELAY = 3000L // 3 seconds
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "✅ onViewCreated called")

        try {
            tokenManager = TokenManager(requireContext())
            Log.d(TAG, "✅ TokenManager initialized")
            
            // Setup lightweight UI elements on main thread
            setupBestDealsRecyclerView()
            setupBestSellersRecyclerView()  // NEW
            setupHotSellingRecyclerView()
            setupSearchBox()
            
            Log.d(TAG, "✅ RecyclerViews and search box setup complete")
            
            // FIX: Move banner setup to background thread to prevent ANR
            // Banner carousel setup can be slightly heavy, so we defer it
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    setupBannerCarouselAsync()
                    Log.d(TAG, "✅ Banner carousel setup completed asynchronously")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error setting up banner carousel: ${e.message}", e)
                }
            }
            
            // FIX #1: Replace hardcoded delay with Flow-based token waiting
            // This properly waits for token availability without blocking
            initializeDataLoading()
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error in onViewCreated: ${e.message}", e)
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * FIX #1: Initialize data loading using reactive Flow with validation
     * Properly waits for token and validates it before loading data
     */
    private fun initializeDataLoading() {
        Log.d(TAG, "⏳ Waiting for token using Flow (non-blocking)...")
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Wait for token to be available (non-blocking) - with timeout protection
                val token = withTimeoutOrNull(5000) {  // 5 second timeout
                    tokenManager.getTokenFlow().first()
                }
                
                Log.d(TAG, "📖 Token from Flow: ${if (token != null) token.take(20) + "..." else "NULL"}")
                
                // Validate token: must not be null, empty, or just whitespace
                if (!token.isNullOrEmpty() && token.trim().isNotEmpty()) {
                    Log.d(TAG, "✅ Valid token received: ${token.trim().take(20)}...")
                    loadBestDeals(token.trim())
                } else {
                    Log.e(TAG, "❌ Invalid token - null or empty")
                    if (isAdded) {
                        Toast.makeText(
                            requireContext(),
                            "Authentication failed. Please login again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                when {
                    e.message?.contains("timeout", ignoreCase = true) == true -> {
                        Log.e(TAG, "❌ Token loading timeout after 5 seconds")
                        if (isAdded) {
                            Toast.makeText(
                                requireContext(),
                                "Token loading timeout. Please try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    else -> {
                        Log.e(TAG, "❌ Error waiting for token: ${e.message}", e)
                        e.printStackTrace()
                        if (isAdded) {
                            Toast.makeText(
                                requireContext(),
                                "Error loading data: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun setupSearchBox() {
        Log.d(TAG, "Setting up search box")
        binding.tvSearch.setOnClickListener {
            Log.d(TAG, "Search box clicked - navigating to SearchFragment")
            try {
                findNavController().navigate(R.id.action_home_to_search)
            } catch (e: Exception) {
                Log.e(TAG, "Navigation error: ${e.message}", e)
                Toast.makeText(requireContext(), "Cannot open search", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Setup banner carousel asynchronously to prevent ANR
     * This runs on a background coroutine to avoid blocking UI thread
     */
    private suspend fun setupBannerCarouselAsync() {
        try {
            Log.d(TAG, "🔄 Setting up Banner Carousel (async)")
            
            // Tạo danh sách banner
            val bannerList = listOf(
                R.drawable.banner_test1,
                R.drawable.banner_test2
            )
            
            // Tạo adapter - LAZY CREATE to avoid memory issues
            bannerAdapter = BannerAdapter(bannerList)
            
            // Post back to main thread for UI operations
            binding.vpBanner.post {
                try {
                    binding.vpBanner.adapter = bannerAdapter
                    binding.vpBanner.offscreenPageLimit = 1  // FIX: Reduce memory consumption
                    
                    // Setup transformer (zoom hiệu ứng) - SIMPLIFIED to reduce memory pressure
                    val compositePageTransformer = CompositePageTransformer().apply {
                        addTransformer(MarginPageTransformer(8))
                    }
                    binding.vpBanner.setPageTransformer(compositePageTransformer)
                    
                    // FIX: Skip TabLayoutMediator completely to avoid memory exhaustion
                    // TabLayout with Material styling is too heavy for emulator memory constraints
                    // Instead, hide the dots indicator and rely on auto-scroll for user feedback
                    binding.tlDots.visibility = android.view.View.GONE
                    Log.d(TAG, "⚠️ TabLayout disabled to preserve memory (using auto-scroll instead)")
                    
                    // Auto-scroll banner
                    startBannerAutoScroll()
                    
                    // Listen khi user scroll thì reset timer
                    binding.vpBanner.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            super.onPageSelected(position)
                            resetBannerAutoScroll()
                        }
                    })
                    
                    Log.d(TAG, "✅ Banner Carousel initialized with ${bannerList.size} images")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error setting up banner UI: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error in banner carousel async setup: ${e.message}", e)
        }
    }

    /**
     * Setup banner carousel with auto-scroll
     * Optimized for memory efficiency
     * DEPRECATED: Use setupBannerCarouselAsync instead to prevent ANR
     */
    private fun setupBannerCarousel() {
        try {
            Log.d(TAG, "Setting up Banner Carousel")
            
            // Tạo danh sách banner
            val bannerList = listOf(
                R.drawable.banner_test1,
                R.drawable.banner_test2
            )
            
            // Tạo adapter - LAZY CREATE to avoid memory issues
            bannerAdapter = BannerAdapter(bannerList)
            binding.vpBanner.adapter = bannerAdapter
            binding.vpBanner.offscreenPageLimit = 1  // FIX: Reduce memory consumption by only keeping 1 page
            
             // Setup transformer (zoom hiệu ứng) - SIMPLIFIED to reduce memory pressure
             val compositePageTransformer = CompositePageTransformer().apply {
                 addTransformer(MarginPageTransformer(8))
             }
             binding.vpBanner.setPageTransformer(compositePageTransformer)
             
             // FIX: Skip TabLayoutMediator completely to avoid memory exhaustion
             binding.tlDots.visibility = android.view.View.GONE
             Log.d(TAG, "⚠️ TabLayout disabled to preserve memory")
             
             // Auto-scroll banner
             startBannerAutoScroll()
            
            // Listen khi user scroll thì reset timer
            binding.vpBanner.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    resetBannerAutoScroll()
                }
            })
            
            Log.d(TAG, "✅ Banner Carousel initialized with ${bannerList.size} images")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error setting up banner: ${e.message}", e)
            // Don't crash if banner setup fails
        }
    }

    /**
     * Bắt đầu auto-scroll banner
     */
    private fun startBannerAutoScroll() {
        if (bannerHandler == null) {
            bannerHandler = Handler(Looper.getMainLooper())
        }
        
        bannerHandler?.postDelayed(object : Runnable {
            override fun run() {
                // Kiểm tra xem fragment còn tồn tại không
                if (isAdded && ::binding.isInitialized) {
                    val nextItem = (binding.vpBanner.currentItem + 1) % Int.MAX_VALUE
                    binding.vpBanner.setCurrentItem(nextItem, true)
                    bannerHandler?.postDelayed(this, BANNER_AUTO_SCROLL_DELAY)
                }
            }
        }, BANNER_AUTO_SCROLL_DELAY)
    }

    /**
     * Reset auto-scroll timer khi user tương tác
     */
    private fun resetBannerAutoScroll() {
        bannerHandler?.removeCallbacksAndMessages(null)
        startBannerAutoScroll()
    }

    /**
     * Stop auto-scroll khi fragment bị destroy
     */
    private fun stopBannerAutoScroll() {
        bannerHandler?.removeCallbacksAndMessages(null)
    }


    private fun setupBestDealsRecyclerView() {
        Log.d(TAG, "Setting up Best Deals RecyclerView")
        binding.rvBestDeals.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        bestDealAdapter = BestDealAdapter(emptyList())
        binding.rvBestDeals.adapter = bestDealAdapter
        Log.d(TAG, "✅ Best Deals RecyclerView initialized")
    }

    private fun setupBestSellersRecyclerView() {
        Log.d(TAG, "Setting up Best Sellers RecyclerView")
        binding.rvBestSellers.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        bestSellerAdapter = BestDealAdapter(emptyList())
        binding.rvBestSellers.adapter = bestSellerAdapter
        Log.d(TAG, "✅ Best Sellers RecyclerView initialized")
    }

    private fun setupHotSellingRecyclerView() {
        Log.d(TAG, "Setting up Hot Selling Products RecyclerView")
        binding.rvHotSelling.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        hotSellingAdapter = ProductAdapter(emptyList())
        binding.rvHotSelling.adapter = hotSellingAdapter
        Log.d(TAG, "✅ Hot Selling Products RecyclerView initialized")
    }

    /**
     * Load best deals from API
     * FIX #3: Added network connectivity check
     *
     * @param token JWT token for authentication
     */
    private fun loadBestDeals(token: String) {
        Log.d(TAG, "🔄 loadBestDeals started")
        Log.d(TAG, "🔄 Fragment attached: $isAdded")
        
        // Check if fragment is still attached
        if (!isAdded) {
            Log.w(TAG, "Fragment is not attached, skipping data load")
            return
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // FIX #3: Check network connectivity before making API call
                val isNetworkAvailable = NetworkUtils.isNetworkAvailable(requireContext())
                val networkType = NetworkUtils.getNetworkTypeName(requireContext())
                
                Log.d(TAG, "🌐 Network Check: Available=$isNetworkAvailable, Type=$networkType")
                
                if (!isNetworkAvailable) {
                    Log.e(TAG, "❌ No internet connection")
                    if (isAdded) {
                        Toast.makeText(
                            requireContext(),
                            "No internet connection. Please check your network.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                Log.d(TAG, "✅ Network available, fetching products...")
                Log.d(TAG, "🔄 Calling ProductRepository.getAllProducts()...")
                val repository = ProductRepository()
                val result = repository.getAllProducts(token)

                result.onSuccess { products ->
                    // Check if fragment is still attached before updating UI
                    if (!isAdded) {
                        Log.w(TAG, "Fragment detached, skipping UI update")
                        return@onSuccess
                    }
                    
                    Log.d(TAG, "✅ SUCCESS: Retrieved ${products.size} products")

                    // Display products info
                    products.forEachIndexed { index, product ->
                        Log.d(TAG, "  [$index] ${product.product_id}: ${product.name} - Image: ${product.getPrimaryImageUrl().take(50)}...")
                    }

                    if (products.isEmpty()) {
                        Log.w(TAG, "⚠️ WARNING: No products received from API")
                        Toast.makeText(requireContext(), "No products available", Toast.LENGTH_SHORT).show()
                        return@onSuccess
                    }

                    // Take top 4 products as best deals
                    val bestDeals = products.take(4)
                    Log.d(TAG, "📊 Best Deals: ${bestDeals.size} products")
                    bestDeals.forEachIndexed { index, product ->
                        Log.d(TAG, "  BestDeal[$index]: ${product.name}")
                    }
                    bestDealAdapter?.updateProducts(bestDeals)
                    Log.d(TAG, "✅ Best Deals adapter updated")

                    // Load hot-selling products (skip the best deals, take next batch)
                    val hotSellingProducts = if (products.size > 4) {
                        products.drop(4).take(6)
                    } else {
                        products
                    }
                    Log.d(TAG, "🔥 Hot Selling Products: ${hotSellingProducts.size} products")
                    hotSellingProducts.forEachIndexed { index, product ->
                        Log.d(TAG, "  HotSelling[$index]: ${product.name}")
                    }
                    hotSellingAdapter?.updateProducts(hotSellingProducts)
                    Log.d(TAG, "✅ Hot Selling Products adapter updated")

                    // Load best sellers (products with highest sales/views) - NEW
                    val bestSellers = products
                        .sortedByDescending { (it.salesCount ?: 0) + (it.viewCount ?: 0) / 10 }
                        .take(5)
                    Log.d(TAG, "⭐ Best Sellers: ${bestSellers.size} products")
                    bestSellers.forEachIndexed { index, product ->
                        Log.d(TAG, "  BestSeller[$index]: ${product.name}")
                    }
                    bestSellerAdapter?.updateProducts(bestSellers)
                    Log.d(TAG, "✅ Best Sellers adapter updated")

                    Toast.makeText(
                        requireContext(),
                        "Loaded ${products.size} products",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                result.onFailure { exception ->
                    Log.e(TAG, "❌ FAILURE: ${exception.message}", exception)
                    exception.printStackTrace()
                    if (isAdded) {
                        Toast.makeText(
                            requireContext(),
                            "Failed to load products: ${exception.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ EXCEPTION: ${e.message}", e)
                e.printStackTrace()
                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopBannerAutoScroll()
    }
}



