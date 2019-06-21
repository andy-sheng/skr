package com.module.home.game.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.core.userinfo.UserInfoServerApi
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.module.RouterConstants
import com.module.home.MainPageSlideApi
import com.module.home.R
import com.zq.dialog.ConfirmMatchInfoView
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.double_room_view_layout.view.*
import okhttp3.MediaType
import okhttp3.RequestBody
import java.util.HashMap

/**
 * 邂逅好声音
 */
class DoubleRoomGameView : RelativeLayout {
    companion object {
        const val SP_HAS_CONFIRM_INFO: String = "sp_has_confirm_info"   // 双人房是否确认过信息
    }

    var mConfirmMatchInfoView: ConfirmMatchInfoView? = null

    private var mModifyDisposable: Disposable? = null  //修改资料
    private var mDisposable: Disposable? = null        //获取次数
    private var userInfoServerApi: UserInfoServerApi? = null
    private var mainPageSlideApi: MainPageSlideApi? = null

    internal var mLastUpdateRemainTime: Long = 0    //上次拉去剩余次数的时间

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, R.layout.double_room_view_layout, this)

        start_match_iv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                var hasConfirm = U.getPreferenceUtils().getSettingBoolean(SP_HAS_CONFIRM_INFO, false)
                if (!hasConfirm) {
                    showConfirmView()
                } else {
                    ARouter.getInstance()
                            .build(RouterConstants.ACTIVITY_DOUBLE_PLAY)
                            .navigation()
                }
            }
        })
    }

    fun initData() {
        getRemainTimes(false)
    }

    fun showConfirmView() {
        mConfirmMatchInfoView = ConfirmMatchInfoView(context, object : ConfirmMatchInfoView.Listener {
            override fun onClickOutView() {
                hideConfirmView(true)
            }

            override fun onSelect(sex: Int, ageTag: Int) {
                if (mModifyDisposable != null && !mModifyDisposable!!.isDisposed) {
                    mModifyDisposable?.dispose()
                }
                if (userInfoServerApi == null) {
                    userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)
                }
                val map = HashMap<String, Any>()
                map["sex"] = sex
                map["stage"] = ageTag
                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                mModifyDisposable = ApiMethods.subscribe(userInfoServerApi?.modifyDoubleUserInfo(body), object : ApiObserver<ApiResult>() {
                    override fun process(obj: ApiResult?) {
                        if (obj?.errno == 0) {
                            U.getPreferenceUtils().setSettingBoolean(SP_HAS_CONFIRM_INFO, true)
                            //TODO 是否需要更新本地资料，服务器确认
                            hideConfirmView(true)
                        } else {
                            U.getToastUtil().showShort(obj?.errmsg)
                        }
                    }

                })
            }

        })

        if (this.indexOfChild(mConfirmMatchInfoView) == -1) {
            addView(mConfirmMatchInfoView)
        }
    }

    fun getRemainTimes(isFlag: Boolean) {
        val now = System.currentTimeMillis()
        if (!isFlag) {
            // 距离上次拉去已经超过30秒了
            if (now - mLastUpdateRemainTime < 30 * 1000) {
                return
            }
        }
        if (mDisposable != null && !mDisposable!!.isDisposed) {
            mDisposable?.dispose()
        }

        if (mainPageSlideApi == null) {
            mainPageSlideApi = ApiManager.getInstance().createService(MainPageSlideApi::class.java)
        }

        mDisposable = ApiMethods.subscribe(mainPageSlideApi?.remainTime, object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult?) {
                if (result?.errno == 0) {
                    mLastUpdateRemainTime = System.currentTimeMillis();
                    var totalRemainTimes = result.data.getIntValue("todayResTimes");
                    val spanStringBuilder = SpanUtils()
                            .append("今日剩余").setForegroundColor(U.getColor(R.color.white_trans_30))
                            .append("" + totalRemainTimes).setForegroundColor(Color.parseColor("#FFC15B"))
                            .append("次").setForegroundColor(U.getColor(R.color.white_trans_30))
                            .create()
                    remain_times_tv.text = spanStringBuilder
                } else {
                    // 请求出错了
                }
            }

        })

    }

    //TODO 后续可能加个动画
    fun hideConfirmView(hasAnimation: Boolean) {
        if (this.indexOfChild(mConfirmMatchInfoView) != -1) {
            removeView(mConfirmMatchInfoView)
        }
    }

    fun destory() {
        mDisposable?.dispose()
        mModifyDisposable?.dispose()
    }
}
