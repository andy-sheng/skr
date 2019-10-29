package com.module.home.persenter

import android.view.Gravity
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.account.UserAccountManager
import com.common.core.global.event.ShowDialogInHomeEvent
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.U
import com.module.home.MainPageSlideApi
import com.module.home.R
import com.module.home.view.VipReceiveCoinView
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import java.util.*

class VipReceiveCoinPresenter(val baseActivity: BaseActivity) : RxLifeCyclePresenter() {

    var vipDialog: DialogPlus? = null

    val PREF_KEY_VIP_RECEIVE = "vip_receive"

    internal var mHasShow = false

    internal var mMainPageSlideApi = ApiManager.getInstance().createService(MainPageSlideApi::class.java)

    fun checkVip() {
        if (!UserAccountManager.getInstance().hasAccount()) {
            MyLog.w(TAG, "no account")
            return
        }

        if (MyUserInfoManager.honorInfo?.isHonor()==false) {
            MyLog.w(TAG, "not a honor")
            return
        }

        if (mHasShow) {
            MyLog.d(TAG, "checkin mHasShow=$mHasShow")
            return
        }

        val lastTs = U.getPreferenceUtils().getSettingLong(PREF_KEY_VIP_RECEIVE, 0)
        val now = System.currentTimeMillis()
        val dayDiff = U.getDateTimeUtils().getDayDiff(lastTs, now)

        if (dayDiff == 0L) {
            MyLog.d(TAG, "今天展示过了 lastTs=$lastTs now=$now")
            mHasShow = true
            return
        }

        val map = HashMap<String, Any>()
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mMainPageSlideApi.vipTakeCoin(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    showDialog()
                }
            }

            override fun onError(e: Throwable) {
                MyLog.e(TAG, e)
            }
        }, this@VipReceiveCoinPresenter)
    }

    private fun showDialog() {
        if (vipDialog == null) {
            val checkInView = VipReceiveCoinView(baseActivity)
            vipDialog = DialogPlus.newDialog(baseActivity)
                    .setContentHolder(ViewHolder(checkInView))
                    .setGravity(Gravity.CENTER)
                    .setContentBackgroundResource(R.color.transparent)
                    .setOverlayBackgroundResource(R.color.black_trans_80)
                    .setExpanded(false)
                    .setCancelable(true)
                    .setOnDismissListener { dialog ->
                        // 不放在api里，因为到api那层不代表已经展示了
                        MyLog.d(TAG, "onDismiss dialog=$dialog")
                        U.getPreferenceUtils().setSettingLong(PREF_KEY_VIP_RECEIVE, System.currentTimeMillis())
                        mHasShow = true
                    }
                    .create()
        }
        EventBus.getDefault().post(ShowDialogInHomeEvent(vipDialog, 40))
    }

}
