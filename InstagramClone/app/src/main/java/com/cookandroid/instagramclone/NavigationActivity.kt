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
    var onFilesGet = {data:Any?->
        data?.let { it ->
            var array = it as ArrayList<*>
            if (array.size > 0 && array[0] is String) {
                try {
                    var bitmap = Glide.with(this).asBitmap().load(InternetService.internetBase?.baseUrl + array[0]).addListener(
                        object: RequestListener<Bitmap>{
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Bitmap>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }

                            override fun onResourceReady(
                                resource: Bitmap?,
                                model: Any?,
                                target: Target<Bitmap>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.e(TAG, "bitmap ready")
                                resource?.let{MyProfile.userProfile = it}
                                return true
                            }
                        }
                    )
                } catch(e: Exception){
                    Log.e("file get error", "${e.message}")
                }
            }
        }
        Unit
    }
    var getMyProfile= {
        InternetService.fileList(func = onFilesGet)
        Unit
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
            var fragment: Fragment? = when(item.itemId){
                R.id.action_home ->{
                    Toast.makeText(this, "click home",Toast.LENGTH_LONG).show()
                    MainNavigationActivity()
                }
                R.id.action_favorate ->{
                    null
                }
                R.id.action_account -> {
                    Toast.makeText(this, "click account",Toast.LENGTH_LONG).show()
                    GetUserPostActivity()
                }
                R.id.action_photo -> {
                    AddPhotoActivity()
                }
                R.id.action_search -> {
                    null
                }
                else ->null
            }
        return fragment?.let{
            supportFragmentManager.beginTransaction().replace(R.id.main_content, fragment).commit()
            true
        } ?: false
    }

    val TAG = "navigation"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        var user = intent.getParcelableExtra<User>("user")

        user?.accessToken?.let{ InternetService.userToken = user.accessToken!!}
        val googleService = GoogleServiceManager()
        InternetService.setInternetBase(googleService).init(GoogleServiceInitData(this)
            {requestCode: Int, resultCode: Int, data: Intent? ->
                when (requestCode) {
                    INTERNET_REQUEST.REQUEST_CODE_SIGN_IN -> {
                        if (resultCode == Activity.RESULT_OK && data != null) {
                            InternetService.asGoogleServiceManager()
                                .handleSignInResult(this, data, getMyProfile)
                        }
                    }
                    INTERNET_REQUEST.REQUEST_CODE_OPEN_DOCUMENT -> {
                        if (resultCode == Activity.RESULT_OK && data != null) {
                            val uri = data.data
                            if (uri != null) {
                                googleService.openFIleFromFilePicker(this, uri)
                            }
                        }
                    }
                }
            }
        )

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
