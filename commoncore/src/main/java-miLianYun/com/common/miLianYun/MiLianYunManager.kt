package com.common.miLianYun

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.common.base.BuildConfig
import com.common.core.account.UserAccountManager
import com.common.log.MyLog
import com.common.utils.U
import com.xiaomi.gamecenter.sdk.*
import com.xiaomi.gamecenter.sdk.entry.MiAccountInfo
import com.xiaomi.gamecenter.sdk.entry.MiAppInfo
import com.xiaomi.gamecenter.sdk.entry.MiBuyInfo

object MiLianYunManager {

    var hasNewVersion = false

    val TAG = "MiLianYunManager"

    var init = false

    /**
     * 小米联运是否开启
     */
    fun lianYunOpen(): Boolean {
        // vivo oppo 华为 强制不走联运
        if(U.getDeviceUtils().isOppo){
            return false
        }
        if(U.getDeviceUtils().isVivo){
            return false
        }
        if(U.getDeviceUtils().isEmui){
            return false
        }
        return U.getChannelUtils().channel == "MI_SHOP" || U.getChannelUtils().channel == "SANDBOX"
//        return true
    }

    private fun initSdk(succesCallback: () -> Unit?) {
        if (init) {
            succesCallback.invoke()
            return
        }
        MyLog.d(TAG, "initSdk")
        val appInfo = MiAppInfo()
        appInfo.appId = "2882303761517932750"
        appInfo.appKey = "5701793259750"
        MiCommplatform.Init(U.app(), appInfo, OnInitProcessListener { i, s ->
            MyLog.d(TAG, "initSdk i = $i, s = $s")
            init = true
            succesCallback.invoke()
        })
    }

    fun loginByMi(callback: (code: Int, uid: String?, sessionId: String?) -> Unit?) {
        login(true, 0, callback)
    }

    fun loginAuto() {
        MyLog.d(TAG, "loginAuto")
        if (UserAccountManager.hasAccount()) {
            if (UserAccountManager.fromLoginMode == 7) {
                // 是小米登录
                loginByMi { code, uid, sessionId ->
                    if (code == 0) {
//                        pay(orderId,fee, callback)
                    } else {
                        U.getToastUtil().showShort("login sdk failed code=$code")
                    }
                }
            } else {
                // 非小米登录
                login(false, 0) { code, uid, sessionId ->
                    if (code == 0) {
//                        pay(orderId,fee, callback)
                    } else {
                        U.getToastUtil().showShort("login sdk failed code=$code")
                    }
                }
            }
        }
    }

    private fun login(useMiAccount: Boolean, deep: Int, callback: (code: Int, uid: String?, sessionId: String?) -> Unit?) {
        MyLog.d(TAG, "login useMiAccount = $useMiAccount, deep = $deep, callback = $callback")
        if (!init) {
            if (deep > 10) {
                MyLog.e(TAG, "小米联运sdk初始化出现了异常")
                return
            }
            initSdk {
                Handler(Looper.getMainLooper()).post {
                    login(useMiAccount, deep + 1, callback)
                }
                return@initSdk null
            }
            return
        }
        val miHasLogin = U.getPreferenceUtils().getSettingBoolean(U.getPreferenceUtils().longlySp(), "mi_has_login", false)
        MiCommplatform.getInstance().miLogin(U.getActivityUtils().topActivity, object : OnLoginProcessListener {
            override fun finishLoginProcess(code: Int, account: MiAccountInfo?) {
                MyLog.d(TAG, "fini·shLoginProcess code = $code, account = $account")
                U.getPreferenceUtils().setSettingBoolean(U.getPreferenceUtils().longlySp(), "mi_has_login", true)
                if (code == MiCode.MI_SUCCESS) {
                    callback.invoke(0, account?.uid, account?.sessionId)
                } else {
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
                if (miHasLogin) {
                    MiLoginType.AUTO_FIRST
                } else {
                    MiLoginType.MANUAL_ONLY
                },
                if (useMiAccount) {
                    MiAccountType.MI_SDK
                } else {
                    MiAccountType.APP
                },
                if (useMiAccount) {
                    null
                } else {
                    UserAccountManager.uuid
                })
    }

    fun pay(orderId: String, fee: Int, cpUserInfo: String, callback: (code: Int, msg: String?) -> Unit) {
        MyLog.d(TAG, "pay orderId = $orderId, fee = $fee, callback = $callback")
        var miBuyInfo = MiBuyInfo()
        miBuyInfo.cpOrderId = orderId //订单号唯一（不为空）
        miBuyInfo.cpUserInfo = cpUserInfo //此参数在用户支付成功后会透传给CP的服务器
        miBuyInfo.feeValue = fee //必须是大于0的整数，100代表1元人民币（不为空）

        MiCommplatform.getInstance().miUniPay(U.getActivityUtils().topActivity, miBuyInfo
        ) { code, msg ->
            MyLog.d(TAG, "pay code = $code, msg = $msg")
            callback.invoke(code, msg)
        }
    }
}