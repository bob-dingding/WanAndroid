package com.hxzk.network

import androidx.lifecycle.LiveData
import com.hxzk.network.model.LoginModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 *作者：created by zjt on 2021/3/12
 *描述:
 *
 */
 interface WanApi {

    companion object {
        /**
         * 是否打印日志
         */
        const val isDebug = true

        /**
         * basrUrl
         */
        const val baseUrl ="https://www.wanandroid.com/"

        fun get(): WanApi {
            val clientBuilder = OkHttpClient.Builder()
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            if (isDebug) {
                val loggingInterceptor = HttpLoggingInterceptor()
                loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
                clientBuilder.addInterceptor(loggingInterceptor)
            }
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(clientBuilder.build())
                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WanApi::class.java)
        }
    }



    /**
     * 登录
     */
    @FormUrlEncoded
    @POST("user/login")
    fun login(@Field("username") username : String, @Field("password") password : String ): LiveData<ApiResponse<LoginModel>>
}