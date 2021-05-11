package com.cookandroid.instagramclone

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.motion.widget.MotionScene
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Transition
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.android.synthetic.main.activity_add_photo.*
import kotlinx.android.synthetic.main.activity_add_photo.view.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AddPhotoActivity : AppCompatActivity() {
    val TAG = "AddPhotoActivity"
    var imageSelected: ImageData? = null
    private val sdf = SimpleDateFormat("yyyy-MM-dd:HH:mm:ss.SSS")

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(INTERNET_REQUEST.activityResult != null) INTERNET_REQUEST.activityResult!!(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun getPathOfAllImages(): ArrayList<String> {
        var result: ArrayList<String> = ArrayList()
        var uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val cursor = contentResolver.query(
            uri,
            null,
            null,
            null,
            MediaStore.MediaColumns.DATE_ADDED + " desc"
        )
        if (cursor != null) {
            while (cursor.moveToNext())
                result.add(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)))
            cursor.close()
        }
        return result
    }

    private fun checkPermissions() {
        var writePermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        var readPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        var cameraPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (writePermission == PackageManager.PERMISSION_DENIED || readPermission == PackageManager.PERMISSION_DENIED || cameraPermission == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                ),
                100
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)
        checkPermissions()
        val imageList = getPathOfAllImages()
        addPhotoRecyclerView.adapter = UserFragmentRecyclerViewAdapter(imageList)
        addPhotoRecyclerView.layoutManager = GridLayoutManager(this, 3)

        val googleService = GoogleServiceManager()
        InternetService.setInternetBase(googleService).init(GoogleServiceInitData(this)
            {requestCode: Int, resultCode: Int, data: Intent? ->
                when (requestCode) {
                    INTERNET_REQUEST.REQUEST_CODE_SIGN_IN -> {
                        if (resultCode == Activity.RESULT_OK && data != null) {
                            InternetService.asGoogleServiceManager()
                                .handleSignInResult(this, data)
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
        add_photo_btn.setOnClickListener{
            var name = sdf.format(Date())
            if(imageSelected != null) {
                InternetService.createFile(GoogleServiceCreateData(name, imageSelected!!.file))
            }
            else throw(IOException("image file is null"))
        }
    }

    inner class UserFragmentRecyclerViewAdapter(private var images: ArrayList<String>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3
            var imageView = ImageView(parent.context)
            imageView.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            return CustomViewHolder(imageView)
        }

        inner class CustomViewHolder(var imageView: ImageView) :
            RecyclerView.ViewHolder(imageView) {
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageView = (holder as CustomViewHolder).imageView
            var file = File(images[position])

            var load = Glide.with(holder.itemView.context).load(file).apply(RequestOptions().centerCrop())
            var bitmap: Bitmap? = null
            load.into(imageView)
            imageView.setOnClickListener {
                load.into(selected_image)
                imageSelected = ImageData(uri = images[position], file = file, bitmap = bitmap)
            }
            try {
                Glide.with(holder.itemView.context).asBitmap().load(file).into(object: CustomTarget<Bitmap>(){
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                    ) {
                        if(imageSelected?.uri == images[position]) imageSelected?.bitmap = resource
                        bitmap = resource
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        bitmap = null
                    }
                })
            } catch(e: Exception) {
                Log.d(TAG,"convert to bitmap error: " + e.message)
            }
        }

        override fun getItemCount(): Int {
            return images.size
        }
    }
}

