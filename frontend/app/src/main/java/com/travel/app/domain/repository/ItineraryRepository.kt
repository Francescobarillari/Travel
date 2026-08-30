package com.travel.app.domain.repository

import it.unical.ea.dtos.itinerary.ItineraryDto
import it.unical.ea.dtos.itinerary.CreateItineraryRequest
import it.unical.ea.dtos.itinerary.ItineraryJoinRequestDto
import it.unical.ea.dtos.itinerary.ItineraryParticipantDto

interface ItineraryRepository {
    suspend fun getItineraries(): Result<List<ItineraryDto>>
    suspend fun getItinerariesByCreator(creatorId: String): Result<List<ItineraryDto>>
    suspend fun createItinerary(request: CreateItineraryRequest): Result<ItineraryDto>
    suspend fun updateItinerary(id: String, request: CreateItineraryRequest): Result<ItineraryDto> = Result.failure(Exception("Not implemented"))
    suspend fun uploadItineraryImage(id: String, imageBytes: ByteArray, mimeType: String, fileName: String): Result<ItineraryDto> = Result.failure(Exception("Not implemented"))
    suspend fun updateItineraryVisibility(id: String, visibility: String): Result<ItineraryDto>
    suspend fun deleteItinerary(id: String): Result<Unit>
    suspend fun getItineraryById(id: String): Result<ItineraryDto> = Result.failure(Exception("Not implemented"))
    suspend fun getBookedItineraries(): Result<List<ItineraryDto>> = Result.failure(Exception("Not implemented"))
    suspend fun joinItineraryByCode(shareCode: String): Result<ItineraryJoinRequestDto> = Result.failure(Exception("Not implemented"))
    suspend fun getItineraryJoinRequests(id: String): Result<List<ItineraryJoinRequestDto>> = Result.failure(Exception("Not implemented"))
    suspend fun acceptJoinRequest(requestId: String): Result<ItineraryJoinRequestDto> = Result.failure(Exception("Not implemented"))
    suspend fun rejectJoinRequest(requestId: String): Result<ItineraryJoinRequestDto> = Result.failure(Exception("Not implemented"))
    suspend fun getItineraryParticipants(id: String): Result<List<ItineraryParticipantDto>> = Result.failure(Exception("Not implemented"))
    suspend fun getJoinedItineraries(): Result<List<ItineraryDto>> = Result.failure(Exception("Not implemented"))
}

