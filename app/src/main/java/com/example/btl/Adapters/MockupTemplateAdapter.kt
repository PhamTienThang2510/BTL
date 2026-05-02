package com.example.btl.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.btl.Model.MockupTemplateModel
import com.example.btl.databinding.ItemMockupTemplateBinding

class MockupTemplateAdapter(
    private var templates: List<MockupTemplateModel> = emptyList(),
    private val onTemplateSelected: (MockupTemplateModel) -> Unit
) : RecyclerView.Adapter<MockupTemplateAdapter.TemplateViewHolder>() {

    inner class TemplateViewHolder(private val binding: ItemMockupTemplateBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(template: MockupTemplateModel) {
            binding.apply {
                tvTemplateName.text = template.name
                tvTemplateDesc.text = template.description

                // Load template thumbnail
                Glide.with(itemView.context)
                    .load(template.image_url)
                    .into(ivTemplateImage)

                // Highlight selected template
                if (template.is_selected) {
                    cardTemplate.setCardBackgroundColor(itemView.context.getColor(android.R.color.holo_blue_light))
                    tvTemplateName.setTextColor(itemView.context.getColor(android.R.color.white))
                } else {
                    cardTemplate.setCardBackgroundColor(itemView.context.getColor(android.R.color.white))
                    tvTemplateName.setTextColor(itemView.context.getColor(android.R.color.black))
                }

                // Click listener
                root.setOnClickListener {
                    onTemplateSelected(template)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        val binding = ItemMockupTemplateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TemplateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
        holder.bind(templates[position])
    }

    override fun getItemCount(): Int = templates.size

    fun updateTemplates(newTemplates: List<MockupTemplateModel>) {
        templates = newTemplates
        notifyDataSetChanged()
    }
}

