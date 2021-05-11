package com.cookandroid.instagramclone

class RegisterData(private var email:String, private var password:String, private var displayId:String, private var nickname:String) {
    public fun setEmail(email:String){
        this.email = email
    }
    public fun setPassword(password:String){
        this.password = password
    }
    public fun setDisplayId(displayId:String){
        this.displayId = displayId
    }
    public fun setNickname(nickname:String){
        this.nickname = nickname
    }

    public fun getEmail(): String {
        return email
    }
    public fun getPassword(): String {
        return password
    }
    public fun getDisplayId(): String {
        return displayId
    }
    public fun getNickname(): String {
        return nickname
    }
}

