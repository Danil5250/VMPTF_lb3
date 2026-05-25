package com.vmptf.mobile.features.posts.data.response

import com.vmptf.mobile.features.posts.data.api.BlogsResponse
import com.vmptf.mobile.features.posts.domain.model.Post
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.Query

data class PostRequest(
    val title: String,
    val content: String,
    val author: String,
    val categoryIds: List<Int>
)

interface PostApiService {
    @GET("blogs")
    suspend fun getPosts(): BlogsResponse

    @GET("blogs/qs")
    suspend fun getFilteredPosts(
        @Query("search") search: String? = null,
        @Query("categoryIds") categoryIds: List<Int>? = null
    ): List<Post>

    @POST("blogs")
    suspend fun createPost(@Body post: PostRequest): Post

    @PUT("blogs/{id}")
    suspend fun updatePost(@Path("id") id: Int, @Body post: PostRequest): Post

    @DELETE("blogs/{id}")
    suspend fun deletePost(@Path("id") id: Int)
}