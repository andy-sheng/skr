package com.common.core.account


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import com.alibaba.android.arouter.launcher.ARouter

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.common.bugly.BuglyInit
import com.common.callback.Callback
import com.common.core.account.event.AccountEvent
import com.common.core.account.event.LoginApiErrorEvent
import com.common.core.channel.HostChannelManager
import com.common.core.myinfo.Location
import com.common.core.myinfo.MyUserInfo
import com.common.core.myinfo.MyUserInfoLocalApi
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.myinfo.MyUserInfoServerApi
import com.common.core.userinfo.UserInfoLocalApi
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.model.HonorInfo
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.userinfo.model.VerifyInfo
import com.common.core.userinfo.remark.RemarkLocalApi
import com.common.jiguang.JiGuangPush
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.statistics.StatisticsAdapter
import com.common.statistics.talkingdata.TDStatistics
import com.common.statistics.umeng.UmengStatistics
import com.common.utils.HandlerTaskTimer
import com.common.utils.U
import com.module.ModuleServiceManager
import com.module.RouterConstants
import com.module.common.ICallback
import com.tendcloud.tenddata.TDAccount
import com.zq.live.proto.Common.UserInfo

import org.greenrobot.eventbus.EventBus

import java.util.HashMap

import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


/**
 * 保存账号token等信息
 * 数据库支持多账号的
 * Created by chengsimin on 16/7/1.
 */
object UserAccountManager {
    val TAG = "UserAccountManager"
    val SYSTEM_ID = 1       //系统id
    val SYSTEM_GRAB_ID = 2  //一唱到底多音
    val SYSTEM_RANK_AI = 3  //AI裁判
    val SYSTEM_AVATAR = "http://res-static.inframe.mobi/common/system2.png" //系统头像

    val NOTICE_AVATAR = "http://res-static.inframe.mobi/common/notice_icon.png" //公告头像

    val MSG_DELAY_GET_RC_TOKEN = 10 * 1000

    private var mAccount: UserAccount? = null

    private var mHasTryConnentRM = false//有没有尝试过登录过融云

    private var mHasloadAccountFromDB = false//有没有尝试load过账号

    private var mOldOrNewAccount = 0

    internal var mUiHanlder: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == MSG_DELAY_GET_RC_TOKEN) {
                getIMToken()
            }
        }
    }

    val systemModel: UserInfoModel
        get() {
            val system = UserInfoModel()
            system.userId = SYSTEM_ID
            system.avatar = SYSTEM_AVATAR
            system.nickname = "系统消息"
            return system
        }

    val uuid: String
        get() = if (mAccount != null) {
            mAccount!!.uid
        } else ""

    val uuidAsLong: Long
        get() = if (mAccount != null) {
            java.lang.Long.parseLong(mAccount!!.uid)
        } else 0L

    val phoneNum: String
        get() = if (mAccount != null) {
            mAccount!!.phoneNum
        } else ""

    val serviceToken: String
        get() = if (mAccount != null) {
            mAccount!!.serviceToken
        } else ""

    val sSecurity: String
        get() = if (mAccount != null) {
            mAccount!!.secretToken
        } else ""

    val rongToken: String
        get() = if (mAccount != null) {
            mAccount!!.rongToken
        } else ""

    val fromLoginMode:Int
        get() = if (mAccount != null) {
            mAccount!!.fromLoginMode
        } else 0

    // 是否是老账号
    private// 一天内的算新用户
    val isNewAccount: Boolean
        get() {
            if (mOldOrNewAccount == 0) {
                val firstLoginTime = U.getPreferenceUtils().getSettingLong("first_login_time", 0)
                if (System.currentTimeMillis() - firstLoginTime > 24 * 3600 * 1000) {
                    mOldOrNewAccount = 2
                } else {
                    mOldOrNewAccount = 1
                }
            }
            return this.mOldOrNewAccount == 1
        }

    fun init() {
        MyLog.d(TAG, "init")
        val channelId = HostChannelManager.getInstance().channelId.toLong()
        val userAccount = UserAccountLocalApi.getUserAccount(channelId)
        setAccount(userAccount, false)
    }

    fun onLoginResult(account: UserAccount?) {
        if (account != null) {
            MyLog.w(TAG, "login" + " accountId=" + account.uid)
            account.isLogOff = false
            // 登出所有其他账号
            UserAccountLocalApi.loginAccount(account)
            // 用户登录成功，这里应该是要发出通知的
            setAccount(account, true)
            U.getActivityUtils().showSnackbar("登录成功", false)

            if (!MyUserInfoManager.isFirstLogin) {
                // 是个老用户，打个点
                StatisticsAdapter.recordCountEvent("signup", "oldid", null, true)
            }
        }
    }

    private fun setAccount(account: UserAccount?, fromServer: Boolean) {
        mAccount = account
        mHasloadAccountFromDB = true
        if (account != null) {
            MyLog.d(TAG, "setAccount" + " accountId=" + account.uid)
            // 取消匿名模式
            //            MiLinkClientAdapter.getInstance().setIsTouristMode(false);
            // MilinkChannelClientAdapter.getInstance().destroy();
            // 进入实名模式
            //            MiLinkClientAdapter.getInstance().initCallBackFirst();
            // 同步昵称等详细信息
            MyUserInfoManager.init()

            if (fromServer) {
                // 与融云服务器建立连接
                MyLog.d(TAG, "从服务器取的账号，可以立马登录融云")
                mHasTryConnentRM = true
                connectRongIM(account.rongToken)
            } else {
                // 账号不是从服务器取的，至少一个服务器请求成功后，才登录融云
                mHasTryConnentRM = false
            }

            trySetAlias()
            //            ScreenLogView.addInfo("用户id", account.getUid());
            UserInfoManager.getInstance().initRemark()
            EventBus.getDefault().post(AccountEvent.SetAccountEvent())
        } else {

        }
    }

    fun hasLoadAccountFromDB(): Boolean {
        return mHasloadAccountFromDB
    }


    fun hasAccount(): Boolean {
        if (mAccount == null) {
            return false
        }
        if (mAccount?.isLogOff == true) {
            return false
        }
        return true
    }

    fun getGategory(category: String): String {
        return if (isNewAccount) "new_$category" else "old_$category"
    }

    // 手机登录
    fun loginByPhoneNum(phoneNum: String, verifyCode: String, callback: Callback<ApiResult>?) {
        StatisticsAdapter.recordCountEvent("signup", "api_begin", null)
        val userAccountServerApi = ApiManager.getInstance().createService(UserAccountServerApi::class.java)
        // 1 为手机登录
        val deviceId = U.getDeviceUtils().deviceID
        val deviceModel = U.getDeviceUtils().productBrand+"_"+U.getDeviceUtils().productModel
        userAccountServerApi.login(1, phoneNum, verifyCode, 20, U.getChannelUtils().channel, deviceId,deviceModel)
                .subscribeOn(Schedulers.io())
                .subscribe(object : ApiObserver<ApiResult>() {
                    override fun process(obj: ApiResult) {
                        if (obj.errno == 0) {
                            val userAccount = parseRsp(obj.data!!, phoneNum,1)
                            TDStatistics.onProfileSignIn(userAccount.uid, TDAccount.AccountType.REGISTERED, MyUserInfoManager.nickName, MyUserInfoManager.isFirstLogin)
                            UmengStatistics.onProfileSignIn("phone", userAccount.uid)
                        } else {
                            //U.getToastUtil().showShort(obj.getErrmsg());
                            val map = HashMap<String, String>()
                            map.put("error", obj.errno.toString() + "")
                            StatisticsAdapter.recordCountEvent("signup", "api_failed", map)
                            EventBus.getDefault().post(LoginApiErrorEvent(obj.errno, obj.errmsg))
                        }
                        mUiHanlder.post {
                            callback?.onCallback(1, obj)
                        }
                    }

                    override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                        super.onNetworkError(errorType)
                        val map = HashMap<String, String>()
                        map.put("error", "network_error")
                        StatisticsAdapter.recordCountEvent("signup", "api_failed", map)
                    }
                })

    }

    /**
     * 第三方登录
     *
     * @param mode        3 为微信登录, 2 为qq
     * @param accessToken
     * @param openId
     */
    fun loginByThirdPart(mode: Int, accessToken: String, openId: String, callback: Callback<ApiResult>?) {
        StatisticsAdapter.recordCountEvent("signup", "api_begin", null)
        val userAccountServerApi = ApiManager.getInstance().createService(UserAccountServerApi::class.java)
        val deviceId = U.getDeviceUtils().deviceID
        val deviceModel = U.getDeviceUtils().productBrand+"_"+U.getDeviceUtils().productModel
        userAccountServerApi.loginWX(mode, accessToken, openId, 20, U.getChannelUtils().channel, deviceId,deviceModel)
                .subscribeOn(Schedulers.io())
                .subscribe(object : ApiObserver<ApiResult>() {
                    override fun process(obj: ApiResult) {
                        if (obj.errno == 0) {
                            val userAccount = parseRsp(obj.data!!, "",mode)
                            if (mode == 3) {
                                TDStatistics.onProfileSignIn(userAccount.uid, TDAccount.AccountType.WEIXIN, MyUserInfoManager.nickName, MyUserInfoManager.isFirstLogin)
                                UmengStatistics.onProfileSignIn("wx", userAccount.uid)
                            } else if (mode == 2) {
                                TDStatistics.onProfileSignIn(userAccount.uid, TDAccount.AccountType.QQ, MyUserInfoManager.nickName, MyUserInfoManager.isFirstLogin)
                                UmengStatistics.onProfileSignIn("icon_qq", userAccount.uid)
                            }else if(mode ==7){
                                TDStatistics.onProfileSignIn(userAccount.uid, TDAccount.AccountType.TYPE7, MyUserInfoManager.nickName, MyUserInfoManager.isFirstLogin)
                                UmengStatistics.onProfileSignIn("icon_mi", userAccount.uid)
                            }
                        } else {
                            //U.getToastUtil().showShort(obj.getErrmsg());
                            val map = HashMap<String, String>()
                            map.put("error", obj.errno.toString() + "")
                            StatisticsAdapter.recordCountEvent("signup", "api_failed", map)
                        }
                        mUiHanlder.post {
                            callback?.onCallback(1, obj)
                        }
                    }

                    override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                        super.onNetworkError(errorType)
                        val map = HashMap<String, String>()
                        map.put("error", "network_error")
                        StatisticsAdapter.recordCountEvent("signup", "api_failed", map)
                    }
                })
    }

    internal fun parseRsp(jsonObject: JSONObject, phoneNum: String,fromMode:Int): UserAccount {
        val secretToken = jsonObject.getJSONObject("token").getString("T")
        val serviceToken = jsonObject.getJSONObject("token").getString("S")
        val rongToken = jsonObject.getJSONObject("token").getString("RC")
        val userInfoModel = JSON.parseObject(jsonObject.getString("profile"), UserInfoModel::class.java)
        val isFirstLogin = jsonObject.getBooleanValue("isFirstLogin")
        if (isFirstLogin) {
            U.getPreferenceUtils().setSettingLong("first_login_time", System.currentTimeMillis())
        }
        val map = HashMap<String, String>()
        map.put("isFirstLogin", "" + isFirstLogin)
        StatisticsAdapter.recordCountEvent("signup", "api_success", map)
        val needBeginnerGuide = jsonObject.getBooleanValue("needBeginnerGuide")

        // 设置个人信息
        val myUserInfo = MyUserInfo.parseFromUserInfoModel(userInfoModel)
        MyUserInfoManager.isFirstLogin = isFirstLogin
        MyUserInfoManager.isNeedBeginnerGuide = needBeginnerGuide
        MyUserInfoLocalApi.insertOrUpdate(myUserInfo)
        MyUserInfoManager.setMyUserInfo(myUserInfo, true, "parseRsp")

        val userAccount = UserAccount()
        userAccount.phoneNum = phoneNum
        userAccount.serviceToken = serviceToken
        userAccount.secretToken = secretToken
        userAccount.rongToken = rongToken
        userAccount.uid = userInfoModel.userId.toString()
        userAccount.needEditUserInfo = isFirstLogin
        userAccount.channelId = HostChannelManager.getInstance().channelId
        userAccount.fromLoginMode = fromMode
        onLoginResult(userAccount)
        return userAccount
    }

    /**
     * 用户主动退出登录
     */
    fun logoff(from: Int, callback: Callback<ApiResult?>?) {
        logoff(from, false, AccountEvent.LogoffAccountEvent.REASON_SELF_QUIT, true, callback)
        mUiHanlder.removeCallbacksAndMessages(null)
    }

    /**
     * 收到账号过期的通知，被踢下线等等
     */
    fun notifyAccountExpired() {
        logoff(1, false, AccountEvent.LogoffAccountEvent.REASON_ACCOUNT_EXPIRED, false, null)
    }

    /**
     * 经过服务器的api认证，账号有效
     */
    fun accountValidFromServer() {
        // 认证有效时，再连融云，防止无效的账号将有效账号的融云踢下线
        tryConnectRongIM(false)
    }

    /**
     * 退出登录
     *
     * @param deleteAccount
     */
    private fun logoff(from: Int, deleteAccount: Boolean, reason: Int, notifyServer: Boolean, callback: Callback<ApiResult?>?) {
        MyLog.w(TAG, "logoff deleteAccount=$deleteAccount reason=$reason notifyServer=$notifyServer")
        if (!hasAccount()) {
            MyLog.w(TAG, "logoff but hasAccount = false")
            return
        }
        if (notifyServer) {
            val userAccountServerApi = ApiManager.getInstance().createService(UserAccountServerApi::class.java)
            ApiMethods.subscribe(userAccountServerApi.loginOut(), object : ApiObserver<ApiResult>() {
                override fun process(result: ApiResult) {
                    if (result.errno == 0) {
                        if (from == 2) {
                            U.getToastUtil().showShort("登出成功了")
                        }
                    }
                }
            })
        }
        ModuleServiceManager.getInstance().msgService.logout()
        if (mAccount != null) {
            val userId = mAccount!!.uid
            Observable.create(ObservableOnSubscribe<Any> { emitter ->
                if (deleteAccount) {
                    UserAccountLocalApi.delete(mAccount)
                } else {
                    if (mAccount != null) {
                        mAccount!!.isLogOff = true
                    }
                    UserAccountLocalApi.insertOrReplace(mAccount)
                }
                mAccount = null
                ApiManager.getInstance().clearCookies()
                // 清除pref 清除数据库
                U.getPreferenceUtils().clearPreference()
                UserInfoLocalApi.deleteAll()
                RemarkLocalApi.deleteAll()
                // 清除备注的缓存
                UserInfoManager.getInstance().clearRemark()
                UmengStatistics.onProfileSignOff()
                JiGuangPush.clearAlias(userId)
                MyUserInfoManager.logoff()
                EventBus.getDefault().post(AccountEvent.LogoffAccountEvent(reason))
                mUiHanlder.post {
                    callback?.onCallback(1, null)
                }
                emitter.onComplete()
            })
                    .subscribeOn(Schedulers.io())
                    .subscribe()
        }
    }

    fun tryConnectRongIM(force: Boolean) {
        if (mHasTryConnentRM && !force) {
            return
        }
        if (force) {
            MyLog.d(TAG, "强制重连融云")
        }
        MyLog.d(TAG, "tryConnectRongIM force=$force")
        if (mAccount != null) {
            val token = mAccount!!.rongToken
            MyLog.d(TAG, "tryConnectRongIM token=$token")
            mHasTryConnentRM = true
            connectRongIM(token)
        } else {
            MyLog.d(TAG, "tryConnectRongIM" + " mAccount==null")
        }
    }

    /**
     * 融云账号被人提了
     * 融云账号被人提了，不一定代表这个账号无效了
     * 这时候访问一下服务器，看看这个账号是不是真的无效
     */
    fun rcKickedByOthers(times: Int) {
        MyLog.d(TAG, "融云被KICK了  rcKickedByOthers times=$times")
        if (times > 5) {
            MyLog.d(TAG, "rcKickedByOthers times=$times,超过重试次数了")
            return
        }
        if (Looper.getMainLooper() == Looper.myLooper()) {
            Observable.create<Boolean> {
                rcKickedByOthers(times)
                it.onComplete()
            }.subscribeOn(Schedulers.io())
                    .subscribe()
            return
        }
        if (hasAccount()) {
            //还有账号
            val api = ApiManager.getInstance().createService(MyUserInfoServerApi::class.java)
            val apiResultCall = api.getUserInfo(uuidAsLong.toInt())
            if (apiResultCall != null) {
                try {
                    val resultResponse = apiResultCall.execute()
                    if (resultResponse != null) {
                        val obj = resultResponse.body()
                        if (obj != null) {
                            if (obj.errno == 0) {
                                connectRongIM(mAccount!!.rongToken)
                            } else if (obj.errno == 107) {
                                notifyAccountExpired()
                            }
                        } else {
                            MyLog.w(TAG, "syncMyInfoFromServer obj==null")
                        }
                    }
                } catch (e: Exception) {
                    MyLog.d(e)
                }

            } else {
                HandlerTaskTimer.newBuilder().delay(2000)
                        .start(object : HandlerTaskTimer.ObserverW() {
                            override fun onNext(integer: Int) {
                                rcKickedByOthers(times + 1)
                            }
                        })
            }
        } else {
            MyLog.d(TAG, "rcKickedByOthers no account,do nothing")
        }
    }

    // 获取IM的token
    private fun getIMToken() {
        MyLog.d(TAG, "getIMToken")
        val userAccountServerApi = ApiManager.getInstance().createService(UserAccountServerApi::class.java)
        ApiMethods.subscribe(userAccountServerApi.imToken, object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                var token: String? = null
                if (result.errno == 0) {
                    token = result.data!!.getString("RC")
                    if (!TextUtils.isEmpty(token)) {
                        // 更新数据库中融云token
                        mAccount!!.rongToken = token
                        UserAccountLocalApi.loginAccount(mAccount)
                        // 连接融云
                        connectRongIM(token)
                    } else {
                        if (MyLog.isDebugLogOpen()) {
                            U.getToastUtil().showShort("服务器返回的融云token为空，检查是否触发了测试环境100用户上线")
                        }
                        MyLog.e(TAG, "getIMToken from Server is null")
                    }
                } else {
                    if (MyLog.isDebugLogOpen() && result.errno == 8302102) {
                        U.getToastUtil().showShort("GET融云token失败  errorCode = 8302102 errmsg = " + result.errmsg)
                    } else {
                        MyLog.e(TAG, "process GET融云token error=$result")
                    }

                }
                if (TextUtils.isEmpty(token)) {
                    mUiHanlder.removeMessages(MSG_DELAY_GET_RC_TOKEN)
                    mUiHanlder.sendEmptyMessageDelayed(MSG_DELAY_GET_RC_TOKEN, (10 * 1000).toLong())
                }
            }
        })
    }

    private fun connectRongIM(rongToken: String?) {
        MyLog.d(TAG, "connectRongIM rongToken=$rongToken")
        if (TextUtils.isEmpty(rongToken)) {
            getIMToken()
        } else {
            ModuleServiceManager.getInstance().msgService.connectRongIM(rongToken, object : ICallback {
                override fun onSucess(obj: Any) {
                    //                    U.getToastUtil().showShort("与服务器连接成功");
                    MyLog.e(TAG, "与融云服务器连接成功")
                    mUiHanlder.removeMessages(MSG_DELAY_GET_RC_TOKEN)
                }

                override fun onFailed(obj: Any, errcode: Int, message: String) {
                    val result = obj as Boolean
                    if (result) {
                        // todo 连接融云失败
                        if (U.getActivityUtils().isAppForeground) {
                            U.getToastUtil().showShort("融云连接不可用")
                        }
                    } else {
                        // todo token有问题, 重试一次(可能过期或者appkey不一致等)
                        getIMToken()
                    }
                }
            })
        }
    }

    //    @Subscribe(threadMode = ThreadMode.POSTING)
    //    public void onEvent(UmengPushRegisterSuccessEvent event) {
    //        trySetAlias();
    //    }

    /**
     * 给Umeng的push通道设置 Alias
     */
    internal fun trySetAlias() {
        if (hasAccount()) {
            //com.common.umeng.UmengPush.UmengPush.setAlias(UserAccountManager.getInstance().getUuid());
            BuglyInit.setUserId(uuid)
            if (U.getChannelUtils().isStaging) {
                com.common.jiguang.JiGuangPush.setAlias("dev_$uuid")
            } else {
                com.common.jiguang.JiGuangPush.setAlias(uuid)
            }

        }
    }

}
