package com.travel.app

import android.app.Application
import com.paypal.checkout.PayPalCheckout
import com.paypal.checkout.config.CheckoutConfig
import com.paypal.checkout.config.Environment
import com.paypal.checkout.config.SettingsConfig
import com.paypal.checkout.createorder.CurrencyCode
import com.paypal.checkout.createorder.UserAction
import com.travel.app.data.AppContainer

class TravelApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inizializza l'AppContainer passando il contesto globale dell'app
        AppContainer.initialize(this)
        
        // Inizializza l'SDK di PayPal con il Client ID
        val config = CheckoutConfig(
            application = this,
            clientId = BuildConfig.PAYPAL_CLIENT_ID,
            environment = Environment.SANDBOX,
            returnUrl = "${BuildConfig.APPLICATION_ID}://paypalpay",
            currencyCode = CurrencyCode.EUR,
            userAction = UserAction.PAY_NOW,
            settingsConfig = SettingsConfig(
                loggingEnabled = true
            )
        )
        PayPalCheckout.setConfig(config)
    }
}
