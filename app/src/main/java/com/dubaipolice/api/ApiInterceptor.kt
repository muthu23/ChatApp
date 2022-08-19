package com.dubaipolice.api

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ApiInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var originalRequest = chain.request()
        originalRequest = originalRequest.newBuilder().build()
        return chain.proceed(originalRequest)
    }

    companion object {
        private val TAG = ApiInterceptor::class.java.canonicalName
    }
}