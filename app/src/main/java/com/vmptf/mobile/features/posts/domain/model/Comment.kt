package com.vmptf.mobile.features.posts.domain.model

data class CommentUser(
    val id: Int,
    val name: String?,
    val email: String
)

data class Comment(
    val id: Int,
    val content: String,
    val postId: Int,
    val userId: Int?,
    val user: CommentUser?,
    val createdAt: String
)

data class CreateCommentRequest(
    val content: String,
    val postId: Int,
    val userId: Int?
)
