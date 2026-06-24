package com.travel.app

import android.app.Application
import com.travel.app.data.AppContainer

class TravelApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inizializza l'AppContainer passando il contesto globale dell'app
        AppContainer.initialize(this)
    }
}
