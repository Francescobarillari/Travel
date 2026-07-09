package com.travel.app.domain.model.review

data class ReviewDto(
    val id: String? = null,
    val authorName: String,
    val isEditable: Boolean = false,
    val rating: Double,
    val comment: String? = null,
    val createdAt: String? = null,
    val activityId: String? = null,
    val activityName: String? = null,
    val itineraryId: String? = null,
    val itineraryName: String? = null
)

data class CreateReviewDto(
    val activityId: String? = null,
    val itineraryId: String? = null,
    val rating: Double,
    val comment: String? = null
)
