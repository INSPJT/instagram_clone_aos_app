package com.cookandroid.instagramclone

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import kotlinx.android.synthetic.main.activity_main_navigation.*
import java.io.File
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment


class MainNavigationActivity : Fragment() {
    val TAG = "MainNavigationActivity"
    var files = ArrayList<String>()
//    var googleService: GoogleServiceManager? = null
    companion object{
        var imageView:ImageView ? = null
//        var cont: Context? = null
    }

    var onFilesGet = {data:Any?->
        data?.let {
            var array = it as ArrayList<*>
            if (array.size > 0 && array[0] is String) {
                try {
                    Glide.with(this).load(InternetService.internetBase?.baseUrl + array[0])
                        .into(myProfile)
                } catch(e: Exception){
                    Log.e("file get error", "${e.message}")
                }
            }
//        array.forEach{Log.d(TAG, "file id get id: " + it); Glide.with(this).load(googleImageUrl+it).into(myProfile)}
        }
        Unit
    }

    var onReadFileGet = {data:Any? ->
        Log.d(TAG, "file content get content: " +  data as String)
        Unit
    }

//    var onLoginHandle = {
//        InternetService.fileList(func = onFilesGet)
//        Unit
//    }

//    var onLoginSuccess = {intent: Any?->
//        startActivityForResult(intent as Intent, INTERNET_REQUEST.REQUEST_CODE_SIGN_IN)
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.activity_main_navigation, container, false)
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main_navigation)
        imageView = myProfile
//        cont = this

        try {
//            googleService = GoogleServiceManager()
//            InternetService.setInternetBase(googleService!!)
//            InternetService.asGoogleServiceManager()
//                .init(GoogleServiceInitData(cont as AppCompatActivity){_:Int,_:Int,_:Intent?-> return@GoogleServiceInitData},onLoginSuccess)
        } catch(e: Exception){
            Log.d(TAG, e.message)
        }
        if(MyProfile.userProfileInitialized()){
            imageView?.setImageBitmap(MyProfile.userProfile)
            Log.e(TAG, "initialized")
        }
        else{
            Log.e(TAG, "not initialized")
        }
        return view
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        activity?.let {
//            when (requestCode) {
//                INTERNET_REQUEST.REQUEST_CODE_SIGN_IN -> {
//                    if (resultCode == Activity.RESULT_OK && data != null) {
//                        InternetService.asGoogleServiceManager()
//                            .handleSignInResult(it, data, onLoginHandle)
//                    }
//                }
//                INTERNET_REQUEST.REQUEST_CODE_OPEN_DOCUMENT -> {
//                    if (resultCode == Activity.RESULT_OK && data != null) {
//                        val uri = data.data
//                        if (uri != null) {
//                            InternetService.asGoogleServiceManager()?.openFIleFromFilePicker(it, uri)
//                        }
//                    }
//                }
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data)
//    }

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
            }
            try {
                Glide.with(holder.itemView.context).asBitmap().load(file).into(object: CustomTarget<Bitmap>(){
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                    ) {
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
