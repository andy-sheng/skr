package com.common.miLianYun

import android.util.Log
import com.common.core.account.UserAccountManager
import com.common.log.MyLog
import com.common.utils.U
import com.xiaomi.gamecenter.sdk.*
import com.xiaomi.gamecenter.sdk.entry.MiAccountInfo
import com.xiaomi.gamecenter.sdk.entry.MiAppInfo
import com.xiaomi.gamecenter.sdk.entry.MiBuyInfo

object MiLianYunManager {

    val TAG = "MiLianYunManager"

    fun initSdk() {
        val appInfo = MiAppInfo()
        appInfo.appId = "2882303761517932750"
        appInfo.appKey = "5701793259750"
        MiCommplatform.Init(U.app(), appInfo, OnInitProcessListener { i, s -> Log.i("MiLianYunManager", "Init success") })
    }

    fun login(callback: (code: Int, uid: String?, sessionId: String?) -> Unit) {
        MiCommplatform.getInstance().miLogin(U.getActivityUtils().topActivity, object : OnLoginProcessListener {
            override fun finishLoginProcess(code: Int, account: MiAccountInfo?) {
                MyLog.d(TAG, "finishLoginProcess code = $code, account = $account")
                if(code == MiCode.MI_SUCCESS){
                    callback.invoke(0, account?.uid, account?.sessionId)
                }else{
                    callback.invoke(code, account?.uid, account?.sessionId)
                }
//                if (MiCode.MI_SUCCESS == code) {
//                    var uid = account?.uid
//                    var sessionId = account?.sessionId
//
////                    handler.sendEmptyMessage(MiCode.MI_SUCCESS)
//                } else if (MiCode.MI_ERROR_ACTION_EXECUTED == code) {
////                    handler.sendEmptyMessage(MiCode.MI_ERROR_ACTION_EXECUTED)
//                } else {
////                    handler.sendEmptyMessage(i)
//                }
            }
        },
                MiLoginType.AUTO_FIRST, MiAccountType.APP, UserAccountManager.uuid)
    }

    fun isLogin(): Boolean {
        return MiCommplatform.getInstance().isLogged
    }

    fun pay(orderId: String, callback: (code: Int, msg: String?) -> Unit) {
        var miBuyInfo = MiBuyInfo()
        miBuyInfo.cpOrderId = orderId //订单号唯一（不为空）
        miBuyInfo.cpUserInfo = "cpUserInfo" //此参数在用户支付成功后会透传给CP的服务器
        miBuyInfo.feeValue = 1 //必须是大于0的整数，100代表1元人民币（不为空）

        MiCommplatform.getInstance().miUniPay(U.getActivityUtils().topActivity, miBuyInfo
        ) { code, msg ->
            MyLog.d(TAG, "pay code = $code, msg = $msg")
            callback.invoke(code, msg)
        }
    }
}