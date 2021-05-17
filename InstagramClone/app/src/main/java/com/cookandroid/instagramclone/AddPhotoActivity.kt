package com.cookandroid.instagramclone

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
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
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
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
import kotlinx.android.synthetic.main.add_viewholder.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AddPhotoActivity : AppCompatActivity() {
    val TAG = "AddPhotoActivity"
    private val sdf = SimpleDateFormat("yyyy-MM-dd:HH:mm:ss.SSS")

    fun sendURL(url: String){
        Log.d(TAG, "Send URL $url")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        INTERNET_REQUEST.activityResult?.let{
            Log.d(TAG, "on Activity Result")
            it(requestCode, resultCode, data)
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

    var sendUrlToServer = {url:ArrayList<String> ->
        Log.d(TAG, "send url to server")
        var retrofit = InternetCommunication.getRetrofitString()
        var retrofitService = retrofit.create(PostService::class.java)

        var service = retrofitService?.post(PostData("test", url))

        service?.enqueue(object: Callback<String>{
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d(TAG, "fail ${t.message}")
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                var message:String = when(response.code()){
                    200-> {
                        Toast.makeText(
                            this@AddPhotoActivity,
                            "${response.body().toString()}",
                            Toast.LENGTH_LONG
                        )
                        "Sucess"
                    }
                    401-> "Unauthorized"
                    403-> "Forbidden"
                    404-> "Not Found"
                    else-> "Failed"
                }
                Log.d(TAG, "send uri to server $message")
            }
        })
        Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)
        checkPermissions()
        val imageList = getPathOfAllImages()
        addPhotoRecyclerView.adapter = UserFragmentRecyclerViewAdapter(imageList)
        addPhotoRecyclerView.layoutManager = GridLayoutManager(this, 3)
        TokenManager.addTokenHeader(InternetService.TEMP_TOKEN)

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

        multiClickBtn.setOnClickListener {
            var adapter = addPhotoRecyclerView.adapter as UserFragmentRecyclerViewAdapter
            adapter.checkedViewHolder.clear()
            adapter.notifyDataSetChanged()
            adapter.visible = !adapter.visible
        }

        add_photo_btn.setOnClickListener{
            var imageSelected = (addPhotoRecyclerView.adapter as UserFragmentRecyclerViewAdapter).checkedViewHolder
            var fileList = ArrayList<FileData>()
            imageSelected.forEach{ it2 ->
                it2.imageData?.let{
                    var name = sdf.format(Date())
                    fileList.add(FileData(name, it.file))
                } ?: throw(IOException("image file is null"))
            }
            InternetService.createFile(GoogleServiceCreateData(fileList,sendUrlToServer))
//            {urls:ArrayList<String>->
//                var str = ""
//                urls.forEach{str += "\n$it" }
//                Log.d(TAG,"complete tasks\n$str")
//            })
        }
    }

    inner class UserFragmentRecyclerViewAdapter(private var images: ArrayList<String>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var visible = false
        var checkedViewHolder = ArrayList<CustomViewHolder>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3
            var view = LayoutInflater.from(parent.context).inflate(R.layout.add_viewholder,parent, false)
            view.layoutParams = LinearLayoutCompat.LayoutParams(width, width)

            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
            var imageData : ImageData? = null
            var checkedIdx = 0
            var imageView: ImageView = view.findViewById(R.id.viewHolder_img)
            var checkView: TextView = view.findViewById(R.id.viewHolder_check)
            init {
                checkView.layoutParams.width = view.layoutParams.width/5
                checkView.layoutParams.height = view.layoutParams.width/5
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageView = (holder as CustomViewHolder).imageView
            var viewHolder:CustomViewHolder = holder
            var file = File(images[position])
            var load = Glide.with(holder.itemView.context).load(file).apply(RequestOptions().centerCrop())
            var bitmap: Bitmap? = null
            load.into(imageView)

            if(!visible) viewHolder.checkedIdx=0
            viewHolder.imageData = ImageData(uri = images[position], file = file, bitmap = bitmap)

            viewHolder.view.setOnClickListener {
                load.into(selected_image)
                if(visible){
                    if(viewHolder.checkedIdx != 0) {
                        viewHolder.checkView.text = ""
                        checkedViewHolder.removeAt(viewHolder.checkedIdx-1)
                        for(i in (viewHolder.checkedIdx-1) until checkedViewHolder.size){
                            checkedViewHolder[i].checkedIdx = i+1
                            checkedViewHolder[i].checkView.text = (i+1).toString()
                        }
                        viewHolder.checkedIdx = 0
                    }
                    else if(checkedViewHolder.size < 10){
                        checkedViewHolder.add(viewHolder)
                        viewHolder.checkedIdx = checkedViewHolder.size
                        viewHolder.checkView.text = viewHolder.checkedIdx.toString()
                    }
                }
                else {
                    checkedViewHolder.clear()
                    checkedViewHolder.add(viewHolder)
                }
            }
            try {
                if(visible) {
                    holder.checkView.visibility = VISIBLE
                    holder.checkView.text = ""
                }
                else {
                    holder.checkView.visibility = View.INVISIBLE
                }
            } catch(e: Exception) {
                Log.d(TAG,"convert to bitmap error: " + e.message)
            }
        }

        override fun getItemCount(): Int {
            return images.size
        }
    }
}

