package com.travel.app.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.CalendarContract
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.ZoneId

object CalendarExportUtil {

    @RequiresApi(Build.VERSION_CODES.O)
    fun addToCalendar(
        context: Context,
        title: String,
        description: String?,
        location: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?
    ) {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, title)
                
                if (!description.isNullOrBlank()) {
                    putExtra(CalendarContract.Events.DESCRIPTION, description)
                }
                
                if (!location.isNullOrBlank()) {
                    putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                }

                if (startTime != null) {
                    val startMillis = startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                }
                
                if (endTime != null) {
                    val endMillis = endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                }
            }
            
            context.startActivity(intent)
            
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, "Nessuna app calendario trovata", android.widget.Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, "Errore durante l'apertura del calendario", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
