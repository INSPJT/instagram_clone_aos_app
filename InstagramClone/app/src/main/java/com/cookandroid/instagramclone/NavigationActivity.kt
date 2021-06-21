package com.cookandroid.instagramclone

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main_navigation.*
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.android.synthetic.main.activity_navigation.bottom_navigation
import kotlinx.android.synthetic.main.main_navigation.*

class NavigationActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var fragment: Fragment? = when(item.itemId){
                R.id.action_home ->{
                    MainNavigationActivity()
                }
                R.id.action_favorate ->{
                    null
                }
                R.id.action_account -> {
                    GetUserPostActivity()
                }
                R.id.action_photo -> {
                    AddPhotoActivity()
                }
                R.id.action_search -> {
                    SearchFragment()
                }
                else ->null
            }
        return fragment?.let{
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_content, fragment)
                .addToBackStack(null)
                .commit()
            true
        } ?: false
    }

    val TAG = "navigation"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        var user = intent.getParcelableExtra<User>("user")

        user?.accessToken?.let{ InternetService.userToken = user.accessToken!!}
        val retrofitService = RetrofitImageService("", "retrofit manager")
        InternetService.setInternetBase(retrofitService)

        Toast.makeText(this, "${InternetService.userToken}",Toast.LENGTH_LONG).show()
        bottom_navigation.setOnNavigationItemSelectedListener(this)
        var home = MainNavigationActivity()
        supportFragmentManager.beginTransaction().replace(R.id.main_content, home).commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        INTERNET_REQUEST.activityResult?.let{it(requestCode, resultCode, data)}
        super.onActivityResult(requestCode, resultCode, data)
    }
}
