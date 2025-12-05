package com.duocuc.tiendaropa

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.duocuc.tiendaropa.utils.Base64Fetcher

class TiendaApplication : Application(), ImageLoaderFactory {
    
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                // Agregar soporte para imágenes base64
                add(Base64Fetcher.Factory())
            }
            .build()
    }
}
