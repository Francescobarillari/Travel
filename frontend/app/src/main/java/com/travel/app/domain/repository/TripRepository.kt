package com.travel.app.domain.repository

import it.unical.ea.dtos.trip.TripDto

interface TripRepository {
    suspend fun getTripById(id: String): Result<TripDto>
    suspend fun searchTrips(query: String, minPrice: Double? = null, maxPrice: Double? = null, page: Int = 0, size: Int = 10): Result<it.unical.ea.dtos.common.PageDto<TripDto>>
}
