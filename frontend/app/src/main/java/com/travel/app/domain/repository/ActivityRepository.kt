package com.travel.app.domain.repository

import it.unical.ea.dtos.activity.ActivityDto

interface ActivityRepository {
    suspend fun createActivity(activity: ActivityDto): Result<ActivityDto>
    suspend fun getActivities(): Result<List<ActivityDto>>
    suspend fun getActivityById(id: String): Result<ActivityDto> = throw NotImplementedError()
    suspend fun searchActivities(query: String, minPrice: Double? = null, maxPrice: Double? = null, page: Int = 0, size: Int = 10): Result<it.unical.ea.dtos.common.PageDto<ActivityDto>> = throw NotImplementedError()
    suspend fun updateActivity(id: String, activity: ActivityDto): Result<ActivityDto> = throw NotImplementedError()
    suspend fun getBookedUsers(id: String): Result<List<it.unical.ea.dtos.user.UserDTO>> = throw NotImplementedError()
}
