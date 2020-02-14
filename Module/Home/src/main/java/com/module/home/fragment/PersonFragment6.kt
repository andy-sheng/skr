package com.module.home.fragment

import android.graphics.Color
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfo
import com.common.core.myinfo.MyUserInfoLocalApi
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.myinfo.event.MyUserInfoEvent
import com.common.core.upgrade.UpgradeData
import com.common.core.upgrade.UpgradeManager
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.UserInfoServerApi
import com.common.core.userinfo.model.ClubMemberInfo
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.friends.VoiceInfoModel
import com.component.level.utils.LevelConfigUtils
import com.component.person.event.UploadMyVoiceInfo
import com.component.person.model.RelationNumModel
import com.component.person.utils.StringFromatUtils
import com.component.person.view.CommonAudioView
import com.component.person.view.PersonClubView
import com.component.person.view.PersonPhotoView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.RouterConstants
import com.module.home.R
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshHeader
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.SimpleMultiPurposeListener
import com.zq.live.proto.Notification.ClubInfoChangeMsg
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class PersonFragment6 : BaseFragment() {

    lateinit var title: CommonTitleBar
    lateinit var imageBg: ExImageView
    lateinit var smartRefresh: SmartRefreshLayout
    lateinit var classicsHeader: ClassicsHeader
    var appbar: AppBarLayout? = null
    lateinit var contentLayout: CollapsingToolbarLayout
    lateinit var userInfoArea: ConstraintLayout
    lateinit var avatarIv: SimpleDraweeView
    lateinit var nameTv: ExTextView
    lateinit var levelArea: ExConstraintLayout
    lateinit var levelIv: ImageView
    lateinit var levelDesc: TextView
    lateinit var audioView: CommonAudioView
    lateinit var userInfoArrows: ExImageView
    lateinit var fansNumTv: ExTextView
    lateinit var followsNumTv: ExTextView
    lateinit var friendsNumTv: ExTextView
    lateinit var bottomIv: ExImageView
    lateinit var honorBgIv: ImageView
    lateinit var openHonorIv: ImageView
    lateinit var honorTimeTv: TextView

    lateinit var toolbar: Toolbar
    lateinit var toolbarLayout: ExConstraintLayout
    lateinit var srlNameTv: ExTextView
    lateinit var settingIv: ImageView
    lateinit var settingRedDot: ExImageView
    lateinit var walletIv: ImageView

    lateinit var clubArea: ConstraintLayout
    lateinit var clubTitleTv: TextView
    lateinit var personClubView: PersonClubView
    lateinit var photoArea: ConstraintLayout
    private var photoView: PersonPhotoView? = null
    lateinit var postArea: ConstraintLayout
    lateinit var postTitleTv: TextView
    lateinit var worksArea: ConstraintLayout
    lateinit var worksTitleTv: TextView
    lateinit var feedsArea: ConstraintLayout
    lateinit var feedTitleTv: TextView

    private val userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)
    private var mLastUpdateTime: Long = 0  // 主页上次刷新时间

    private var appbarListener: AppBarLayout.OnOffsetChangedListener? = null
    private var lastVerticalOffset = Integer.MAX_VALUE
    private var srollDivider = U.getDisplayUtils().dip2px(122f)  // 滑到分界线的时候

    var fansNum = 0
    var followNum = 0
    var friendNum = 0

    var postNum = 0  // 帖子数
    var workNum = 0  // 作品数
    var feedNum = 0  // 神曲数

    override fun initView(): Int {
        return R.layout.person_fragment6_layout
    }

    override fun initData(savedInstanceState: Bundle?) {

        initBaseContainArea()
        initAppBarArea()
        initToolBarArea()
        initPersonArea()
        initSettingArea()

        initScrollAndRefresh()

        refreshUserInfoView()
        refreshClubInfo()
    }

    override fun onFragmentVisible() {
        super.onFragmentVisible()
        // 拿个人主页，和照片
        getHomePage(false)
        photoView?.initData(false)
    }

    private fun getHomePage(flag: Boolean) {
        // 主页不经常变化
        val now = System.currentTimeMillis()
        if (!flag) {
            if (now - mLastUpdateTime < 60 * 1000) {
                return
            }
        }

        ApiMethods.subscribe(userInfoServerApi.getHomePage(MyUserInfoManager.uid), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    mLastUpdateTime = System.currentTimeMillis()
                    val userInfoModel = JSON.parseObject(result.data!!.getString("userBaseInfo"), UserInfoModel::class.java)
                    val voiceInfoModel = JSON.parseObject(result.data!!.getString("voiceInfo"), VoiceInfoModel::class.java)
                    val relationNumModes = JSON.parseArray(result.data!!.getJSONObject("userRelationCntInfo").getString("cnt"), RelationNumModel::class.java)

                    val myUserInfo = MyUserInfo.parseFromUserInfoModel(userInfoModel)
                    MyUserInfoLocalApi.insertOrUpdate(myUserInfo)
                    MyUserInfoManager.setMyUserInfo(myUserInfo, true, "getHomePage")

                    relationNumModes?.let {
                        for (mode in it) {
                            when {
                                mode.relation == UserInfoManager.RELATION.FRIENDS.value -> friendNum = mode.cnt
                                mode.relation == UserInfoManager.RELATION.FANS.value -> fansNum = mode.cnt
                                mode.relation == UserInfoManager.RELATION.FOLLOW.value -> followNum = mode.cnt
                            }
                        }
                    }
                    refreshVoiceInfo(voiceInfoModel)
                    refreshUserInfoView()
                    refreshRelationInfo()
                }
                finishRefreshLoadMore()
            }

            override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                super.onNetworkError(errorType)
                finishRefreshLoadMore()
            }
        }, this)

    }

    private fun finishRefreshLoadMore() {
        smartRefresh.finishLoadMore()
        smartRefresh.finishRefresh()
    }

    private fun initScrollAndRefresh() {
        smartRefresh.setEnableRefresh(true)
        smartRefresh.setEnableLoadMore(false)
        smartRefresh.setEnableLoadMoreWhenContentNotFull(false)
        smartRefresh.setEnableOverScrollDrag(false)
        smartRefresh.setHeaderMaxDragRate(1.5f)
        smartRefresh.setOnMultiPurposeListener(object : SimpleMultiPurposeListener() {

            internal var lastScale = 0f

            override fun onRefresh(refreshLayout: RefreshLayout) {
                super.onRefresh(refreshLayout)
                getHomePage(true)
                photoView?.initData(true)
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                super.onLoadMore(refreshLayout)
            }

            override fun onHeaderMoving(header: RefreshHeader?, isDragging: Boolean, percent: Float, offset: Int, headerHeight: Int, maxDragHeight: Int) {
                super.onHeaderMoving(header, isDragging, percent, offset, headerHeight, maxDragHeight)
                val scale = offset.toFloat() / U.getDisplayUtils().dip2px(300f).toFloat() + 1
                if (Math.abs(scale - lastScale) >= 0.01) {
                    lastScale = scale
                    imageBg.scaleX = scale
                    imageBg.scaleY = scale
                }
            }
        })

        if (appbarListener == null) {
            appbarListener = AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                imageBg.translationY = verticalOffset.toFloat()
                if (lastVerticalOffset != verticalOffset) {
                    lastVerticalOffset = verticalOffset

                    val srollLimit = appBarLayout.totalScrollRange  // 总的滑动长度
                    if (verticalOffset == 0) {
                        // 展开状态
                        if (toolbar.visibility != View.GONE) {
                            toolbar.visibility = View.GONE
                            toolbarLayout.visibility = View.GONE
                        }
                    } else if (Math.abs(verticalOffset) >= srollDivider) {
                        // 完全收缩状态
                        if (toolbar.visibility != View.VISIBLE) {
                            toolbar.visibility = View.VISIBLE
                            toolbarLayout.visibility = View.VISIBLE
                        }

                        if (Math.abs(verticalOffset) >= srollLimit) {
                            srlNameTv.setAlpha(1f)
                        } else {
                            srlNameTv.setAlpha((Math.abs(verticalOffset) - srollDivider).toFloat() / (srollLimit - srollDivider).toFloat())
                        }
                    } else {
                        // TODO: 2019/4/8 过程中，可以加动画，先直接显示
                        if (toolbar.visibility != View.GONE) {
                            toolbar.visibility = View.GONE
                            toolbarLayout.visibility = View.GONE
                        }
                    }
                }
            }
        }
        appbar?.removeOnOffsetChangedListener(appbarListener)
        lastVerticalOffset = Integer.MAX_VALUE
        appbar?.addOnOffsetChangedListener(appbarListener)
    }


    private fun refreshUserInfoView() {
        if (MyUserInfoManager.hasMyUserInfo()) {
            AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(MyUserInfoManager.avatar)
                    .setCircle(true)
                    .build())
            nameTv.text = MyUserInfoManager.nickName
            srlNameTv.text = MyUserInfoManager.nickName

            if (MyUserInfoManager.honorInfo != null && MyUserInfoManager.honorInfo?.isHonor() == true) {
                openHonorIv.visibility = View.GONE
                honorTimeTv.visibility = View.VISIBLE
                val timeDesc = SpanUtils()
                        .append("还有").setForegroundColor(Color.parseColor("#CC8B572A"))
                        .append("${MyUserInfoManager.honorInfo?.leftDays}").setForegroundColor(Color.parseColor("#FF0000"))
                        .append("天到期").setForegroundColor(Color.parseColor("#CC8B572"))
                        .create()
            } else {
                openHonorIv.visibility = View.VISIBLE
                honorTimeTv.visibility = View.GONE
            }

            val ranking = MyUserInfoManager.ranking
            if (ranking != null && LevelConfigUtils.getSmallImageResoucesLevel(ranking.mainRanking) != 0) {
                levelArea.visibility = View.VISIBLE
                levelIv.background = U.getDrawable(LevelConfigUtils.getSmallImageResoucesLevel(ranking.mainRanking))
                levelDesc.text = ranking.rankingDesc
            } else {
                levelArea.visibility = View.GONE
            }
        }
    }

    private fun refreshVoiceInfo(voiceInfoModel: VoiceInfoModel?) {
//        mVoiceInfoModel = voiceInfoModel
//        if (voiceInfoModel != null) {
//            if (voiceInfoModel.auditStatus == VoiceInfoModel.EVAS_UN_AUDIT) {
//                // 未审核
//                mAudioView.bindData(voiceInfoModel.duration, "审核中")
//            } else {
//                mAudioView.bindData(voiceInfoModel.duration)
//            }
//            mAudioView.setVisibility(View.VISIBLE)
//            mEditAudio.setText("+编辑语音")
//        } else {
//            mEditAudio.setText("+添加声音签名")
//            mAudioView.setVisibility(View.GONE)
//        }
    }

    private fun refreshRelationInfo() {
        val friendBuilder = SpanUtils()
                .append(friendNum.toString()).setForegroundColor(U.getColor(R.color.white_trans_80)).setFontSize(24, true).setBold()
                .append("\n好友").setFontSize(12, true).setForegroundColor(U.getColor(R.color.white_trans_50))
                .create()
        friendsNumTv.text = friendBuilder

        val fansBuilder = SpanUtils()
                .append(fansNum.toString()).setForegroundColor(U.getColor(R.color.white_trans_80)).setFontSize(24, true).setBold()
                .append("\n粉丝").setFontSize(12, true).setForegroundColor(U.getColor(R.color.white_trans_50))
                .create()
        fansNumTv.text = fansBuilder

        val focusBuilder = SpanUtils()
                .append(followNum.toString()).setForegroundColor(U.getColor(R.color.white_trans_80)).setFontSize(24, true).setBold()
                .append("\n关注").setFontSize(12, true).setForegroundColor(U.getColor(R.color.white_trans_50))
                .create()
        followsNumTv.text = focusBuilder
    }

    private fun refreshClubInfo() {
        val clubMemberInfo = MyUserInfoManager.myUserInfo?.clubInfo
        if (clubMemberInfo?.club != null && !TextUtils.isEmpty(clubMemberInfo.club?.name)) {
            clubArea.visibility = View.VISIBLE
            personClubView.bindData(clubMemberInfo.club)
        } else {
            clubArea.visibility = View.GONE
        }
    }

    private fun initBaseContainArea() {
        title = rootView.findViewById(R.id.title)
        imageBg = rootView.findViewById(R.id.image_bg)
        smartRefresh = rootView.findViewById(R.id.smart_refresh)
        classicsHeader = rootView.findViewById(R.id.classics_header)
    }

    private fun initAppBarArea() {
        appbar = rootView.findViewById(R.id.appbar)
        contentLayout = rootView.findViewById(R.id.content_layout)
        userInfoArea = rootView.findViewById(R.id.user_info_area)
        avatarIv = rootView.findViewById(R.id.avatar_iv)
        nameTv = rootView.findViewById(R.id.name_tv)
        levelArea = rootView.findViewById(R.id.level_area)
        levelIv = rootView.findViewById(R.id.level_iv)
        levelDesc = rootView.findViewById(R.id.level_desc)
        audioView = rootView.findViewById(R.id.audio_view)
        userInfoArrows = rootView.findViewById(R.id.user_info_arrows)
        fansNumTv = rootView.findViewById(R.id.fans_num_tv)
        followsNumTv = rootView.findViewById(R.id.follows_num_tv)
        friendsNumTv = rootView.findViewById(R.id.friends_num_tv)
        bottomIv = rootView.findViewById(R.id.bottom_iv)
        honorBgIv = rootView.findViewById(R.id.honor_bg_iv)
        openHonorIv = rootView.findViewById(R.id.open_honor_iv)
        honorTimeTv = rootView.findViewById(R.id.honor_time_tv)

        avatarIv.setDebounceViewClickListener {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_EDIT_INFO)
                    .navigation()
        }

        friendsNumTv.setDebounceViewClickListener {
            // 好友，双向关注
            openRelationFragment(UserInfoManager.RELATION.FRIENDS.value)
        }

        fansNumTv.setDebounceViewClickListener {
            // 粉丝，我关注的
            openRelationFragment(UserInfoManager.RELATION.FANS.value)
        }

        followsNumTv.setDebounceViewClickListener {
            // 关注, 关注我的
            openRelationFragment(UserInfoManager.RELATION.FOLLOW.value)
        }

        openHonorIv.setAnimateDebounceViewClickListener {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                    .withString("url", ApiManager.getInstance().findRealUrlByChannel("https://app.inframe.mobi/user/newVip?title=1"))
                    .greenChannel().navigation()
        }

        honorTimeTv.setAnimateDebounceViewClickListener {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                    .withString("url", ApiManager.getInstance().findRealUrlByChannel("https://app.inframe.mobi/user/newVip?title=1"))
                    .greenChannel().navigation()
        }
    }

    private fun openRelationFragment(mode: Int) {
        val bundle = Bundle()
        bundle.putInt("from_page_key", mode)
        bundle.putInt("friend_num_key", friendNum)
        bundle.putInt("follow_num_key", followNum)
        bundle.putInt("fans_num_key", fansNum)
        ARouter.getInstance()
                .build(RouterConstants.ACTIVITY_RELATION)
                .with(bundle)
                .navigation()
    }

    private fun initToolBarArea() {
        toolbar = rootView.findViewById(R.id.toolbar)
        toolbarLayout = rootView.findViewById(R.id.toolbar_layout)
        srlNameTv = rootView.findViewById(R.id.srl_name_tv)
    }

    private fun initSettingArea() {
        settingIv = rootView.findViewById(R.id.setting_iv)
        settingRedDot = rootView.findViewById(R.id.setting_red_dot)
        walletIv = rootView.findViewById(R.id.wallet_iv)

        settingIv.setDebounceViewClickListener {
            ARouter.getInstance()
                    .build(RouterConstants.ACTIVITY_SETTING)
                    .navigation()
        }
        walletIv.setDebounceViewClickListener {
            ARouter.getInstance()
                    .build(RouterConstants.ACTIVITY_WALLET)
                    .navigation()
        }

        refreshSettingRedDot()
    }

    private fun refreshSettingRedDot() {
        if (UpgradeManager.getInstance().needShowRedDotTips()) {
            settingRedDot.visibility = View.VISIBLE
        } else {
            settingRedDot.visibility = View.GONE
        }
    }

    private fun initPersonArea() {
        // 家族
        clubArea = rootView.findViewById(R.id.club_area)
        clubTitleTv = rootView.findViewById(R.id.club_title_tv)
        personClubView = rootView.findViewById(R.id.person_club_view)

        // 照片
        photoArea = rootView.findViewById(R.id.photo_area)
        photoView = rootView.findViewById(R.id.photo_view)

        // 帖子
        postArea = rootView.findViewById(R.id.post_area)
        postTitleTv = rootView.findViewById(R.id.post_title_tv)

        // 作品
        worksArea = rootView.findViewById(R.id.works_area)
        worksTitleTv = rootView.findViewById(R.id.works_title_tv)

        // 神曲
        feedsArea = rootView.findViewById(R.id.feeds_area)
        feedTitleTv = rootView.findViewById(R.id.feed_title_tv)

        clubArea.setDebounceViewClickListener {

        }

        photoArea.setDebounceViewClickListener {

        }

        postArea.setDebounceViewClickListener {

        }

        worksArea.setDebounceViewClickListener {

        }

        feedsArea.setDebounceViewClickListener {

        }
    }

    override fun isInViewPager(): Boolean {
        return true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: UploadMyVoiceInfo) {
        refreshVoiceInfo(event.model)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvnet(userInfoChangeEvent: MyUserInfoEvent.UserInfoChangeEvent) {
        refreshUserInfoView()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ClubInfoChangeMsg) {
        // 我自己家族信息的改变
        refreshClubInfo()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: UpgradeData.RedDotStatusEvent) {
        refreshSettingRedDot()
    }

    override fun destroy() {
        super.destroy()
        appbar?.removeOnOffsetChangedListener(appbarListener)
        photoView?.destory()
    }
}