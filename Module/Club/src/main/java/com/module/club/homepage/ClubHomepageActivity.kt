//package com.module.club.homepage
//
//import android.content.Intent
//import android.os.Bundle
//import android.support.constraint.ConstraintLayout
//import android.support.design.widget.AppBarLayout
//import android.support.v7.widget.Toolbar
//import android.view.View
//import android.widget.LinearLayout
//import android.widget.RelativeLayout
//import android.widget.TextView
//import com.alibaba.android.arouter.launcher.ARouter
//import com.alibaba.fastjson.JSON
//import com.common.base.BaseActivity
//import com.common.core.avatar.AvatarUtils
//import com.common.core.myinfo.MyUserInfoManager
//import com.common.core.userinfo.model.ClubMemberInfo
//import com.common.core.view.setDebounceViewClickListener
//import com.common.log.MyLog
//import com.common.rxretrofit.ApiManager
//import com.common.rxretrofit.ControlType
//import com.common.rxretrofit.RequestControl
//import com.common.rxretrofit.subscribe
//import com.common.statistics.StatisticsAdapter
//import com.common.utils.U
//import com.common.utils.dp
//import com.common.view.ex.ExImageView
//import com.common.view.ex.ExTextView
//import com.common.view.ex.drawable.DrawableCreator
//import com.component.busilib.view.MarqueeTextView
//import com.component.person.view.PersonTagView
//import com.facebook.drawee.view.SimpleDraweeView
//import com.imagebrowse.big.BigImageBrowseFragment
//import com.module.ModuleServiceManager
//import com.module.RouterConstants
//import com.module.club.ClubServerApi
//import com.module.club.R
//import com.module.club.homepage.event.ClubInfoChangeEvent
//import com.module.club.homepage.room.ClubPartyRoomView
//import ClubMemberView
//import com.module.club.manage.setting.ClubManageActivity
//import com.scwang.smartrefresh.layout.SmartRefreshLayout
//import com.scwang.smartrefresh.layout.api.RefreshHeader
//import com.scwang.smartrefresh.layout.api.RefreshLayout
//import com.scwang.smartrefresh.layout.listener.SimpleMultiPurposeListener
//import com.zq.live.proto.Common.EClubMemberRoleType
//import kotlinx.coroutines.launch
//import okhttp3.MediaType
//import okhttp3.RequestBody
//import org.greenrobot.eventbus.Subscribe
//import org.greenrobot.eventbus.ThreadMode
//import useroperate.OperateFriendActivity
//import useroperate.inter.AbsRelationOperate
//import kotlin.math.abs
//
//class ClubHomepageActivity : BaseActivity() {
//    private val SP_KEY_APPLY_WATER_LEVEL = "sp_key_apply_water_level"               // 申请水位
//    private val SP_KEY_APPLY_WATER_LEVEL_CLUBID = "sp_key_apply_water_level_clubid" // 申请水位对应的clubID
//
//    private var imageBg: SimpleDraweeView? = null
//    private var smartRefresh: SmartRefreshLayout? = null
//    private var container: LinearLayout? = null
//
//    private var clubRoomView: ClubPartyRoomView? = null
//
//    private var appbar: AppBarLayout? = null
//    private var clubAvatarSdv: SimpleDraweeView? = null
//    private var clubNameTv: TextView? = null
//    private var clubTagView: PersonTagView? = null
//
//    private var clubNoticeTv: MarqueeTextView? = null
//
//    private var functionArea: ConstraintLayout? = null
//    private var applyTv: ExTextView? = null
//    private var applyRedIv: ExImageView? = null
//    private var memberTv: ExTextView? = null
//    private var contributionTv: ExTextView? = null
//
//    private var memberView: ClubMemberView? = null  //换成一个view来做
//
//    private var clubIntroduceTitle: TextView? = null
//    private var clubIntroduceContent: ExTextView? = null
//
//    private var toolbar: Toolbar? = null
//    private var toolbarLayout: RelativeLayout? = null
//    private var srlNameTv: TextView? = null
//
//    private var ivBack: ExImageView? = null
//    private var moreBtn: ExImageView? = null
//
//    private var applyArea: ConstraintLayout? = null
//    private var applyEnterTv: ExTextView? = null
//    private var openGroupTv: ExTextView? = null
//
//    private var lastVerticalOffset = Int.MAX_VALUE
//    private var scrollDivider = U.getDisplayUtils().dip2px(150f)  // 滑到分界线的时候
//
//    private val clubServerApi = ApiManager.getInstance().createService(ClubServerApi::class.java)
//    private var clubMemberInfo: ClubMemberInfo? = null
//    private var clubID: Int = 0
//    private var isMyClub = false
//
//    private var applyTimeMs: Long = 0
//    private var applyRedCount: Int = 0
//
//    private val noticeDrawable = DrawableCreator.Builder()
//            .setSolidColor(U.getColor(R.color.black_trans_50))
//            .setCornersRadius(4.dp().toFloat())
//            .build()
//
//    private var isClubInfoChange = false // club信息改变
//
//    override fun initView(savedInstanceState: Bundle?): Int {
//        return R.layout.club_home_page_activity_layout
//    }
//
//    override fun initData(savedInstanceState: Bundle?) {
//        U.getStatusBarUtil().setTransparentBar(this, false)
//        clubMemberInfo = intent.getSerializableExtra("clubMemberInfo") as ClubMemberInfo?
//        clubID = clubMemberInfo?.club?.clubID ?: 0
//        if (clubMemberInfo == null) {
//            finish()
//        }
//
//        imageBg = this.findViewById(R.id.image_bg)
//        smartRefresh = this.findViewById(R.id.smart_refresh)
//        container = this.findViewById(R.id.container)
//
//        clubRoomView = this.findViewById(R.id.club_room_view)
//
//        appbar = this.findViewById(R.id.appbar)
//        clubAvatarSdv = this.findViewById(R.id.club_avatar_sdv)
//        clubNameTv = this.findViewById(R.id.club_name_tv)
//        clubTagView = this.findViewById(R.id.club_tag_view)
//        clubNoticeTv = this.findViewById(R.id.club_notice_tv)
//
//        functionArea = this.findViewById(R.id.function_area)
//        var inviteTv: View = this.findViewById(R.id.invite_tv)
//
//        inviteTv.setDebounceViewClickListener {
//            OperateFriendActivity.open(OperateFriendActivity.Companion.Builder()
//                    .setIsEnableFans(true)
//                    .setIsEnableFriend(true)
//                    .setText("邀请")
//                    .setListener(AbsRelationOperate.ClickListener { _, _, _, userInfoModel, callback ->
//                        launch {
//                            val map = mapOf(
//                                    "toUserID" to userInfoModel?.userId
//                            )
//                            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
//                            val result = subscribe(RequestControl("sendInvitation", ControlType.CancelThis)) {
//                                clubServerApi.sendInvitation(body)
//                            }
//                            if (result.errno == 0) {
//                                U.getToastUtil().showShort("发送邀请成功")
//                                ModuleServiceManager.getInstance().msgService.sendClubInviteMsg(userInfoModel?.userId?.toString()
//                                        , result.data.getString("invitationID")
//                                        , result.data.getLongValue("expireAt")
//                                        , result.data.getString("content")
//                                )
//                                callback?.onCallback(1, "已邀请")
//                            } else if (result.errno == 8440211) {
//                                U.getToastUtil().showShort("对方的版本过低，邀请失败")
//                                ModuleServiceManager.getInstance().msgService.sendTxtMsg(userInfoModel?.userId?.toString()
//                                        , result.errmsg)
//                            } else {
//                                U.getToastUtil().showShort(result.errmsg)
//
//                            }
//                        }
//                        StatisticsAdapter.recordCountEvent("family","invite",null)
//                    }))
//        }
//
//        applyTv = this.findViewById(R.id.apply_tv)
//        applyRedIv = this.findViewById(R.id.apply_red_iv)
//        memberTv = this.findViewById(R.id.member_tv)
//        contributionTv = this.findViewById(R.id.contribution_tv)
//
//        memberView = this.findViewById(R.id.member_view)
//
//        clubIntroduceTitle = this.findViewById(R.id.club_introduce_title)
//        clubIntroduceContent = this.findViewById(R.id.club_introduce_content)
//
//        toolbar = this.findViewById(R.id.toolbar)
//        toolbarLayout = this.findViewById(R.id.toolbar_layout)
//        srlNameTv = this.findViewById(R.id.srl_name_tv)
//
//        ivBack = this.findViewById(R.id.iv_back)
//        moreBtn = this.findViewById(R.id.more_btn)
//
//        applyArea = this.findViewById(R.id.apply_area)
//        applyEnterTv = this.findViewById(R.id.apply_enter_tv)
//        openGroupTv = this.findViewById(R.id.open_club_conversation_tv)
//
//        isMyClub = (clubMemberInfo?.roleType == EClubMemberRoleType.ECMRT_Founder.value
//                || clubMemberInfo?.roleType == EClubMemberRoleType.ECMRT_CoFounder.value
//                || clubMemberInfo?.roleType == EClubMemberRoleType.ECMRT_Hostman.value
//                || clubMemberInfo?.roleType == EClubMemberRoleType.ECMRT_Common.value)
//
//        if (isMyClub) {
//            clubNoticeTv?.visibility = View.VISIBLE
//            memberView?.visibility = View.GONE
//            functionArea?.visibility = View.VISIBLE
//            applyArea?.visibility = View.GONE
//            moreBtn?.visibility = View.VISIBLE
//
//            if (clubMemberInfo?.roleType == EClubMemberRoleType.ECMRT_Founder.value
//                    || clubMemberInfo?.roleType == EClubMemberRoleType.ECMRT_CoFounder.value) {
//                applyTv?.visibility = View.VISIBLE
//                applyRedIv?.visibility = View.VISIBLE
//            } else {
//                applyTv?.visibility = View.GONE
//                applyRedIv?.visibility = View.GONE
//            }
//        } else {
//            clubNoticeTv?.visibility = View.GONE
//            memberView?.visibility = View.VISIBLE
//            functionArea?.visibility = View.GONE
//            applyArea?.visibility = View.VISIBLE
//            moreBtn?.visibility = View.GONE
//        }
//
//        adjustNotchPhone()
//        initTopArea()
//        initMemberArea()
//        initRoomArea()
//        initToolBarScroll()
//        initApplyEnter()
//        initOpenClubConversation()
//
//        refreshClubUI()
//
//        // 初始化数据
//        clubRoomView?.initData(null)
//        if (!isMyClub) {
//            memberView?.initData()
//        } else {
//            checkApplyRed()
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        if (isClubInfoChange) {
//            isClubInfoChange = false
//            clubRoomView?.initData(null)
//            getClubMemberInfo()
//            if (!isMyClub) {
//                memberView?.initData()
//            } else {
//                checkApplyRed()
//            }
//        }
//    }
//
//
//    private fun adjustNotchPhone() {
//        if (U.getDeviceUtils().hasNotch(this@ClubHomepageActivity)) {
//            val layoutParams = clubAvatarSdv?.layoutParams as ConstraintLayout.LayoutParams
//            layoutParams.topMargin = layoutParams.topMargin + U.getStatusBarUtil().getStatusBarHeight(this@ClubHomepageActivity)
//            clubAvatarSdv?.layoutParams = layoutParams
//        }
//    }
//
//    private fun initTopArea() {
//        clubAvatarSdv?.setDebounceViewClickListener {
//            BigImageBrowseFragment.open(false, this, clubMemberInfo?.club?.logo)
//        }
//
//        ivBack?.setDebounceViewClickListener { finish() }
//
//        moreBtn?.setDebounceViewClickListener {
//            // 跳到设置页面
//            val intent = Intent(this, ClubManageActivity::class.java)
//            intent.putExtra("clubMemberInfo", clubMemberInfo)
//            startActivity(intent)
//        }
//
//        applyTv?.setDebounceViewClickListener {
//            U.getPreferenceUtils().setSettingLong(SP_KEY_APPLY_WATER_LEVEL, applyTimeMs)
//            applyRedIv?.visibility = View.GONE
//            ARouter.getInstance().build(RouterConstants.ACTIVITY_LIST_APPLY_CLUB)
//                    .withSerializable("clubMemberInfo", clubMemberInfo)
//                    .navigation()
//        }
//
//        memberTv?.setDebounceViewClickListener {
//            ARouter.getInstance().build(RouterConstants.ACTIVITY_LIST_MEMBER)
//                    .withSerializable("clubMemberInfo", clubMemberInfo)
//                    .navigation()
//        }
//
//        contributionTv?.setDebounceViewClickListener {
//            ARouter.getInstance().build(RouterConstants.ACTIVITY_LIST_CLUB_RANK)
//                    .withInt("clubID", clubMemberInfo?.club?.clubID ?: 0)
//                    .navigation()
//        }
//    }
//
//    private fun initMemberArea() {
//        memberView?.clubID = clubID
//        memberView?.memberCnt = clubMemberInfo?.club?.memberCnt ?: 0
//    }
//
//    private fun initRoomArea() {
//        clubRoomView?.clubID = clubID
//        smartRefresh?.apply {
//            setEnableRefresh(true)
//            setEnableLoadMore(true)
//            setEnableLoadMoreWhenContentNotFull(false)
//            setEnableOverScrollDrag(true)
//            setHeaderMaxDragRate(1.5f)
//
//            setOnMultiPurposeListener(object : SimpleMultiPurposeListener() {
//                internal var lastScale = 0f
//
//                override fun onRefresh(refreshLayout: RefreshLayout) {
//                    smartRefresh?.setEnableLoadMore(true)
//                    clubRoomView?.initData {
//                        finishRereshAndLoadMore()
//                        smartRefresh?.setEnableLoadMore(it)
//                    }
//                    if (!isMyClub) {
//                        memberView?.initData()
//                    } else {
//                        checkApplyRed()
//                    }
//                }
//
//                override fun onLoadMore(refreshLayout: RefreshLayout) {
//                    clubRoomView?.loadMoreData {
//                        finishRereshAndLoadMore()
//                        smartRefresh?.setEnableLoadMore(it)
//                    }
//                }
//
//                override fun onHeaderMoving(header: RefreshHeader?, isDragging: Boolean, percent: Float, offset: Int, headerHeight: Int, maxDragHeight: Int) {
//                    super.onHeaderMoving(header, isDragging, percent, offset, headerHeight, maxDragHeight)
//                    val scale = offset.toFloat() / U.getDisplayUtils().dip2px(300f).toFloat() + 1
//                    if (abs(scale - lastScale) >= 0.01) {
//                        lastScale = scale
//                        imageBg?.scaleX = scale
//                        imageBg?.scaleY = scale
//                    }
//                }
//            })
//
//        }
//    }
//
//    private fun initToolBarScroll() {
//        appbar?.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
//            // TODO: 2019-06-23 也可以加效果，看产品怎么说
//            imageBg?.translationY = verticalOffset.toFloat()
//            if (lastVerticalOffset != verticalOffset) {
//                lastVerticalOffset = verticalOffset
//                if (verticalOffset == 0) {
//                    // 展开状态
//                    if (toolbar?.visibility != View.GONE) {
//                        toolbar?.visibility = View.GONE
//                        toolbarLayout?.visibility = View.GONE
//                    }
//                } else if (abs(verticalOffset) >= scrollDivider) {
//                    // 完全收缩状态
//                    if (toolbar?.visibility != View.VISIBLE) {
//                        toolbar?.visibility = View.VISIBLE
//                        toolbarLayout?.visibility = View.VISIBLE
//                    }
//
//                    if (abs(verticalOffset) >= appBarLayout.totalScrollRange) {
//                        srlNameTv?.alpha = 1f
//                    } else {
//                        srlNameTv?.alpha = (abs(verticalOffset) - scrollDivider).toFloat() / (appBarLayout.totalScrollRange - scrollDivider).toFloat()
//                    }
//                } else {
//                    // TODO: 2019/4/8 过程中，可以加动画，先直接显示
//                    if (toolbar?.visibility != View.GONE) {
//                        toolbar?.visibility = View.GONE
//                        toolbarLayout?.visibility = View.GONE
//                    }
//                }
//            }
//        })
//    }
//
//    private fun initOpenClubConversation(){
//        openGroupTv?.setDebounceViewClickListener {
//            clubMemberInfo?.club?.name?.let {
//                ModuleServiceManager.getInstance().msgService.startClubChat(this, clubID.toString(), it)
//            }?:MyLog.e(TAG, "未获取到家族信息")
//        }
//    }
//
//    private fun initApplyEnter() {
//        applyEnterTv?.setDebounceViewClickListener {
//            launch {
//                val map = mapOf(
//                        "clubID" to clubID,
//                        "text" to ""
//                )
//                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
//                val result = subscribe(RequestControl("applyJoinClub", ControlType.CancelThis)) {
//                    clubServerApi.applyJoinClub(body)
//                }
//                if (result.errno == 0) {
//                    U.getToastUtil().showShort("申请成功")
//                } else {
//                    U.getToastUtil().showShort(result.errmsg)
//                }
//            }
//        }
//    }
//
//    private fun getClubMemberInfo() {
//        launch {
//            val result = subscribe(RequestControl("getClubMemberInfo", ControlType.CancelThis)) {
//                clubServerApi.getClubMemberInfo(MyUserInfoManager.uid.toInt(), clubID)
//            }
//            if (result.errno == 0) {
//                clubMemberInfo = JSON.parseObject(result.data.getString("info"), ClubMemberInfo::class.java)
//                refreshClubUI()
//            }
//            finishRereshAndLoadMore()
//        }
//    }
//
//    private fun checkApplyRed() {
//        if (U.getPreferenceUtils().getSettingInt(SP_KEY_APPLY_WATER_LEVEL_CLUBID, 0) != clubID) {
//            // clubID变了，重置一下数据
//            U.getPreferenceUtils().setSettingInt(SP_KEY_APPLY_WATER_LEVEL_CLUBID, clubID)
//            U.getPreferenceUtils().setSettingLong(SP_KEY_APPLY_WATER_LEVEL, 0)
//        }
//
//        launch {
//            val lastTimeMs = U.getPreferenceUtils().getSettingLong(SP_KEY_APPLY_WATER_LEVEL, 0)
//            val result = subscribe(RequestControl("getCountMemberApply", ControlType.CancelThis)) {
//                clubServerApi.getCountMemberApply(clubID, lastTimeMs)
//            }
//            if (result.errno == 0) {
//                applyTimeMs = result.data.getLongValue("timeMs")
//                applyRedCount = result.data.getIntValue("total")
//
//                // 是否显示红点
//                if (applyRedCount > 0) {
//                    applyRedIv?.visibility = View.VISIBLE
//                } else {
//                    applyRedIv?.visibility = View.GONE
//                }
//            }
//        }
//    }
//
//    private fun refreshClubUI() {
//        val clubInfo = clubMemberInfo?.club
//        AvatarUtils.loadAvatarByUrl(imageBg, AvatarUtils.newParamsBuilder(clubInfo?.logo)
//                .setCircle(false)
//                .setBlur(true)
//                .build())
//        AvatarUtils.loadAvatarByUrl(clubAvatarSdv, AvatarUtils.newParamsBuilder(clubInfo?.logo)
//                .setCircle(false)
//                .setCornerRadius(8.dp().toFloat())
//                .build())
//        clubNameTv?.text = clubInfo?.name
//        clubTagView?.setClubID(clubID)
//        clubTagView?.setClubHot(clubMemberInfo?.club?.hot ?: 0)
//        clubNoticeTv?.background = noticeDrawable
//        clubNoticeTv?.text = "公告: ${clubInfo?.notice}"
//        clubIntroduceContent?.text = clubInfo?.desc
//        srlNameTv?.text = clubInfo?.name
//    }
//
//    private fun finishRereshAndLoadMore() {
//        smartRefresh?.finishLoadMore()
//        smartRefresh?.finishRefresh()
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(event: ClubInfoChangeEvent) {
//        isClubInfoChange = true
//    }
//
//    override fun canSlide(): Boolean {
//        return false
//    }
//}