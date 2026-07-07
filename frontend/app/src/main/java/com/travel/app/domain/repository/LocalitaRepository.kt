package com.travel.app.domain.repository

import it.unical.ea.dtos.location.LocationDto as LocalitaDto

interface LocalitaRepository {
    suspend fun getLocalitaById(id: String): Result<LocalitaDto>
    suspend fun searchLocalita(query: String, includeExternal: Boolean = false, page: Int = 0, size: Int = 10): Result<it.unical.ea.dtos.common.PageDto<LocalitaDto>>
}
