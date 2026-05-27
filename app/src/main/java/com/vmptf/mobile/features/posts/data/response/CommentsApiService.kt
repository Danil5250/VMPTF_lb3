package com.vmptf.mobile.features.posts.data.response

import com.vmptf.mobile.features.posts.domain.model.Comment
import com.vmptf.mobile.features.posts.domain.model.CreateCommentRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

//routes using which mobile works with backend
interface CommentsApiService {
    @GET("comments")
    suspend fun getCommentsByPost(@Query("postId") postId: Int): List<Comment>

    @POST("comments")
    suspend fun createComment(@Body request: CreateCommentRequest): Comment

    @DELETE("comments/{id}")
    suspend fun deleteComment(
        @Path("id") id: Int
    ): Response<Unit>
}
