package com.travel.app.service.notification

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.travel.app.data.AppContainer

class NotificationSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("NotificationSyncWorker", "Starting notification sync background work...")
        
        // Inizializza l'AppContainer se necessario
        if (!AppContainer.isInitialized) {
            AppContainer.initialize(applicationContext)
        }

        val token = AppContainer.sessionManager.getSessionToken()
        if (token.isNullOrBlank()) {
            Log.d("NotificationSyncWorker", "User not logged in, skipping notification sync.")
            return Result.success()
        }

        return try {
            val notifications = AppContainer.apiService.getUnreadNotifications()
            Log.d("NotificationSyncWorker", "Fetched ${notifications.size} unread notifications.")

            for (notification in notifications) {
                // Mostra la notifica sul dispositivo
                NotificationHelper.showNotification(
                    context = applicationContext,
                    title = notification.title ?: "Travel Update",
                    message = notification.message ?: "",
                    channelId = NotificationHelper.CHANNEL_BOOKINGS_ID
                )

                // Segna la notifica come letta sul backend
                try {
                    AppContainer.apiService.markNotificationAsRead(notification.id.toString())
                } catch (e: Exception) {
                    Log.e("NotificationSyncWorker", "Failed to mark notification as read", e)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("NotificationSyncWorker", "Error syncing notifications", e)
            Result.retry()
        }
    }
}
