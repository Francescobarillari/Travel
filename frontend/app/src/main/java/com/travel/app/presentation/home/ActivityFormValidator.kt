package com.travel.app.presentation.home

import it.unical.ea.dtos.activity.TimeSlotDto
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object ActivityFormValidator {

    fun validateActivityForm(
        title: String,
        location: String,
        isEditMode: Boolean,
        // Single activity fields (used in edit mode)
        startLdt: LocalDateTime?,
        endLdt: LocalDateTime?,
        // Recurring activity fields (used in create mode)
        startDate: LocalDate?,
        endDate: LocalDate?,
        selectedDays: Set<String>,
        timeSlots: List<TimeSlotDto>,
        // Common fields
        maxParticipants: Int?,
        price: Double?
    ): String? {
        if (title.isBlank()) {
            return "Il titolo dell'attività è obbligatorio"
        }
        if (location.isBlank()) {
            return "La posizione dell'attività è obbligatoria"
        }

        if (maxParticipants == null || maxParticipants < 1) {
            return "Il numero massimo di partecipanti deve essere almeno 1"
        }
        if (price == null || price < 0.0) {
            return "Il prezzo non può essere negativo o vuoto"
        }

        if (isEditMode) {
            if (startLdt == null) return "La data e ora di inizio sono obbligatorie"
            if (endLdt == null) return "La data e ora di fine sono obbligatorie"
            if (startLdt.isAfter(endLdt)) {
                return "La data/ora di inizio deve essere precedente alla data/ora di fine"
            }
        } else {
            if (startDate == null) return "La data di inizio periodo è obbligatoria"
            if (endDate == null) return "La data di fine periodo è obbligatoria"
            
            val today = LocalDate.now()
            if (startDate.isBefore(today)) {
                return "La data di inizio periodo non può essere antecedente a oggi"
            }
            if (startDate.isAfter(endDate)) {
                return "La data di inizio periodo deve essere precedente o uguale alla data di fine"
            }

            if (selectedDays.isEmpty()) {
                return "Seleziona almeno un giorno della settimana"
            }
            if (timeSlots.isEmpty()) {
                return "Seleziona almeno una fascia oraria"
            }

            // Check if selected days of week are compatible with the date range
            if (!isDaysOfWeekCompatible(startDate, endDate, selectedDays)) {
                return "I giorni della settimana selezionati non rientrano nel periodo specificato"
            }

            // Check if time slots are valid and do not overlap
            if (hasInvalidTimeSlots(timeSlots)) {
                return "L'ora di inizio di ogni fascia oraria deve essere precedente all'ora di fine"
            }
            if (hasOverlappingTimeSlots(timeSlots)) {
                return "Le fasce orarie selezionate non possono sovrapporsi"
            }
        }

        return null
    }

    private fun isDaysOfWeekCompatible(startDate: LocalDate, endDate: LocalDate, selectedDays: Set<String>): Boolean {
        var current = startDate
        val uppercaseDays = selectedDays.map { it.uppercase() }.toSet()
        val limitDate = if (startDate.plusDays(7).isBefore(endDate)) startDate.plusDays(7) else endDate
        while (!current.isAfter(limitDate)) {
            val dayName = current.dayOfWeek.name
            if (uppercaseDays.contains(dayName)) {
                return true
            }
            current = current.plusDays(1)
        }
        return false
    }

    private fun hasInvalidTimeSlots(slots: List<TimeSlotDto>): Boolean {
        return slots.any { !it.startTime.isBefore(it.endTime) }
    }

    private fun hasOverlappingTimeSlots(slots: List<TimeSlotDto>): Boolean {
        for (i in slots.indices) {
            for (j in i + 1 until slots.size) {
                val s1 = slots[i]
                val s2 = slots[j]
                if (s1.startTime.isBefore(s2.endTime) && s2.startTime.isBefore(s1.endTime)) {
                    return true
                }
            }
        }
        return false
    }
}
