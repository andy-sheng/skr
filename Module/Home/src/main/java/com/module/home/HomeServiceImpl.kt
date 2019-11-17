package com.module.home

import android.app.Activity
import android.content.Context

import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.common.utils.U
import com.module.RouterConstants
import com.module.home.event.AuthSuccessEvent
import com.module.home.feedback.FeedbackFragment
import com.module.home.fragment.HalfRechargeFragment
import com.module.home.fragment.PersonFragment4
import com.module.home.game.view.openRaceActivity
import com.module.home.updateinfo.UploadAccountInfoActivity
import com.module.home.updateinfo.activity.EditAgeTagActivity

import org.greenrobot.eventbus.EventBus

@Route(path = RouterConstants.SERVICE_HOME, name = "测试服务")
class HomeServiceImpl : IHomeService {
    val TAG = "ChannelServiceImpl"

    /**
     * 主要返回的是只在 channel 自定义类型，注意在 commonservice 中增加接口，
     * 如是一个自定义view，增加自定义view需要的接口即可
     * 如果是一个实体类，可以简单的直接移动到 commonservice 相应的包下
     */
    override fun getData(type: Int, `object`: Any?): Any? {
        if (0 == type) {
            return PersonFragment4::class.java
        } else if (1 == type) {
            return HomeActivity::class.java.simpleName
        } else if (2 == type) {
            return HalfRechargeFragment::class.java
        } else if (3 == type) {
            return FeedbackFragment::class.java
        }

        return null
    }

    override fun init(context: Context) {

    }

    override fun authSuccess() {
        EventBus.getDefault().post(AuthSuccessEvent())
    }

    override fun goUploadAccountInfoActivity(activity: Activity) {
        UploadAccountInfoActivity.open(activity)
    }

    override fun goHomeActivity(loginActivity: Activity) {
        HomeActivity.open(loginActivity)
    }

    override fun goEditAgeActivity(runnable: Runnable) {
        ARouter.getInstance().build(RouterConstants.ACTIVITY_EDIT_AGE)
                .withInt("from", 0)
                .navigation()

        EditAgeTagActivity.setActionRunnable(runnable)
    }

    override fun goRaceMatchByAudience() {
        openRaceActivity(U.getActivityUtils().homeActivity,true)
    }
}
