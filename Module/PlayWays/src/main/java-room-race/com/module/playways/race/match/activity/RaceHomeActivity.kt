package com.module.playways.race.match.activity

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.base.BaseFragment
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.myinfo.event.MyUserInfoEvent
import com.common.core.permission.SkrAudioPermission
import com.common.core.userinfo.UserInfoServerApi
import com.common.core.userinfo.model.ScoreStateModel
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.rxretrofit.*
import com.common.statistics.StatisticsAdapter
import com.common.utils.FragmentUtils
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExRelativeLayout
import com.common.view.ex.ExTextView
import com.component.level.view.NormalLevelView2
import com.component.person.model.ScoreDetailModel
import com.component.person.model.UserRankModel
import com.component.person.view.UserInfoTitleView
import com.dialog.view.TipsDialogView
import com.imagebrowse.big.BigImageBrowseFragment
import com.module.RouterConstants
import com.module.playways.IPlaywaysModeService
import com.module.playways.R
import com.module.playways.race.RaceRoomServerApi
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.regex.Pattern

@Route(path = RouterConstants.ACTIVITY_RACE_HOME)
class RaceHomeActivity : BaseActivity() {

    private var mSmartRefreshLayout: SmartRefreshLayout? = null
    private var mIvVoiceRoom: ExImageView? = null
    private var mIvAthleticsPk: ExImageView? = null
    private var mUserInfoTitle: UserInfoTitleView? = null
    private var mLevelGapTv: TextView? = null
    private var mClassicsHeader: ClassicsHeader? = null
    private var mMedalLayout: ExRelativeLayout? = null
    private var mLevelView: NormalLevelView2? = null
    private var mLevelTv: ExTextView? = null
    private var mPaiweiImg: ImageView? = null
    private var mRankNumTv: ExTextView? = null
    private var mSingendImg: ImageView? = null
    private var mSingendNumTv: ExTextView? = null
    private var mRankArea: RelativeLayout? = null
    private var mRankText: ExTextView? = null
    private var mRankDiffIv: ExImageView? = null
    private var mMedalIv: ExImageView? = null
    private var ivBack: ImageView? = null

    private val userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.race_home_activity_layout
    }

    override fun canSlide(): Boolean {
        return false
    }

    override fun useEventBus(): Boolean {
        return true
    }

    override fun initData(savedInstanceState: Bundle?) {

        mSmartRefreshLayout = findViewById(R.id.smart_refresh_layout)
        mClassicsHeader = findViewById(R.id.classics_header)

        mUserInfoTitle = findViewById(R.id.user_info_title)
        mLevelGapTv = findViewById(R.id.level_gap_tv)
        mMedalLayout = findViewById(R.id.medal_layout)
        mLevelView = findViewById(R.id.level_view)
        mLevelTv = findViewById(R.id.level_tv)
        mPaiweiImg = findViewById(R.id.paiwei_img)
        mRankNumTv = findViewById(R.id.rank_num_tv)
        mSingendImg = findViewById(R.id.singend_img)
        mSingendNumTv = findViewById(R.id.singend_num_tv)
        mRankArea = findViewById(R.id.rank_area)
        mRankText = findViewById(R.id.rank_text)
        mRankDiffIv = findViewById(R.id.rank_diff_iv)
        mIvAthleticsPk = findViewById(R.id.iv_athletics_pk)
        mIvVoiceRoom = findViewById(R.id.iv_voice_room)
        mMedalIv = findViewById(R.id.medal_iv)
        ivBack = findViewById(R.id.iv_back)

        mSmartRefreshLayout?.setEnableRefresh(true)
        mSmartRefreshLayout?.setEnableLoadMore(false)
        mSmartRefreshLayout?.setEnableLoadMoreWhenContentNotFull(true)
        mSmartRefreshLayout?.setEnableOverScrollDrag(true)
        mSmartRefreshLayout?.setRefreshHeader(mClassicsHeader!!)
        mSmartRefreshLayout?.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {

            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                refreshBaseInfo()
                getLevelPage()
                getRankLevel()
            }
        })

        mIvAthleticsPk?.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View) {
                StatisticsAdapter.recordCountEvent("game_rank", "rankgame", null)
                openPlayWaysActivityByRank()
            }
        })

        mRankArea?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                StatisticsAdapter.recordCountEvent("game_rank", "ranklist", null)
                val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                val baseFragment = iRankingModeService.leaderboardFragmentClass as Class<BaseFragment>
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this@RaceHomeActivity, baseFragment)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build())
            }
        })

        mIvVoiceRoom?.setOnClickListener(object : AnimateClickListener() {
            override fun click(v: View) {
                StatisticsAdapter.recordCountEvent("game_rank", "practice", null)
                ARouter.getInstance().build(RouterConstants.ACTIVITY_AUDIOROOM)
                        .withBoolean("selectSong", true)
                        .navigation()
            }
        })

        mUserInfoTitle?.setListener {
            BigImageBrowseFragment.open(false, this, MyUserInfoManager.avatar)
        }

        mLevelGapTv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                        .withString(RouterConstants.KEY_WEB_URL, "https://fe.inframe.mobi/pages/banner/2p8p3gf3ujzxsw97z.html")
                        .navigation()
            }
        })

        ivBack?.setAnimateDebounceViewClickListener { finish() }


        refreshBaseInfo()
        getLevelPage()
        getRankLevel()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MyUserInfoEvent.UserInfoChangeEvent) {
        refreshBaseInfo()
    }

    private fun refreshBaseInfo() {
        mUserInfoTitle?.showBaseInfo()
    }

    private fun getLevelPage() {
        ApiMethods.subscribe(userInfoServerApi.getLevelDetail(MyUserInfoManager.uid), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val scoreDetailModel = JSON.parseObject(result.data!!.toJSONString(), ScoreDetailModel::class.java)
                    //                    ScoreStateModel stateModel = JSON.parseObject(result.getData().getString("ranking"), ScoreStateModel.class);
                    //                    long raceTicketCnt = result.getData().getLongValue("raceTicketCnt");
                    //                    long standLightCnt = result.getData().getLongValue("standLightCnt");
                    showUserLevel(scoreDetailModel.scoreStateModel)
                    showGameStatic(scoreDetailModel.raceTicketCnt, scoreDetailModel.standLightCnt)
                }
                mSmartRefreshLayout?.finishRefresh()
            }
        }, this)
    }

    private fun getRankLevel() {
        ApiMethods.subscribe(userInfoServerApi.reginDiff, object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val userRankModel = JSON.parseObject(result.data!!.getString("diff"), UserRankModel::class.java)
                    showRankView(userRankModel)
                    mSmartRefreshLayout?.finishRefresh()
                }
            }

            override fun onError(e: Throwable) {
                mSmartRefreshLayout?.finishRefresh()
                U.getToastUtil().showShort("网络异常")
            }

            override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                mSmartRefreshLayout?.finishRefresh()
                U.getToastUtil().showShort("网络超时")
            }
        }, this)
    }

    fun showRankView(userRankModel: UserRankModel?) {
        mUserInfoTitle?.showRankView(userRankModel)

        when {
            userRankModel == null -> {
                // 为空
                mRankDiffIv?.visibility = View.GONE
                mRankText?.visibility = View.GONE
            }
            userRankModel.diff == 0 -> {
                // 默认按照上升显示
                mRankDiffIv?.visibility = View.GONE
                mRankText?.text = highlight(userRankModel.getText(), userRankModel.getHighlight(), true)
            }
            userRankModel.diff > 0 -> {
                mRankDiffIv?.visibility = View.VISIBLE
                mRankDiffIv?.setImageResource(R.drawable.shangsheng_ic)
                mRankText?.text = highlight(userRankModel.getText(), userRankModel.getHighlight(), true)
            }
            else -> {
                mRankDiffIv?.visibility = View.VISIBLE
                mRankDiffIv?.setImageResource(R.drawable.xiajiang_ic)
                mRankText?.text = highlight(userRankModel.getText(), userRankModel.getHighlight(), false)
            }
        }

//        showPopWindow(userRankModel.getDiff());

        when {
            userRankModel == null -> mMedalIv?.visibility = View.GONE
            userRankModel.badge == UserRankModel.STAR_BADGE -> mMedalIv?.background = resources.getDrawable(R.drawable.paiming)
            userRankModel.badge == UserRankModel.TOP_BADGE -> mMedalIv?.background = resources.getDrawable(R.drawable.paihang)
            userRankModel.badge == UserRankModel.SHANDIAN_BADGE -> mMedalIv?.background = resources.getDrawable(R.drawable.dabai)
        }
    }

    fun showGameStatic(raceTicketCnt: Long, standLightCnt: Long) {

        val raceStringBuilder = SpanUtils()
                .append(raceTicketCnt.toString()).setFontSize(14, true)
                .create()
        mRankNumTv?.text = raceStringBuilder
        val standStringBuilder = SpanUtils()
                .append(standLightCnt.toString()).setFontSize(14, true)
                .create()
        mSingendNumTv?.text = standStringBuilder

    }

    fun showUserLevel(model: ScoreStateModel?) {
        // 展示段位信息
        model?.let {
            mLevelView?.bindData(it.mainRanking, it.subRanking)
            mLevelTv?.text = it.rankingDesc
            mLevelGapTv?.text = "距离下次升段还需${it.maxExp - it.currExp}经验"
        }
    }

    private fun highlight(text: String, target: String, isUp: Boolean): SpannableString {
        val spannableString = SpannableString(text)
        val pattern = Pattern.compile(target)
        val matcher = pattern.matcher(text)
        while (matcher.find()) {
            val span = ForegroundColorSpan(Color.parseColor("#FF3B3C"))
            spannableString.setSpan(span, matcher.start(), matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return spannableString
    }

    private fun openPlayWaysActivityByRank() {
        launch(Dispatchers.Main) {
            var tipsDialogView: TipsDialogView? = null
            val api = ApiManager.getInstance().createService(RaceRoomServerApi::class.java)
            val check = subscribe(RequestControl("checkRank", ControlType.CancelThis)) { api.checkRank(1) }
            if (check.errno == 0) {
                // 可以进房间
                val skrAudioPermission = SkrAudioPermission()
                skrAudioPermission.ensurePermission({
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_RACE_MATCH_ROOM)
                            .navigation()
                }, true)
            } else {
                if (check.errno == ERROR_NETWORK_BROKEN) {
                    tipsDialogView?.dismiss()
                    tipsDialogView = TipsDialogView.Builder(this@RaceHomeActivity)
                            .setMessageTip("网络连接不可用，请检查网络后重试")
                            .setOkBtnTip("确定")
                            .setOkBtnClickListener(object : DebounceViewClickListener() {
                                override fun clickValid(v: View?) {
                                    tipsDialogView?.dismiss()
                                }
                            })
                            .build()
                    tipsDialogView?.showByDialog()
                } else if (check.errno > 0) {
                    tipsDialogView?.dismiss()
                    tipsDialogView = TipsDialogView.Builder(this@RaceHomeActivity)
                            .setMessageTip(check.errmsg)
                            .setOkBtnTip("确定")
                            .setOkBtnClickListener(object : DebounceViewClickListener() {
                                override fun clickValid(v: View?) {
                                    tipsDialogView?.dismiss()
                                }
                            })
                            .build()
                    tipsDialogView?.showByDialog()
                }
            }
        }
    }
}