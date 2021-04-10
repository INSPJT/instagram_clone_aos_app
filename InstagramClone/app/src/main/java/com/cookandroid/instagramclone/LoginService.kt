package com.cookandroid.instagramclone

import android.util.Log
import android.widget.Toast
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface LoginService {

    @Headers("accept: application/json",
        "content-type: application/json")
//    @FormUrlEncoded //-> Body 쓸땐 이걸 쓰면 오류난다. -> Field쓸때만 쓰면 될듯
    @POST("/auth/signin") // url
    fun Login(
        @Body params : HashMap<String,String>
    ) : Call<User>
}

interface LoginRegister{
    @Headers("accpet: text/plain",
        "content-type: application/json")
    @POST("/auth/signup")
    fun Register(
        @Body body: RegisterData
    ) : Call<String>
}

object InternetCommunication {
    private val url = "https://yurivon.ml" // 접속 url
    private var retrofit: Retrofit? = null
    private var retrofitScalar : Retrofit? = null
    private var interceptor =  HttpLoggingInterceptor()
    private val logger = OkHttpClient.Builder().addInterceptor(interceptor).readTimeout(20, TimeUnit.SECONDS) .writeTimeout( 20, TimeUnit.SECONDS ).build()

    init {
        Log.d("Init", "haha init")
        var gson = GsonBuilder().setLenient().create()

        interceptor.level = HttpLoggingInterceptor.Level.BODY
        retrofit = Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create(gson)).client(logger).build()
        retrofitScalar = Retrofit.Builder().baseUrl(url).addConverterFactory(ScalarsConverterFactory.create()).addConverterFactory(GsonConverterFactory.create()).client(logger).build()
    }

    fun makeRetrofitInfo() {
        if(retrofit == null) return
        retrofit = Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).client(logger).build()
    }

    fun makeRetrofitScalar() {
        if(retrofitScalar == null) return
        retrofitScalar = Retrofit.Builder().baseUrl(url).addConverterFactory(ScalarsConverterFactory.create()).client(logger).build()
    }

    fun getRetrofitGson(): Retrofit {
        if(retrofit == null) makeRetrofitInfo()
        return retrofit!!
    }

    fun getRetrofitString(): Retrofit {
        if(retrofitScalar == null) makeRetrofitScalar()
        return retrofitScalar!!
    }
}

