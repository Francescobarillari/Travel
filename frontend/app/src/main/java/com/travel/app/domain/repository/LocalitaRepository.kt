package com.travel.app.domain.repository

import it.unical.ea.dtos.localita.LocalitaDto

interface LocalitaRepository {
    suspend fun getLocalitaById(id: String): Result<LocalitaDto>
    suspend fun searchLocalita(query: String, page: Int = 0, size: Int = 10): Result<it.unical.ea.dtos.common.PageDto<LocalitaDto>>
}
