package com.common.core.userinfo.noremind

import com.alibaba.fastjson.JSON
import com.common.callback.Callback
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.ResponseCallBack
import com.common.core.userinfo.UserInfoServerApi
import com.common.core.userinfo.model.NoRemindInfoModel
import com.common.log.MyLog
import com.common.rxretrofit.*
import com.common.utils.U
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import java.util.HashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

/**
 * 消息免打扰管理
 */
object NoRemindManager : CoroutineScope by GlobalScope {

    private val TAG = "NoRemindManager"
    private val PREF_KEY_NO_REMIND_REFRESHED = "no_remind_refreshed"
    private val ERMT_USER_MSG = 1
    private val ERMT_GROUP_MSG = 2

    private val userInfoServerApi: UserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)

    /**
     * 消息免打扰是否开启 暂时关闭 临时发个版本
     */
    fun open(): Boolean {
        return false
    }

    fun setClubNoRemind(userID: Int, enable: Boolean, responseCallBack: ResponseCallBack<*>) {
        setNoRemind(userID, enable, responseCallBack, ERMT_GROUP_MSG)
    }

    fun setFriendNoRemind(userID: Int, enable: Boolean, responseCallBack: ResponseCallBack<*>) {
        setNoRemind(userID, enable, responseCallBack, ERMT_USER_MSG)
    }

    /**
     * 设置消息免打扰
     * @param userID
     * @param responseCallBack
     * @param remindMsgType
     */
    private fun setNoRemind(userID: Int, enable: Boolean, responseCallBack: ResponseCallBack<*>, remindMsgType: Int) {
        if (!open()) {
            return
        }
        if (userID <= 0) {
            MyLog.w(TAG, "setNoRemind userID=$userID")
            return
        }

        //检查是否缓存过数据，数据可能被清理过
        refreshNoRemindCacheIfNeeded()

        val map = HashMap<String, Any>()
        map["toID"] = userID
        map["enable"] = enable
        map["remindMsgType"] = remindMsgType

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        launch {
            val apiResult = subscribe(RequestControl("setNoRemind", ControlType.CancelThis)) {
                userInfoServerApi.setNoRemind(body)
            }

            if (apiResult.errno == 0) {

                launch(Dispatchers.IO) {
                    if (enable) {
                        NoRemindInfoLocalApi.insertOrReplace(NoRemindInfoModel(userID.toLong(), remindMsgType))
                    } else {
                        NoRemindInfoLocalApi.deleteDisturbed(NoRemindInfoModel(userID.toLong(), remindMsgType))
                    }
                }

                responseCallBack.onServerSucess(null)

            } else {

                responseCallBack.onServerFailed()
            }

        }
    }

    fun isFriendNoRemind(userID: Int): Boolean {
        return isNoRemind(userID, ERMT_USER_MSG)
    }

    fun isClubNoRemind(userID: Int): Boolean {
        return isNoRemind(userID, ERMT_GROUP_MSG)
    }

    private fun isNoRemind(userID: Int, msgType: Int): Boolean {
        return NoRemindInfoLocalApi.isNoReminded(userID, msgType)
    }

    /**
     * 刷新本地免打扰缓存
     */
    fun refreshNoRemindCache(userID: Int) {
        if (!open()) {
            return
        }
        if (userID <= 0) {
            MyLog.w(TAG, "refreshNoRemindCache userID=$userID")
            return
        }
        //使用服务器免打扰名单，更新本地缓存
        NoRemindInfoLocalApi.clearNoRemind()
        U.getPreferenceUtils().removePreference(PREF_KEY_NO_REMIND_REFRESHED)
        //分页数据，分多次请求获取全部列表数据
        launch(Dispatchers.IO) {
            val cnt = 20
            var remindMsgType = ERMT_USER_MSG
            var hasMore = true
            var offset = 0
            while (hasMore) {
                val apiResult = subscribe(RequestControl("getNoRemindList", ControlType.CancelThis)) {
                    userInfoServerApi.getNoRemindList(userID, remindMsgType, offset, cnt)
                }

                if (apiResult.errno == 0 && apiResult.data != null) {
                    hasMore = apiResult.data.getBoolean("hasMore")
                    apiResult.data.getJSONArray("toIDs")?.let { jsonArray ->
                        val models = jsonArray.toJavaList(Int::class.java).map { NoRemindInfoModel(it.toLong(), remindMsgType) }
                        NoRemindInfoLocalApi.insertOrReplace(models)
                    }

                } else {
                    hasMore = false
                    //出现异常，可能导致免打扰数据获取不完整，需要下次重新下载
                    U.getPreferenceUtils().removePreference(PREF_KEY_NO_REMIND_REFRESHED)
                }
                offset += cnt
                //好友免打扰数据获取完毕，开始获取家族免打扰数据
                if (!hasMore && remindMsgType == ERMT_USER_MSG) {
                    remindMsgType = ERMT_GROUP_MSG
                    hasMore = true
                    offset = 0
                }
            }
            U.getPreferenceUtils().setSettingBoolean(PREF_KEY_NO_REMIND_REFRESHED, true)
        }
    }

    /**
     * 是否刷新过本地免打扰缓存
     * @return
     */
    fun refreshNoRemindCacheIfNeeded() {
        if (!open()) {
            return
        }
        val needed = U.getPreferenceUtils().getSettingBoolean(PREF_KEY_NO_REMIND_REFRESHED, false)
        if (MyUserInfoManager.uid > 0 && needed) {
            refreshNoRemindCache(MyUserInfoManager.uid.toInt())
        }
    }

}