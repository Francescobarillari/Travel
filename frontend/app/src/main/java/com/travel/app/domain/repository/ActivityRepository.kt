package com.travel.app.domain.repository

import it.unical.ea.dtos.activity.ActivityDto

interface ActivityRepository {
    suspend fun createActivity(activity: ActivityDto): Result<ActivityDto>
    suspend fun getActivities(): Result<List<ActivityDto>>
}
