package com.cookandroid.instagramclone

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log
import android.util.Pair;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class DriverServiceHelper(private val driveService: Drive) {
    private final val mExecutor = Executors.newSingleThreadExecutor()
    val TAG = "Google driver helper"
    fun createFile(name:String = "defulat", file: java.io.File): Task<String> {
        return Tasks.call(mExecutor, Callable {
            val metaData= File()
                .setName("$name")
                .setParents(listOf("18PipmTk0vVGT_D_Lk2dLAKBu-TwTH3qm"))
            var mediaContent = FileContent("image/jpeg", file)
            var googleFile = driveService.files().create(metaData, mediaContent).setFields("id").execute()

            Log.d("Google Driver", "${googleFile.id}")
            googleFile.id
        })
    }

    fun readFile(fileId: String): Task<Pair<String, String>> {
        return Tasks.call(mExecutor, Callable {
            val metadata = driveService.files().get(fileId).execute()
            val name = metadata.name

            var res = Pair<String, String>("", "")
            try {
                val ls = driveService.files().get(fileId).executeMediaAsInputStream()
                val reader = BufferedReader(InputStreamReader(ls))
                var stringBuilder = StringBuilder()
                var line: String? = ""
                while (line != null) {
                    line = reader.readLine()
                    stringBuilder.append(line)
                }
                val contents = stringBuilder.toString()
                res = Pair.create(name!!, contents!!)
                res
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
