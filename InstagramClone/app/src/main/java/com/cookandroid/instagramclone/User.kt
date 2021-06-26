package com.cookandroid.instagramclone

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    var email: String?,
    var password: String?,
    var accessToken: String?,
    var accessTokenExpiresIn: Long,
    var grantType: String?,
    var refreshToken: String?,
    var nickname: String?,
    var expiresTime: String?
) : Parcelable




/*{
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(email)
        dest?.writeString(password)
        dest?.writeString(accessToken)
        dest?.writeLong(accessTokenExpiresIn)
        dest?.writeString(grantType)
        dest?.writeString(refreshToken)
        dest?.writeString(nickname)
        dest?.writeString(expiresTime)
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}
*/