package com.module.playways.doubleplay.activity

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.permission.SkrAudioPermission
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiManager.APPLICATION_JSON
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.statistics.StatisticsAdapter
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.component.busilib.verify.SkrVerifyUtils
import com.component.busilib.view.SelectSexDialogView
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.doubleplay.DoubleRoomServerApi
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.RequestBody

@Route(path = RouterConstants.ACTIVITY_DOUBLE_HOME)
class DoubleHomeActivity : BaseActivity() {

    private var inviteFriendIv: ExTextView? = null
    private var exTextView: ExTextView? = null
    private var startMatchIv: ExTextView? = null
    private var randomMatchTv: ExTextView? = null
    private var remainTimesTv: TextView? = null
    private var filterTv: ExTextView? = null

    internal var mRealNameVerifyUtils = SkrVerifyUtils()

    val mainPageSlideApi: DoubleRoomServerApi = ApiManager.getInstance().createService(DoubleRoomServerApi::class.java)
    var hasRemainTime: Boolean = false        //默认已经没有次数了

    val mSkrAudioPermission: SkrAudioPermission = SkrAudioPermission()

    var mSelectSexDialogPlus: DialogPlus? = null

    var mSelectView: SelectSexDialogView? = null
    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.double_home_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        inviteFriendIv = findViewById(R.id.invite_friend_iv)
        exTextView = findViewById(R.id.exTextView)
        startMatchIv = findViewById(R.id.start_match_iv)
        randomMatchTv = findViewById(R.id.random_match_tv)
        remainTimesTv = findViewById(R.id.remain_times_tv)
        filterTv = findViewById(R.id.filter_tv)

        startMatchIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (!U.getNetworkUtils().hasNetwork()) {
                    U.getToastUtil().showLong("网络连接失败 请检查网络")
                    return
                }

                if (hasRemainTime) {
                    StatisticsAdapter.recordCountEvent("game_cp", "invite1", null)
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
                                                finish()
                                            } else {
                                                showSexFilterView(true)
                                            }
                                        }, {
                                            MyLog.e("SelectSexDialogView", it)
                                        })
                            }
                        }
                    }, true)
                } else {
                    U.getToastUtil().showLong("今日唱聊匹配次数用完啦～")
                    StatisticsAdapter.recordCountEvent("game_cp", "invite1_outchance", null)
                }
            }
        })

        inviteFriendIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                StatisticsAdapter.recordCountEvent("game_cp", "invite3", null)
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
                            goCreateDoubleRoom()
                        }
                    }
                }, true)
            }
        })

        filterTv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                showSexFilterView(false)
            }
        })

        getRemainTimes()
    }

    fun goCreateDoubleRoom() {
        val body = RequestBody.create(MediaType.parse(APPLICATION_JSON), JSON.toJSONString(null))
        ApiMethods.subscribe(mainPageSlideApi.createRoom(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val doubleRoomData = DoubleRoomData.makeRoomDataFromJsonObject(result.data!!)
                    doubleRoomData.doubleRoomOri = DoubleRoomData.DoubleRoomOri.CREATE
                    doubleRoomData.inviterId = MyUserInfoManager.getInstance().uid
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_DOUBLE_PLAY)
                            .withSerializable("roomData", doubleRoomData)
                            .navigation()
                    finish()
                } else {
                    U.getToastUtil().showShort(result.errmsg)
                }
            }

            override fun onError(e: Throwable) {
                U.getToastUtil().showShort("网络错误")
            }

            override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                U.getToastUtil().showShort("网络延迟")
            }
        }, this)
    }

    private fun getRemainTimes() {
        ApiMethods.subscribe(mainPageSlideApi.remainTime, object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult?) {
                if (result?.errno == 0) {
                    var totalRemainTimes = result.data.getIntValue("todayResTimes");
                    hasRemainTime = totalRemainTimes > 0
                    val spanStringBuilder = SpanUtils()
                            .append("今日剩余").setForegroundColor(U.getColor(R.color.white_trans_50))
                            .append("" + totalRemainTimes).setForegroundColor(Color.parseColor("#FFC15B"))
                            .append("次").setForegroundColor(U.getColor(R.color.white_trans_50))
                            .create()
                    remainTimesTv?.text = spanStringBuilder
                } else {
                    // 请求出错了
                }
            }
        }, this)
    }

    fun showSexFilterView(needMatch: Boolean) {
        if (mSelectSexDialogPlus == null) {
            mSelectView = SelectSexDialogView(this)
            mSelectSexDialogPlus = DialogPlus.newDialog(this)
                    .setContentHolder(ViewHolder(mSelectView))
                    .setGravity(Gravity.BOTTOM)
                    .setContentBackgroundResource(R.color.transparent)
                    .setOverlayBackgroundResource(R.color.black_trans_80)
                    .setExpanded(false)
                    .create()
        }

        mSelectView?.onClickMatch = { isFindMale, isMeMale ->
            mSelectSexDialogPlus?.dismiss()
            if (needMatch) {
                val bundle = Bundle()
                bundle.putBoolean("is_find_male", isFindMale
                        ?: true)
                bundle.putBoolean("is_me_male", isMeMale
                        ?: true)
                ARouter.getInstance()
                        .build(RouterConstants.ACTIVITY_DOUBLE_MATCH)
                        .withBundle("bundle", bundle)
                        .navigation()
            }
        }

        mSelectView?.reset()
        mSelectSexDialogPlus?.show()
    }

    override fun useEventBus(): Boolean {
        return false
    }
}