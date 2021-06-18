package com.cookandroid.instagramclone

import android.os.AsyncTask
import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object InternetCommunication {
    private const val url = "https://codevpros.com"//https://yurivon.ml" // 접속 url"https://13.209.101.178"
    var tokenInterceptor = object: Interceptor{
        override fun intercept(chain: Interceptor.Chain): Response {
            var request = if(InternetCommunication::token.isInitialized) {
                chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
            }
            else {
                Log.e("token", "token is not initialized")
                chain.request()
            }
            return chain.proceed(request)
        }
    }
    private val retrofitBuilder: Retrofit.Builder = Retrofit.Builder()
    private val retrofitScalarBuilder: Retrofit.Builder = Retrofit.Builder()
    private var retrofit: Retrofit? = null
    private var retrofitScalar : Retrofit? = null
    private var interceptor =  HttpLoggingInterceptor()
    private var okHttpClientBuilder = OkHttpClient.Builder()
    private val loggerBuilder = okHttpClientBuilder.addInterceptor(interceptor).addInterceptor(
        tokenInterceptor).readTimeout(20, TimeUnit.SECONDS) .writeTimeout( 20, TimeUnit.SECONDS )
    private var logger = loggerBuilder.build()
    lateinit var token: String

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

class RetrofitImageService(override val baseUrl: String, override val TAG: String) : InternetServiceClass {
    var retrofit = InternetCommunication.getRetrofitString()
    var retrofitService = retrofit.create(ImageService::class.java)
    inner class ImageProcessWait(var files: ArrayList<FileData>, val func: (resources:ArrayList<String>) -> Unit) : AsyncTask<Void,Void,Unit>(){
        override fun doInBackground(vararg p0: Void?) {
            var cnt = 0
            var urls = ArrayList<String>()
            files.forEach {
                var service = retrofitService.sendImage(FormDataUtil.getImageBody("file", it.file))

                service?.enqueue(object: Callback<String> {
                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Log.e(TAG, t.message)
                        cnt++
                    }

                    override fun onResponse(call: Call<String>, response: retrofit2.Response<String>) {
                        cnt++
                        val message = when(response.code()) {
                            200-> {
                                Log.e(TAG, "url : ${response.body()}")
                                response.body()?.let{url-> urls.add(url)}
                                "Sucess"
                            }
                            else -> "Unknown error"
                        }
                        Log.e(TAG,message)
                    }
                })
            }
            while(cnt != files.size) Thread.sleep(100)
            if(urls.size == files.size) func(urls)
            else{
                Log.d(TAG, "some send image url was failed")
            }
        }
    }

    override fun createFile(data: Any?) {
        var (files, func)  = data as GoogleServiceCreateData
        ImageProcessWait(files, func).execute()
        Log.d(TAG,"create file")
    }

    override fun init(data: Any?, func: ((Any?) -> Unit)?) {
    }

    override fun handlePermission(data: Any?) {
    }

    override fun fileList(data: Any?, func: ((Any?) -> Unit)?): ArrayList<String> {
        return ArrayList()
    }

    override fun readFile(data: Any?, func: ((Any?) -> Unit)?): String {
        return ""
    }
}

