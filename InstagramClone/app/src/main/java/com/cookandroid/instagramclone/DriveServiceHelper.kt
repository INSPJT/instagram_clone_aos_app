package com.cookandroid.instagramclone

import android.app.Activity
import android.content.ContentResolver;
import android.content.Context
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri;
import android.os.AsyncTask
import android.provider.OpenableColumns;
import android.util.Log
import android.util.Pair;
import androidx.core.app.ActivityCompat.startActivityForResult
import com.bumptech.glide.Glide
import com.bumptech.glide.Glide.init
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import okhttp3.internal.wait
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import kotlin.collections.ArrayList

class DriverServiceHelper(private val driveService: Drive) {
    private val sdf = SimpleDateFormat("yyyy-MM-dd:HH:mm:ss.SSS")
    private final val mExecutor = Executors.newSingleThreadExecutor()
    val TAG = "Google driver helper"
    fun createFile(name:String = "defulat", file: java.io.File): Task<String> {
        return Tasks.call(mExecutor, Callable {
            val metaData= File()
                .setName("$name")
                .setParents(listOf("18PipmTk0vVGT_D_Lk2dLAKBu-TwTH3qm"))
            var mediaContent = FileContent("image/jpeg", file)
            var googleFile = driveService.files().create(metaData, mediaContent).setFields("id").execute()

            googleFile.id
        })
    }
    data class readFileData(val fileId: String, val cont: Context)

    fun readFile(fileId: readFileData): Task<Pair<String, String>> {
        return Tasks.call(mExecutor, Callable {
            val metadata = driveService.files().get(fileId.fileId).execute()
            val name = metadata.name

            var res = Pair<String, String>("", "")
            try {
                val inputStream = driveService.files().get(fileId.fileId).executeMediaAsInputStream()
//                var os = ByteArrayOutputStream()
//                driveService.files().get(fileId).executeAndDownloadTo(os)
//                var str = os.toString()
//                Log.d(TAG, str)
//                if(MainNavigationActivity.imageView != null && MainNavigationActivity.cont != null){
//                    Glide.with(cont!!)
//                        .asBitmap()
//                        .load(os.toByteArray())
//                        .into(object: CustomTarget<Bitmap>(){
//                            override fun onLoadCleared(placeholder: Drawable?) {
//                            }
//
//                            override fun onResourceReady(
//                                resource: Bitmap,
//                                transition: Transition<in Bitmap>?
//                            ) {
//                                MainNavigationActivity.imageView!!.setImageBitmap(resource)
//                            }
//
//                        })
//                }

                val reader = BufferedReader(InputStreamReader(inputStream))
                var stringBuilder = StringBuilder()
                var line: String? = ""
                while (line != null) {
                    line = reader.readLine()
                    stringBuilder.append(line)
                }
                val contents = stringBuilder.toString()
                val byte = contents.toByteArray()
                if(MainNavigationActivity.imageView != null){
                    Glide.with(fileId.cont)
                        .asBitmap()
                        .load(byte)
                        .into(object: CustomTarget<Bitmap>(){
                            override fun onLoadCleared(placeholder: Drawable?) {
                            }

                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                MainNavigationActivity.imageView!!.setImageBitmap(resource)
                            }

                        })
                }
                res = Pair.create(name!!, contents!!)
                return@Callable res
            } catch (e: Exception) {
                Log.d("Google Drive", "Google Drive Read Error " + e.message)
            }
            res
        })
    }

    fun saveFile(fileId: String, name: String, content: String): Task<Nothing?> {
        return Tasks.call(mExecutor, Callable {
            var metadata = File().setName(name)
            var contentStream = ByteArrayContent.fromString("text/plain", content)
            driveService.files().update(fileId, metadata, contentStream).execute()
            null
        })
    }

    fun queryFile(): Task<FileList> {
        Log.d(TAG,"query")
        return Tasks.call(
            mExecutor,
            Callable<FileList> { driveService.files().list().setSpaces("drive").execute() })
    }

    fun createFilePickerIntent(): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        return intent
    }

    fun openFileUsingStorageAccessFramework(
        contentResolver: ContentResolver,
        uri: Uri
    ): Task<Pair<String, String>> {
        return Tasks.call(mExecutor, Callable {
            var name: String? = null
            try {
                var cursor = contentResolver.query(uri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    var nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    name = cursor.getString(nameIndex)
                } else {
                    throw IOException("Empty cursor returned for file.")
                }
            } catch (e: Exception) {
                Log.d("Google API", e.message)
            }
            var content: String? = null
            try {
                var iStream = contentResolver.openInputStream(uri)
                var reader = BufferedReader(InputStreamReader(iStream))
                var sb = StringBuilder()
                var line: String? = ""
                while (line != null) {
                    line = reader.readLine()
                    sb.append(line)
                }
            } catch (e: Exception) {
                Log.d("Google Drive", "FIle Storace Acess was failed -> " + e.message)
            }

            Pair.create(name ?: "", content ?: "")
        })
    }
}


class GoogleServiceManager: InternetServiceClass{
    override val baseUrl: String
        get() = "https://drive.google.com/uc?export=view&id="
    private lateinit var driverServiceHelper: DriverServiceHelper

    override fun readFile(data: Any?,func:((Any?)->Unit)?): String {
        if(!(data is String)) {
            throw IOException("data is not string")
        }

        var res: String = ""
        driverServiceHelper.readFile(data as DriverServiceHelper.readFileData).addOnSuccessListener {
            Log.d(TAG, "read file success name: ${it.first} content: ${it.second}")
            if(func!=null) func(it.second)
        }.addOnFailureListener {
            Log.d(TAG, "read file failed: "+ it.message)
        }

        return res
    }

    override fun fileList(data: Any?, func:((Any?)->Unit)?): ArrayList<String> {
        var ret= ArrayList<String>()
        driverServiceHelper.queryFile().addOnSuccessListener {
            Log.d(TAG, "read file list success")
            for(item in it.files) {
                ret.add(item.id)
            }
            if(func != null) func(ret)
        }.addOnFailureListener{
            Log.d(TAG,"read file list failed")
        }
        return ret
    }

    override val TAG: String
        get() = "GoogleServiceManager"

    override fun handlePermission(data: Any?) {
        var a = Pair<String,String>("haha", "hho")
    }

    override fun init(data: Any?,func: ((Any?)->Unit)?) {
        var compact = (data as GoogleServiceInitData).compact
        INTERNET_REQUEST.activityResult = (data as GoogleServiceInitData).activityResult

        var client = GoogleSignIn.getClient(compact, setGoogleSignInOption())
        if(func != null) func(client.signInIntent)
        compact.startActivityForResult(client.signInIntent, INTERNET_REQUEST.REQUEST_CODE_SIGN_IN)
    }

    inner class FileProcessTask(var func:(url:ArrayList<String>)->Unit = {}) : AsyncTask<ArrayList<FileData>, ArrayList<FileData>, ArrayList<String>>(){
        override fun doInBackground(vararg p0: ArrayList<FileData>?): ArrayList<String> {
            var tasks = ArrayList<Task<String>>()
            var urls = ArrayList<String>()
            p0.forEach{p->
                p?.forEach{data->
                    tasks.add(driverServiceHelper.createFile(data.name, data.file).addOnSuccessListener {
                        Log.d(TAG, "create file - name : ${data.name}, id: $it")
                        urls.add("${InternetService.getBaseUrl()}$it")
                    }.addOnFailureListener{
                        Log.d(TAG, "create file failed")
                    })
                }
            }
            for(p in tasks) {
                Tasks.await(p)
            }
            return urls
        }

        override fun onPostExecute(result: ArrayList<String>?) {
            super.onPostExecute(result)
            Log.d(TAG, "task completed")
            result?.let{func(result)}
        }
    }
    override fun createFile(data: Any?) {
        if(data !is GoogleServiceCreateData) throw IOException("Data Type invalid")
        var fileList = data.files
        var task = FileProcessTask(data.func)
        task.execute(fileList)
    }

    private fun setGoogleSignInOption(): GoogleSignInOptions {
         return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
    }
    fun handleSignInResult(context: Context, result: Intent, caller:(()->Unit)?=null) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
            .addOnSuccessListener{
                Log.d(TAG, "singed in as "+it.email)
                var credential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(DriveScopes.DRIVE_FILE))
                credential.selectedAccount = it.account
                var googleDriverService = Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory(),
                    credential)
                    .setApplicationName("Drive API Migration")
                    .build()
                driverServiceHelper = DriverServiceHelper(googleDriverService)
                if(caller != null) caller()

//                var fileList = InternetService.fileList{
//
//                }
//                var content = ArrayList<String>()
//                fileList.forEach { content.add(InternetService.readFile(it)); Log.d(TAG,it) }
//                content.forEach { Log.d(TAG, it) }
            }
            .addOnFailureListener{
                    exception -> Log.e(TAG,"Unable to sign in.",exception)
            }
    }

    fun openFIleFromFilePicker(context: Context, uri: Uri){
        if(driverServiceHelper != null){
            Log.d(TAG, "Opening " + uri.path)
            driverServiceHelper?.openFileUsingStorageAccessFramework(context.contentResolver, uri)
        }
        else{
            Log.d(TAG, "Opening " + uri.path + " failed")
        }
    }
}
