package com.cookandroid.instagramclone

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable

data class MediaUrls(
    var mediaUrlId:Int,
    var url: String,
    var type: String
)

data class UserPostData(
    var postId: Int,
    var mediaUrls: ArrayList<MediaUrls>,
    var content: String?,
    var likeCount: Int,
    var commentCount: Int,
    var createdAt: String,
    var modifiedAt: String,
    var isLike: Boolean
)

data class MemberDTO(
    var nickname: String?,
    var displayId: String?,
    var profileImageUrl: String?,
    var introduction: String?,
    var isFollow: Boolean
): Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    @SuppressLint("NewApi")
    override fun writeToParcel(p0: Parcel?, p1: Int) {
        p0?.writeString(nickname)
        p0?.writeString(displayId)
        p0?.writeString(profileImageUrl)
        p0?.writeString(introduction)
        p0?.writeBoolean(isFollow)
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
    var memberDto: MemberDTO?,
    var postCount: Int,
    var followerCount: Int,
    var followingCount: Int
): Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(MemberDTO.javaClass.classLoader),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(memberDto, 0)
        parcel.writeInt(postCount)
        parcel.writeInt(followerCount)
        parcel.writeInt(followingCount)
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