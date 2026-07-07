package com.travel.app.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

object CalendarExportUtil {

    @RequiresApi(Build.VERSION_CODES.O)
    fun exportToIcs(
        context: Context,
        title: String,
        description: String?,
        location: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?
    ) {
        val icsContent = generateIcsContent(title, description, location, startTime, endTime)
        
        try {
            val icsDir = File(context.cacheDir, "ics")
            if (!icsDir.exists()) {
                icsDir.mkdirs()
            }
            val icsFile = File(icsDir, "event_${UUID.randomUUID()}.ics")
            icsFile.writeText(icsContent)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                icsFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/calendar")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            // Fallback to ACTION_SEND if ACTION_VIEW is not supported by any app
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/calendar"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(Intent.createChooser(sendIntent, "Esporta evento nel calendario"))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            // Handle error, e.g., show Toast
            android.widget.Toast.makeText(context, "Errore durante l'esportazione", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateIcsContent(
        title: String,
        description: String?,
        location: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?
    ): String {
        // Simple ICS formatter
        val dtFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
        
        // Convert to UTC
        val startUtc = startTime?.atZone(ZoneId.systemDefault())?.withZoneSameInstant(ZoneId.of("UTC"))
        val endUtc = endTime?.atZone(ZoneId.systemDefault())?.withZoneSameInstant(ZoneId.of("UTC"))

        val dtStart = startUtc?.format(dtFormatter)
        val dtEnd = endUtc?.format(dtFormatter) ?: dtStart

        val builder = StringBuilder()
        builder.append("BEGIN:VCALENDAR\n")
        builder.append("VERSION:2.0\n")
        builder.append("PRODID:-//TravelApp//IT\n")
        builder.append("BEGIN:VEVENT\n")
        
        val nowUtc = LocalDateTime.now().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC"))
        builder.append("DTSTAMP:${nowUtc.format(dtFormatter)}\n")
        
        if (dtStart != null) {
            builder.append("DTSTART:$dtStart\n")
        }
        if (dtEnd != null) {
            builder.append("DTEND:$dtEnd\n")
        }
        
        builder.append("SUMMARY:${escapeIcsText(title)}\n")
        
        if (!description.isNullOrBlank()) {
            builder.append("DESCRIPTION:${escapeIcsText(description)}\n")
        }
        if (!location.isNullOrBlank()) {
            builder.append("LOCATION:${escapeIcsText(location)}\n")
        }
        
        builder.append("UID:${UUID.randomUUID()}\n")
        builder.append("END:VEVENT\n")
        builder.append("END:VCALENDAR\n")
        
        return builder.toString()
    }

    private fun escapeIcsText(text: String): String {
        return text.replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(",", "\\,")
            .replace("\n", "\\n")
    }
}
