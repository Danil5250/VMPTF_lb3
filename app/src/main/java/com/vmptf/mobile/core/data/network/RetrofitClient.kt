package com.vmptf.mobile.core.data.network

import retrofit2.Retrofit
import retrofit2.Retrofit.Builder
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var retrofit: Retrofit? = null

    //singleton = in all project exists only one instance of Retrofit
    fun getClient(baseUrl: String): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder() //construct the client
                .baseUrl(baseUrl)
                //attach Gson library to convert raw text in JSON to class and objects
                .addConverterFactory(GsonConverterFactory.create())
                .build() //збираємо build retrofit
        }
        //!! = retrofit isn't null
        return retrofit!!
    }
}