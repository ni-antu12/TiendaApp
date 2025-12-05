package com.duocuc.tiendaropa.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

object ImageUtils {
    /**
     * Comprime una imagen desde un URI y la convierte a Base64
     * @param context Contexto de la aplicación
     * @param uri URI de la imagen seleccionada
     * @param maxWidth Ancho máximo de la imagen (default: 800px)
     * @param maxHeight Alto máximo de la imagen (default: 800px)
     * @param quality Calidad de compresión JPEG (0-100, default: 70)
     * @return String en formato Base64 o null si falla
     */
    fun compressAndEncodeImage(
        context: Context,
        uri: Uri,
        maxWidth: Int = 800,
        maxHeight: Int = 800,
        quality: Int = 70
    ): String? {
        try {
            // Leer la imagen desde el URI
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (originalBitmap == null) return null
            
            // Calcular el nuevo tamaño manteniendo la proporción
            val ratio = minOf(
                maxWidth.toFloat() / originalBitmap.width,
                maxHeight.toFloat() / originalBitmap.height
            )
            
            val newWidth = (originalBitmap.width * ratio).toInt()
            val newHeight = (originalBitmap.height * ratio).toInt()
            
            // Redimensionar la imagen
            val resizedBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                newWidth,
                newHeight,
                true
            )
            
            // Comprimir a JPEG
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val byteArray = outputStream.toByteArray()
            
            // Limpiar recursos
            originalBitmap.recycle()
            resizedBitmap.recycle()
            outputStream.close()
            
            // Convertir a Base64
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            
            // Log del tamaño para debugging
            android.util.Log.d("ImageUtils", "Original size: ${originalBitmap.width}x${originalBitmap.height}")
            android.util.Log.d("ImageUtils", "Compressed size: ${newWidth}x${newHeight}")
            android.util.Log.d("ImageUtils", "Base64 length: ${base64String.length} chars (~${byteArray.size / 1024}KB)")
            
            return "data:image/jpeg;base64,$base64String"
            
        } catch (e: Exception) {
            android.util.Log.e("ImageUtils", "Error compressing image: ${e.message}", e)
            return null
        }
    }
}
