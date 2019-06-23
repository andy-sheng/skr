package com.module.playways.doubleplay.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.core.avatar.AvatarUtils
import com.common.image.fresco.BaseImageView
import com.common.rx.RxRetryAssist
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.doubleplay.DoubleRoomServerApi
import com.module.playways.doubleplay.model.DoubleEndRoomModel
import io.reactivex.Observable


class DoubleGameEndFragment : BaseFragment() {
    lateinit var mReportTv: ExTextView
    lateinit var mCloseIv: ImageView
    lateinit var mWhiteBg: ExImageView
    lateinit var mAvatarIv: BaseImageView
    lateinit var mEndTv: ExTextView
    lateinit var mChatTimeTv: ExTextView
    lateinit var mEndTipTv: ExTextView
    lateinit var mFollowTv: ExTextView
    lateinit var mMatchAgain: ExTextView
    lateinit var mLastNumTv: ExTextView

    lateinit var mDoubleRoomData: DoubleRoomData

    private var mDoubleRoomServerApi = ApiManager.getInstance().createService(DoubleRoomServerApi::class.java)

    override fun initView(): Int {
        return R.layout.double_game_end_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mReportTv = mRootView.findViewById<View>(R.id.report_tv) as ExTextView
        mCloseIv = mRootView.findViewById<View>(R.id.close_iv) as ImageView
        mWhiteBg = mRootView.findViewById<View>(R.id.white_bg) as ExImageView
        mAvatarIv = mRootView.findViewById<View>(R.id.avatar_iv) as BaseImageView
        mEndTv = mRootView.findViewById<View>(R.id.end_tv) as ExTextView
        mChatTimeTv = mRootView.findViewById<View>(R.id.chat_time_tv) as ExTextView
        mEndTipTv = mRootView.findViewById<View>(R.id.end_tip_tv) as ExTextView
        mFollowTv = mRootView.findViewById<View>(R.id.follow_tv) as ExTextView
        mMatchAgain = mRootView.findViewById<View>(R.id.match_again) as ExTextView
        mLastNumTv = mRootView.findViewById<View>(R.id.last_num_tv) as ExTextView

        mMatchAgain.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                activity?.finish()
                ARouter.getInstance()
                        .build(RouterConstants.ACTIVITY_DOUBLE_MATCH)
                        .navigation()
            }
        })

//        Observable.create<String> {
            ApiMethods.subscribe(mDoubleRoomServerApi.getEndGameInfo(mDoubleRoomData.gameId), object : ApiObserver<ApiResult>() {
                override fun process(obj: ApiResult?) {
//                    it.onComplete()
                    if (obj?.errno == 0) {
                        val model = JSON.parseObject(obj.data.toJSONString(), DoubleEndRoomModel::class.java)
                        setEndData(model)
                    } else {
                        U.getToastUtil().showShort(obj?.errmsg)
                    }
                }

                override fun onNetworkError(errorType: ErrorType?) {
//                    it.onError(Throwable("网络延迟"))
                }

                override fun onError(e: Throwable) {
//                    it.onError(Throwable("网络错误"))
                }
            }, this@DoubleGameEndFragment)
//        }.retryWhen(RxRetryAssist(10, "")).subscribe()
    }

    fun setEndData(model: DoubleEndRoomModel) {
        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder("")
                .setCircle(true)
                .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                .setBorderColor(Color.WHITE)
                .build())

        mEndTv.text = model.combineRoomCloseReasonDesc
        mChatTimeTv.text = "你与${mDoubleRoomData.getAntherUser()?.nickname}唱聊了${(model.chatDurTime / 60000) + 1}分钟"

        if (model.todayResTimes <= 0) {
            mLastNumTv.visibility = View.GONE
        } else {
            mLastNumTv.text = "今日剩余${model.todayResTimes}次"
            mLastNumTv.visibility = View.VISIBLE
        }

        if (model.isIsFriend || model.isIsFollow) {
            val followState = DrawableCreator.Builder()
                    .setCornersRadius(U.getDisplayUtils().dip2px(20f).toFloat())
                    .setSolidColor(U.getColor(R.color.white))
                    .setStrokeColor(Color.parseColor("#AD6C00"))
                    .setStrokeWidth(U.getDisplayUtils().dip2px(1f).toFloat())
                    .build()

            mFollowTv.text = "已关注"
            mFollowTv.background = followState
            mFollowTv.setTextColor(Color.parseColor("#AD6C00"))
        } else {
            val followState = DrawableCreator.Builder()
                    .setCornersRadius(U.getDisplayUtils().dip2px(20f).toFloat())
                    .setSolidColor(Color.parseColor("#FFC15B"))
                    .build()

            mFollowTv.text = "关注Ta"
            mFollowTv.background = followState
            mFollowTv.setTextColor(Color.parseColor("#AD6C00"))
        }
    }

    override fun setData(type: Int, data: Any?) {
        if (type == 0) {
            mDoubleRoomData = data as DoubleRoomData
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }
}