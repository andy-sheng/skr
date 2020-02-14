package com.common.rxretrofit

import android.support.v4.util.ArrayMap
import android.text.TextUtils
import android.util.Log
import com.common.core.account.UserAccountManager
import com.common.log.MyLog
import com.common.utils.U
import retrofit2.Call
import retrofit2.Response
import retrofit2.adapter.rxjava2.HttpException
import retrofit2.await
import java.net.SocketTimeoutException
import java.net.UnknownHostException

val requestMap = ArrayMap<String, Call<ApiResult>>()

const val ERROR_NETWORK_BROKEN = -2
const val ERROR_JOB_CANCELED = -3

suspend fun subscribe(rc: RequestControl? = null, api: () -> Call<ApiResult>): ApiResult {
    return subscribe(rc, api, false)
}

suspend fun subscribeSync(rc: RequestControl? = null, api: () -> Call<ApiResult>): ApiResult {
    return subscribe(rc, api, true)
}

suspend fun subscribe(rc: RequestControl? = null, api: () -> Call<ApiResult>, syncMode: Boolean): ApiResult {
    if (!U.getNetworkUtils().hasNetwork()) {
        return ApiResult().apply {
            errno = ERROR_NETWORK_BROKEN
            errmsg = "网络链接不可用"
        }
    }
    rc?.let {
        MyLog.d("ApiObserverKt", "${rc.key}请求")
        val job = requestMap[rc.key]
        if (rc.controlType == ControlType.CancelThis) {
            if (job != null) {
                MyLog.d("ApiObserverKt", "${rc.key}取消本次")
                return ApiResult().apply {
                    errno = -1
                    errmsg = "重复请求被取消"
                }
            }
        }
        if (rc.controlType == ControlType.CancelLast) {
            if (job != null) {
                MyLog.d("ApiObserverKt", "${rc.key}取消上次")
                job.cancel()
            }
        }
    }
    return subscribe(rc?.key, api, syncMode)
}

private suspend fun subscribe(apiKey: String? = null, api: () -> Call<ApiResult>, syncMode: Boolean): ApiResult {
    try {
        val result = callReal(apiKey, api, syncMode)
        if (result?.errno != 0) {
            if (MyLog.isDebugLogOpen()) {
                U.getToastUtil().showShort("errno:${result?.errno} errmsg:${result?.errmsg}", -100, -1)
            }
        }
        if (result?.errno == 107) {
            UserAccountManager.notifyAccountExpired()
        } else if (result?.errno == 0) {
            UserAccountManager.accountValidFromServer()
        }
        return result
    } catch (e: Exception) {
        var log = Log.getStackTraceString(e)
        if (TextUtils.isEmpty(log)) {
            log = e.message
        }
        MyLog.e("ApiObserverKt", log)
        if (MyLog.isDebugLogOpen()) {
            if (!TextUtils.isEmpty(log)) {
                U.getToastUtil().showShort(log, -100, -1)
            }
        }
        if (e is HttpException) {
            if (e.code() == 404) {
                return ApiResult().apply {
                    errno = ERROR_NETWORK_BROKEN
                    errmsg = "HttpException 404"
                }
            }
        } else if (e is UnknownHostException) {
            return ApiResult().apply {
                errno = ERROR_NETWORK_BROKEN
                errmsg = "UnknownHostException"
            }
        } else if (e is SocketTimeoutException) {
            return ApiResult().apply {
                errno = ERROR_NETWORK_BROKEN
                errmsg = "SocketTimeoutException"
            }
        } else if (e.message?.contains("cancelled") == true) {
            return ApiResult().apply {
                errno = ERROR_JOB_CANCELED
                errmsg = ""
            }
        }

        return ApiResult().apply {
            errno = ERROR_NETWORK_BROKEN
            errmsg = "网络较差，导致请求失败"
        }
    }
}

@Suppress("BlockingMethodInNonBlockingContext")
private suspend fun callReal(apiKey: String?, api: () -> Call<ApiResult>, syncMode: Boolean): ApiResult {
    MyLog.d("ApiObserverKt", "$apiKey 开始请求")
    try {
        val call = api.invoke()
        apiKey?.let {
            requestMap.put(apiKey, call)
        }
        // await 会等待任务在其他线程完成, execute在当前线程同步执行
        return if (syncMode)
            call.execute().body()!!
        else
            call.await()
    } finally {
        MyLog.d("ApiObserverKt", "$apiKey 请求结束")
        apiKey?.let {
            requestMap.remove(apiKey)
        }
    }
}