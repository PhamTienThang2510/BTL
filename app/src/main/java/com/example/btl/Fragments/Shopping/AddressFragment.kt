package com.example.btl.Fragments.Shopping

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.btl.Api.RetrofitClient
import com.example.btl.DataStore.TokenManager
import com.example.btl.Model.CreateAddressRequest
import com.example.btl.Model.LocationOption
import com.example.btl.R
import com.example.btl.Repository.LocationRepository
import com.example.btl.databinding.FragmentAddressBinding
import kotlinx.coroutines.launch

class AddressFragment : Fragment() {

    private var _binding: FragmentAddressBinding? = null
    private val binding: FragmentAddressBinding
        get() = _binding!!

    private val tagName = "AddressFragment"
    private val locationRepository = LocationRepository()

    private var bearerToken: String = ""
    private var provinces: List<LocationOption> = emptyList()
    private var districts: List<LocationOption> = emptyList()
    private var wards: List<LocationOption> = emptyList()

    private var selectedProvince: LocationOption? = null
    private var selectedDistrict: LocationOption? = null
    private var selectedWard: LocationOption? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUi()
        setupClickListeners()
        setupLocationSelectors()
        loadLocationData()
    }

    private fun setupUi() {
        // Current flow is create-address only from Billing screen.
        binding.buttonDelelte.visibility = View.GONE
        binding.progressbarAddress.visibility = View.GONE
    }

    private fun setupClickListeners() {
        binding.imageAddressClose.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.buttonSave.setOnClickListener {
            createAddress()
        }
    }

    private fun createAddress() {
        val title = binding.edAddressTitle.text?.toString()?.trim().orEmpty()
        val fullName = binding.edFullName.text?.toString()?.trim().orEmpty()
        val street = binding.edStreet.text?.toString()?.trim().orEmpty()
        val phone = binding.edPhone.text?.toString()?.trim().orEmpty()

        if (fullName.isBlank() || phone.isBlank() || street.isBlank()) {
            Toast.makeText(requireContext(), getString(R.string.address_fill_required), Toast.LENGTH_SHORT).show()
            return
        }

        if (!isLocationValid()) {
            Toast.makeText(requireContext(), getString(R.string.address_select_location_required), Toast.LENGTH_SHORT).show()
            return
        }

        val wardId = selectedWard!!.id
        val detailParts = listOf(
            title,
            street,
            selectedWard?.name,
            selectedDistrict?.name,
            selectedProvince?.name
        ).filter { !it.isNullOrBlank() }
        val addressDetail = detailParts.joinToString(", ")

        lifecycleScope.launch {
            try {
                setLoading(true)

                if (bearerToken.isBlank()) {
                    Toast.makeText(requireContext(), getString(R.string.address_relogin_required), Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val request = CreateAddressRequest(
                    receiver_name = fullName,
                    phone = phone,
                    ward_id = wardId,
                    address_detail = addressDetail,
                    isDefault = false
                )

                val response = RetrofitClient.addressApi.createAddress(request)

                if (response.isSuccessful && response.body() != null) {
                    val newAddress = response.body()!!
                    Log.d(tagName, "createAddress: Success id=${newAddress.address_id}")

                    findNavController().previousBackStackEntry?.savedStateHandle?.set("address_added", true)
                    findNavController().previousBackStackEntry?.savedStateHandle?.set("new_address_id", newAddress.address_id)

                    Toast.makeText(requireContext(), getString(R.string.address_add_success), Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Log.e(tagName, "createAddress: Failed code=${response.code()}")
                    Toast.makeText(requireContext(), getString(R.string.address_add_failed), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(tagName, "createAddress: Exception ${e.message}", e)
                Toast.makeText(requireContext(), getString(R.string.error, e.message ?: ""), Toast.LENGTH_SHORT).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressbarAddress.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonSave.isEnabled = !isLoading
    }

    private fun setupLocationSelectors() {
        configureSelector(binding.edCity)
        configureSelector(binding.edState)
        configureSelector(binding.edWard)

        binding.edCity.setOnItemClickListener { _, _, position, _ ->
            selectedProvince = provinces.getOrNull(position)

            selectedDistrict = null
            selectedWard = null
            districts = emptyList()
            wards = emptyList()
            binding.edState.setText("", false)
            binding.edWard.setText("", false)

            selectedProvince?.let { loadDistricts(it.id) }
        }

        binding.edState.setOnClickListener {
            if (selectedProvince == null) {
                Toast.makeText(requireContext(), getString(R.string.address_select_province_first), Toast.LENGTH_SHORT).show()
                binding.edCity.requestFocus()
                binding.edCity.showDropDown()
            }
        }

        binding.edState.setOnItemClickListener { _, _, position, _ ->
            selectedDistrict = districts.getOrNull(position)

            selectedWard = null
            wards = emptyList()
            binding.edWard.setText("", false)

            selectedDistrict?.let { loadWards(it.id) }
        }

        binding.edWard.setOnClickListener {
            if (selectedDistrict == null) {
                Toast.makeText(requireContext(), getString(R.string.address_select_district_first), Toast.LENGTH_SHORT).show()
                binding.edState.requestFocus()
                binding.edState.showDropDown()
            }
        }

        binding.edWard.setOnItemClickListener { _, _, position, _ ->
            selectedWard = wards.getOrNull(position)
        }
    }

    private fun configureSelector(view: android.widget.AutoCompleteTextView) {
        view.threshold = 0
        view.setOnClickListener { view.showDropDown() }
        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                view.showDropDown()
            }
            false
        }
        view.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) view.showDropDown()
        }
    }

    private fun loadLocationData() {
        lifecycleScope.launch {
            val rawToken = TokenManager(requireContext()).getToken().orEmpty()
            bearerToken = if (rawToken.isNotBlank()) "Bearer $rawToken" else ""
            loadProvinces()
        }
    }

    private suspend fun loadProvinces() {
        provinces = locationRepository.getProvinces(bearerToken)
        Log.d(tagName, "loadProvinces: ${provinces.size} items")
        setDropdownAdapter(binding.edCity, provinces)
    }

    private fun loadDistricts(provinceId: Int) {
        lifecycleScope.launch {
            districts = locationRepository.getDistricts(provinceId, bearerToken)
            Log.d(tagName, "loadDistricts: provinceId=$provinceId, count=${districts.size}")
            setDropdownAdapter(binding.edState, districts)
        }
    }

    private fun loadWards(districtId: Int) {
        lifecycleScope.launch {
            wards = locationRepository.getWards(districtId, bearerToken)
            Log.d(tagName, "loadWards: districtId=$districtId, count=${wards.size}")
            setDropdownAdapter(binding.edWard, wards)
        }
    }

    private fun setDropdownAdapter(view: android.widget.AutoCompleteTextView, options: List<LocationOption>) {
        val names = options.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
        view.setAdapter(adapter)
    }

    private fun isLocationValid(): Boolean {
        return selectedProvince != null &&
            selectedDistrict != null &&
            selectedWard != null &&
            selectedProvince?.name == binding.edCity.text?.toString()?.trim() &&
            selectedDistrict?.name == binding.edState.text?.toString()?.trim() &&
            selectedWard?.name == binding.edWard.text?.toString()?.trim()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

