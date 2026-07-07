package com.travel.app.domain.repository

import it.unical.ea.dtos.itinerary.ItineraryDto
import it.unical.ea.dtos.itinerary.CreateItineraryRequest

interface ItineraryRepository {
    suspend fun getItineraries(): Result<List<ItineraryDto>>
    suspend fun getItinerariesByCreator(creatorId: String): Result<List<ItineraryDto>>
    suspend fun createItinerary(request: CreateItineraryRequest): Result<ItineraryDto>
    suspend fun updateItineraryVisibility(id: String, visibility: String): Result<ItineraryDto>
    suspend fun deleteItinerary(id: String): Result<Unit>
}
