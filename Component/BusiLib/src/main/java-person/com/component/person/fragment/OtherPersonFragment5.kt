package com.component.person.fragment

import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.AppBarLayout
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseFragment
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfo
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.ResponseCallBack
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.event.RelationChangeEvent
import com.common.core.userinfo.event.RemarkChangeEvent
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.view.setDebounceViewClickListener
import com.common.flutter.boost.FlutterBoostController
import com.common.log.MyLog
import com.common.player.SinglePlayer
import com.common.player.SinglePlayerCallbackAdapter
import com.common.rxretrofit.ApiManager
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.common.utils.dp
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.busilib.R
import com.component.busilib.friends.VoiceInfoModel
import com.component.busilib.view.NickNameView
import com.component.dialog.BusinessCardDialogView
import com.component.level.utils.LevelConfigUtils
import com.component.person.OtherPersonActivity.Companion.BUNDLE_USER_ID
import com.component.person.event.UploadHomePageEvent
import com.component.person.model.RelationNumModel
import com.component.person.model.ScoreDetailModel
import com.component.person.photo.view.PersonPhotoView
import com.component.person.presenter.OtherPersonPresenter
import com.component.person.relation.PersonRelationView
import com.component.person.view.*
import com.dialog.view.TipsDialogView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.ModuleServiceManager
import com.module.RouterConstants
import com.module.club.IClubModuleService
import com.module.home.IHomeService
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshHeader
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.SimpleMultiPurposeListener
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.abs
import kotlin.math.sin

class OtherPersonFragment5 : BaseFragment(), IOtherPersonView, RequestCallBack {

    val SP_KEY_HAS_SHOW_SPFOLLOW = "SP_KEY_HAS_SHOW_SPFOLLOW"  // 提醒特别关注的

    lateinit var imageBg: SimpleDraweeView
    lateinit var bottomBg: ExImageView
    lateinit var smartRefresh: SmartRefreshLayout
    lateinit var classicsHeader: ClassicsHeader

    lateinit var appbar: AppBarLayout
    lateinit var qinmiTv: ExTextView

    lateinit var toolbar: Toolbar
    lateinit var toolbarLayout: RelativeLayout

    lateinit var ivBack: ExImageView
    lateinit var moreBtn: ExImageView
    lateinit var functionArea: ConstraintLayout
    lateinit var inviteIv: ExTextView
    lateinit var messageIv: ExTextView
    lateinit var followIv: ExTextView

    lateinit var userInfoArea: ConstraintLayout
    lateinit var nameView: NickNameView
    lateinit var levelArea: ExConstraintLayout
    lateinit var levelIv: ImageView
    lateinit var levelDesc: TextView
    lateinit var audioView: CommonAudioView
    lateinit var signTv: TextView
    lateinit var personTagView: PersonTagView
    lateinit var divider: View

    var relationView: PersonRelationView? = null
    lateinit var clubArea: ConstraintLayout
    lateinit var clubTitleTv: TextView
    lateinit var personClubView: PersonClubView
    private var photoView: PersonPhotoView? = null
    lateinit var postArea: ConstraintLayout
    lateinit var postTitleTv: TextView
    lateinit var feedsArea: ConstraintLayout
    lateinit var feedTitleTv: TextView

    private var infoModel: UserInfoModel = UserInfoModel()
    private var userId: Int = 0
    private var fansNum: Int = 0
    private var meliTotal: Int = 0
    private var qinMiCntTotal: Int = 0
    private var voiceInfoModel: VoiceInfoModel? = null

    lateinit var mPresenter: OtherPersonPresenter

    private var mPersonMoreOpView: PersonMoreOpView? = null
    private var mTipsDialogView: TipsDialogView? = null
    private var mEditRemarkDialog: DialogPlus? = null
    private var mDialogPlus: DialogPlus? = null
    private var mBusinessCardDialogView: BusinessCardDialogView? = null

    private var lastVerticalOffset = Int.MAX_VALUE
    private var srollDivider = U.getDisplayUtils().dip2px(150f)  // 滑到分界线的时候
    private var bottomTopMargin = 0  // 底部背景距离顶部距离的初始值

    var uploadHomePageFlag = false

    private var isPlay = false
    private var playTag = "OtherPersonFragment4" + hashCode()
    private var playCallback = object : SinglePlayerCallbackAdapter() {
        override fun onCompletion() {
            super.onCompletion()
            stopPlay()
        }

        override fun onPlaytagChange(oldPlayerTag: String?, newPlayerTag: String?) {
            if (newPlayerTag !== playTag) {
                stopPlay()
            }
        }
    }


    override fun initView(): Int {
        return R.layout.other_person_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        initBaseContainArea()
        initAppBarLayoutArea()
        initToolBarArea()
        initPersonArea()
        initTopArea()
        initFunctionArea()

        initScrollAndRefresh()

        mPresenter = OtherPersonPresenter(this)
        addPresent(mPresenter)
        bindData()

        SinglePlayer.addCallback(playTag, playCallback)

        // 得到底部距离顶部的初始值
        val bottomParams = bottomBg.layoutParams as RelativeLayout.LayoutParams
        bottomTopMargin = bottomParams.topMargin
    }

    private fun bindData() {
        val bundle = arguments
        if (bundle != null) {
            userId = bundle.getInt(BUNDLE_USER_ID)
            infoModel.userId = userId
        }

        if (userId.toLong() == MyUserInfoManager.uid) {
            functionArea.visibility = View.GONE
            moreBtn.visibility = View.GONE
        }

        if (userId != 0) {
            mPresenter.getHomePage(userId)
            photoView?.initData(userId.toLong(), true)
            relationView?.initData(userId.toLong(), true)
        } else {
            MyLog.w(TAG, "bindData error userID = 0")
        }


    }

    override fun onNewIntent(intent: Intent?): Boolean {
        arguments = intent?.extras
        bindData()
        return true
    }

    private fun stopPlay() {
        isPlay = false
        SinglePlayer.stop(playTag)
        audioView.setPlay(false)
    }

    override fun onFragmentVisible() {
        super.onFragmentVisible()
        if (uploadHomePageFlag) {
            mPresenter.getHomePage(userId)
            photoView?.initData(userId.toLong(), true)
            relationView?.initData(userId.toLong(), true)
        }
    }

    override fun onFragmentInvisible(reason: Int) {
        super.onFragmentInvisible(reason)
        stopPlay()
    }

    private fun initBaseContainArea() {
        imageBg = rootView.findViewById(R.id.image_bg)
        bottomBg = rootView.findViewById(R.id.bottom_bg)
        smartRefresh = rootView.findViewById(R.id.smart_refresh)
        classicsHeader = rootView.findViewById(R.id.classics_header)
    }

    private fun initAppBarLayoutArea() {
        appbar = rootView.findViewById(R.id.appbar)
        qinmiTv = rootView.findViewById(R.id.qinmi_tv)

        qinmiTv.setDebounceViewClickListener { showQinmiTips() }
    }

    private fun initToolBarArea() {
        toolbar = rootView.findViewById(R.id.toolbar)
        toolbarLayout = rootView.findViewById(R.id.toolbar_layout)
    }

    private fun initTopArea() {
        ivBack = rootView.findViewById(R.id.iv_back)
        moreBtn = rootView.findViewById(R.id.more_btn)

        ivBack.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (activity != null) {
                    activity!!.finish()
                }
            }
        })

        moreBtn.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mPersonMoreOpView != null) {
                    mPersonMoreOpView!!.dismiss()
                }
                mPersonMoreOpView = PersonMoreOpView(context, infoModel.userId, infoModel.isFollow, infoModel.isSPFollow, false)
                mPersonMoreOpView!!.setListener(object : PersonMoreOpView.Listener {
                    override fun onClickRemark() {
                        mPersonMoreOpView?.dismiss()
                        showRemarkDialog()
                    }

                    override fun onClickSpFollow() {
                        mPersonMoreOpView?.dismiss()
                        if (infoModel.isSPFollow) {
                            // 取消特别关注
                            delSpFollow(infoModel)
                        } else {
                            // 特别关注去
                            addSpFollow(infoModel)
                        }
                    }

                    override fun onClickReport() {
                        mPersonMoreOpView?.dismiss()

                        val channelService = ARouter.getInstance().build(RouterConstants.SERVICE_HOME).navigation() as IHomeService
                        val baseFragmentClass = channelService.getData(3, null) as Class<BaseFragment>
                        U.getFragmentUtils().addFragment(
                                FragmentUtils.newAddParamsBuilder(activity, baseFragmentClass)
                                        .setAddToBackStack(true)
                                        .setHasAnimation(true)
                                        .addDataBeforeAdd(0, 1)
                                        .addDataBeforeAdd(1, userId)
                                        .setEnterAnim(R.anim.slide_in_bottom)
                                        .setExitAnim(R.anim.slide_out_bottom)
                                        .build())
                    }

                    override fun onClickKick() {

                    }

                    override fun onClickBlack(isInBlack: Boolean) {
                        mPersonMoreOpView?.dismiss()
                        if (isInBlack) {
                            UserInfoManager.getInstance().removeBlackList(userId, object : ResponseCallBack<Any?>() {
                                override fun onServerSucess(o: Any?) {
                                    U.getToastUtil().showShort("移除黑名单成功")
                                }

                                override fun onServerFailed() {

                                }
                            })
                        } else {
                            UserInfoManager.getInstance().addToBlacklist(userId, object : ResponseCallBack<Any?>() {
                                override fun onServerSucess(o: Any?) {
                                    U.getToastUtil().showShort("加入黑名单成功")
                                }

                                override fun onServerFailed() {

                                }
                            })
                        }

                    }
                })
                mPersonMoreOpView!!.showAt(moreBtn)
            }
        })
    }

    private fun initFunctionArea() {
        functionArea = rootView.findViewById(R.id.function_area)
        inviteIv = rootView.findViewById(R.id.invite_iv)
        messageIv = rootView.findViewById(R.id.message_iv)
        followIv = rootView.findViewById(R.id.follow_iv)

        followIv.setDebounceViewClickListener {
            if (!U.getNetworkUtils().hasNetwork()) {
                U.getToastUtil().showShort("网络异常，请检查网络后重试!")
                return@setDebounceViewClickListener
            }
            if (infoModel != null) {
                if (infoModel.isFollow) {
                    unFollow(infoModel)
                } else {
                    UserInfoManager.getInstance().mateRelation(infoModel.userId, UserInfoManager.RA_BUILD, infoModel.isFriend)
                }
            }
        }

        messageIv.setDebounceViewClickListener {
            if (infoModel != null) {
                val needPop = ModuleServiceManager.getInstance().msgService.startPrivateChat(context,
                        infoModel.userId.toString(),
                        infoModel.nicknameRemark,
                        infoModel.isFriend
                )
                if (needPop) {
                    activity?.finish()
                }
            }
        }

        // 去掉邀请
        inviteIv.visibility = View.GONE
//        mInviteIv.setDebounceViewClickListener {
//            val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
//            iRankingModeService.tryInviteToRelay(userId, infoModel.isFriend)
//        }
    }


    private fun initPersonArea() {
        // 顶部个人信息
        userInfoArea = rootView.findViewById(R.id.user_info_area)
        nameView = rootView.findViewById(R.id.name_view)
        levelArea = rootView.findViewById(R.id.level_area)
        levelIv = rootView.findViewById(R.id.level_iv)
        levelDesc = rootView.findViewById(R.id.level_desc)
        audioView = rootView.findViewById(R.id.audio_view)
        signTv = rootView.findViewById(R.id.sign_tv)
        personTagView = rootView.findViewById(R.id.person_tag_view)
        divider = rootView.findViewById(R.id.divider)

        audioView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (isPlay) {
                    // 暂停音乐
                    stopPlay()
                } else {
                    // 播放音乐
                    voiceInfoModel?.let {
                        isPlay = true
                        audioView.setPlay(true)
                        SinglePlayer.startPlay(playTag, it.voiceURL)
                    }
                }
            }
        })

        // 关系
        relationView = rootView.findViewById(R.id.relation_view)

        // 家族
        clubArea = rootView.findViewById(R.id.club_area)
        clubTitleTv = rootView.findViewById(R.id.club_title_tv)
        personClubView = rootView.findViewById(R.id.person_club_view)

        // 照片
        photoView = rootView.findViewById(R.id.photo_view)

        // 帖子
        postArea = rootView.findViewById(R.id.post_area)
        postTitleTv = rootView.findViewById(R.id.post_title_tv)

        // 神曲
        feedsArea = rootView.findViewById(R.id.feeds_area)
        feedTitleTv = rootView.findViewById(R.id.feed_title_tv)

        relationView?.setDebounceViewClickListener {
            // todo 补一个flutter的界面
            FlutterBoostController.openFlutterPage(activity!!,"OtherRelationPage", mutableMapOf(
                    "targetId" to userId
            ))
        }

        clubArea.setDebounceViewClickListener {
            infoModel.clubInfo?.club?.let {
                val clubServices = ARouter.getInstance().build(RouterConstants.SERVICE_CLUB).navigation() as IClubModuleService
                clubServices.tryGoClubHomePage(it.clubID)
            }
        }

        photoView?.setDebounceViewClickListener {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_PERSON_PHOTO)
                    .withInt("userID", userId)
                    .navigation()
        }

        postArea.setDebounceViewClickListener {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_PERSON_POST)
                    .withSerializable("userInfoModel", infoModel)
                    .navigation()
        }

        feedsArea.setDebounceViewClickListener {
            // todo神曲做么
            ARouter.getInstance().build(RouterConstants.ACTIVITY_PERSON_FEED)
                    .withSerializable("userInfoModel", infoModel)
                    .navigation()
        }
    }

    private fun initScrollAndRefresh() {
        smartRefresh.setEnableRefresh(true)
        smartRefresh.setEnableLoadMore(false)
        smartRefresh.setEnableLoadMoreWhenContentNotFull(false)
        smartRefresh.setEnableOverScrollDrag(true)
        smartRefresh.setHeaderMaxDragRate(1.5f)
        smartRefresh.setOnMultiPurposeListener(object : SimpleMultiPurposeListener() {
            internal var lastScale = 0f

            override fun onRefresh(refreshLayout: RefreshLayout) {
                mPresenter.getHomePage(userId)
                photoView?.initData(userId.toLong(), true)
                relationView?.initData(userId.toLong(), true)
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {

            }

            override fun onHeaderMoving(header: RefreshHeader?, isDragging: Boolean, percent: Float, offset: Int, headerHeight: Int, maxDragHeight: Int) {
                super.onHeaderMoving(header, isDragging, percent, offset, headerHeight, maxDragHeight)
                val scale = offset.toFloat() / U.getDisplayUtils().dip2px(300f).toFloat() + 1
                if (abs(scale - lastScale) >= 0.01) {
                    lastScale = scale
                    imageBg.scaleX = scale
                    imageBg.scaleY = scale
                }
                if (offset > 0) {
                    bottomBg.translationY = offset.toFloat()
                }
            }
        })

        appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            // TODO: 2019-06-23 也可以加效果，看产品怎么说
            imageBg.translationY = verticalOffset.toFloat()
            if (verticalOffset < 0) {
                bottomBg.translationY = verticalOffset.toFloat()
                if (abs(verticalOffset) >= srollDivider) {
                    qinmiTv.alpha = 0f
                } else {
                    qinmiTv.alpha = 1 - abs(verticalOffset).toFloat() / srollDivider.toFloat()
                }
            } else {
                qinmiTv.alpha = 1f
            }
            if (lastVerticalOffset != verticalOffset) {
                lastVerticalOffset = verticalOffset
                if (verticalOffset == 0) {
                    // 展开状态
                    if (toolbar.visibility != View.GONE) {
                        toolbar.visibility = View.GONE
                        toolbarLayout.visibility = View.GONE
                    }
                } else if (abs(verticalOffset) >= srollDivider) {
                    // 完全收缩状态
                    if (toolbar.visibility != View.VISIBLE) {
                        toolbar.visibility = View.VISIBLE
                        toolbarLayout.visibility = View.VISIBLE
                    }
                } else {
                    // TODO: 2019/4/8 过程中，可以加动画，先直接显示
                    if (toolbar.visibility != View.GONE) {
                        toolbar.visibility = View.GONE
                        toolbarLayout.visibility = View.GONE
                    }
                }
            }
        })
    }


    override fun useEventBus(): Boolean {
        return true
    }

    override fun showHomePageInfo(userInfoModel: UserInfoModel,
                                  relationNumModels: List<RelationNumModel>?,
                                  meiLiCntTotal: Int, qinMiCntTotal: Int,
                                  voiceInfoModel: VoiceInfoModel?) {
        uploadHomePageFlag = false
        this.infoModel = userInfoModel
        this.meliTotal = meiLiCntTotal
        this.qinMiCntTotal = qinMiCntTotal
        this.voiceInfoModel = voiceInfoModel
        relationNumModels?.let {
            for (mode in it) {
                when {
                    mode.relation == UserInfoManager.RELATION.FANS.value -> fansNum = mode.cnt
                }
            }
        }

        refreshUserInfoArea()
        refreshRelation(userInfoModel.isFriend, userInfoModel.isFollow, userInfoModel.isSPFollow)

        if (userInfoModel.isFollow) {
            if (!U.getPreferenceUtils().getSettingBoolean(SP_KEY_HAS_SHOW_SPFOLLOW, false)) {
                showSpFollowTips()
            }
        }
        finishRefreshLoadMore()
    }

    override fun getHomePageFail() {
        uploadHomePageFlag = false
        finishRefreshLoadMore()
    }

    private fun finishRefreshLoadMore() {
        smartRefresh.finishLoadMore()
        smartRefresh.finishRefresh()
    }

    private fun showRemarkDialog() {
        val editRemarkView = EditRemarkView(activity, infoModel!!.nickname, infoModel!!.getNicknameRemark(null))
        editRemarkView.setListener(object : EditRemarkView.Listener {
            override fun onClickCancel() {
                if (mEditRemarkDialog != null) {
                    mEditRemarkDialog!!.dismiss()
                }
                U.getKeyBoardUtils().hideSoftInputKeyBoard(activity)
            }

            override fun onClickSave(remarkName: String) {
                if (mEditRemarkDialog != null) {
                    mEditRemarkDialog!!.dismiss()
                }
                U.getKeyBoardUtils().hideSoftInputKeyBoard(activity)
                if (TextUtils.isEmpty(remarkName) && TextUtils.isEmpty(infoModel!!.nicknameRemark)) {
                    // 都为空
                    return
                } else if (!TextUtils.isEmpty(infoModel!!.nicknameRemark) && infoModel!!.nicknameRemark == remarkName) {
                    // 相同
                    return
                } else {
                    UserInfoManager.getInstance().updateRemark(remarkName, userId)
                }
            }
        })

        mEditRemarkDialog = DialogPlus.newDialog(context!!)
                .setContentHolder(ViewHolder(editRemarkView))
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setInAnimation(R.anim.fade_in)
                .setOutAnimation(R.anim.fade_out)
                .setExpanded(false)
                .setGravity(Gravity.BOTTOM)
                .setOnDismissListener { U.getKeyBoardUtils().hideSoftInputKeyBoard(activity) }
                .create()
        mEditRemarkDialog!!.show()

    }

    private fun showSpFollowTips() {
        U.getPreferenceUtils().setSettingBoolean(SP_KEY_HAS_SHOW_SPFOLLOW, true)
        mDialogPlus?.dismiss(false)
        mDialogPlus = DialogPlus.newDialog(context!!)
                .setContentHolder(ViewHolder(R.layout.other_person_tips_view_layout))
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setMargin(U.getDisplayUtils().dip2px(32f), -1, U.getDisplayUtils().dip2px(32f), -1)
                .setExpanded(false)
                .setGravity(Gravity.CENTER)
                .setOnClickListener { dialog, view -> dialog.dismiss() }
                .create()
        mDialogPlus?.show()
    }

    private fun showQinmiTips() {
        mDialogPlus?.dismiss(false)
        mDialogPlus = DialogPlus.newDialog(context!!)
                .setContentHolder(ViewHolder(R.layout.person_qinmi_rule_layout))
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setMargin(U.getDisplayUtils().dip2px(32f), -1, U.getDisplayUtils().dip2px(32f), -1)
                .setExpanded(false)
                .setGravity(Gravity.CENTER)
                .setOnClickListener { dialog, view -> dialog.dismiss() }
                .create()
        mDialogPlus?.show()
    }

    override fun refreshRelation(isFriend: Boolean, isFollow: Boolean, isSpFollow: Boolean) {
        infoModel.isFriend = isFriend
        infoModel.isFollow = isFollow
        infoModel.isSPFollow = isSpFollow
        when {
            isFriend -> {
                followIv.text = "互关"
                followIv.background = U.getDrawable(R.drawable.common_hollow_yellow_icon)
            }
            isFollow -> {
                followIv.text = "已关注"
                followIv.background = U.getDrawable(R.drawable.common_hollow_yellow_icon)
            }
            else -> {
                followIv.text = "关注Ta"
                followIv.background = U.getDrawable(R.drawable.common_yellow_button_icon)
            }
        }
    }

    private fun refreshUserInfoArea() {
        AvatarUtils.loadAvatarByUrl(imageBg, AvatarUtils.newParamsBuilder(infoModel.avatar)
                .build())
        nameView.setHonorText(infoModel.nicknameRemark, infoModel.honorInfo)
        signTv.text = infoModel.signature

        if (infoModel.ranking != null && LevelConfigUtils.getSmallImageResoucesLevel(infoModel.ranking?.mainRanking
                        ?: 0) != 0) {
            levelArea.visibility = View.VISIBLE
            levelIv.background = U.getDrawable(LevelConfigUtils.getSmallImageResoucesLevel(infoModel.ranking?.mainRanking
                    ?: 0))
            levelDesc.text = infoModel.ranking?.rankingDesc
        } else {
            levelArea.visibility = View.GONE
        }

        personTagView.setFansNum(fansNum)
        personTagView.setCharmTotal(meliTotal)
        infoModel?.let {
            personTagView.setSex(it.sex)
            personTagView.setLocation(it.location)
            personTagView.setUserID(it.userId)
        }

        audioView.minSize = 100.dp()
        if (voiceInfoModel != null && voiceInfoModel?.auditStatus == VoiceInfoModel.EVAS_AUDIT_OK) {
            // 有声音且审核通过
            audioView.visibility = View.VISIBLE
            audioView.bindData(voiceInfoModel?.duration ?: 0)
        } else {
            audioView.visibility = View.GONE
        }

        if (qinMiCntTotal > 0) {
            qinmiTv.visibility = View.VISIBLE
            qinmiTv.text = qinMiCntTotal.toString()
        } else {
            qinmiTv.visibility = View.INVISIBLE
        }

        val clubMemberInfo = infoModel.clubInfo
        if (clubMemberInfo?.club != null && !TextUtils.isEmpty(clubMemberInfo.club?.name)) {
            clubArea.visibility = View.VISIBLE
            personClubView.bindData(clubMemberInfo.club)
        } else {
            clubArea.visibility = View.GONE
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RelationChangeEvent) {
        if (event.useId == infoModel.userId) {
            refreshRelation(event.isFriend, event.isFollow, event.isSpFollow)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RemarkChangeEvent) {
        if (event.userId == infoModel.userId) {
            nameView.setHonorText(infoModel.nicknameRemark, infoModel.honorInfo)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: UploadHomePageEvent) {
        uploadHomePageFlag = true
    }

    override fun showSpFollowVip() {
        mTipsDialogView?.dismiss(false)
        mTipsDialogView = TipsDialogView.Builder(context)
                .setMessageTip("非VIP最多特别关注3个用户，是否开通vip享受15人上限～")
                .setConfirmTip("开通VIP")
                .setCancelTip("取消")
                .setConfirmBtnClickListener(object : AnimateClickListener() {
                    override fun click(view: View) {
                        mTipsDialogView?.dismiss()
                        ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                                .withString("url", ApiManager.getInstance().findRealUrlByChannel("https://app.inframe.mobi/user/newVip?title=1"))
                                .greenChannel().navigation()
                    }
                })
                .setCancelBtnClickListener(object : AnimateClickListener() {
                    override fun click(view: View) {
                        mTipsDialogView?.dismiss()
                    }
                })
                .build()
        mTipsDialogView?.showByDialog()
    }

    private fun addSpFollow(userInfoModel: UserInfoModel?) {
        mTipsDialogView?.dismiss(false)
        mTipsDialogView = TipsDialogView.Builder(context)
                .setMessageTip("是否对ta开启特别关注\n开启后，对方上线、发贴、聊天信息、上传照片等将有特别提醒")
                .setConfirmTip("开启")
                .setCancelTip("取消")
                .setConfirmBtnClickListener(object : AnimateClickListener() {
                    override fun click(view: View) {
                        mTipsDialogView?.dismiss()
                        mPresenter.addSpFollow(userId)
                    }
                })
                .setCancelBtnClickListener(object : AnimateClickListener() {
                    override fun click(view: View) {
                        mTipsDialogView?.dismiss()
                    }
                })
                .build()

        mTipsDialogView?.showByDialog()
    }

    private fun delSpFollow(userInfoModel: UserInfoModel?) {
        mTipsDialogView?.dismiss(false)
        mTipsDialogView = TipsDialogView.Builder(context)
                .setMessageTip("是否对ta关闭特别关注\n关闭后，你将无法收到关于ta的特别提醒啦")
                .setConfirmTip("关闭")
                .setCancelTip("取消")
                .setConfirmBtnClickListener(object : AnimateClickListener() {
                    override fun click(view: View) {
                        mTipsDialogView?.dismiss()
                        mPresenter.delSpFollow(userId)
                    }
                })
                .setCancelBtnClickListener(object : AnimateClickListener() {
                    override fun click(view: View) {
                        mTipsDialogView?.dismiss()
                    }
                })
                .build()

        mTipsDialogView?.showByDialog()
    }

    private fun unFollow(userInfoModel: UserInfoModel?) {
        mTipsDialogView?.dismiss(false)
        mTipsDialogView = TipsDialogView.Builder(context)
                .setTitleTip("取消关注")
                .setMessageTip("是否取消关注")
                .setConfirmTip("取消关注")
                .setCancelTip("不了")
                .setConfirmBtnClickListener(object : AnimateClickListener() {
                    override fun click(view: View) {
                        mTipsDialogView?.dismiss()
                        UserInfoManager.getInstance().mateRelation(userInfoModel!!.userId, UserInfoManager.RA_UNBUILD, userInfoModel.isFriend)
                    }
                })
                .setCancelBtnClickListener(object : AnimateClickListener() {
                    override fun click(view: View) {
                        mTipsDialogView?.dismiss()
                    }
                })
                .build()
        mTipsDialogView?.showByDialog()
    }

    override fun destroy() {
        super.destroy()
        SinglePlayer.stop(playTag)
        SinglePlayer.removeCallback(playTag)
        mTipsDialogView?.dismiss(false)
        mPersonMoreOpView?.dismiss()
        mEditRemarkDialog?.dismiss(false)
        mDialogPlus?.dismiss()
        mBusinessCardDialogView?.dismiss(false)
        photoView?.destory()
        relationView?.destory()
    }

    override fun onRequestSucess(hasMore: Boolean) {
        smartRefresh.finishRefresh()
        smartRefresh.finishLoadMore()
        smartRefresh.setEnableLoadMore(hasMore)
    }

    companion object {
        const val PERSON_CENTER_TOP_ICON = "http://res-static.inframe.mobi/app/person_center_top_bg.png"
    }
}
