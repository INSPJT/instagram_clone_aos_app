package com.cookandroid.instagramclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login_register.*
import okhttp3.internal.concurrent.TaskRunner.Companion.logger
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class LoginRegisterActivity : AppCompatActivity() {
    private val retrofit = InternetCommunication.getRetrofitString()
    private val retrofitService = retrofit.create(LoginRegister::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_register)

        register_btn.setOnClickListener {
            val text_email = email.text.toString()
            val text_pw = password.text.toString()
            val display_id = display_id.text.toString()
            val nickname = nickname.text.toString()

            val body = RegisterData(text_email, text_pw, display_id, nickname)

            try {
                   retrofitService.Register(body).enqueue(object : Callback<String> {
                    override fun onResponse(
                        call: Call<String>,
                        response: Response<String>
                    ) { // 통신 성공
                        when (response.code()) {
                            201 -> { // 성공
                                Toast.makeText(
                                    this@LoginRegisterActivity,
                                    "${response.body().toString()}",
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                                Log.d("성공", "성공")
                            }
                            401 -> {//Unauthorized
                                Toast.makeText(
                                    this@LoginRegisterActivity,
                                    "Unauthorized",
                                    Toast.LENGTH_LONG
                                ).show()
                                Log.d("code", response.code().toString())

                            }
                            403 -> {//Forbidden
                                Toast.makeText(
                                    this@LoginRegisterActivity,
                                    "Forbiden",
                                    Toast.LENGTH_LONG
                                ).show()
                                Log.d("code", response.code().toString())

                            }
                            404 -> { // 서버 오류
                                Toast.makeText(
                                    this@LoginRegisterActivity,
                                    "서버 이상",
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                                Log.d("code", response.code().toString())
                            }
                            409 -> { //이미 존재
                                Toast.makeText(
                                    this@LoginRegisterActivity,
                                    "이미 존재하는 아이디입니다.",
                                    Toast.LENGTH_LONG
                                ).show()
                                Log.d("code", response.code().toString())
                            }
                        }

                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Toast.makeText(this@LoginRegisterActivity, "로그인 실패", Toast.LENGTH_LONG)
                            .show()
                        Log.d("실패 로그", t.toString())
                    }
                })
            }
            catch(e:Exception){
                Log.d("CRUD 실패", e.message)
            }
        }
    }
}
