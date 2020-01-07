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


private fun processRequest(req:Request,callback:(r:String?)->Unit?){
    var call = ApiManager.getInstance().httpClient.newCall(req)
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

fun httpGet(url:String,params:HashMap<String,Any?>?,callback:(r:String?)->Unit?){
    val reqBuild = Request.Builder()
    val urlBuilder = HttpUrl.parse(url)?.newBuilder()
    if(params!=null){
        for(k in params?.keys){
            urlBuilder?.addQueryParameter(k, params?.get(k)?.toString())
        }
    }
    reqBuild.url(urlBuilder?.build())
    processRequest(reqBuild.build(),callback)
}


fun httpPost(url:String,params:HashMap<String,Any?>?,callback:(r:String?)->Unit?){
    var content = "";
    if(params!=null){
        content = JSON.toJSONString(params);
    }
    val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), content)
    val request = Request.Builder().url(url).post(body).build()
    processRequest(request,callback)
}


fun httpPut(url:String,params:HashMap<String,Any?>?,callback:(r:String?)->Unit?){
    var content = "";
    if(params!=null){
        content = JSON.toJSONString(params);
    }
    val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), content)
    val request = Request.Builder().url(url).put(body).build()
    processRequest(request,callback)
}