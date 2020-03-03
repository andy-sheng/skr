package com.module.home.persenter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.account.UserAccountManager
import com.common.core.global.event.ShowDialogInHomeEvent
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.BaseImageView
import com.common.image.fresco.FrescoWorker
import com.common.image.model.BaseImage
import com.common.image.model.ImageFactory
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.*
import com.common.utils.U
import com.module.RouterConstants
import com.module.home.MainPageSlideApi
import com.module.home.R
import com.module.home.model.AppWindowModel
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import org.greenrobot.eventbus.EventBus


class EnterHomeDialogPresenter(val baseActivity: BaseActivity) : RxLifeCyclePresenter() {

    val PREF_KEY_HOME_TASK_DIALOG = "home_task_dialog"

    val mHasShowSet = HashSet<Int>()

    var list: (List<AppWindowModel>)? = null

    internal var mMainPageSlideApi = ApiManager.getInstance().createService(MainPageSlideApi::class.java)

    fun check() {
        if (!UserAccountManager.hasAccount()) {
            MyLog.w(TAG, "no account")
            return
        }

        if (MyUserInfoManager.honorInfo?.isHonor() == false) {
            MyLog.w(TAG, "not a honor")
            return
        }

        if (list == null) {
            ApiMethods.subscribe(mMainPageSlideApi.queryAppWindows(), object : ApiObserver<ApiResult>() {
                override fun process(result: ApiResult) {
                    if (result.errno == 0) {
                        val appWindowModelList: (List<AppWindowModel>)? = JSON.parseArray(result.data.getString("windows"), AppWindowModel::class.java)
                        list = appWindowModelList
                        list?.forEach {
                            single(it)
                        }
                    }
                }

                override fun onError(e: Throwable) {
                    MyLog.e(TAG, e)
                }
            }, EnterHomeDialogPresenter@ this, RequestControl("EnterHomeDialogPresenter check", ControlType.CancelThis))
        } else {
            list?.forEach {
                single(it)
            }
        }
    }

    private fun single(model: AppWindowModel) {
        if (mHasShowSet.contains(model.msgType)) {
            MyLog.d(TAG, "model.msgType is ${model.msgType} 已经展示过了")
            return
        }

        val lastTs = U.getPreferenceUtils().getSettingLong("$PREF_KEY_HOME_TASK_DIALOG${model.msgType}", 0)

        if (model.showType == 1) {
            //一天一次
            val now = System.currentTimeMillis()
            val dayDiff = U.getDateTimeUtils().getDayDiff(lastTs, now)

            if (dayDiff == 0L) {
                MyLog.d(TAG, "今天展示过了 lastTs=$lastTs now=$now type is ${model.msgType}")
                mHasShowSet.add(model.msgType)
            } else {
                showDialog(model)
            }
        } else {
            //2，只弹一次
            if (lastTs > 0) {
                MyLog.d(TAG, "展示过了 lastTs=$lastTs type is ${model.msgType}")
                mHasShowSet.add(model.msgType)
            } else {
                showDialog(model)
            }
        }
    }

    private fun showDialog(model: AppWindowModel) {
        val view = LayoutInflater.from(U.app()).inflate(R.layout.task_tips_dialog_view_layout, null, false)
        val mainIv: BaseImageView = view.findViewById(R.id.main_iv)
        val closeIv: View = view.findViewById(R.id.close_iv)
        val dialogTask: DialogPlus = DialogPlus.newDialog(baseActivity)
                .setContentHolder(ViewHolder(view))
                .setGravity(Gravity.CENTER)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setExpanded(false)
                .setCancelable(true)
                .setOnDismissListener { dialog ->
                    // 不放在api里，因为到api那层不代表已经展示了
                    MyLog.d(TAG, "onDismiss dialog=$dialog")
                    U.getPreferenceUtils().setSettingLong("$PREF_KEY_HOME_TASK_DIALOG${model.msgType}", System.currentTimeMillis())
                    mHasShowSet.add(model.msgType)
                }
                .create()

        mainIv.layoutParams.width = U.getDisplayUtils().dip2px(model.width.toFloat())
        mainIv.layoutParams.height = U.getDisplayUtils().dip2px(model.height.toFloat())

        mainIv.setDebounceViewClickListener {
            dialogTask?.dismiss()
            ARouter.getInstance().build(RouterConstants.ACTIVITY_SCHEME)
                    .withString("uri", model.schema)
                    .navigation()
        }

        FrescoWorker.loadImage(mainIv, ImageFactory.newPathImage(model.picURL)
                .setWidth(U.getDisplayUtils().dip2px(model.width.toFloat()))
                .setHeight(U.getDisplayUtils().dip2px(model.height.toFloat()))
                .build<BaseImage>())

        closeIv.setDebounceViewClickListener {
            dialogTask?.dismiss()
        }

        dialogTask?.getHolderView()?.setOnClickListener {
            dialogTask?.dismiss()
        }

        EventBus.getDefault().post(ShowDialogInHomeEvent(dialogTask, 40))
    }

}
