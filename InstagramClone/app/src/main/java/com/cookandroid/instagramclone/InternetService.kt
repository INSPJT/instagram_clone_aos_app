package com.cookandroid.instagramclone

import android.content.Intent
import java.io.IOException
import java.util.*

interface InternetServiceClass{
    val baseUrl: String
    val TAG: String
    fun createFile(data: Any? = null)
    fun init(data: Any? = null, func:((Any?)->Unit)?=null)
    fun handlePermission(data: Any? = null)
    fun fileList(data: Any? = null, func: ((Any?)->Unit)?): ArrayList<String>
    fun readFile(data: Any? = null,func: ((Any?)->Unit)?=null): String
}

object InternetService {
    val TEMP_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyMiIsImF1dGgiOiJST0xFX1VTRVIiLCJleHAiOjE2MjEyNzQxOTR9.X2hv6uPeepH0UshVaT4syUrvFQm2fDHG5cIGusQ8PsXpHwxZOMctM0YMRkUBMzt9RdCbhjFj8RazVn0li4wU4Q"
    var internetBase: InternetServiceClass? = null
    fun createFile(data: Any? = null) {
        internetBase?.createFile(data)
    }
    fun init(data: Any? = null,func:((Any?)->Unit)?=null) = internetBase?.init(data,func)
    fun handlePermission(data:Any? = null) {internetBase?.handlePermission(data)}
    fun asGoogleServiceManager(): GoogleServiceManager {return internetBase as GoogleServiceManager}
    fun setInternetBase(base: InternetServiceClass): InternetService {
        internetBase = base
        return this
    }
    fun fileList(data: Any? = null,func:((Any?)->Unit)? = null):ArrayList<String> {
        return internetBase?.fileList(data,func) ?: ArrayList()
    }
    fun readFile(data: Any?=null, func: ((Any?)->Unit)?=null): String {return internetBase?.readFile(data,func) ?: throw IOException("result is null")
    }
    fun getBaseUrl(): String{
        return internetBase?.baseUrl ?: throw(IOException("null pointer exception"))
    }
}

object INTERNET_REQUEST{
    val REQUEST_CODE_SIGN_IN:Int = 1
    val REQUEST_CODE_OPEN_DOCUMENT:Int = 2
    var activityResult : ((Int, Int, Intent?)->Unit)?  = null
}
