package com.cookandroid.instagramclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import java.io.File

data class GoogleServiceCreateData(val name: String, val file: File)
data class GoogleServiceInitData(val compact:AppCompatActivity, val activityResult: ((Int, Int, Intent?)->Unit)? = null)
