package com.module.home.game.view

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseActivity
import com.common.base.BaseFragment
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.GameStatisModel
import com.common.core.userinfo.model.UserLevelModel
import com.common.core.userinfo.model.UserRankModel
import com.common.statistics.StatisticsAdapter
import com.common.utils.FragmentUtils
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExRelativeLayout
import com.common.view.ex.ExTextView
import com.component.busilib.constans.GameModeType
import com.imagebrowse.big.BigImageBrowseFragment
import com.module.RouterConstants
import com.module.home.R
import com.module.home.persenter.PkInfoPresenter
import com.module.home.view.IPkInfoView
import com.module.home.widget.UserInfoTitleView
import com.module.playways.IPlaywaysModeService
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import com.component.level.view.NormalLevelView2
import java.util.regex.Pattern

// 排位赛
class PKGameView(fragment: BaseFragment) : RelativeLayout(fragment.context), IPkInfoView {
    val TAG = "PKGameView"

    private val mSmartRefreshLayout: SmartRefreshLayout
    private val mIvVoiceRoom: ExImageView
    private val mIvAthleticsPk: ExImageView
    private val mUserInfoTitle: UserInfoTitleView
    private val mClassicsHeader: ClassicsHeader
    private val mMedalLayout: ExRelativeLayout
    private val mLevelView: NormalLevelView2
    private val mLevelTv: ExTextView
    private val mPaiweiImg: ImageView
    private val mRankNumTv: ExTextView
    private val mSingendImg: ImageView
    private val mSingendNumTv: ExTextView
    private val mRankArea: RelativeLayout
    private val mRankText: ExTextView
    private val mRankDiffIv: ExImageView
    private val mMedalIv: ExImageView


    private var rank = 0           //当前父段位
    private var subRank = 0        //当前子段位
    private var starNum = 0        //当前星星
    private var starLimit = 0      //当前星星上限
    private var levelDesc: String = ""

    private var mPkInfoPresenter: PkInfoPresenter? = null

    init {
        View.inflate(context, R.layout.pk_game_view_layout, this)

        mSmartRefreshLayout = findViewById(R.id.smart_refresh_layout)
        mClassicsHeader = findViewById(R.id.classics_header)

        mUserInfoTitle = findViewById(R.id.user_info_title)
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
        mUserInfoTitle.topUserBg.visibility = View.GONE
        mSmartRefreshLayout.setEnableRefresh(true)
        mSmartRefreshLayout.setEnableLoadMore(false)
        mSmartRefreshLayout.setEnableLoadMoreWhenContentNotFull(true)
        mSmartRefreshLayout.setEnableOverScrollDrag(true)
        mSmartRefreshLayout.setRefreshHeader(mClassicsHeader)
        mSmartRefreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {

            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                initData(true)
            }
        })

        mIvAthleticsPk.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View) {
                StatisticsAdapter.recordCountEvent("game_rank", "rankgame", null)
                ARouter.getInstance().build(RouterConstants.ACTIVITY_PLAY_WAYS)
                        .withInt("key_game_type", GameModeType.GAME_MODE_CLASSIC_RANK)
                        .withBoolean("selectSong", true)
                        .navigation()

                //                StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_HOME),
                //                        StatConstants.KEY_RANK_CLICK, null);
            }
        })

        mRankArea.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                StatisticsAdapter.recordCountEvent("game_rank", "ranklist", null)
                val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                val baseFragment = iRankingModeService.leaderboardFragmentClass as Class<BaseFragment>
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(context as BaseActivity, baseFragment)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build())
            }
        })

        mIvVoiceRoom.setOnClickListener(object : AnimateClickListener() {
            override fun click(v: View) {
                StatisticsAdapter.recordCountEvent("game_rank", "practice", null)
                ARouter.getInstance().build(RouterConstants.ACTIVITY_AUDIOROOM)
                        .withBoolean("selectSong", true)
                        .navigation()
            }
        })

        mUserInfoTitle.setListener {
            BigImageBrowseFragment.open(false,
                    fragment.activity, MyUserInfoManager.getInstance().avatar)
        }

        mPkInfoPresenter = PkInfoPresenter(this)
        refreshBaseInfo()
    }

    override fun showRankView(userRankModel: UserRankModel?) {
        mUserInfoTitle.showRankView(userRankModel)

        when {
            userRankModel == null -> {
                // 为空
                mRankDiffIv.visibility = View.GONE
                mRankText.visibility = View.GONE
            }
            userRankModel.diff == 0 -> {
                // 默认按照上升显示
                mRankDiffIv.visibility = View.GONE
                mRankText.text = highlight(userRankModel.getText(), userRankModel.getHighlight(), true)
            }
            userRankModel.diff > 0 -> {
                mRankDiffIv.visibility = View.VISIBLE
                mRankDiffIv.setImageResource(R.drawable.shangsheng_ic)
                mRankText.text = highlight(userRankModel.getText(), userRankModel.getHighlight(), true)
            }
            else -> {
                mRankDiffIv.visibility = View.VISIBLE
                mRankDiffIv.setImageResource(R.drawable.xiajiang_ic)
                mRankText.text = highlight(userRankModel.getText(), userRankModel.getHighlight(), false)
            }
        }

//        showPopWindow(userRankModel.getDiff());

        when {
            userRankModel == null -> mMedalIv.visibility = View.GONE
            userRankModel.badge == UserRankModel.STAR_BADGE -> mMedalIv.background = resources.getDrawable(R.drawable.paiming)
            userRankModel.badge == UserRankModel.TOP_BADGE -> mMedalIv.background = resources.getDrawable(R.drawable.paihang)
            userRankModel.badge == UserRankModel.SHANDIAN_BADGE -> mMedalIv.background = resources.getDrawable(R.drawable.dabai)
        }
    }

    override fun showGameStatic(list: MutableList<GameStatisModel>?) {
        mSmartRefreshLayout.finishRefresh()
        if (list != null && list.size > 0) {
            for (gameStatisModel in list) {
                if (gameStatisModel.mode == GameModeType.GAME_MODE_CLASSIC_RANK) {
                    val stringBuilder = SpanUtils()
                            .append(gameStatisModel.totalTimes.toString()).setFontSize(14, true)
                            .append("场").setFontSize(10, true)
                            .create()
                    mRankNumTv.text = stringBuilder
                } else if (gameStatisModel.mode == GameModeType.GAME_MODE_GRAB) {
                    val stringBuilder = SpanUtils()
                            .append(gameStatisModel.totalTimes.toString()).setFontSize(14, true)
                            .append("首").setFontSize(10, true)
                            .create()
                    mSingendNumTv.text = stringBuilder
                }
            }
        }
    }

    fun initData(flag: Boolean) {
        refreshBaseInfo()
        mPkInfoPresenter?.getHomePage(MyUserInfoManager.getInstance().uid, flag)
    }

    override fun showUserLevel(list: MutableList<UserLevelModel>?) {
        mSmartRefreshLayout.finishRefresh()
        // 展示段位信息
        if (list != null && list.size > 0) {
            for (userLevelModel in list) {
                when {
                    userLevelModel.type == UserLevelModel.RANKING_TYPE -> rank = userLevelModel.score
                    userLevelModel.type == UserLevelModel.SUB_RANKING_TYPE -> {
                        subRank = userLevelModel.score
                        levelDesc = userLevelModel.desc
                    }
                    userLevelModel.type == UserLevelModel.TOTAL_RANKING_STAR_TYPE -> starNum = userLevelModel.score
                    userLevelModel.type == UserLevelModel.REAL_RANKING_STAR_TYPE -> starLimit = userLevelModel.score
                }
            }
        }
        mLevelView.bindData(rank, subRank)
        mLevelTv.text = levelDesc
    }

    override fun refreshBaseInfo() {
        mUserInfoTitle.showBaseInfo()
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

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        destory()
    }

    fun destory() {
        mPkInfoPresenter?.destroy()
    }

}