package com.common.rxretrofit

import android.text.TextUtils
import android.util.Log
import com.common.core.account.UserAccountManager
import com.common.log.MyLog
import com.common.utils.U
import retrofit2.adapter.rxjava2.HttpException
import java.lang.Exception
import java.net.SocketTimeoutException
import java.net.UnknownHostException

suspend fun subscribe(api: suspend () -> ApiResult?): ApiResult? {
    try {
        val result = api.invoke()
        if (result?.errno != 0) {
            if (MyLog.isDebugLogOpen()) {
                U.getToastUtil().showShort("errno:${result?.errno} errmsg:${result?.errmsg}", -100, -1)
            }
        }
        if (result?.errno == 107) {
            UserAccountManager.getInstance().notifyAccountExpired()
        } else if (result?.errno == 0) {
            UserAccountManager.getInstance().accountValidFromServer()
        }
        return result
    } catch (e: Exception) {
        var log = Log.getStackTraceString(e)
        if (TextUtils.isEmpty(log)) {
            log = e.message
        }
        MyLog.e("ApiObserverKt", log)
        if (e is HttpException) {
            if (e.code() == 404) {
            }
        } else if (e is UnknownHostException) {
        } else if (e is SocketTimeoutException) {
        }
        if (MyLog.isDebugLogOpen()) {
            if (!TextUtils.isEmpty(log)) {
                U.getToastUtil().showShort(log, -100, -1)
            }
        }
    }
    return null

}