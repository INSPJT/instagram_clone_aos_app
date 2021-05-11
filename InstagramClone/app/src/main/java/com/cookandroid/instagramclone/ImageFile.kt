package com.cookandroid.instagramclone

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import java.io.File

class ImageFileData(var uri: String?) : Parcelable {
    constructor(parcel: Parcel) : this(uri = parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uri)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ImageFileData> {
        override fun createFromParcel(parcel: Parcel): ImageFileData {
            return ImageFileData(parcel)
        }

        override fun newArray(size: Int): Array<ImageFileData?> {
            return arrayOfNulls(size)
        }
    }
}

data class ImageData(
   var uri:String,
   var bitmap: Bitmap?,
   var file: File
)

