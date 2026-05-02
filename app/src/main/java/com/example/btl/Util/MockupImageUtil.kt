package com.example.btl.Util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

/**
 * Utility class for handling local images in mockup testing
 * Provides methods to convert between different image formats and locations
 */
object MockupImageUtil {
    
    /**
     * Convert Bitmap to File and save to app cache directory
     * @param context Android context
     * @param bitmap Bitmap to save
     * @param fileName File name to save as
     * @return File path as String or null if failed
     */
    fun saveBitmapToLocalFile(context: Context, bitmap: Bitmap, fileName: String): String? {
        return try {
            val cacheDir = context.cacheDir
            val file = File(cacheDir, fileName)
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            fos.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Get local file URI as string (file://)
     * @param filePath Absolute file path
     * @return file:// URI as String
     */
    fun getFileUri(filePath: String): String {
        return "file://$filePath"
    }
    
    /**
     * Check if URI is local file
     * @param uriString URI string to check
     * @return true if local file, false if remote URL
     */
    fun isLocalFile(uriString: String): Boolean {
        return uriString.startsWith("file://") || uriString.startsWith("/")
    }
    
    /**
     * Convert URI to file path
     * @param uri Android URI
     * @return File path or null
     */
    fun uriToFilePath(uri: Uri): String? {
        return if (uri.scheme == "file") {
            uri.path
        } else {
            null
        }
    }
    
    /**
     * Generate mock rendered image (for testing without backend)
     * Creates a simple overlay effect on original bitmap
     * @param originalBitmap Original design image
     * @return Rendered bitmap
     */
    fun generateMockRenderedImage(
        originalBitmap: Bitmap
    ): Bitmap {
        // Create a copy for rendering
        val rendered = originalBitmap.copy(originalBitmap.config, true)
        
        // In production, this would call Sudomock API
        // For testing, we just return the original with a marker
        // Real implementation would apply template effects
        
        return rendered
    }
    
    /**
     * Clean up old mockup image files from cache
     * @param context Android context
     * @param maxAgeHours Files older than this will be deleted
     */
    fun cleanupOldMockups(context: Context, maxAgeHours: Long = 24) {
        try {
            val cacheDir = context.cacheDir
            val currentTime = System.currentTimeMillis()
            val maxAge = maxAgeHours * 60 * 60 * 1000
            
            cacheDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("MOCKUP_") && 
                    (currentTime - file.lastModified()) > maxAge) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

