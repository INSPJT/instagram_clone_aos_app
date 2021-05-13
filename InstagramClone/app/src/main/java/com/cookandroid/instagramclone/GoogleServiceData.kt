package com.cookandroid.instagramclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import java.io.File

data class FileData(val name:String, val file: File)
data class GoogleServiceCreateData(val files: ArrayList<FileData>, val func:(ArrayList<String>)->Unit = {})
data class GoogleServiceInitData(val compact:AppCompatActivity, val activityResult: ((Int, Int, Intent?)->Unit)? = null)
