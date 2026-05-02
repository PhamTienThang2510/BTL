package com.example.btl.Adapters

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.btl.Fragments.Shopping.CategoryProductsFragment
import com.example.btl.Model.Category

class CategoryTabAdapter(
    fragment: Fragment,
    private val categories: List<Category>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = categories.size

    override fun createFragment(position: Int): Fragment {
        val category = categories[position]
        Log.d("CategoryTabAdapter", "createFragment: position=$position, categoryId=${category.category_id}, name=${category.name}")
        val fragment = CategoryProductsFragment.newInstance(category.category_id, category.name)
        Log.d("CategoryTabAdapter", "createFragment: Created fragment with categoryId=${category.category_id}")
        return fragment
    }
}

