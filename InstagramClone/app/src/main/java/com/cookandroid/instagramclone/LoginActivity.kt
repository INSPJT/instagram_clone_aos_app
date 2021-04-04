package com.cookandroid.instagramclone


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
<<<<<<< Updated upstream
<<<<<<< HEAD
    val url = "http://117.17.196.61:80" // 접속 url
=======
    val url = "http://13.209.101.178:80" // 접속 url
>>>>>>> ce06c286fe5a21d8b9ac4468c379ae822abe7527
=======
    val url = "http://117.17.196.61:80" // 접속 url
>>>>>>> Stashed changes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val logger = OkHttpClient.Builder().addInterceptor(interceptor).readTimeout(20,TimeUnit.SECONDS).writeTimeout(20,TimeUnit.SECONDS).build()

        val retrofit = Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).client(logger).build()


        val retrofitService = retrofit.create(LoginService::class.java)

        login_btn.setOnClickListener {
            val text_email = input_email.text.toString()
            val text_pw = input_pw.text.toString()
            val Body = HashMap<String,String>()

            Body.put("email",text_email)
            Body.put("password",text_pw)

            retrofitService.Login(Body).enqueue(object : Callback<User>{
                override fun onResponse(call: Call<User>, response: Response<User>) { // 통신 성공
                    when(response.code()){
                        200 -> { // 성공
                            val accesstoken = response.body()!!.accessToken
                            val accesstokenexpirein = response.body()!!.accessTokenExpiresIn
                            val granttype = response.body()!!.grantType
                            val refreshtoken = response.body()!!.refreshToken

                            val user = User(text_email,text_pw,accesstoken,accesstokenexpirein,granttype,refreshtoken,"","")

                            Toast.makeText(this@LoginActivity,"로그인 성공",Toast.LENGTH_LONG).show()
                            Log.d("성공","성공")

                            val intent = Intent(this@LoginActivity,MainActivity::class.java)
                            intent.putExtra("user",user)
                            startActivity(intent)

                        }
                        401 -> { // 로그인 실패
                            Toast.makeText(this@LoginActivity,"회원가입이 필요",Toast.LENGTH_LONG).show()
                            Log.d("code",response.code().toString())
                        }
                        404 -> { // 서버 오류
                            Toast.makeText(this@LoginActivity,"서버 이상",Toast.LENGTH_LONG).show()
                            Log.d("code",response.code().toString())
                        }
                    }

                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(this@LoginActivity,"로그인 실패",Toast.LENGTH_LONG).show()
                    Log.d("실패 로그",t.toString())
                }
            })

        }


    }
}