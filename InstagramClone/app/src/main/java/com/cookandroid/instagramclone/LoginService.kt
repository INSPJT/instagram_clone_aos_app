package com.cookandroid.instagramclone

import retrofit2.Call
import retrofit2.http.*

interface LoginService {

    @Headers("accept: application/json",
        "content-type: application/json")
    //@FormUrlEncoded -> Body 쓸땐 이걸 쓰면 오류난다. -> Field쓸때만 쓰면 될듯
    @POST("/auth/signin") // url
    fun Login(
        @Body params : HashMap<String,String>
    ) : Call<User>
<<<<<<< Updated upstream
<<<<<<< Updated upstream
<<<<<<< HEAD
=======



>>>>>>> ce06c286fe5a21d8b9ac4468c379ae822abe7527
=======
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
}