package com.cookandroid.instagramclone

import android.content.Context
import android.content.SharedPreferences
import com.cookandroid.instagramclone.PreferenceHelper.set
import com.cookandroid.instagramclone.PreferenceHelper.get

class SharedManager(context: Context) {

    private val prefs: SharedPreferences = PreferenceHelper.defaultPrefs(context)

    fun saveCurrentUser(user: User) {
        prefs["email"] = user.email
        prefs["password"] = user.password
        prefs["accessToken"] = user.accessToken
        prefs["accessTokenExpiresIn"] = user.accessTokenExpiresIn
        prefs["grantType"] = user.grantType
        prefs["refreshToken"] = user.refreshToken
        prefs["nickname"] = user.nickname
        prefs["expiresTime"] = user.expiresTime
    }

    fun getCurrentUser(): User {
        return User("","","",0,"","","","").apply {
            email = prefs["email", ""]
            password = prefs["password", ""]
            accessToken = prefs["accessToken",""]
            accessTokenExpiresIn = prefs["accessTokenExpiresIn",0]
            grantType = prefs["grantType",""]
            refreshToken = prefs["refreshToken",""]
            nickname = prefs["nickname",""]
            expiresTime = prefs["expiresTime",""]
        }
    }


}
