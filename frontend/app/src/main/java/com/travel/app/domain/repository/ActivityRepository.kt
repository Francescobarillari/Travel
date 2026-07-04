package com.travel.app.domain.repository

import it.unical.ea.dtos.activity.ActivityDto

interface ActivityRepository {
    suspend fun createActivity(activity: ActivityDto): Result<ActivityDto> = Result.failure(NotImplementedError())
    suspend fun getActivities(): Result<List<ActivityDto>> = Result.failure(NotImplementedError())
    suspend fun getActivityById(id: String): Result<ActivityDto> = Result.failure(NotImplementedError())
    suspend fun searchActivities(query: String, minPrice: Double? = null, maxPrice: Double? = null, page: Int = 0, size: Int = 10): Result<it.unical.ea.dtos.common.PageDto<ActivityDto>> = Result.failure(NotImplementedError())
}
