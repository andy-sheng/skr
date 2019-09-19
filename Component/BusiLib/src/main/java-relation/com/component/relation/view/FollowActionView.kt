package com.component.relation.view

import android.content.Context
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.UserInfoServerApi
import com.common.core.userinfo.event.RelationChangeEvent
import com.common.rxretrofit.*
import com.common.view.ex.ExTextView
import io.reactivex.disposables.Disposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

abstract class FollowActionView(context: Context) : ExTextView(context) {
    val userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)
    //好友态
    abstract fun isFriendState()

    //关注态
    abstract fun isFollowState()

    //陌生态
    abstract fun isStrangerState()

    abstract fun useEventBus(): Boolean

    var userID: Int? = null

    var mTask: Disposable? = null

    init {
        if (useEventBus()) {
            EventBus.getDefault().register(this)
        }
    }

    fun showRelation(isBlacked: Boolean, isFollow: Boolean, isFriend: Boolean) {
        if (isFriend) {
            isFriendState()
        } else if (isFollow) {
            isFollowState()
        } else {
            isStrangerState()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RelationChangeEvent) {
        userID?.let {
            if (it == event.useId) {
                if (event.isFriend) {
                    isFriendState()
                } else if (event.isFollow) {
                    isFollowState()
                } else {
                    isStrangerState()
                }
            }
        }
    }

    fun getRelation() {
        mTask = ApiMethods.subscribe(userInfoServerApi.getRelation(MyUserInfoManager.getInstance().uid.toInt()), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult?) {
                if (result?.errno == 0) {
                    showRelation(result.data.getBooleanValue("isBlacked"), result.data.getBooleanValue("isFollow"), result.data.getBooleanValue("isFriend"))
                }
            }
        }, RequestControl("FollowActionView " + "getRelation", ControlType.CancelThis))
    }

    fun follow(userID: Int) {
        UserInfoManager.getInstance().mateRelation(userID, UserInfoManager.RA_BUILD, false, 0, null)
    }

    fun destroy() {
        EventBus.getDefault().unregister(this)
        mTask?.dispose()
    }
}