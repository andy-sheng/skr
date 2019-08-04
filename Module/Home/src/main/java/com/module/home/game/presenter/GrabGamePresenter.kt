package com.module.home.game.presenter

import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.common.core.myinfo.event.MyUserInfoEvent
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.*
import com.common.utils.U
import com.component.busilib.friends.GrabSongApi
import com.component.busilib.friends.SpecialModel
import com.module.home.game.view.IGrabGameView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class GrabGamePresenter(internal var grabGameView: IGrabGameView) : RxLifeCyclePresenter() {

    private val mGrabSongApi: GrabSongApi = ApiManager.getInstance().createService(GrabSongApi::class.java)
    private var mLastUpdateQuickInfo: Long = 0    //快速加入房间更新成功时间
    private var mIsFirstQuick = true

    init {
        addToLifeCycle()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    fun initQuickRoom(isFlag: Boolean) {
        MyLog.d(TAG, "initQuickRoom isFlag=$isFlag")
        val now = System.currentTimeMillis()
        if (!isFlag) {
            // 半个小时更新一次吧
            if (now - mLastUpdateQuickInfo < 30 * 60 * 1000) {
                return
            }
        }

        var spResult = ""
        if (mIsFirstQuick) {
            // 先用SP里面的
            mIsFirstQuick = false
            spResult = U.getPreferenceUtils().getSettingString(U.getPreferenceUtils().longlySp(), "grab_game_tags", "")
            if (!TextUtils.isEmpty(spResult)) {
                try {
                    var jsonObject = JSON.parseObject(spResult, JSONObject::class.java)
                    var list = JSON.parseArray(jsonObject.getString("tags"), SpecialModel::class.java)
                    var offset = jsonObject.getIntValue("offset")
                    grabGameView.setQuickRoom(list, offset)
                } catch (e: Exception) {
                }

            }
        }

        val finalSpResult = spResult
        ApiMethods.subscribe(mGrabSongApi.getSepcialList(0, 20), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult) {
                if (obj.errno == 0) {
                    mLastUpdateQuickInfo = System.currentTimeMillis()
                    if (obj.data!!.toJSONString() != finalSpResult) {
                        U.getPreferenceUtils().setSettingString(U.getPreferenceUtils().longlySp(), "grab_game_tags", obj.data!!.toJSONString())
                        val list = JSON.parseArray(obj.data!!.getString("tags"), SpecialModel::class.java)
                        val offset = obj.data!!.getIntValue("offset")
                        grabGameView.setQuickRoom(list, offset)
                    }
                }
            }
        }, this, RequestControl("getSepcialList", ControlType.CancelThis))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MyUserInfoEvent.UserInfoChangeEvent) {
        initQuickRoom(true)
    }

    override fun destroy() {
        super.destroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }
}
