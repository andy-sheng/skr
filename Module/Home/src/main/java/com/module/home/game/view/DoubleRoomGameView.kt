package com.module.home.game.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.core.userinfo.UserInfoServerApi
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.module.RouterConstants
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
                mDisposable = ApiMethods.subscribe(userInfoServerApi?.modifyDoubleUserInfo(body), object : ApiObserver<ApiResult>() {
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

    //TODO 后续可能加个动画
    fun hideConfirmView(hasAnimation: Boolean) {
        if (this.indexOfChild(mConfirmMatchInfoView) != -1) {
            removeView(mConfirmMatchInfoView)
        }
    }

    fun destory() {

    }
}
