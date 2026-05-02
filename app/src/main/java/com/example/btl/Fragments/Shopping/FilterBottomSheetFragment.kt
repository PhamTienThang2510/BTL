package com.example.btl.Fragments.Shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.lifecycle.ViewModelProvider
import com.example.btl.ViewModel.SearchViewModel
import com.example.btl.databinding.FragmentFilterBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.NumberFormat
import java.util.*

class FilterBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentFilterBottomSheetBinding
    private lateinit var searchViewModel: SearchViewModel

    // Track selected filters
    private var minPriceSelected = 0
    private var maxPriceSelected = 10000000 // 10 million VND default max
    private val selectedColorsSet = mutableSetOf<String>()
    private val selectedLocationsSet = mutableSetOf<String>()

    private val priceMultiplier = 10000 // Each seekbar step is 10k VND

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFilterBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel
        searchViewModel = ViewModelProvider(requireActivity()).get(SearchViewModel::class.java)
        
        setupUI()
        loadCurrentFilters()
    }

    private fun setupUI() {
        // Price range seekbar
        binding.priceSeekbar.max = 1000 // 0 to 1000 * 10,000 = 10,000,000 VND
        binding.priceSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                maxPriceSelected = progress * priceMultiplier
                updatePriceText()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Apply price filter
                searchViewModel.setFilterPriceRange(minPriceSelected, maxPriceSelected)
            }
        })

        // Color chips
        setupColorSelection()

        // Location chips
        setupLocationSelection()

        // Apply Filter button
        binding.btnApplyFilter.setOnClickListener {
            applyFiltersAndClose()
        }
    }

    private fun updatePriceText() {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        val minStr = formatter.format(minPriceSelected)
        val maxStr = formatter.format(maxPriceSelected)
        binding.tvPriceRange.text = "$minStr - $maxStr"
    }

    private fun setupColorSelection() {
        binding.colorBlack.setOnClickListener {
            updateColorSet("Black", binding.colorBlack.isChecked)
        }
        binding.colorPurple.setOnClickListener {
            updateColorSet("Purple", binding.colorPurple.isChecked)
        }
        binding.colorBlue.setOnClickListener {
            updateColorSet("Blue", binding.colorBlue.isChecked)
        }
        binding.colorBeige.setOnClickListener {
            updateColorSet("Beige", binding.colorBeige.isChecked)
        }
        binding.colorPink.setOnClickListener {
            updateColorSet("Pink", binding.colorPink.isChecked)
        }
    }

    private fun updateColorSet(color: String, isChecked: Boolean) {
        if (isChecked) {
            selectedColorsSet.add(color)
        } else {
            selectedColorsSet.remove(color)
        }
    }

    private fun setupLocationSelection() {
        binding.btnSanDiego.setOnClickListener {
            updateLocationSet("Hà Nội", binding.btnSanDiego.isChecked)
        }
        binding.btnNewYork.setOnClickListener {
            updateLocationSet("TP.HCM", binding.btnNewYork.isChecked)
        }
        binding.btnAmsterdam.setOnClickListener {
            updateLocationSet("Đà Nẵng", binding.btnAmsterdam.isChecked)
        }
    }

    private fun updateLocationSet(location: String, isChecked: Boolean) {
        if (isChecked) {
            selectedLocationsSet.add(location)
        } else {
            selectedLocationsSet.remove(location)
        }
    }

    private fun loadCurrentFilters() {
        // Load current price range
        searchViewModel.priceRange.observe(viewLifecycleOwner) { priceRange ->
            if (priceRange != null) {
                minPriceSelected = priceRange.first
                maxPriceSelected = priceRange.second
                binding.priceSeekbar.progress = maxPriceSelected / priceMultiplier
                updatePriceText()
            }
        }

        // Load current colors
        searchViewModel.selectedColors.observe(viewLifecycleOwner) { colors ->
            selectedColorsSet.clear()
            selectedColorsSet.addAll(colors)
            // Update UI check state if needed (optional since it's a bottom sheet that re-opens)
            binding.colorBlack.isChecked = "Black" in colors
            binding.colorPurple.isChecked = "Purple" in colors
            binding.colorBlue.isChecked = "Blue" in colors
            binding.colorBeige.isChecked = "Beige" in colors
            binding.colorPink.isChecked = "Pink" in colors
        }

        // Load current locations
        searchViewModel.selectedLocations.observe(viewLifecycleOwner) { locations ->
            selectedLocationsSet.clear()
            selectedLocationsSet.addAll(locations)
            binding.btnSanDiego.isChecked = "Hà Nội" in locations
            binding.btnNewYork.isChecked = "TP.HCM" in locations
            binding.btnAmsterdam.isChecked = "Đà Nẵng" in locations
        }
    }

    private fun applyFiltersAndClose() {
        // Apply filters through ViewModel
        searchViewModel.setFilterColors(selectedColorsSet.toList())
        searchViewModel.setFilterLocations(selectedLocationsSet.toList())
        // Note: price is already set in onStopTrackingTouch or can be set here again for safety
        searchViewModel.setFilterPriceRange(minPriceSelected, maxPriceSelected)

        dismiss()
    }
}
