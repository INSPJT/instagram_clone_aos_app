package com.cookandroid.instagramclone

import okhttp3.Interceptor
import okhttp3.Response

object TokenManager{
    var token = ""
    var tokenInterceptor: Interceptor? = null
    fun addTokenHeader(t: String) {
        if(t == token) return
        tokenInterceptor = object: Interceptor{
            override fun intercept(chain: Interceptor.Chain): Response {
                var request = chain.request().newBuilder().addHeader("Authorization", "Bearer $t").build()
                return chain.proceed(request)
            }
        }
        tokenInterceptor?.let{InternetCommunication.addInterceptor(tokenInterceptor!!)}
    }
}
