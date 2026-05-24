package com.vmptf.mobile.features.posts.data.response

import com.vmptf.mobile.features.posts.data.api.BlogsResponse
import com.vmptf.mobile.features.posts.domain.model.Post
import retrofit2.http.GET
import retrofit2.http.Query

interface PostApiService {
    @GET("blogs")
    suspend fun getPosts(): BlogsResponse

    @GET("blogs/qs")
    suspend fun getFilteredPosts(
        @Query("search") search: String? = null,
        @Query("categoryIds") categoryIds: List<Int>? = null
    ): List<Post>
}