package com.duocuc.tiendaropa.utils

import android.graphics.BitmapFactory
import android.util.Base64
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import okio.Buffer

/**
 * Fetcher personalizado para Coil que soporta imágenes en formato base64
 * Maneja URLs que empiezan con "data:image/"
 */
class Base64Fetcher(
    private val data: String,
    private val options: Options
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        // Extraer el base64 del formato data:image/jpeg;base64,XXXXX
        val base64String = data.substringAfter("base64,")
        
        // Decodificar base64 a bytes
        val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
        
        // Crear un Buffer con los bytes
        val buffer = Buffer().write(imageBytes)
        
        // Retornar como SourceResult
        return SourceResult(
            source = ImageSource(buffer, options.context),
            mimeType = "image/jpeg",
            dataSource = DataSource.MEMORY
        )
    }

    class Factory : Fetcher.Factory<String> {
        override fun create(
            data: String,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher? {
            // Solo manejar URLs que empiecen con "data:image/"
            return if (data.startsWith("data:image/")) {
                Base64Fetcher(data, options)
            } else {
                null
            }
        }
    }
}
