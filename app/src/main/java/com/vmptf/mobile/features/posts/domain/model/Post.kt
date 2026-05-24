package com.vmptf.mobile.features.posts.domain.model

data class Category(
    val id: Int,
    val name: String
)

data class Post(
    val id: Int,
    val title: String,
    val content: String?,
    val author: String?,
    val createdAt: String,      // String для сумісності з Gson
    val categories: List<Category> = emptyList()
)