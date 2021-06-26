package com.cookandroid.instagramclone


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private val sharedManager: SharedManager by lazy { SharedManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val retrofit  = InternetCommunication.getRetrofitGson()
        var retrofitService= retrofit.create(LoginService::class.java)

        val prevUser = sharedManager.getCurrentUser() // 현재 단말에서 접속하고 있는 유저 정보 (한번이라도 로그인하면 생김) -> 아직 로그아웃은 구현 안함

        Log.d("current user check", prevUser.email.toString())

        if(prevUser.email != ""){ // 일단 email 하나만 체크 기본 디폴트는 "" 이기 때문
            // 여기서 추가적으로 더 나아가야할 부분은 accesstoken 이나 expire 부분들 처리 해야하는거 추가될 예정
            val intent =
                Intent(this@LoginActivity, NavigationActivity::class.java)
            intent.putExtra("user", prevUser)
            startActivity(intent)
        }

        login_btn.setOnClickListener {
                val text_email = input_email.text.toString()
                val text_pw = input_pw.text.toString()
                val Body = HashMap<String, String>()

                Body.put("email", text_email)
                Body.put("password", text_pw)

                try {
                    retrofitService!!.Login(Body).enqueue(object : Callback<User> {
                        override fun onResponse(
                            call: Call<User>,
                            response: Response<User>
                        ) { // 통신 성공
                            when (response.code()) {
                                200 -> { // 성공

                                    val currentUser = User("","","",0,"","","","").apply{
                                        email = text_email
                                        password = text_pw
                                        accessToken = response.body()!!.accessToken
                                        accessTokenExpiresIn = response.body()!!.accessTokenExpiresIn
                                        grantType = response.body()!!.grantType
                                        refreshToken = response.body()!!.refreshToken
                                        nickname = response.body()!!.nickname
                                        expiresTime = response.body()!!.expiresTime
                                    }
                                    sharedManager.saveCurrentUser(currentUser)

                                    Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_LONG)
                                        .show()
                                    Log.d("성공", "성공")
                                    val intent =
                                        Intent(this@LoginActivity, NavigationActivity::class.java)
                                    intent.putExtra("user", currentUser)
                                    startActivity(intent)
                                }
                                401 -> { // 로그인 실패
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "회원가입이 필요",
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                    Log.d("code", response.code().toString())
                                }
                                404 -> { // 서버 오류
                                    Toast.makeText(this@LoginActivity, "서버 이상", Toast.LENGTH_LONG)
                                        .show()
                                    Log.d("code", response.code().toString())
                                }
                            }

                        }

                        override fun onFailure(call: Call<User>, t: Throwable) {
                            Toast.makeText(this@LoginActivity, "로그인 실패", Toast.LENGTH_LONG).show()
                            Log.d("실패 로그", t.toString())
                        }
                    })
                }
                catch(e:Exception){
                    Log.d("버튼 오류", e.message)
                }
        }

        register_btn.setOnClickListener{
            val intent = Intent(this@LoginActivity, LoginRegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
