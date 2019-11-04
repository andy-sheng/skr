package com.common.core.myinfo

import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.common.core.account.UserAccountManager
import com.common.core.myinfo.event.MyUserInfoEvent
import com.common.core.userinfo.model.HonorInfo
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.userinfo.model.VerifyInfo
import com.common.log.MyLog
import com.common.rx.RxRetryAssist
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.LbsUtils
import com.common.utils.U
import com.module.ModuleServiceManager
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 * 保存个人详细信息，我的信息的管理, 其实是对User的decorate
 * Created by chengsimin on 16/7/1.
 */
object MyUserInfoManager {

    const val TAG = "MyUserInfoManager"

    internal const val PREF_KEY_UPDATE_LACATION_TS = "update_location_ts"

    var myUserInfo: MyUserInfo? = MyUserInfo()
        private set
    /**
     * 当前的 mUser 信息是从服务器同步过的么，标记下
     *
     * @return
     */
    var isUserInfoFromServer = false
        private set
    // TODO: 2019/5/9 先下掉新手引导入口
    var isNeedBeginnerGuide = false
        get() = false
    var isFirstLogin = false    // 标记是否第一次登录
    //    private boolean mHasLoadFromDB = false;
    private var mHasGrabCertifyPassed = false// 抢唱时的认证
    private var mRealNameVerified = 0 // 实名认证

    //是否需要完善资料
    //        if (TextUtils.isEmpty(MyUserInfoManager.getInstance().getNickName())) {
    //            MyLog.d(TAG, "isNeedCompleteInfo nickName is null");
    //            return true;
    //        }
    //        if (MyUserInfoManager.getInstance().getSex() == 0) {
    //            MyLog.d(TAG, "isNeedCompleteInfo sex == 0");
    //            return true;
    //        }
    val isNeedCompleteInfo: Boolean
        get() = isFirstLogin

    val uid: Long
        get() = if (myUserInfo != null && myUserInfo!!.userId != 0L) {
            myUserInfo!!.userId
        } else {
            UserAccountManager.uuidAsLong
        }

    val nickName: String
        get() = if (myUserInfo?.userNickname != null) myUserInfo!!.userNickname else ""

    val age: Int
        get() {
            if (myUserInfo != null && !TextUtils.isEmpty(myUserInfo!!.birthday)) {
                val array = myUserInfo!!.birthday.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (!TextUtils.isEmpty(array[0])) {
                    val year = Integer.valueOf(array[0])
                    return Calendar.getInstance().get(Calendar.YEAR) - year
                }
            }
            return 0
        }

    val ageStage: Int
        get() = if (myUserInfo != null) myUserInfo!!.ageStage else 0

    val ageStageString: String
        get() {
            if (myUserInfo != null && myUserInfo!!.ageStage != 0) {
                if (myUserInfo!!.ageStage == 1) {
                    return "小学党"
                } else if (myUserInfo!!.ageStage == 2) {
                    return "中学党"
                } else if (myUserInfo!!.ageStage == 3) {
                    return "大学党"
                } else if (myUserInfo!!.ageStage == 4) {
                    return "工作党"
                }
            }
            return ""
        }

    val constellation: String
        get() {
            if (myUserInfo != null && !TextUtils.isEmpty(myUserInfo!!.birthday)) {
                val array = myUserInfo!!.birthday.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (array != null && array.size >= 3) {
                    if (!TextUtils.isEmpty(array[1]) && !TextUtils.isEmpty(array[2])) {
                        val month = Integer.valueOf(array[1])
                        val day = Integer.valueOf(array[2])
                        return U.getDateTimeUtils().getConstellation(month, day)
                    }
                }
            }
            return ""
        }

    val avatar: String
        get() = if (myUserInfo != null) myUserInfo!!.avatar else ""

    val vipType: Int
        get() = if (myUserInfo != null && myUserInfo!!.vipInfo != null) myUserInfo!!.vipInfo.vipType else 0

    val vipInfo: VerifyInfo?
        get() = if (myUserInfo != null) myUserInfo!!.vipInfo else null

    val honorInfo: HonorInfo?
        get() = if (myUserInfo != null) myUserInfo!!.honorInfo else null

    val signature: String
        get() = if (myUserInfo != null) myUserInfo!!.signature else ""

    val sex: Int
        get() = if (myUserInfo != null) myUserInfo!!.sex else 0

    val birthday: String
        get() = if (myUserInfo != null) myUserInfo!!.birthday else ""

    val location: Location
        get() = myUserInfo!!.location

    val realLocation: Location
        get() = myUserInfo!!.location2

    val locationDesc: String
        get() = if (!hasLocation()) {
            "火星"
        } else myUserInfo!!.location.desc

    val locationProvince: String
        get() = if (!hasLocation()) {
            "火星"
        } else myUserInfo!!.location.province

    /**
     * 是否实名认证
     *
     * @return
     */
    var isRealNameVerified: Boolean
        get() {
            if (mRealNameVerified == 0) {
                val v = U.getPreferenceUtils().getSettingBoolean("mRealNameVerified", false)
                if (v) {
                    mRealNameVerified = 1
                } else {
                    mRealNameVerified = -1
                }
            }
            return mRealNameVerified == 1
        }
        set(realNameVerified) {
            if (realNameVerified) {
                U.getPreferenceUtils().setSettingBoolean("mRealNameVerified", realNameVerified)
            }
        }

    fun init() {
        load()
    }

    private fun load() {
        Observable.create(ObservableOnSubscribe<Any> { emitter ->
            if (UserAccountManager.hasAccount()) {
                if (isUserInfoFromServer && myUserInfo != null) {
                    MyLog.d(TAG, "load。 mUser 有效 来自server，取消本次")
                } else {
                    val userInfo = MyUserInfoLocalApi.getUserInfoByUUid(UserAccountManager.uuidAsLong)
                    MyLog.d(TAG, "load myUserInfo uid =" + UserAccountManager.uuidAsLong)
                    MyLog.d(TAG, "load myUserInfo=" + userInfo!!)
                    if (userInfo != null) {
                        setMyUserInfo(userInfo, false, "load")
                    }
                    // 从服务器同步个人信息
                    syncMyInfoFromServer()
                }

            }
            //                mHasLoadFromDB = true;
            //                EventBus.getDefault().post(new MyUserInfoEvent.UserInfoLoadOkEvent());
            emitter.onComplete()
        })
                .subscribeOn(Schedulers.io())
                .subscribe()
    }

    fun logoff() {
        myUserInfo = MyUserInfo()
        isUserInfoFromServer = false
    }

    fun setMyUserInfo(myUserInfo: MyUserInfo?, fromServer: Boolean, from: String) {
        MyLog.d(TAG, "setMyUserInfo myUserInfo=$myUserInfo fromServer=$fromServer from=$from")
        if (myUserInfo != null) {
            this.myUserInfo = myUserInfo
            if (!isUserInfoFromServer) {
                isUserInfoFromServer = fromServer
            }
            ModuleServiceManager.getInstance().msgService.updateCurrentUserInfo()
            //user信息设定成功了，发出eventbus
            EventBus.getDefault().post(MyUserInfoEvent.UserInfoChangeEvent())
        }
    }

    /**
     * 从服务器同步个人信息
     */
    private fun syncMyInfoFromServer() {
        val api = ApiManager.getInstance().createService(MyUserInfoServerApi::class.java)
        val apiResultCall = api.getUserInfo(uid.toInt())
        if (apiResultCall != null) {
            try {
                val resultResponse = apiResultCall.execute()
                if (resultResponse != null) {
                    val obj = resultResponse.body()
                    if (obj != null) {
                        if (obj.errno == 0) {
                            val userInfoModel = JSON.parseObject(obj.data!!.toString(), UserInfoModel::class.java)
                            val myUserInfo = MyUserInfo.parseFromUserInfoModel(userInfoModel)
                            MyUserInfoLocalApi.insertOrUpdate(myUserInfo)
                            setMyUserInfo(myUserInfo, true, "syncMyInfoFromServer")
                        } else if (obj.errno == 107) {
                            UserAccountManager.notifyAccountExpired()
                        }
                    } else {
                        MyLog.w(TAG, "syncMyInfoFromServer obj==null")
                    }
                }
            } catch (e: Exception) {
                MyLog.d(e)
            }

        }
    }


    /**
     * 更新用户信息
     */
    @JvmOverloads
    fun updateInfo(updateParams: MyInfoUpdateParams, updateLocalIfServerFailed: Boolean = true, isCompleteInfo: Boolean = false, callback: ServerCallback? = null) {

        val map = HashMap<String, Any?>()
        if (updateParams.nickName != null) {
            map["nickname"] = updateParams.nickName
            if (updateLocalIfServerFailed) {
                myUserInfo!!.userNickname = updateParams.nickName
            }
        }
        if (updateParams.sex != -1) {
            map["sex"] = updateParams.sex
            if (updateLocalIfServerFailed) {
                myUserInfo!!.sex = updateParams.sex
            }
        }
        if (updateParams.birthday != null) {
            map["birthday"] = updateParams.birthday
            if (updateLocalIfServerFailed) {
                myUserInfo!!.birthday = updateParams.birthday
            }
        }
        if (updateParams.avatar != null) {
            map["avatar"] = updateParams.avatar
            if (updateLocalIfServerFailed) {
                myUserInfo!!.avatar = updateParams.avatar
            }
        }
        if (updateParams.sign != null) {
            map["signature"] = updateParams.sign
            if (updateLocalIfServerFailed) {
                myUserInfo!!.signature = updateParams.sign
            }
        }
        if (updateParams.location != null) {
            map["location"] = updateParams.location
            if (updateLocalIfServerFailed) {
                myUserInfo!!.location = updateParams.location
            }
        }

        if (updateParams.realLocation != null) {
            map["location2"] = updateParams.realLocation
            if (updateLocalIfServerFailed) {
                myUserInfo!!.location2 = updateParams.realLocation
            }
        }

        if (updateParams.ageStage != 0) {
            map["ageStage"] = updateParams.ageStage
            if (updateLocalIfServerFailed) {
                myUserInfo!!.ageStage = updateParams.ageStage
            }
        }

        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSON.toJSONString(map))
        val myUserAccountServerApi = ApiManager.getInstance().createService(MyUserInfoServerApi::class.java)
        val apiResultObservable = myUserAccountServerApi.updateInfo(body)
        ApiMethods.subscribe(apiResultObservable.retryWhen(RxRetryAssist(2, 5, true)), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult) {
                if (obj.errno == 0) {
                    if (!updateLocalIfServerFailed) {
                        if (updateParams.nickName != null) {
                            myUserInfo!!.userNickname = updateParams.nickName
                        }
                        if (updateParams.sex != -1) {
                            myUserInfo!!.sex = updateParams.sex
                        }
                        if (updateParams.birthday != null) {
                            myUserInfo!!.birthday = updateParams.birthday
                        }
                        if (updateParams.avatar != null) {
                            myUserInfo!!.avatar = updateParams.avatar
                        }
                        if (updateParams.sign != null) {
                            myUserInfo!!.signature = updateParams.sign
                        }
                        if (updateParams.location != null) {
                            myUserInfo!!.location = updateParams.location
                        }
                        if (updateParams.realLocation != null) {
                            myUserInfo!!.location2 = updateParams.realLocation
                        }
                        if (updateParams.ageStage != 0) {
                            myUserInfo!!.ageStage = updateParams.ageStage
                        }
                    }

                    if (!updateLocalIfServerFailed) {
                        val userInfoModel = JSON.parseObject(obj.data!!.toString(), UserInfoModel::class.java)
                        myUserInfo!!.userId = userInfoModel.userId.toLong()
                        myUserInfo!!.userNickname = userInfoModel.nickname
                        myUserInfo!!.avatar = userInfoModel.avatar
                        myUserInfo!!.vipInfo = userInfoModel.vipInfo
                        myUserInfo!!.birthday = userInfoModel.birthday
                        myUserInfo!!.location = userInfoModel.location
                        myUserInfo!!.location2 = userInfoModel.location2
                        myUserInfo!!.sex = userInfoModel.sex
                        myUserInfo!!.signature = userInfoModel.signature
                        myUserInfo!!.userDisplayname = userInfoModel.nickname
                        myUserInfo!!.ageStage = userInfoModel.ageStage
                    }

                    callback?.onSucess()
                    //写入数据库
                    Observable.create(ObservableOnSubscribe<Any> { emitter ->
                        MyUserInfoLocalApi.insertOrUpdate(myUserInfo)
                        // 取得个人信息
                        val userInfo = MyUserInfoLocalApi.getUserInfoByUUid(UserAccountManager.uuidAsLong)
                        if (userInfo != null) {
                            setMyUserInfo(myUserInfo, true, "updateInfo")
                        }
                        if (updateParams.location != null) {
                            // 有传地址位置
                            U.getPreferenceUtils().setSettingLong(PREF_KEY_UPDATE_LACATION_TS, System.currentTimeMillis())
                        }
                        emitter.onComplete()
                    })
                            .subscribeOn(Schedulers.io())
                            .subscribe()
                } else {
                    U.getToastUtil().showShort(obj.errmsg)
                    callback?.onFail()
                }
            }
        })
    }

    fun hasAgeStage(): Boolean {
        return myUserInfo != null && myUserInfo!!.ageStage != 0
    }

    fun hasMyUserInfo(): Boolean {
        return myUserInfo != null && myUserInfo!!.userId > 0
    }

    fun hasLocation(): Boolean {
        return myUserInfo!!.location != null && myUserInfo!!.location.desc.length > 0
    }

    fun hasRealLocation(): Boolean {
        return myUserInfo!!.location2 != null && myUserInfo!!.location2.desc.length > 0
    }

    fun trySyncRealLocation() {
        if (!hasLocation()) {
            // 没有地理位置
            uploadRealLocation()
        }
        // TODO: 2019/2/8 去掉位置更新策略，除了第一次，让用户主动触发
        //        else {
        //            long lastUpdateLocationTs = U.getPreferenceUtils().getSettingLong(PREF_KEY_UPDATE_LACATION_TS, 0);
        //            if (System.currentTimeMillis() - lastUpdateLocationTs > 3600 * 1000 * 6) {
        //                uploadRealLocation();
        //            }
        //        }
    }

    /**
     * 上传真实地理位置
     */
    @JvmOverloads
    fun uploadRealLocation(callback: LbsUtils.Callback? = null) {
        U.getLbsUtils().getLocation(false) { location ->
            MyLog.d(TAG, "onReceive location=$location")
            if (location != null && location.isValid) {
                val l = Location()
                l.province = location.province
                l.city = location.city
                l.district = location.district
                updateInfo(MyUserInfoManager
                        .newMyInfoUpdateParamsBuilder()
                        .setRealLocation(l)
                        .build(), true)
            }
            callback?.onReceive(location)
        }
    }

    /**
     * 是否通过了抢唱时的认证
     *
     * @return
     */
    fun hasGrabCertifyPassed(): Boolean {
        if (MyLog.isDebugLogOpen()) {
            return true
        }
        if (!mHasGrabCertifyPassed) {
            mHasGrabCertifyPassed = U.getPreferenceUtils().getSettingBoolean("hasGrabCertifyPassed", false)
        }
        return mHasGrabCertifyPassed
    }

    fun setGrabCertifyPassed(mHasPassedCertify: Boolean) {
        if (!mHasPassedCertify) {
            U.getPreferenceUtils().setSettingBoolean("hasGrabCertifyPassed", mHasPassedCertify)
            mHasGrabCertifyPassed = true
        }
    }

    //    public boolean hasLoadFromDB() {
    //        return mHasLoadFromDB;
    //    }


    class MyInfoUpdateParams private constructor() {
        var nickName: String? = null
        var sex = -1
        var birthday: String? = null
        internal var avatar: String? = null
        var sign: String? = null
        var location: Location? = null  // 显示位置
        var realLocation: Location? = null   // 真实位置
        var ageStage: Int = 0


        fun setAvatar(avatar: String) {
            this.avatar = avatar
        }

        class Builder internal constructor() {
            internal var mParams = MyInfoUpdateParams()

            fun setNickName(nickName: String): Builder {
                mParams.nickName = nickName
                return this
            }

            fun setSex(sex: Int): Builder {
                mParams.sex = sex
                return this
            }

            fun setBirthday(birthday: String): Builder {
                mParams.birthday = birthday
                return this
            }

            fun setAvatar(avatar: String): Builder {
                mParams.setAvatar(avatar)
                return this
            }

            fun setSign(sign: String): Builder {
                mParams.sign = sign
                return this
            }

            fun setLocation(location: Location): Builder {
                mParams.location = location
                return this
            }

            fun setRealLocation(location: Location): Builder {
                mParams.realLocation = location
                return this
            }

            fun setAgeStage(ageStage: Int): Builder {
                mParams.ageStage = ageStage
                return this
            }

            fun build(): MyInfoUpdateParams {
                return mParams
            }
        }
    }

    interface ServerCallback {
        fun onSucess()

        fun onFail()
    }

    fun newMyInfoUpdateParamsBuilder(): MyInfoUpdateParams.Builder {
        return MyInfoUpdateParams.Builder()
    }
}
/**
 * 更新用户信息
 */
