package com.module.home.persenter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseActivity
import com.common.core.account.UserAccountManager
import com.common.core.global.event.ShowDialogInHomeEvent
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.utils.U
import com.module.RouterConstants
import com.module.home.MainPageSlideApi
import com.module.home.R
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import org.greenrobot.eventbus.EventBus
import com.trello.rxlifecycle2.RxLifecycle.bindUntilEvent
import android.R



class EnterHomeDialogPresenter(val baseActivity: BaseActivity) : RxLifeCyclePresenter() {

    var dialogTask: DialogPlus? = null

    val PREF_KEY_HOME_TASK_DIALOG = "home_task_dialog"

    internal var mHasShow = false

    internal var mMainPageSlideApi = ApiManager.getInstance().createService(MainPageSlideApi::class.java)

    fun check() {
//        if(true){
//            showDialog()
//            return
//        }
        if (!UserAccountManager.hasAccount()) {
            MyLog.w(TAG, "no account")
            return
        }

        if (MyUserInfoManager.honorInfo?.isHonor() == false) {
            MyLog.w(TAG, "not a honor")
            return
        }

        if (mHasShow) {
            MyLog.d(TAG, "checkin mHasShow=$mHasShow")
            return
        }

        val lastTs = U.getPreferenceUtils().getSettingLong(PREF_KEY_HOME_TASK_DIALOG, 0)
        val now = System.currentTimeMillis()
        val dayDiff = U.getDateTimeUtils().getDayDiff(lastTs, now)

        if (dayDiff == 0L) {
            MyLog.d(TAG, "今天展示过了 lastTs=$lastTs now=$now")
            mHasShow = true
            return
        }
        showDialog()
    }

    private fun showDialog() {
        if (dialogTask == null) {

            val view = LayoutInflater.from(U.app()).inflate(R.layout.task_tips_dialog_view_layout, null,false)
            val mainIv: View = view.findViewById(R.id.main_iv)
            val closeIv:View = view.findViewById(R.id.close_iv)
            dialogTask = DialogPlus.newDialog(baseActivity)
                    .setContentHolder(ViewHolder(view))
                    .setGravity(Gravity.CENTER)
                    .setContentBackgroundResource(R.color.transparent)
                    .setOverlayBackgroundResource(R.color.black_trans_80)
                    .setExpanded(false)
                    .setCancelable(true)
                    .setOnDismissListener { dialog ->
                        // 不放在api里，因为到api那层不代表已经展示了
                        MyLog.d(TAG, "onDismiss dialog=$dialog")
                        U.getPreferenceUtils().setSettingLong(PREF_KEY_HOME_TASK_DIALOG, System.currentTimeMillis())
                        mHasShow = true
                    }
                    .create()

            mainIv.setDebounceViewClickListener {
                dialogTask?.dismiss()

                ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                        .withString("url", ApiManager.getInstance().findRealUrlByChannel("http://test.app.inframe.mobi/task/new?title=1"))
                        .navigation()
            }

            closeIv.setDebounceViewClickListener {
                dialogTask?.dismiss()
            }

            dialogTask?.getHolderView()?.setOnClickListener {
                dialogTask?.dismiss()
            }
        }
        EventBus.getDefault().post(ShowDialogInHomeEvent(dialogTask, 40))
    }

}
