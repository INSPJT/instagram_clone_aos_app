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
import androidx.constraintlayout.motion.widget.MotionScene.Transition as Transition1

class AddPhotoActivity : AppCompatActivity() {
    val REQUEST_CODE_SIGN_IN = 1
    val REQUEST_CODE_OPEN_DOCUMENT = 2
    val TAG = "AddPhotoActivity"
    var mDriveServiceHelper: DriverServiceHelper? = null
    var imageSelected: ImageData? = null

    private fun requestSignIn() {
        Log.d(TAG, "Requesting Sign in")

        var signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        var client = GoogleSignIn.getClient(this, signInOptions)

        startActivityForResult(client.getSignInIntent(), REQUEST_CODE_SIGN_IN)
    }

    private fun handleSignInResult(result: Intent) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
            .addOnSuccessListener{
                Log.d(TAG, "singed in as "+it.email)
                var credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(DriveScopes.DRIVE_FILE))
                credential.selectedAccount = it.account
                var googleDriverService = Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory(),
                    credential)
                    .setApplicationName("Drive API Migration")
                    .build()
                mDriveServiceHelper = DriverServiceHelper(googleDriverService)
                mDriveServiceHelper?.queryFile()?.addOnSuccessListener {
                    Log.d(TAG,"read file success")
                }?.addOnFailureListener{
                    Log.d(TAG,"read files Failed")
                }
            }
            .addOnFailureListener{
                    exception -> Log.e(TAG,"Unable to sign in.",exception)
            }
    }

    fun openFIleFromFilePicker(uri: Uri){
        if(mDriveServiceHelper != null){
            Log.d(TAG, "Opening " + uri.path)
            mDriveServiceHelper?.openFileUsingStorageAccessFramework(contentResolver, uri)
        }
        else{
            Log.d(TAG, "Opening " + uri.path + " failed")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            REQUEST_CODE_SIGN_IN-> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    handleSignInResult(data)
                }
            }
            REQUEST_CODE_OPEN_DOCUMENT->{
                if(resultCode == Activity.RESULT_OK && data != null) {
                    val uri = data.getData()
                    if(uri!=null){
                        openFIleFromFilePicker(uri)
                    }
                }
            }
        }
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

        requestSignIn()

        add_photo_btn.setOnClickListener{
            var sdf = SimpleDateFormat("yyyy-MM-dd:HH:mm:ss.SSS")
            var name = sdf.format(Date())
            if(imageSelected != null) mDriveServiceHelper?.createFile(name, imageSelected!!.file)
            else throw(IOException("image file is null"))
            Log.d(TAG, "Upload id: $name")
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

