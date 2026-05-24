package com.vmptf.mobile.core.data.network

import com.vmptf.mobile.features.posts.data.response.PostApiService


object api {
    private const val BASE_URL = "http://10.0.2.2:3000/api/"

    val retrofitService: PostApiService by lazy {
        RetrofitClient.getClient(BASE_URL)
            .create(PostApiService::class.java)
    }
}