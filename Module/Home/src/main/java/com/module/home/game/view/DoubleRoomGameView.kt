package com.module.home.game.view

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.RelativeLayout
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.permission.SkrAudioPermission
import com.common.core.userinfo.UserInfoServerApi
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.statistics.StatisticsAdapter
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.component.busilib.verify.SkrVerifyUtils
import com.module.RouterConstants
import com.module.home.MainPageSlideApi
import com.module.home.R
import com.module.playways.IPlaywaysModeService
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.zq.person.view.ConfirmMatchInfoView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.double_room_view_layout.view.*
import okhttp3.MediaType
import okhttp3.RequestBody
import java.util.*

/**
 * 邂逅好声音
 */
class DoubleRoomGameView : RelativeLayout {
    companion object {
        const val SP_HAS_CONFIRM_INFO: String = "sp_has_confirm_info"   // 双人房是否确认过信息
    }

    var mConfirmMatchInfoView: ConfirmMatchInfoView? = null

    internal var mRealNameVerifyUtils = SkrVerifyUtils()

    private var mModifyDisposable: Disposable? = null  //修改资料
    private var mDisposable: Disposable? = null        //获取次数
    private var userInfoServerApi: UserInfoServerApi? = null
    private var mainPageSlideApi: MainPageSlideApi? = null
    var hasRemainTime: Boolean = false        //默认已经没有次数了
    internal var mLastUpdateRemainTime: Long = 0    //上次拉去剩余次数的时间

    var mSkrAudioPermission: SkrAudioPermission

    var mSelectSexDialogPlus: DialogPlus? = null

    var mSelectView: SelectSexDialogView? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, R.layout.double_room_view_layout, this)
        mSkrAudioPermission = SkrAudioPermission()
        mainPageSlideApi = ApiManager.getInstance().createService(MainPageSlideApi::class.java)

        start_match_iv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (!U.getNetworkUtils().hasNetwork()) {
                    U.getToastUtil().showLong("网络连接失败 请检查网络")
                    return
                }

                if (hasRemainTime) {
                    StatisticsAdapter.recordCountEvent("cp", "invite1", null)
                    mSkrAudioPermission.ensurePermission({
                        mRealNameVerifyUtils.checkJoinDoubleRoomPermission {
                            /**
                             * 判断有没有年龄段
                             */
                            if (!MyUserInfoManager.getInstance().hasAgeStage()) {
                                ARouter.getInstance().build(RouterConstants.ACTIVITY_EDIT_AGE)
                                        .withInt("from", 0)
                                        .navigation()
                            } else {
                                val sex = object {
                                    var mIsFindMale: Boolean? = null
                                    var mMeIsMale: Boolean? = null
                                }

                                Observable.create<Boolean> {
                                    if (U.getPreferenceUtils().hasKey("is_find_male") && U.getPreferenceUtils().hasKey("is_me_male")) {
                                        sex.mIsFindMale = U.getPreferenceUtils().getSettingBoolean("is_find_male", true)
                                        sex.mMeIsMale = U.getPreferenceUtils().getSettingBoolean("is_me_male", true)
                                        it.onNext(true)
                                    } else {
                                        it.onNext(false)
                                    }

                                    it.onComplete()
                                }.subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe({
                                            if (it) {
                                                val bundle = Bundle()
                                                bundle.putBoolean("is_find_male", sex.mIsFindMale
                                                        ?: true)
                                                bundle.putBoolean("is_me_male", sex.mMeIsMale
                                                        ?: true)
                                                ARouter.getInstance()
                                                        .build(RouterConstants.ACTIVITY_DOUBLE_MATCH)
                                                        .withBundle("bundle", bundle)
                                                        .navigation()
                                            } else {
                                                showSexFilterView()
                                            }
                                        }, {
                                            MyLog.e("SelectSexDialogView", it)
                                        })
                            }
                        }
                    }, true)
                } else {
                    U.getToastUtil().showLong("今日唱聊匹配次数用完啦～")
                }
            }
        })

        invite_friend_iv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                mSkrAudioPermission.ensurePermission({
                    mRealNameVerifyUtils.checkJoinDoubleRoomPermission {
                        /**
                         * 判断有没有年龄段
                         */
                        if (!MyUserInfoManager.getInstance().hasAgeStage()) {
                            ARouter.getInstance().build(RouterConstants.ACTIVITY_EDIT_AGE)
                                    .withInt("from", 0)
                                    .navigation()
                        } else {
                            val playWaysService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                            playWaysService?.createDoubleRoom()
                        }
                    }
                }, true)
            }
        })

        filter_tv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                showSexFilterView()
            }
        })
    }

    fun initData() {
        getRemainTimes(false)
    }

    fun showSexFilterView() {
        if (mSelectSexDialogPlus == null) {
            mSelectView = SelectSexDialogView(this@DoubleRoomGameView.context)
            mSelectView?.onClickMatch = { isFindMale, isMeMale ->
                mSelectSexDialogPlus?.dismiss()
            }

            mSelectSexDialogPlus = DialogPlus.newDialog(context!!)
                    .setContentHolder(ViewHolder(mSelectView))
                    .setGravity(Gravity.BOTTOM)
                    .setContentBackgroundResource(R.color.transparent)
                    .setOverlayBackgroundResource(R.color.black_trans_80)
                    .setExpanded(false)
                    .create()
        }

        mSelectView?.reset()
        mSelectSexDialogPlus?.show()
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
            // 距离上次拉去已经超过10秒了
            if (now - mLastUpdateRemainTime < 10 * 1000) {
                return
            }
        }
        if (mDisposable != null && !mDisposable!!.isDisposed) {
            mDisposable?.dispose()
        }

        mDisposable = ApiMethods.subscribe(mainPageSlideApi?.remainTime, object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult?) {
                if (result?.errno == 0) {
                    mLastUpdateRemainTime = System.currentTimeMillis();
                    var totalRemainTimes = result.data.getIntValue("todayResTimes");
                    hasRemainTime = totalRemainTimes > 0
                    val spanStringBuilder = SpanUtils()
                            .append("今日剩余").setForegroundColor(U.getColor(R.color.white_trans_80))
                            .append("" + totalRemainTimes).setForegroundColor(Color.parseColor("#FFC15B"))
                            .append("次").setForegroundColor(U.getColor(R.color.white_trans_80))
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
