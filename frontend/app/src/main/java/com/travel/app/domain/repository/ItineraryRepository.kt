package com.travel.app.domain.repository

import it.unical.ea.dtos.itinerary.ItineraryDto

interface ItineraryRepository {
    suspend fun getItineraries(): Result<List<ItineraryDto>>
}
