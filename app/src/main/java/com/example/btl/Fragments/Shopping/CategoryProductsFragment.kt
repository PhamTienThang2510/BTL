package com.example.btl.Fragments.Shopping

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.btl.R
import com.example.btl.Adapters.CategoryTabAdapter
import com.example.btl.Adapters.ProductAdapter
import com.example.btl.DataStore.TokenManager
import com.example.btl.Model.Product
import com.example.btl.Repository.MockupRepository
import com.example.btl.ViewModel.CategoryProductsViewModel
import com.example.btl.ViewModel.ProductViewModel
import com.example.btl.databinding.FragmentCategoryProductsBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class CategoryProductsFragment : Fragment() {
    private lateinit var binding: FragmentCategoryProductsBinding
    private val productViewModel: ProductViewModel by viewModels()
    private lateinit var tokenManager: TokenManager
    private lateinit var mockupRepository: MockupRepository
    private var tabAdapter: CategoryTabAdapter? = null
    private var productAdapter: ProductAdapter? = null

    // For individual category (when created via newInstance)
    private var categoryId: Int = 0
    private var categoryName: String = ""
    private lateinit var categoryViewModel: CategoryProductsViewModel

    companion object {
        private const val ARG_CATEGORY_ID = "category_id"
        private const val ARG_CATEGORY_NAME = "category_name"
        private const val TAG = "CategoryProductsFragment"

        fun newInstance(categoryId: Int, categoryName: String): CategoryProductsFragment {
            Log.d(TAG, "newInstance: categoryId=$categoryId, categoryName=$categoryName")
            return CategoryProductsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_CATEGORY_ID, categoryId)
                    putString(ARG_CATEGORY_NAME, categoryName)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            categoryId = it.getInt(ARG_CATEGORY_ID, 0)  // Default to 0, not 1
            categoryName = it.getString(ARG_CATEGORY_NAME, "")
            Log.d(TAG, "onCreate: categoryId=$categoryId, categoryName=$categoryName")
        }
        Log.d(TAG, "onCreate: Final categoryId=$categoryId (0 means Shop tab, >0 means specific category)")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView called")
        binding = FragmentCategoryProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: categoryId=$categoryId")

        tokenManager = TokenManager(requireContext())
        mockupRepository = MockupRepository(requireContext())

        // Initialize categoryViewModel after categoryId is set from arguments
        if (categoryId > 0) {
            Log.d(TAG, "onViewCreated: Initializing categoryViewModel for categoryId=$categoryId")
            categoryViewModel = ViewModelProvider(
                this,
                CategoryProductsViewModel.Factory(categoryId)
            ).get(CategoryProductsViewModel::class.java)
        }

        // If this is individual category (has categoryId), load its products
        if (categoryId > 0) {
            Log.d(TAG, "onViewCreated: Showing products for category=$categoryName (id=$categoryId)")
            // Hide search, tabs, viewpager - show only products RecyclerView
            binding.tvSearch.visibility = View.GONE
            binding.frameMicrophone.visibility = View.GONE
            binding.tabLayout.visibility = View.GONE
            binding.viewpagerCategoryProducts.visibility = View.GONE
            binding.rvProducts.visibility = View.VISIBLE

            setupProductsRecyclerView()
            setupProductsObserver()
            loadProductsByCategory()
        } else {
            Log.d(TAG, "onViewCreated: Showing all categories in Shop tab")
            // This is Shop tab showing all categories
            binding.rvProducts.visibility = View.GONE
            setupObservers()
            loadCategories()

            // Setup Search box click listener
            setupSearchBoxListener()
        }
    }

    // Setup Search box to navigate to SearchFragment
    private fun setupSearchBoxListener() {
        binding.tvSearch.setOnClickListener {
            Log.d(TAG, "Search box clicked - navigating to SearchFragment")
            try {
                findNavController().navigate(R.id.action_shop_to_search)
            } catch (e: Exception) {
                Log.e(TAG, "Search navigation error: ${e.message}", e)
                Toast.makeText(requireContext(), "Cannot open search", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Setup RecyclerView layout and adapter
    private fun setupProductsRecyclerView() {
        Log.d(TAG, "setupProductsRecyclerView called")
        binding.rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        productAdapter = ProductAdapter(
            products = emptyList(),
            showMockupButton = true,
            onCreateMockupClick = { product -> createMockupForProduct(product) }
        )
        binding.rvProducts.adapter = productAdapter
        Log.d(TAG, "setupProductsRecyclerView: RecyclerView configured")
    }

    private fun createMockupForProduct(product: Product) {
        viewLifecycleOwner.lifecycleScope.launch {
            Toast.makeText(requireContext(), "Dang tao mockup cho ${product.name}...", Toast.LENGTH_SHORT).show()
            val render = mockupRepository.createMockupForProduct(product)
            if (render != null) {
                Toast.makeText(
                    requireContext(),
                    "Da tao mockup thanh cong (Render ID: ${render.render_id})",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(requireContext(), "Tao mockup that bai", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // For individual category - setup RecyclerView for products
    private fun setupProductsObserver() {
        Log.d(TAG, "setupProductsObserver: Starting observer for categoryId=$categoryId")
        viewLifecycleOwner.lifecycleScope.launch {
            Log.d(TAG, "setupProductsObserver: Collecting products from ViewModel")
            categoryViewModel.products.collect { products ->
                Log.d(TAG, "setupProductsObserver: ✅ Received ${products.size} products for categoryId=$categoryId")
                if (products.isEmpty()) {
                    Log.w(TAG, "setupProductsObserver: ⚠️ WARNING - Products list is EMPTY!")
                }
                products.forEach { product ->
                    Log.d(TAG, "setupProductsObserver: Product: name=${product.name}, price=${product.price}, image=${product.image_url}")
                }
                Log.d(TAG, "setupProductsObserver: Calling productAdapter.updateProducts(${products.size})")
                productAdapter?.updateProducts(products)
                Log.d(TAG, "setupProductsObserver: RecyclerView updated")
            }
        }
    }

    // For Shop tab - load all categories and show tabs
    private fun setupObservers() {
        Log.d(TAG, "setupObservers: Setting up category observer")
        viewLifecycleOwner.lifecycleScope.launch {
            productViewModel.categories.collect { categories ->
                Log.d(TAG, "setupObservers: Received ${categories.size} categories")
                categories.forEach { cat ->
                    Log.d(TAG, "Category: id=${cat.category_id}, name=${cat.name}")
                }
                if (categories.isNotEmpty()) {
                    // Setup ViewPager2 with TabLayout
                    tabAdapter = CategoryTabAdapter(this@CategoryProductsFragment, categories)
                    binding.viewpagerCategoryProducts.adapter = tabAdapter

                    // Connect TabLayout with ViewPager2
                    TabLayoutMediator(binding.tabLayout, binding.viewpagerCategoryProducts) { tab, position ->
                        tab.text = categories[position].name
                        Log.d(TAG, "TabLayout: Set tab[$position] = ${categories[position].name}")
                    }.attach()
                }
            }
        }
    }

    private fun loadCategories() {
        Log.d(TAG, "loadCategories: Getting token")
        viewLifecycleOwner.lifecycleScope.launch {
            val token = tokenManager.getToken()
            Log.d(TAG, "loadCategories: token=${token?.take(20)}...")
            if (token != null) {
                Log.d(TAG, "loadCategories: Calling productViewModel.loadCategories(token)")
                productViewModel.loadCategories(token)
            } else {
                Log.e(TAG, "loadCategories: ERROR - Token is null!")
            }
        }
    }

    // For individual category - load its products
    private fun loadProductsByCategory() {
        Log.d(TAG, "loadProductsByCategory: Getting token for categoryId=$categoryId")
        viewLifecycleOwner.lifecycleScope.launch {
            val token = tokenManager.getToken()
            Log.d(TAG, "loadProductsByCategory: token=${token?.take(20)}...")
            if (token != null && categoryId > 0) {
                Log.d(TAG, "loadProductsByCategory: Calling API for categoryId=$categoryId")
                categoryViewModel.loadProductsByCategory(token)
            } else {
                Log.e(TAG, "loadProductsByCategory: ERROR - token=${token != null}, categoryId=$categoryId")
            }
        }
    }
}

