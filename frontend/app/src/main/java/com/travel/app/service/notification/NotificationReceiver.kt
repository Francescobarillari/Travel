package com.travel.app.service.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Promemoria Viaggio"
        val message = intent.getStringExtra("message") ?: "Hai un viaggio programmato in arrivo!"
        val channelId = intent.getStringExtra("channelId") ?: NotificationHelper.CHANNEL_REMINDERS_ID
        
        NotificationHelper.showNotification(
            context = context,
            title = title,
            message = message,
            channelId = channelId
        )
    }
}
