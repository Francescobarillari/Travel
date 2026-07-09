package com.travel.app.service.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.travel.app.MainActivity
import com.travel.app.R

object NotificationHelper {

    const val CHANNEL_BOOKINGS_ID = "bookings_channel"
    const val CHANNEL_REMINDERS_ID = "reminders_channel"

    private const val PREFS_NAME = "notifications_prefs"
    private const val KEY_SHOWN_IDS = "shown_notification_ids"

    // Traccia le notifiche già mostrate sul dispositivo: la lettura in-app è separata
    // (una notifica resta "non letta" finché l'utente non la legge dalla campanella),
    // ma non deve essere ri-mostrata come push ad ogni sync.
    fun hasBeenShown(context: Context, notificationId: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_SHOWN_IDS, emptySet())?.contains(notificationId) == true
    }

    fun markAsShown(context: Context, notificationId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_SHOWN_IDS, emptySet()) ?: emptySet()
        prefs.edit().putStringSet(KEY_SHOWN_IDS, current + notificationId).apply()
    }

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nameBookings = "Prenotazioni"
            val descBookings = "Notifiche relative alle prenotazioni effettuate"
            val importanceBookings = NotificationManager.IMPORTANCE_HIGH
            val channelBookings = NotificationChannel(CHANNEL_BOOKINGS_ID, nameBookings, importanceBookings).apply {
                description = descBookings
            }

            val nameReminders = "Promemoria Viaggi"
            val descReminders = "Promemoria e avvisi prima della partenza"
            val importanceReminders = NotificationManager.IMPORTANCE_DEFAULT
            val channelReminders = NotificationChannel(CHANNEL_REMINDERS_ID, nameReminders, importanceReminders).apply {
                description = descReminders
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channelBookings)
            notificationManager.createNotificationChannel(channelReminders)
        }
    }

    @android.annotation.SuppressLint("MissingPermission", "NotificationPermission")
    fun showNotification(
        context: Context,
        title: String,
        message: String,
        channelId: String = CHANNEL_BOOKINGS_ID,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        // Verifica del permesso per Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}
