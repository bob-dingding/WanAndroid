package com.hxzk.network

import androidx.lifecycle.LiveData
import com.hxzk.network.model.ApiResponse
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type
import java.util.concurrent.atomic.AtomicBoolean

/**
 *作者：created by zjt on 2021/3/12
 *描述:在LiveDataCallAdapter将Retrofit的Call对象适配成LiveData
 *
 */
class LiveDataCallAdapter <T>(private val responseType: Type) : CallAdapter<T, LiveData<T>> {
    override fun adapt(call: Call<T>): LiveData<T> {
        return object : LiveData<T>() {
            private val started = AtomicBoolean(false)
            //LiveData的onActive
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {//确保执行一次
                    call.enqueue(object : Callback<T> {
                        override fun onFailure(call: Call<T>, t: Throwable) {
                            val value = ApiResponse<T>(null, -1, t.message ?: "") as T
                            postValue(value)
                        }
                        override fun onResponse(call: Call<T>, response: Response<T>) {
                            postValue(response.body())
                        }
                    })
                }
            }
        }
    }

    override fun responseType() = responseType
}