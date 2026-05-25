package com.vmptf.mobile.features.posts.data.response

import com.vmptf.mobile.features.posts.domain.model.Comment
import com.vmptf.mobile.features.posts.domain.model.CreateCommentRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CommentsApiService {
    @GET("comments")
    suspend fun getCommentsByPost(@Query("postId") postId: Int): List<Comment>

    @POST("comments")
    suspend fun createComment(@Body request: CreateCommentRequest): Comment
}
