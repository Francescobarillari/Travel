package com.travel.app.domain.repository

import it.unical.ea.dtos.activity.ActivityDto
import it.unical.ea.dtos.activity.ActivityTemplateDto
import it.unical.ea.dtos.activity.CreateActivityRequestDto

interface ActivityRepository {
    suspend fun createActivity(activity: CreateActivityRequestDto): Result<ActivityTemplateDto>
    suspend fun getActivities(): Result<List<ActivityDto>>
    suspend fun getActivityById(id: String): Result<ActivityDto> = throw NotImplementedError()
    suspend fun searchActivities(query: String, minStartTime: String? = null, page: Int = 0, size: Int = 10): Result<it.unical.ea.dtos.common.PageDto<ActivityTemplateDto>> = throw NotImplementedError()
    suspend fun updateActivity(id: String, activity: ActivityDto): Result<ActivityDto> = throw NotImplementedError()
    suspend fun getBookedUsers(id: String): Result<List<it.unical.ea.dtos.user.UserDTO>> = throw NotImplementedError()
    suspend fun deleteActivity(id: String): Result<Unit> = throw NotImplementedError()
}
