package com.duocuc.tiendaropa.utils

import android.app.Application

class TiendaApp : Application() {
    companion object {
        lateinit var instance: TiendaApp
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}

