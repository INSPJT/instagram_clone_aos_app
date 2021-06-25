package com.cookandroid.instagramclone

import android.os.Parcel
import android.os.Parcelable

data class MediaUrls(
    var mediaUrlId:Int,
    var url: String,
    var type: String
)

data class UserPostData(
    var postId: Long,
    var mediaUrls: ArrayList<MediaUrls>,
    var content: String?,
    var likeCount: Int,
    var commentCount: Int,
    var createdAt: String,
    var modifiedAt: String,
    var isLike: Boolean
)

data class MemberDTO(
    var nickname: String? = "",
    var displayId: String? = "",
    var profileImageUrl: String? = "",
    var introduction: String? = "",
    var isFollow: Boolean = false
): Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(p0: Parcel?, p1: Int) {
        p0?.writeString(nickname)
        p0?.writeString(displayId)
        p0?.writeString(profileImageUrl)
        p0?.writeString(introduction)
        p0?.writeByte(if(isFollow)1.toByte() else 0.toByte())
    }

    override fun describeContents(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object CREATOR : Parcelable.Creator<MemberDTO> {
        override fun createFromParcel(parcel: Parcel): MemberDTO {
            return MemberDTO(parcel)
        }

        override fun newArray(size: Int): Array<MemberDTO?> {
            return arrayOfNulls(size)
        }
    }
}

data class ProfileResponse(
    var displayId: String? = null,
    var nickname: String?= null,
    var profileImageUrl: String? = null,
    var introduction:String? = null,
    var postCount: Long = 0,
    var followingCount: Long = 0,
    var followerCount: Long= 0,
    var isFollowedByMe: Boolean = false
): Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(displayId)
        parcel.writeString(nickname)
        parcel.writeString(profileImageUrl)
        parcel.writeString(introduction)
        parcel.writeLong(postCount)
        parcel.writeLong(followingCount)
        parcel.writeLong(followerCount)
        parcel.writeByte(if(isFollowedByMe) 1.toByte() else 0.toByte())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProfileResponse> {
        override fun createFromParcel(parcel: Parcel): ProfileResponse {
            return ProfileResponse(parcel)
        }

        override fun newArray(size: Int): Array<ProfileResponse?> {
            return arrayOfNulls(size)
        }
    }

}