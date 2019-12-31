package com.common.rxretrofit

import android.os.Handler
import android.os.Looper
import com.alibaba.fastjson.JSON
import com.common.log.MyLog
import okhttp3.*
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


val hanlder = Handler(Looper.getMainLooper())

fun httpGet(url:String,params:HashMap<String,Any?>?,callback:(r:String?)->Unit?){
    val reqBuild = Request.Builder()
    val urlBuilder = HttpUrl.parse(url)?.newBuilder()
    if(params!=null){
        for(k in params?.keys){
            urlBuilder?.addQueryParameter(k, params?.get(k)?.toString())
        }
    }
    reqBuild.url(urlBuilder?.build())
    var call = ApiManager.getInstance().httpClient.newCall(reqBuild.build())
    call.enqueue(object :Callback{
        override fun onFailure(call: Call, e: IOException) {
            //MyLog.d("ApiSimple", "onFailure call = $call, e = $e")
            hanlder.post {
                var result = ApiResult()
                result.errno = -1
                if (e is SocketTimeoutException) {
                    result.errmsg = "SocketTimeoutException"
                } else if (e is UnknownHostException) {
                    result.errmsg = "UnknownHostException"
                }
                callback?.invoke(JSON.toJSONString(result))
            }
        }

        override fun onResponse(call: Call, response: Response) {
            //MyLog.d("ApiSimple", "onResponse call = $call, response = $response")
            hanlder.post {
                if(response.code() == 200){
                    callback?.invoke(response.body()?.string())
                }else{
                    callback?.invoke(response.body()?.string())
                }
            }

        }
    })
}