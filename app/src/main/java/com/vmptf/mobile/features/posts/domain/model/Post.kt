package com.vmptf.mobile.features.posts.domain.model

import java.util.Date

data class Post(
    val id: Int,
    val title: String,
    val content: String?,     // Может быть null
    val author: String?,      // Может быть null
    val createdAt: Date,      // Gson умеет парсить даты по умолчанию (ISO 8601)
    //val comments: List<Comment>,
    //val categories: List<Category>
)