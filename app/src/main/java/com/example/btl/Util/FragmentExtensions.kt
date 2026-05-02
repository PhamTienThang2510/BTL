package com.example.btl.Util

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import android.widget.Toast

fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(requireContext(), message, duration).show()
}

fun Fragment.navigate(actionId: Int) {
    try {
        findNavController().navigate(actionId)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Fragment.navigateWithArgs(actionId: Int, args: android.os.Bundle) {
    try {
        findNavController().navigate(actionId, args)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

