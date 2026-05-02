package com.example.btl.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.btl.Model.Address
import com.example.btl.R
import com.example.btl.databinding.AddressRvItemBinding

class AddressAdapter(
    private var addresses: List<Address> = emptyList(),
    private val onAddressSelected: (Address) -> Unit = {}
) : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    private var selectedAddressId: Int = -1

    inner class AddressViewHolder(private val binding: AddressRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(address: Address) {
            binding.apply {
                tvAddressName.text = address.receiver_name
                tvAddressPhone.text = address.phone
                tvAddressDetail.text = address.address_detail

                // Update selection state with scale/alpha highlight
                val isSelected = address.address_id == selectedAddressId
                if (isSelected) {
                    root.scaleX = 1.04f
                    root.scaleY = 1.04f
                    root.alpha = 1.0f
                    root.setBackgroundColor(itemView.context.getColor(R.color.white))
                } else {
                    root.scaleX = 1.0f
                    root.scaleY = 1.0f
                    root.alpha = 0.7f
                    root.setBackgroundColor(itemView.context.getColor(R.color.white))
                }

                // Handle click to select address
                root.setOnClickListener {
                    val oldSelected = selectedAddressId
                    selectedAddressId = address.address_id ?: -1
                    
                    // Notify changes
                    notifyItemChanged(addresses.indexOfFirst { it.address_id == oldSelected })
                    notifyItemChanged(addresses.indexOfFirst { it.address_id == selectedAddressId })
                    
                    onAddressSelected(address)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val binding = AddressRvItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AddressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        holder.bind(addresses[position])
    }

    override fun getItemCount(): Int = addresses.size

    fun updateAddresses(newAddresses: List<Address>) {
        addresses = newAddresses
        notifyDataSetChanged()
    }

    fun selectAddress(addressId: Int) {
        val oldSelected = selectedAddressId
        selectedAddressId = addressId
        
        if (oldSelected != -1) {
            notifyItemChanged(addresses.indexOfFirst { it.address_id == oldSelected })
        }
        notifyItemChanged(addresses.indexOfFirst { it.address_id == selectedAddressId })
    }
}

