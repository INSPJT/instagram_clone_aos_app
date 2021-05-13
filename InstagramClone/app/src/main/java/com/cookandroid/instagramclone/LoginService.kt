package com.cookandroid.instagramclone

import android.util.Log
import android.widget.Toast
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*
import java.io.IOException
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
    private const val url = "https://codevpros.com"//https://yurivon.ml" // 접속 url"https://13.209.101.178"
    private val retrofitBuilder: Retrofit.Builder = Retrofit.Builder()
    private val retrofitScalarBuilder: Retrofit.Builder = Retrofit.Builder()
    private var retrofit: Retrofit? = null
    private var retrofitScalar : Retrofit? = null
    private var interceptor =  HttpLoggingInterceptor()
    private var okHttpClientBuilder = OkHttpClient.Builder()
    private val loggerBuilder = okHttpClientBuilder.addInterceptor(interceptor).readTimeout(20, TimeUnit.SECONDS) .writeTimeout( 20, TimeUnit.SECONDS )
    private var logger = loggerBuilder.build()

    init {
        Log.d("Init", "haha init")
        var gson = GsonBuilder().setLenient().create()

        interceptor.level = HttpLoggingInterceptor.Level.BODY
        retrofitBuilder.baseUrl(url).addConverterFactory(GsonConverterFactory.create(gson)).client(logger)
        retrofitScalarBuilder.baseUrl(url).addConverterFactory(ScalarsConverterFactory.create()).addConverterFactory(GsonConverterFactory.create()).client(logger)
        updateLogger()
    }

    private fun updateLogger(){
        retrofit = retrofitBuilder.client(logger).build()
        retrofitScalar = retrofitScalarBuilder.client(logger).build()
    }

    fun addInterceptor(inter: Interceptor) {
        logger = loggerBuilder.addInterceptor(inter).build()
        updateLogger()
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

