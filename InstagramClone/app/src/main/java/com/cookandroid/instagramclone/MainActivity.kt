package com.cookandroid.instagramclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_add_photo)
        //setContentView(R.layout.post_explanation)

        setContentView(R.layout.posting)

        val user = intent.getParcelableExtra<User>("user")

        Log.i("accessToken",user!!.accessToken.toString())
        Log.i("email",user.email.toString())
        Log.i("pw",user.password.toString())

    }
}
