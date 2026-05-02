package com.example.btl.Fragments.Shopping

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.btl.Adapters.PopularSearchAdapter
import com.example.btl.DataStore.TokenManager
import com.example.btl.ViewModel.SearchViewModel
import com.example.btl.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var viewModel: SearchViewModel
    private lateinit var tokenManager: TokenManager
    private lateinit var adapter: PopularSearchAdapter
    private val TAG = "SearchFragment"
    
    // Debounce search queries (prevent too many API calls)
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private val SEARCH_DELAY_MS = 300L  // Wait 300ms after user stops typing

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Cancel pending search operations to avoid memory leaks
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")

        tokenManager = TokenManager(requireContext())
        
        // Initialize ViewModel - use activity scope so filters persist across fragments
        viewModel = ViewModelProvider(requireActivity()).get(SearchViewModel::class.java)
        viewModel.init()

        // Setup UI
        setupUI()

        // Setup observers
        setupObservers()

        // Load popular products
        getTokenAndLoadPopular()
    }

    private fun setupUI() {
        Log.d(TAG, "setupUI called")

        // Setup RecyclerView for popular search
        adapter = PopularSearchAdapter(emptyList())
        binding.rvPopularSearch.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SearchFragment.adapter
        }

        // Setup search input with debouncing to prevent spam
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim() ?: ""
                Log.d(TAG, "Search query: '$query'")

                // Remove pending search requests
                searchRunnable?.let { searchHandler.removeCallbacks(it) }

                if (query.isNotEmpty()) {
                    // Schedule search after delay
                    searchRunnable = Runnable {
                        getTokenAndSearch(query)
                    }
                    searchHandler.postDelayed(searchRunnable!!, SEARCH_DELAY_MS)
                } else {
                    viewModel.clearSearchResults()
                    getTokenAndLoadPopular()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Back button click
        binding.ivBack.setOnClickListener {
            Log.d(TAG, "Back button clicked")
            findNavController().navigateUp()
        }

        // Filter button click
        binding.ivFilter.setOnClickListener {
            Log.d(TAG, "Filter button clicked")
            val filterBottomSheet = FilterBottomSheetFragment()
            filterBottomSheet.show(childFragmentManager, FilterBottomSheetFragment::class.java.simpleName)
        }

        // Clear all button click
        binding.tvClearAll.setOnClickListener {
            Log.d(TAG, "Clear All clicked")
            viewModel.clearLastSearches()
            updateLastSearchChips(emptyList())
        }

        // History chips are configured in updateLastSearchChips()
    }

    private fun setupObservers() {
        Log.d(TAG, "setupObservers called")

        viewModel.searchResults.observe(viewLifecycleOwner) { products ->
            Log.d(TAG, "searchResults updated: ${products.size} products")
            adapter.updateProducts(products)

            if (products.isEmpty() && binding.etSearch.text.toString().isNotEmpty()) {
                Log.d(TAG, "No products found for query")
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.d(TAG, "isLoading: $isLoading")
            binding.apply {
                if (isLoading) {
                    // Could show progress bar here
                } else {
                    // Hide progress bar
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Log.e(TAG, "Error: $error")
                // Only show critical errors to avoid toast spam
                if (error.contains("401") || error.contains("500")) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.lastSearchQueries.observe(viewLifecycleOwner) { queries ->
            Log.d(TAG, "lastSearchQueries updated: ${queries.size} queries")
            updateLastSearchChips(queries)
        }

        viewModel.popularProducts.observe(viewLifecycleOwner) { products ->
            Log.d(TAG, "popularProducts updated: ${products.size} products")
            val query = binding.etSearch.text?.toString()?.trim().orEmpty()
            if (query.isEmpty()) {
                adapter.updateProducts(products)
            }
        }
    }

    private fun updateLastSearchChips(queries: List<String>) {
        Log.d(TAG, "updateLastSearchChips: ${queries.size} queries")

        // Hide chips if no queries
        if (queries.isEmpty()) {
            binding.llChipsRow1.visibility = View.GONE
            binding.llChipsRow2.visibility = View.GONE
            return
        }

        binding.llChipsRow1.visibility = View.VISIBLE

        fun bindHistoryChip(chip: View, query: String) {
            (chip as? com.google.android.material.chip.Chip)?.apply {
                text = query
                visibility = View.VISIBLE
                setOnClickListener {
                    binding.etSearch.setText(query)
                    binding.etSearch.setSelection(query.length)
                }
                setOnLongClickListener {
                    viewModel.removeLastSearch(query)
                    true
                }
            }
        }
        
        // Update first row
        if (queries.isNotEmpty()) {
            bindHistoryChip(binding.chipElectronics, queries[0])
        }

        if (queries.size > 1) {
            bindHistoryChip(binding.chipPants, queries[1])
        } else {
            binding.chipPants.visibility = View.GONE
        }

        // Update second row
        if (queries.size > 2 || queries.size > 3) {
            binding.llChipsRow2.visibility = View.VISIBLE

            if (queries.size > 2) {
                bindHistoryChip(binding.chipThreeSecond, queries[2])
            }

            if (queries.size > 3) {
                bindHistoryChip(binding.chipLongShirt, queries[3])
            }
        } else {
            binding.llChipsRow2.visibility = View.GONE
        }
    }

    private fun getTokenAndSearch(query: String) {
        Log.d(TAG, "getTokenAndSearch: $query")
        
        val token = tokenManager.getToken()
        if (token != null) {
            Log.d(TAG, "Token retrieved, searching...")
            viewModel.searchProducts(query, token)
        } else {
            Log.e(TAG, "No token available")
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getTokenAndLoadPopular() {
        Log.d(TAG, "getTokenAndLoadPopular called")
        
        val token = tokenManager.getToken()
        if (token != null) {
            Log.d(TAG, "Token retrieved, loading popular products...")
            viewModel.loadPopularProducts(token)
        } else {
            Log.e(TAG, "No token available")
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show()
        }
    }
}
