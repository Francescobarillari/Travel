package com.travel.app

import android.app.Application
import com.stripe.android.PaymentConfiguration
import com.travel.app.data.AppContainer

class TravelApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inizializza l'AppContainer passando il contesto globale dell'app
        AppContainer.initialize(this)
        
        // Inizializza l'SDK di Stripe con la chiave pubblica da BuildConfig
        PaymentConfiguration.init(this, BuildConfig.STRIPE_PUBLISHABLE_KEY)
    }
}
