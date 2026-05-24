package com.vmptf.mobile.features.posts.data.response

import com.vmptf.mobile.features.posts.data.api.BlogsResponse
import com.vmptf.mobile.features.posts.domain.model.Post
import retrofit2.http.GET

///api/blogs
interface PostApiService {
    @GET("blogs") // Хвост твоей ссылки, в итоге будет http://ваш_ip:8080/users
    suspend fun getPosts(): BlogsResponse // Твоя data class моделька
}