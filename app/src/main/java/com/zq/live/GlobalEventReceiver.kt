package com.zq.live

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.common.clipboard.ClipboardUtils
import com.common.core.account.UserAccountManager
import com.common.core.account.event.AccountEvent
import com.common.core.kouling.SkrKouLingUtils
import com.common.core.login.LoginActivity
import com.common.core.share.SharePanel
import com.common.core.share.SharePlatform
import com.common.core.share.ShareType
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.statistics.StatisticsAdapter
import com.common.utils.ActivityUtils
import com.common.utils.LogUploadUtils
import com.common.utils.NetworkUtils
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.component.busilib.manager.BgMusicManager
import com.component.busilib.recommend.RA
import com.component.busilib.recommend.RAServerApi
import com.dialog.view.TipsDialogView
import com.module.RouterConstants
import com.umeng.socialize.UMShareAPI
import com.umeng.socialize.bean.SHARE_MEDIA
import com.component.person.photo.manager.PhotoLocalApi
import com.module.feeds.make.FeedsMakeLocalApi
import com.module.feeds.watch.manager.FeedCollectLocalApi
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 这个类放在最顶层，因为只有可能它 调用 其它
 * 其它不后悔调用它
 */
object GlobalEventReceiver{

    internal var mUiHandler = Handler(Looper.getMainLooper())

    internal var mTipsDialogView: TipsDialogView? = null

    private val mNetworkChangeRunnable = Runnable { showNetworkDisConnectDialog() }

    val TAG = "GlobalEventReceiver"

    fun register() {
        EventBus.getDefault().register(this)
        if (UserAccountManager.getInstance().hasAccount()) {
            initABtestInfo()
        }
    }


    @Subscribe
    fun onEvent(event: ActivityUtils.ForeOrBackgroundChange) {
        if (event.foreground) {
            // 检查剪贴板
            val str = ClipboardUtils.getPaste()
            //            str = "dD0xJnU9MTM0MzA4OCZyPTEwMA==";
            if (!TextUtils.isEmpty(str)) {
                SkrKouLingUtils.tryParseScheme(str)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: AccountEvent.LogoffAccountEvent) {
        PhotoLocalApi.deleteAll()
        FeedsMakeLocalApi.deleteAll()
        FeedCollectLocalApi.deleteAll()
        if (event.reason == AccountEvent.LogoffAccountEvent.REASON_ACCOUNT_EXPIRED) {
            MyLog.w(TAG, "LogoffAccountEvent" + " 账号已经过期，需要重新登录,跳到登录页面")
        }
        if (!UserAccountManager.getInstance().hasAccount()) {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_LOGIN)
                    .withInt(LoginActivity.KEY_REASON, LoginActivity.REASON_LOGOFF)
                    .navigation()
        }
        val homeActivity = U.getActivityUtils().homeActivity
        if (homeActivity != null) {
            UMShareAPI.get(U.app()).deleteOauth(homeActivity, SHARE_MEDIA.WEIXIN, null)
            UMShareAPI.get(U.app()).deleteOauth(homeActivity, SHARE_MEDIA.QQ, null)
        }
        BgMusicManager.getInstance().destory()
    }

    @Subscribe
    fun onEvent(event: AccountEvent.SetAccountEvent) {
        initABtestInfo()
    }

    /**
     * 获取AB test 相关的信息
     */
    private fun initABtestInfo() {
        val raServerApi = ApiManager.getInstance().createService(RAServerApi::class.java)
        var dadian = {
            if (RA.hasTestList()) {
                val map = hashMapOf("testList" to RA.getTestList())
                StatisticsAdapter.recordCountEvent("ra", "active", map)
            }
        }
        ApiMethods.subscribe(raServerApi.getABtestInfo(RA.getTestList(),U.getAppInfoUtils().versionCode.toString()), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult) {
                if (obj.errno == 0) {
                    val vars = obj.data!!.getString("vars")
                    val testList = obj.data!!.getString("testList")
                    RA.setVar(vars)
                    RA.setTestList(testList)
                    dadian.invoke()
                } else {
                    dadian.invoke()
                }
            }

            override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                super.onNetworkError(errorType)
                dadian.invoke()
            }
        })
    }

    @Subscribe
    fun onEvent(event: LogUploadUtils.RequestOthersUploadLogSuccess) {
        val sharePanel = SharePanel(U.getActivityUtils().topActivity)
        val title = String.format("日志 id=%s,name=%s,date=%s", event.uploaderId, event.uploaderName, event.date)
        sharePanel.setShareContent(event.uploaderAvatar, title, event.extra, event.mLogUrl)
        sharePanel.share(SharePlatform.WEIXIN, ShareType.TEXT)
        MyLog.w(TAG, title + " url:" + event.mLogUrl)
        U.getToastUtil().showLong(title + "拉取成功，请将其分享给研发同学")
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: NetworkUtils.NetworkChangeEvent) {
        if (event.type == -1) {
            mUiHandler.removeCallbacksAndMessages(mNetworkChangeRunnable)
            mUiHandler.postDelayed(mNetworkChangeRunnable, 3000)
        } else {
            mUiHandler.removeCallbacks(mNetworkChangeRunnable)
        }
    }

    private fun showNetworkDisConnectDialog() {
        if (mTipsDialogView != null) {
            mTipsDialogView!!.dismiss(false)
        }
        val activity = U.getActivityUtils().topActivity
        if (activity != null) {
            mTipsDialogView = TipsDialogView.Builder(activity)
                    .setMessageTip("网络异常\n请检查网络连接后重试")
                    .setOkBtnTip("确认")
                    .setOkBtnClickListener(object : AnimateClickListener() {
                        override fun click(view: View) {
                            if (mTipsDialogView != null) {
                                mTipsDialogView!!.dismiss()
                            }
                        }
                    })
                    .build()
            mTipsDialogView!!.showByDialog()
        }
    }
}
