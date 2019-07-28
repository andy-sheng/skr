package com.common.rxretrofit

import com.common.core.account.UserAccountManager
import com.common.log.MyLog
import com.common.utils.U
import java.lang.Exception

 suspend fun subscribe(api: suspend () -> ApiResult): ApiResult {
    val result = api.invoke()
    try {
        if (result.errno != 0) {
            if (MyLog.isDebugLogOpen()) {
                U.getToastUtil().showShort("errno:" + result.errno + " errmsg:" + result.errmsg, -100, -1)
            }
        }
        if (result.errno == 107) {
            UserAccountManager.getInstance().notifyAccountExpired()
        } else if (result.errno == 0) {
            UserAccountManager.getInstance().accountValidFromServer()
        }
    } catch (e: Exception) {
        MyLog.e(e)
    }
    return result

}