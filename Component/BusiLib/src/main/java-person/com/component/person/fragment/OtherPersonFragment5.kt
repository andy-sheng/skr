package com.component.person.fragment

import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.AppBarLayout
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseFragment
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.ResponseCallBack
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.event.RelationChangeEvent
import com.common.core.userinfo.event.RemarkChangeEvent
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.core.view.setDebounceViewClickListener
import com.common.player.SinglePlayer
import com.common.player.SinglePlayerCallbackAdapter
import com.common.rxretrofit.ApiManager
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.common.utils.dp
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.viewpager.NestViewPager
import com.common.view.viewpager.SlidingTabLayout
import com.component.busilib.R
import com.component.busilib.friends.VoiceInfoModel
import com.component.dialog.BusinessCardDialogView
import com.component.level.utils.LevelConfigUtils
import com.component.person.OtherPersonActivity.Companion.BUNDLE_USER_ID
import com.component.person.event.ChildViewPlayAudioEvent
import com.component.person.event.UploadHomePageEvent
import com.component.person.model.RelationNumModel
import com.component.person.model.ScoreDetailModel
import com.component.person.photo.view.OtherPhotoWallView
import com.component.person.presenter.OtherPersonPresenter
import com.component.person.view.*
import com.dialog.view.TipsDialogView
import com.facebook.drawee.view.SimpleDraweeView
import com.imagebrowse.big.BigImageBrowseFragment
import com.module.ModuleServiceManager
import com.module.RouterConstants
import com.module.club.IClubModuleService
import com.module.feeds.IPersonFeedsWall
import com.module.home.IHomeService
import com.module.playways.IPlaywaysModeService
import com.module.post.IPersonPostsWall
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshHeader
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.SimpleMultiPurposeListener
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.abs

class OtherPersonFragment5 : BaseFragment(), IOtherPersonView, RequestCallBack {

    val SP_KEY_HAS_SHOW_SPFOLLOW = "SP_KEY_HAS_SHOW_SPFOLLOW"  // 提醒特别关注的

    private var mUserInfoModel: UserInfoModel = UserInfoModel()
    private var mUserId: Int = 0
    private var mFansNum: Int = 0
    private var mMeliTotal: Int = 0

    lateinit var mPresenter: OtherPersonPresenter

    lateinit var mImageBg: ImageView
    lateinit var mBottomBg: ImageView
    lateinit var mSmartRefresh: SmartRefreshLayout
    lateinit var mAppbar: AppBarLayout
    lateinit var mUserInfoArea: ConstraintLayout

    lateinit var mIvBack: ExImageView
    lateinit var mMoreBtn: ExImageView

    lateinit var mAvatarIv: SimpleDraweeView
    lateinit var mLevelBg: ImageView
    lateinit var mLevelDesc: TextView
    lateinit var mGuardView: GuardView
    lateinit var mPersonClubName: TextView

    lateinit var mQinmiTv: TextView
    lateinit var mNameTv: ExTextView
    lateinit var mBusinessIv: ImageView
    lateinit var mHonorIv: ImageView
    lateinit var mAudioView: CommonAudioView
    lateinit var mSignTv: TextView

    lateinit var mToolbar: Toolbar
    lateinit var mToolbarLayout: RelativeLayout
    lateinit var mSrlNameTv: TextView

    lateinit var mPersonTab: SlidingTabLayout
    lateinit var mPersonVp: NestViewPager
    lateinit var mPersonTabAdapter: PagerAdapter

    internal var mOtherPhotoWallView: OtherPhotoWallView? = null
    internal var mPostsWallView: IPersonPostsWall? = null
    internal var mFeedsWallView: IPersonFeedsWall? = null

    lateinit var mFunctionArea: ConstraintLayout
    lateinit var mInviteIv: ExTextView
    lateinit var mMessageIv: ExTextView
    lateinit var mFollowIv: ExTextView

    private var mPersonMoreOpView: PersonMoreOpView? = null
    private var mTipsDialogView: TipsDialogView? = null
    private var mEditRemarkDialog: DialogPlus? = null
    private var mDialogPlus: DialogPlus? = null

    var lastVerticalOffset = Int.MAX_VALUE

    private var isPlay = false
    private var playTag = "OtherPersonFragment4" + hashCode()
    private var playCallback: SinglePlayerCallbackAdapter? = null
    private var mVoiceInfoModel: VoiceInfoModel? = null

    private var mBusinessCardDialogView: BusinessCardDialogView? = null

    internal var srollDivider = U.getDisplayUtils().dip2px(150f)  // 滑到分界线的时候
    internal var bottomTopMargin = 0  // 底部背景距离顶部距离的初始值

    var uploadHomePageFlag = false

    override fun initView(): Int {
        return R.layout.other_person_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        initBaseContainArea()
        initTopArea()
        initUserInfoArea()
        initPersonTabArea()
        initFunctionArea()

        mPresenter = OtherPersonPresenter(this)
        addPresent(mPresenter)
        bindData()

        playCallback = object : SinglePlayerCallbackAdapter() {
            override fun onCompletion() {
                super.onCompletion()
                isPlay = false
                SinglePlayer.stop(playTag)
                mAudioView.setPlay(false)
            }

            override fun onPlaytagChange(oldPlayerTag: String?, newPlayerTag: String?) {
                if (newPlayerTag !== playTag) {
                    isPlay = false
                    SinglePlayer.stop(playTag)
                    mAudioView.setPlay(false)
                }
            }
        }
        SinglePlayer.addCallback(playTag, playCallback!!)

        // 得到底部距离顶部的初始值
        val bottomParams = mBottomBg.layoutParams as RelativeLayout.LayoutParams
        bottomTopMargin = bottomParams.topMargin
    }

    private fun bindData() {
        val bundle = arguments
        if (bundle != null) {
            mUserId = bundle.getInt(BUNDLE_USER_ID)
            mUserInfoModel!!.userId = mUserId
            mPresenter.getHomePage(mUserId)
        }

        if (mUserId.toLong() == MyUserInfoManager.uid) {
            mFunctionArea.visibility = View.GONE
            mMoreBtn.visibility = View.GONE
        }
    }

    override fun onNewIntent(intent: Intent?): Boolean {
        arguments = intent?.extras
        bindData()
        return true
    }

    private fun initBaseContainArea() {
        mImageBg = rootView.findViewById(R.id.image_bg)
        mBottomBg = rootView.findViewById(R.id.bottom_bg)
        mSmartRefresh = rootView.findViewById(R.id.smart_refresh)
        mAppbar = rootView.findViewById(R.id.appbar)
        mToolbarLayout = rootView.findViewById(R.id.toolbar_layout)
        mUserInfoArea = rootView.findViewById(R.id.user_info_area)
        mToolbar = rootView.findViewById(R.id.toolbar)
        mSrlNameTv = rootView.findViewById(R.id.srl_name_tv)

        mSmartRefresh.setEnableRefresh(true)
        mSmartRefresh.setEnableLoadMore(true)
        mSmartRefresh.setEnableLoadMoreWhenContentNotFull(false)
        mSmartRefresh.setEnableOverScrollDrag(true)
        mSmartRefresh.setHeaderMaxDragRate(1.5f)
        mSmartRefresh.setOnMultiPurposeListener(object : SimpleMultiPurposeListener() {
            internal var lastScale = 0f

            override fun onRefresh(refreshLayout: RefreshLayout) {
                mPresenter.getHomePage(mUserId)
                when {
                    mPersonVp.currentItem == 0 -> mOtherPhotoWallView?.getPhotos(true)
                    mPersonVp.currentItem == 1 -> mPostsWallView?.getPosts(true)
                    mPersonVp.currentItem == 2 -> mFeedsWallView?.getFeeds(true)
                }
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                when {
                    mPersonVp.currentItem == 0 -> mOtherPhotoWallView?.getMorePhotos()
                    mPersonVp.currentItem == 1 -> mPostsWallView?.getMorePosts()
                    mPersonVp.currentItem == 2 -> mFeedsWallView?.getMoreFeeds()
                }
            }

            override fun onHeaderMoving(header: RefreshHeader?, isDragging: Boolean, percent: Float, offset: Int, headerHeight: Int, maxDragHeight: Int) {
                super.onHeaderMoving(header, isDragging, percent, offset, headerHeight, maxDragHeight)
                val scale = offset.toFloat() / U.getDisplayUtils().dip2px(300f).toFloat() + 1
                if (abs(scale - lastScale) >= 0.01) {
                    lastScale = scale
                    mImageBg.scaleX = scale
                    mImageBg.scaleY = scale
                }
                if (offset > 0) {
                    mBottomBg.translationY = offset.toFloat()
                }
            }
        })

        mAppbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            // TODO: 2019-06-23 也可以加效果，看产品怎么说
            mImageBg.translationY = verticalOffset.toFloat()
            if (verticalOffset < 0) {
                mBottomBg.translationY = verticalOffset.toFloat()
            }
            if (lastVerticalOffset != verticalOffset) {
                lastVerticalOffset = verticalOffset
                if (verticalOffset == 0) {
                    // 展开状态
                    if (mToolbar.visibility != View.GONE) {
                        mToolbar.visibility = View.GONE
                        mToolbarLayout.visibility = View.GONE
                    }
                } else if (abs(verticalOffset) >= srollDivider) {
                    // 完全收缩状态
                    if (mToolbar.visibility != View.VISIBLE) {
                        mToolbar.visibility = View.VISIBLE
                        mToolbarLayout.visibility = View.VISIBLE
                    }

                    if (abs(verticalOffset) >= appBarLayout.totalScrollRange) {
                        mSrlNameTv.alpha = 1f
                    } else {
                        mSrlNameTv.alpha = (abs(verticalOffset) - srollDivider).toFloat() / (appBarLayout.totalScrollRange - srollDivider).toFloat()
                    }
                } else {
                    // TODO: 2019/4/8 过程中，可以加动画，先直接显示
                    if (mToolbar.visibility != View.GONE) {
                        mToolbar.visibility = View.GONE
                        mToolbarLayout.visibility = View.GONE
                    }
                }
            }
        })
    }

    private fun initTopArea() {
        mIvBack = rootView.findViewById<View>(R.id.iv_back) as ExImageView
        mMoreBtn = rootView.findViewById<View>(R.id.more_btn) as ExImageView

        mIvBack.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (activity != null) {
                    activity!!.finish()
                }
            }
        })

        mMoreBtn.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mPersonMoreOpView != null) {
                    mPersonMoreOpView!!.dismiss()
                }
                mPersonMoreOpView = PersonMoreOpView(context, mUserInfoModel.userId, mUserInfoModel.isFollow, mUserInfoModel.isSPFollow, false)
                mPersonMoreOpView!!.setListener(object : PersonMoreOpView.Listener {
                    override fun onClickRemark() {
                        mPersonMoreOpView?.dismiss()
                        showRemarkDialog()
                    }

                    override fun onClickSpFollow() {
                        mPersonMoreOpView?.dismiss()
                        if (mUserInfoModel.isSPFollow) {
                            // 取消特别关注
                            delSpFollow(mUserInfoModel)
                        } else {
                            // 特别关注去
                            addSpFollow(mUserInfoModel)
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
                                        .addDataBeforeAdd(1, mUserId)
                                        .setEnterAnim(R.anim.slide_in_bottom)
                                        .setExitAnim(R.anim.slide_out_bottom)
                                        .build())
                    }

                    override fun onClickKick() {

                    }

                    override fun onClickBlack(isInBlack: Boolean) {
                        mPersonMoreOpView?.dismiss()
                        if (isInBlack) {
                            UserInfoManager.getInstance().removeBlackList(mUserId, object : ResponseCallBack<Any?>() {
                                override fun onServerSucess(o: Any?) {
                                    U.getToastUtil().showShort("移除黑名单成功")
                                }

                                override fun onServerFailed() {

                                }
                            })
                        } else {
                            UserInfoManager.getInstance().addToBlacklist(mUserId, object : ResponseCallBack<Any?>() {
                                override fun onServerSucess(o: Any?) {
                                    U.getToastUtil().showShort("加入黑名单成功")
                                }

                                override fun onServerFailed() {

                                }
                            })
                        }

                    }
                })
                mPersonMoreOpView!!.showAt(mMoreBtn)
            }
        })
    }

    private fun showRemarkDialog() {
        val editRemarkView = EditRemarkView(activity, mUserInfoModel!!.nickname, mUserInfoModel!!.getNicknameRemark(null))
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
                if (TextUtils.isEmpty(remarkName) && TextUtils.isEmpty(mUserInfoModel!!.nicknameRemark)) {
                    // 都为空
                    return
                } else if (!TextUtils.isEmpty(mUserInfoModel!!.nicknameRemark) && mUserInfoModel!!.nicknameRemark == remarkName) {
                    // 相同
                    return
                } else {
                    UserInfoManager.getInstance().updateRemark(remarkName, mUserId)
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

    private fun initUserInfoArea() {

        mAvatarIv = rootView.findViewById(R.id.avatar_iv)
        mLevelBg = rootView.findViewById(R.id.level_bg)
        mLevelDesc = rootView.findViewById(R.id.level_desc)
        mGuardView = rootView.findViewById(R.id.guard_view)
        mPersonClubName = rootView.findViewById(R.id.person_club_name)

        mQinmiTv = rootView.findViewById(R.id.qinmi_tv)
        mNameTv = rootView.findViewById(R.id.name_tv)
        mHonorIv = rootView.findViewById(R.id.honor_iv)
        mAudioView = rootView.findViewById(R.id.audio_view)
        mSignTv = rootView.findViewById(R.id.sign_tv)
        mBusinessIv = rootView.findViewById(R.id.business_iv)

        if (mUserId == MyUserInfoManager.uid.toInt()) {
            mQinmiTv.visibility = View.GONE
        } else {
            mQinmiTv.visibility = View.VISIBLE
        }

        mGuardView.clickListener = {
            if (it == null) {
                // 去守护
                EventBus.getDefault().post(UploadHomePageEvent())
                ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                        .withString(RouterConstants.KEY_WEB_URL, ApiManager.getInstance().findRealUrlByChannel("https://dev.app.inframe.mobi/user/protector?title=1&userID=$mUserId"))
                        .navigation()
            } else {
                if (it.userId == MyUserInfoManager.uid.toInt()) {
                    EventBus.getDefault().post(UploadHomePageEvent())
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                            .withString(RouterConstants.KEY_WEB_URL, ApiManager.getInstance().findRealUrlByChannel("https://dev.app.inframe.mobi/user/protector?title=1&userID=$mUserId"))
                            .navigation()
                } else {
                    // 跳到个人主页
                    val bundle = Bundle()
                    bundle.putInt("bundle_user_id", it.userId)
                    ARouter.getInstance()
                            .build(RouterConstants.ACTIVITY_OTHER_PERSON)
                            .with(bundle)
                            .navigation()
                }
            }
        }

        mGuardView.clickMore = {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_GUARD_LIST)
                    .withInt("userID", it)
                    .withInt("from", 2)
                    .navigation()
        }

        mAvatarIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                BigImageBrowseFragment.open(false, activity, mUserInfoModel!!.avatar)
            }
        })

        mBusinessIv.setAnimateDebounceViewClickListener {
            mBusinessCardDialogView?.dismiss(false)
            mBusinessCardDialogView = BusinessCardDialogView(context!!, mUserInfoModel, mMeliTotal, mFansNum)
            mBusinessCardDialogView?.showByDialog()
        }

        mAudioView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (isPlay) {
                    // 暂停音乐
                    isPlay = false
                    mAudioView.setPlay(false)
                    SinglePlayer.stop(playTag)
                } else {
                    // 播放音乐
                    mVoiceInfoModel?.let {
                        isPlay = true
                        mFeedsWallView?.stopPlay()
                        mPostsWallView?.stopPlay()
                        mAudioView.setPlay(true)
                        SinglePlayer.startPlay(playTag, it.voiceURL)
                    }
                }
            }
        })

        mQinmiTv.setDebounceViewClickListener { showQinmiTips() }

        mPersonClubName.setDebounceViewClickListener {
            val clubServices = ARouter.getInstance().build(RouterConstants.SERVICE_CLUB).navigation() as IClubModuleService
            clubServices.tryGoClubHomePage(mUserInfoModel.clubInfo?.club?.clubID ?: 0)
        }
    }

//    private fun setAppBarCanScroll(canScroll: Boolean) {
//        if (isAppbarCanSrcoll == canScroll) {
//            return
//        }
//        if (mAppbar != null && mAppbar!!.layoutParams != null) {
//            val params = mAppbar!!.layoutParams as CoordinatorLayout.LayoutParams
//            val behavior = AppBarLayout.Behavior()
//            behavior.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
//                override fun canDrag(appBarLayout: AppBarLayout): Boolean {
//                    isAppbarCanSrcoll = canScroll
//                    return canScroll
//                }
//            })
//            params.behavior = behavior
//            mAppbar.layoutParams = params
//        }
//    }

    private fun initPersonTabArea() {
        mPersonTab = rootView.findViewById<View>(R.id.person_tab) as SlidingTabLayout
        mPersonVp = rootView.findViewById<View>(R.id.person_vp) as NestViewPager

        mPersonTab.setCustomTabView(R.layout.person_tab_view, R.id.tab_tv)
        mPersonTab.setSelectedIndicatorColors(U.getColor(R.color.black_trans_20))
        mPersonTab.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_NONE)
        mPersonTab.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NONE)
        mPersonTab.setIndicatorWidth(U.getDisplayUtils().dip2px(67f))
        mPersonTab.setIndicatorBottomMargin(U.getDisplayUtils().dip2px(12f))
        mPersonTab.setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(28f).toFloat())
        mPersonTab.setIndicatorCornorRadius(U.getDisplayUtils().dip2px(14f).toFloat())
        mPersonTabAdapter = object : PagerAdapter() {
            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                when (position) {
                    0 -> {
                        // 照片墙
                        if (mOtherPhotoWallView == null) {
                            mOtherPhotoWallView = OtherPhotoWallView(this@OtherPersonFragment5, mUserId, this@OtherPersonFragment5, null)
                        }
                        if (container.indexOfChild(mOtherPhotoWallView) == -1) {
                            container.addView(mOtherPhotoWallView)
                        }
                        mOtherPhotoWallView!!.getPhotos(false)
                        return mOtherPhotoWallView!!
                    }
                    1 -> {
                        // 帖子
                        if (mPostsWallView == null) {
                            val postModuleService = ModuleServiceManager.getInstance().postsService
                            mPostsWallView = postModuleService.getPostsWall(this@OtherPersonFragment5.activity, mUserInfoModel, this@OtherPersonFragment5)
                        }
                        if (container.indexOfChild(mPostsWallView as View) == -1) {
                            container.addView(mPostsWallView as View)
                        }
                        return mPostsWallView!!
                    }
                    2 -> {
                        // 神曲
                        if (mFeedsWallView == null) {
                            val mIFeedsModuleService = ModuleServiceManager.getInstance().feedsService
                            mFeedsWallView = mIFeedsModuleService.getPersonFeedsWall(this@OtherPersonFragment5, mUserInfoModel, this@OtherPersonFragment5)
                        }
                        if (container.indexOfChild(mFeedsWallView as View) == -1) {
                            container.addView(mFeedsWallView as View)
                        }
                        return mFeedsWallView!!
                    }
                }
                return super.instantiateItem(container, position)
            }

            override fun getCount(): Int {
                return 3
            }

            override fun getItemPosition(`object`: Any): Int {
                return PagerAdapter.POSITION_NONE
            }

            override fun getPageTitle(position: Int): CharSequence? {
                when (position) {
                    0 -> return "相册"
                    1 -> return "帖子"
                    2 -> return "神曲"
                    else -> return ""
                }
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view === `object`
            }
        }
        mPersonVp.adapter = mPersonTabAdapter
        mPersonTab.setViewPager(mPersonVp)
        mPersonTabAdapter.notifyDataSetChanged()

        mPersonVp.run {
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

                }

                override fun onPageSelected(position: Int) {
                    viewSelected(position)
                }

                override fun onPageScrollStateChanged(state: Int) {

                }
            })
        }
    }

    override fun onFragmentVisible() {
        super.onFragmentVisible()
        viewSelected(mPersonVp.currentItem)
        if (uploadHomePageFlag) {
            mPresenter.getHomePage(mUserId)
        }
    }

    override fun onFragmentInvisible(reason: Int) {
        super.onFragmentInvisible(reason)
        mFeedsWallView?.unselected(1)
        mPostsWallView?.unselected(1)
    }

    private fun viewSelected(position: Int) {
        when (position) {
            0 -> {
                mOtherPhotoWallView?.mHasMore?.let {
                    mSmartRefresh.setEnableLoadMore(it)
                }
                mOtherPhotoWallView?.getPhotos(false)
                mPostsWallView?.unselected(1)
                mFeedsWallView?.unselected(1)
            }
            1 -> {
                mPostsWallView?.isHasMore?.let {
                    mSmartRefresh.setEnableLoadMore(it)
                }
                mPostsWallView?.selected()
                mFeedsWallView?.unselected(1)
            }
            2 -> {
                mFeedsWallView?.isHasMore?.let {
                    mSmartRefresh.setEnableLoadMore(it)
                }
                mFeedsWallView?.selected()
                mPostsWallView?.unselected(1)
            }
        }
    }

    private fun initFunctionArea() {
        mFunctionArea = rootView.findViewById(R.id.function_area)
        mInviteIv = rootView.findViewById(R.id.invite_iv)
        mMessageIv = rootView.findViewById(R.id.message_iv)
        mFollowIv = rootView.findViewById(R.id.follow_iv)

        mFollowIv.setDebounceViewClickListener {
            if (!U.getNetworkUtils().hasNetwork()) {
                U.getToastUtil().showShort("网络异常，请检查网络后重试!")
                return@setDebounceViewClickListener
            }
            if (mUserInfoModel != null) {
                if (mUserInfoModel.isFollow) {
                    unFollow(mUserInfoModel)
                } else {
                    UserInfoManager.getInstance().mateRelation(mUserInfoModel.userId, UserInfoManager.RA_BUILD, mUserInfoModel.isFriend)
                }
            }
        }

        mMessageIv.setDebounceViewClickListener {
            if (mUserInfoModel != null) {
                val needPop = ModuleServiceManager.getInstance().msgService.startPrivateChat(context,
                        mUserInfoModel.userId.toString(),
                        mUserInfoModel.nicknameRemark,
                        mUserInfoModel.isFriend
                )
                if (needPop) {
                    activity?.finish()
                }
            }
        }

        // 去掉邀请
        mInviteIv.visibility = View.GONE
//        mInviteIv.setDebounceViewClickListener {
//            val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
//            iRankingModeService.tryInviteToRelay(mUserId, mUserInfoModel.isFriend)
//        }
    }

    override fun useEventBus(): Boolean {
        return true
    }


    override fun showHomePageInfo(userInfoModel: UserInfoModel,
                                  relationNumModels: List<RelationNumModel>?,
                                  meiLiCntTotal: Int, qinMiCntTotal: Int,
                                  scoreDetailModel: ScoreDetailModel, voiceInfoModel: VoiceInfoModel?, guardUserList: List<UserInfoModel>?, guardCntTotal: Int) {
        uploadHomePageFlag = false
        mSmartRefresh.finishRefresh()
        showUserInfo(userInfoModel)
        showRelationNum(relationNumModels)
        showUserRelation(userInfoModel.isFriend, userInfoModel.isFollow, userInfoModel.isSPFollow)
        showScoreDetail(scoreDetailModel)

        this.mMeliTotal = meiLiCntTotal
        mGuardView.bindData(mUserId, guardUserList, guardCntTotal)

        if (userInfoModel.isFollow) {
            if (!U.getPreferenceUtils().getSettingBoolean(SP_KEY_HAS_SHOW_SPFOLLOW, false)) {
                showSpFollowTips()
            }
        }
        if (qinMiCntTotal > 0) {
            mQinmiTv.visibility = View.VISIBLE
            mQinmiTv.text = qinMiCntTotal.toString()
        } else {
            mQinmiTv.visibility = View.GONE
        }

        mVoiceInfoModel = voiceInfoModel
        mSignTv.text = userInfoModel.signature
        if (voiceInfoModel != null) {
            mAudioView.bindData(voiceInfoModel.duration)
            mAudioView.visibility = View.VISIBLE
            mSignTv.visibility = View.GONE
        } else {
            mAudioView.visibility = View.GONE
            mSignTv.visibility = View.VISIBLE
        }
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
        showUserRelation(isFriend, isFollow, isSpFollow)
    }

    private fun showScoreDetail(scoreDetailModel: ScoreDetailModel) {
        if (scoreDetailModel.scoreStateModel != null && LevelConfigUtils.getRaceCenterAvatarBg(scoreDetailModel.scoreStateModel!!.mainRanking) != 0) {
            mLevelBg.visibility = View.VISIBLE
            mLevelDesc.visibility = View.VISIBLE
            mLevelBg.background = U.getDrawable(LevelConfigUtils.getRaceCenterAvatarBg(scoreDetailModel.scoreStateModel!!.mainRanking))
            mLevelDesc.text = scoreDetailModel.scoreStateModel!!.rankingDesc
        } else {
            mLevelBg.visibility = View.GONE
            mLevelDesc.visibility = View.GONE
        }
    }

    override fun getHomePageFail() {
        uploadHomePageFlag = false
        mSmartRefresh.finishRefresh()
    }

    private fun showUserInfo(model: UserInfoModel) {
        this.mUserInfoModel = model
        mFeedsWallView?.setUserInfoModel(model)
        mPostsWallView?.setUserInfoModel(model)
        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(model.avatar)
                .setCircle(true)
                .build())
        mNameTv.text = model.nicknameRemark
        mSrlNameTv.text = model.nicknameRemark

        if (model.honorInfo != null && model.honorInfo.isHonor()) {
            mHonorIv.visibility = View.VISIBLE
        } else {
            mHonorIv.visibility = View.GONE
        }


        if (!TextUtils.isEmpty(model.clubInfo?.club?.name)) {
            mPersonClubName.visibility = View.VISIBLE
            mPersonClubName.text = "【${model.clubInfo?.club?.name}】"
            // 适配下背景
            val bottomParams = mBottomBg.layoutParams as RelativeLayout.LayoutParams
            bottomParams.topMargin = bottomTopMargin + 36.dp()
            mBottomBg.layoutParams = bottomParams
        } else {
            mPersonClubName.visibility = View.GONE
            // 适配下背景
            val bottomParams = mBottomBg.layoutParams as RelativeLayout.LayoutParams
            bottomParams.topMargin = bottomTopMargin
            mBottomBg.layoutParams = bottomParams

        }
    }


    private fun showRelationNum(list: List<RelationNumModel>?) {
        if (list != null && list.isNotEmpty()) {
            for (mode in list) {
                if (mode.relation == UserInfoManager.RELATION.FANS.value) {
                    mFansNum = mode.cnt
                }
            }
        }
    }

    private fun showUserRelation(isFriend: Boolean, isFollow: Boolean, isSpFollow: Boolean) {
        mUserInfoModel.isFriend = isFriend
        mUserInfoModel.isFollow = isFollow
        mUserInfoModel.isSPFollow = isSpFollow
        when {
            isFriend -> {
                mFollowIv.text = "互关"
                mFollowIv.background = U.getDrawable(R.drawable.common_hollow_yellow_icon)
            }
            isFollow -> {
                mFollowIv.text = "已关注"
                mFollowIv.background = U.getDrawable(R.drawable.common_hollow_yellow_icon)
            }
            else -> {
                mFollowIv.text = "关注Ta"
                mFollowIv.background = U.getDrawable(R.drawable.common_yellow_button_icon)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RelationChangeEvent) {
        if (event.useId == mUserInfoModel.userId) {
            showUserRelation(event.isFriend, event.isFollow, event.isSpFollow)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RemarkChangeEvent) {
        if (event.userId == mUserInfoModel!!.userId) {
            mNameTv.text = mUserInfoModel!!.nicknameRemark
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ChildViewPlayAudioEvent) {
        isPlay = false
        SinglePlayer.stop(playTag)
        mAudioView.setPlay(false)
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
                        mPresenter.addSpFollow(mUserId)
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
                        mPresenter.delSpFollow(mUserId)
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
        mOtherPhotoWallView?.destory()
        mPostsWallView?.destroy()
        mFeedsWallView?.destroy()
        mTipsDialogView?.dismiss(false)
        mPersonMoreOpView?.dismiss()
        mEditRemarkDialog?.dismiss(false)
        mFeedsWallView?.destroy()
        mDialogPlus?.dismiss()
        mBusinessCardDialogView?.dismiss(false)
    }

    override fun onRequestSucess(hasMore: Boolean) {
        mSmartRefresh.finishRefresh()
        mSmartRefresh.finishLoadMore()
        mSmartRefresh.setEnableLoadMore(hasMore)
    }

    companion object {
        const val PERSON_CENTER_TOP_ICON = "http://res-static.inframe.mobi/app/person_center_top_bg.png"
    }
}
