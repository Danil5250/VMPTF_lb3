package com.vmptf.mobile.core.data.network

import com.vmptf.mobile.features.auth.data.api.AuthApiService
import com.vmptf.mobile.features.posts.data.response.CategoriesApiService
import com.vmptf.mobile.features.posts.data.response.CommentsApiService
import com.vmptf.mobile.features.posts.data.response.PostApiService


object api {
    //special address to connect from emulator to server
    //localhost in emulator points to the phone
    //emulator reserves address 10.0.2.2 to pc

    //read only and
    //const compile time constant
    private const val BASE_URL = "http://10.0.2.2:3000/api/"

    // by lazy = lazy init, code in {} will be executed only when we call retrofitService
    // for the first time, next calls a complete object will be returned
    val retrofitService: PostApiService by lazy {
        RetrofitClient.getClient(BASE_URL)
            .create(PostApiService::class.java)
    }

    val authService: AuthApiService by lazy {
        RetrofitClient.getClient(BASE_URL)
            .create(AuthApiService::class.java)
    }

    val categoriesService: CategoriesApiService by lazy {
        RetrofitClient.getClient(BASE_URL)
            .create(CategoriesApiService::class.java)
    }

    val commentsService: CommentsApiService by lazy {
        RetrofitClient.getClient(BASE_URL)
            .create(CommentsApiService::class.java)
    }
}