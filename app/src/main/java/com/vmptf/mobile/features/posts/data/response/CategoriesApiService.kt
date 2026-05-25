package com.vmptf.mobile.features.posts.data.response

import com.vmptf.mobile.features.posts.domain.model.Category
import retrofit2.http.GET

interface CategoriesApiService {
    @GET("categories")
    suspend fun getCategories(): CategoriesResponse
}

data class CategoriesResponse(
    val result: List<Category>
)
