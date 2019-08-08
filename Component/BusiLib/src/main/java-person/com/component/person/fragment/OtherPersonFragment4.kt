package com.component.person.fragment

import android.graphics.Color
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseFragment
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.event.RelationChangeEvent
import com.common.core.userinfo.event.RemarkChangeEvent
import com.common.core.userinfo.model.GameStatisModel
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.userinfo.model.UserLevelModel
import com.common.core.userinfo.model.UserRankModel
import com.common.flowlayout.FlowLayout
import com.common.flowlayout.TagAdapter
import com.common.flowlayout.TagFlowLayout
import com.common.image.fresco.FrescoWorker
import com.common.image.model.BaseImage
import com.common.image.model.ImageFactory
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.common.view.viewpager.NestViewPager
import com.common.view.viewpager.SlidingTabLayout
import com.component.busilib.R

import com.dialog.view.TipsDialogView
import com.facebook.drawee.view.SimpleDraweeView
import com.imagebrowse.big.BigImageBrowseFragment
import com.module.ModuleServiceManager
import com.module.RouterConstants
import com.module.home.IHomeService
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshHeader
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.SimpleMultiPurposeListener
import com.component.dialog.BusinessCardDialogView
import com.component.level.view.NormalLevelView2
import com.zq.live.proto.Common.ESex
import com.component.person.utils.StringFromatUtils
import com.component.person.model.TagModel
import com.component.person.presenter.OtherPersonPresenter
import com.component.person.view.EditRemarkView
import com.component.person.view.IOtherPersonView
import com.component.person.photo.view.OtherPhotoWallView
import com.component.person.view.PersonMoreOpView
import com.component.person.view.RequestCallBack

import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import java.util.ArrayList
import java.util.HashMap

import com.component.person.model.RelationNumModel

import com.component.person.OtherPersonActivity.Companion.BUNDLE_USER_ID
import com.module.feeds.IPersonFeedsWall

class OtherPersonFragment4 : BaseFragment(), IOtherPersonView, RequestCallBack {

    private val mTags = ArrayList<TagModel>()  //标签
    private val mHashMap = HashMap<Int, String>()

    lateinit var mTagAdapter: TagAdapter<TagModel>
    private var fansNum = 0 // 粉丝数
    private var charmNum = 0 // 魅力值

    internal var isAppbarCanSrcoll = true  // AppBarLayout是否可以滚动

    internal var mUserInfoModel: UserInfoModel = UserInfoModel()
    internal var mUserId: Int = 0

    internal var mDialogPlus: DialogPlus? = null

    lateinit var mPresenter: OtherPersonPresenter

    lateinit var mImageBg: SimpleDraweeView
    lateinit var mSmartRefresh: SmartRefreshLayout
    lateinit var mAppbar: AppBarLayout
    lateinit var mToolbarLayout: CollapsingToolbarLayout
    lateinit var mUserInfoArea: ConstraintLayout

    lateinit var mIvBack: ExImageView
    lateinit var mMoreBtn: ExImageView
    private var mPersonMoreOpView: PersonMoreOpView? = null

    lateinit var mAvatarIv: SimpleDraweeView
    lateinit var mLevelView: NormalLevelView2
    lateinit var mNameTv: ExTextView
    lateinit var mSexIv: ImageView
    lateinit var mBusinessCard: ImageView
    lateinit var mSignTv: ExTextView
    lateinit var mFlowlayout: TagFlowLayout
    lateinit var mUseridTv: TextView

    lateinit var mToolbar: Toolbar
    lateinit var mSrlNameTv: TextView

    lateinit var mPersonTab: SlidingTabLayout
    lateinit var mPersonVp: NestViewPager
    lateinit var mPersonTabAdapter: PagerAdapter

    internal var mOtherPhotoWallView: OtherPhotoWallView? = null
    internal var mFeedsWallView: IPersonFeedsWall? = null
//    internal var mProducationWallView: ProducationWallView? = null

    lateinit var mFunctionArea: LinearLayout
    lateinit var mFollowIv: ExTextView
    lateinit var mMessageIv: ExTextView

    internal var mEditRemarkDialog: DialogPlus? = null

    // 未关注
    private val mUnFollowDrawable = DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#FFC15B"))
            .setCornersRadius(U.getDisplayUtils().dip2px(20f).toFloat())
            .build()

    // 已关注 或 互关
    private val mFollowDrawable = DrawableCreator.Builder()
            .setStrokeColor(Color.parseColor("#AD6C00"))
            .setStrokeWidth(U.getDisplayUtils().dip2px(1f).toFloat())
            .setCornersRadius(U.getDisplayUtils().dip2px(20f).toFloat())
            .build()

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

        val bundle = arguments
        if (bundle != null) {
            mUserId = bundle.getInt(BUNDLE_USER_ID)
            mUserInfoModel!!.userId = mUserId
            mPresenter.getHomePage(mUserId)
        }

        if (mUserId.toLong() == MyUserInfoManager.getInstance().uid) {
            mFunctionArea.visibility = View.GONE
            mMoreBtn.visibility = View.GONE
        }
    }


    private fun initBaseContainArea() {
        mImageBg = rootView.findViewById<View>(R.id.image_bg) as SimpleDraweeView
        mSmartRefresh = rootView.findViewById<View>(R.id.smart_refresh) as SmartRefreshLayout
        mAppbar = rootView.findViewById<View>(R.id.appbar) as AppBarLayout
        mToolbarLayout = rootView.findViewById<View>(R.id.toolbar_layout) as CollapsingToolbarLayout
        mUserInfoArea = rootView.findViewById<View>(R.id.user_info_area) as ConstraintLayout
        mToolbar = rootView.findViewById<View>(R.id.toolbar) as Toolbar
        mSrlNameTv = rootView.findViewById<View>(R.id.srl_name_tv) as TextView

        FrescoWorker.loadImage(mImageBg, ImageFactory.newPathImage(OtherPersonFragment4.PERSON_CENTER_TOP_ICON)
                .build<BaseImage>())

        mSmartRefresh.setEnableRefresh(true)
        mSmartRefresh.setEnableLoadMore(true)
        mSmartRefresh.setEnableLoadMoreWhenContentNotFull(false)
        mSmartRefresh.setEnableOverScrollDrag(true)
        mSmartRefresh.setHeaderMaxDragRate(1.5f)
        mSmartRefresh.setOnMultiPurposeListener(object : SimpleMultiPurposeListener() {
            internal var lastScale = 0f

            override fun onRefresh(refreshLayout: RefreshLayout) {
                mPresenter.getHomePage(mUserId)
                if (mPersonVp.currentItem == 0) {
                    mOtherPhotoWallView?.getPhotos(true)
                } else if (mPersonVp.currentItem == 1) {
                    mFeedsWallView?.getFeeds(true)
                }
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                if (mPersonVp.currentItem == 0) {
                    mOtherPhotoWallView?.getMorePhotos()
                } else if (mPersonVp.currentItem == 1) {
                    mFeedsWallView?.getMoreFeeds()
                }
            }

            override fun onHeaderMoving(header: RefreshHeader?, isDragging: Boolean, percent: Float, offset: Int, headerHeight: Int, maxDragHeight: Int) {
                super.onHeaderMoving(header, isDragging, percent, offset, headerHeight, maxDragHeight)
                val scale = offset.toFloat() / U.getDisplayUtils().dip2px(300f).toFloat() + 1
                if (Math.abs(scale - lastScale) >= 0.01) {
                    lastScale = scale
                    mImageBg.scaleX = scale
                    mImageBg.scaleY = scale
                }
            }
        })

        mAppbar!!.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            // TODO: 2019-06-23 也可以加效果，看产品怎么说
            mImageBg.translationY = verticalOffset.toFloat()
            if (verticalOffset == 0) {
                // 展开状态
                if (mToolbar.visibility != View.GONE) {
                    mToolbar.visibility = View.GONE
                }
            } else if (Math.abs(verticalOffset) >= appBarLayout.totalScrollRange) {
                // 完全收缩状态
                if (mToolbar.visibility != View.VISIBLE) {
                    mToolbar.visibility = View.VISIBLE
                }
            } else {
                // TODO: 2019/4/8 过程中，可以加动画，先直接显示
                if (mToolbar.visibility != View.GONE) {
                    mToolbar.visibility = View.GONE
                }
            }
        }
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
                mPersonMoreOpView = PersonMoreOpView(context, mUserInfoModel!!.userId, mUserInfoModel!!.isFollow, false)
                mPersonMoreOpView!!.setListener(object : PersonMoreOpView.Listener {
                    override fun onClickRemark() {
                        if (mPersonMoreOpView != null) {
                            mPersonMoreOpView!!.dismiss()
                        }
                        // TODO: 2019/5/22 修改备注昵称
                        showRemarkDialog()
                    }

                    override fun onClickUnFollow() {
                        if (mPersonMoreOpView != null) {
                            mPersonMoreOpView!!.dismiss()
                        }
                        unFollow(mUserInfoModel)
                    }

                    override fun onClickReport() {
                        if (mPersonMoreOpView != null) {
                            mPersonMoreOpView!!.dismiss()
                        }

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
                        if (mPersonMoreOpView != null) {
                            mPersonMoreOpView!!.dismiss()
                        }

                        if (isInBlack) {
                            UserInfoManager.getInstance().removeBlackList(mUserId, object : UserInfoManager.ResponseCallBack<Any?>() {
                                override fun onServerSucess(o: Any?) {
                                    U.getToastUtil().showShort("移除黑名单成功")
                                }

                                override fun onServerFailed() {

                                }
                            })
                        } else {
                            UserInfoManager.getInstance().addToBlacklist(mUserId, object : UserInfoManager.ResponseCallBack<Any?>() {
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
        mAvatarIv = rootView.findViewById<View>(R.id.avatar_iv) as SimpleDraweeView
        mLevelView = rootView.findViewById<View>(R.id.level_view) as NormalLevelView2
        mNameTv = rootView.findViewById<View>(R.id.name_tv) as ExTextView
        mSexIv = rootView.findViewById<View>(R.id.sex_iv) as ImageView
        mBusinessCard = rootView.findViewById<View>(R.id.business_card) as ImageView
        mUseridTv = rootView.findViewById<View>(R.id.userid_tv) as ExTextView
        mSignTv = rootView.findViewById<View>(R.id.sign_tv) as ExTextView
        mFlowlayout = rootView.findViewById<View>(R.id.flowlayout) as TagFlowLayout

        mTagAdapter = object : TagAdapter<TagModel>(mTags) {
            override fun getView(parent: FlowLayout, position: Int, tagModel: TagModel): View {
                if (tagModel.type != CHARM_TAG) {
                    val tv = LayoutInflater.from(context).inflate(R.layout.other_person_tag_textview,
                            mFlowlayout, false) as ExTextView
                    tv.text = tagModel.content
                    return tv
                } else {
                    val tv = LayoutInflater.from(context).inflate(R.layout.other_person_charm_tag,
                            mFlowlayout, false) as ExTextView
                    tv.text = tagModel.content
                    return tv
                }
            }
        }
        mFlowlayout.adapter = mTagAdapter

        mAvatarIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                BigImageBrowseFragment.open(false, activity, mUserInfoModel!!.avatar)
            }
        })

        mBusinessCard.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                // TODO: 2019-06-19 打开名片页面
                showBusinessCard()
            }
        })
    }

    private fun showBusinessCard() {
        val businessCardDialogView = BusinessCardDialogView(context!!, mUserInfoModel!!, fansNum, charmNum)
        mDialogPlus = DialogPlus.newDialog(activity!!)
                .setContentHolder(ViewHolder(businessCardDialogView))
                .setGravity(Gravity.CENTER)
                .setMargin(U.getDisplayUtils().dip2px(40f), -1, U.getDisplayUtils().dip2px(40f), -1)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setExpanded(false)
                .create()
        mDialogPlus!!.show()
    }


    private fun setAppBarCanScroll(canScroll: Boolean) {
        if (isAppbarCanSrcoll == canScroll) {
            return
        }
        if (mAppbar != null && mAppbar!!.layoutParams != null) {
            val params = mAppbar!!.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = AppBarLayout.Behavior()
            behavior.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
                override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                    isAppbarCanSrcoll = canScroll
                    return canScroll
                }
            })
            params.behavior = behavior
            mAppbar!!.layoutParams = params
        }
    }

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
                if (position == 0) {
                    // 照片墙
                    if (mOtherPhotoWallView == null) {
                        mOtherPhotoWallView = OtherPhotoWallView(this@OtherPersonFragment4, mUserId, this@OtherPersonFragment4, null)
                    }
                    if (container.indexOfChild(mOtherPhotoWallView) == -1) {
                        container.addView(mOtherPhotoWallView)
                    }
                    mOtherPhotoWallView!!.getPhotos(false)
                    return mOtherPhotoWallView!!
                } else if (position == 1) {
                    // 神曲
                    if (mFeedsWallView == null) {
                        val mIFeedsModuleService = ModuleServiceManager.getInstance().feedsService
                        mFeedsWallView = mIFeedsModuleService.getPersonFeedsWall(this@OtherPersonFragment4, mUserInfoModel, this@OtherPersonFragment4)
                    }
                    if (container.indexOfChild(mFeedsWallView as View) == -1) {
                        container.addView(mFeedsWallView as View)
                    }
                    return mFeedsWallView!!
                }
                return super.instantiateItem(container, position)
            }

            override fun getCount(): Int {
                return 2
            }

            override fun getItemPosition(`object`: Any): Int {
                return PagerAdapter.POSITION_NONE
            }

            override fun getPageTitle(position: Int): CharSequence? {
                if (position == 0) {
                    return "相册"
                } else if (position == 1) {
                    return "神曲"
                }
                return ""
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view === `object`
            }
        }
        mPersonVp.adapter = mPersonTabAdapter
        mPersonTab.setViewPager(mPersonVp)
        mPersonTabAdapter.notifyDataSetChanged()

        mPersonVp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                viewSelected(position)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }

    override fun onFragmentVisible() {
        super.onFragmentVisible()
        viewSelected(mPersonVp.currentItem)
    }

    private fun viewSelected(position: Int) {
        if (position == 0) {
            mOtherPhotoWallView?.mHasMore?.let {
                mSmartRefresh.setEnableLoadMore(it)
            }
            mOtherPhotoWallView?.getPhotos(false)
            mFeedsWallView?.unselected()
        } else if (position == 1) {
            mFeedsWallView?.isHasMore?.let {
                mSmartRefresh.setEnableLoadMore(it)
            }
            mFeedsWallView?.selected()
        }
    }

    private fun initFunctionArea() {
        mFunctionArea = rootView.findViewById(R.id.function_area)
        mFollowIv = rootView.findViewById(R.id.follow_iv)
        mMessageIv = rootView.findViewById(R.id.message_iv)

        mFollowIv.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View) {
                if (!U.getNetworkUtils().hasNetwork()) {
                    U.getToastUtil().showShort("网络异常，请检查网络后重试!")
                    return
                }
                if (mUserInfoModel != null) {
                    var tag: Int? = null
                    if (mFollowIv.tag != null) {
                        tag = mFollowIv.tag as Int
                    }
                    if (tag != null) {
                        if (tag == RELATION_FOLLOWED) {
                            unFollow(mUserInfoModel)
                        } else if (tag == RELATION_UN_FOLLOW) {
                            UserInfoManager.getInstance().mateRelation(mUserInfoModel!!.userId, UserInfoManager.RA_BUILD, mUserInfoModel!!.isFriend)
                        }
                    }
                }
            }
        })

        mMessageIv.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View) {
                if (mUserInfoModel != null) {
                    val needPop = ModuleServiceManager.getInstance().msgService.startPrivateChat(context,
                            mUserInfoModel!!.userId.toString(),
                            mUserInfoModel!!.nicknameRemark,
                            mUserInfoModel!!.isFriend
                    )
                    if (needPop) {
                        U.getFragmentUtils().popFragment(this@OtherPersonFragment4)
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
                                  userRankModels: List<UserRankModel>?,
                                  userLevelModels: List<UserLevelModel>?,
                                  gameStatisModels: List<GameStatisModel>?,
                                  isFriend: Boolean, isFollow: Boolean, meiLiCntTotal: Int) {
        mSmartRefresh.finishRefresh()
        showUserInfo(userInfoModel)
        showRelationNum(relationNumModels)
        showReginRank(userRankModels)
        showUserRelation(isFriend, isFollow)
        showUserLevel(userLevelModels)
        showCharms(meiLiCntTotal)
    }

    private fun showCharms(meiLiCntTotal: Int) {
        charmNum = meiLiCntTotal

        mHashMap.put(CHARM_TAG, "魅力 " + StringFromatUtils.formatMillion(meiLiCntTotal))
        refreshTag()
    }

    private fun showUserLevel(list: List<UserLevelModel>?) {
        var mainRank = 0
        var subRank = 0
        if (list != null && list.isNotEmpty()) {
            for (userLevelModel in list) {
                if (userLevelModel.type == UserLevelModel.RANKING_TYPE) {
                    mainRank = userLevelModel.score
                } else if (userLevelModel.type == UserLevelModel.SUB_RANKING_TYPE) {
                    subRank = userLevelModel.score
                }
            }
        }
        mLevelView.bindData(mainRank, subRank)
    }

    override fun getHomePageFail() {
        mSmartRefresh.finishRefresh()
    }

    private fun showUserInfo(model: UserInfoModel) {
        this.mUserInfoModel = model
        mFeedsWallView?.setUserInfoModel(model)
        AvatarUtils.loadAvatarByUrl(mAvatarIv,
                AvatarUtils.newParamsBuilder(model.avatar)
                        .setBorderColor(Color.WHITE)
                        .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                        .setCircle(true)
                        .build())

        mNameTv.text = model.nicknameRemark
        mUseridTv.text = "ID:" + model.userId
        if (model.sex == ESex.SX_MALE.value) {
            mSexIv.visibility = View.VISIBLE
            mSexIv.setBackgroundResource(R.drawable.sex_man_icon)
        } else if (model.sex == ESex.SX_FEMALE.value) {
            mSexIv.visibility = View.VISIBLE
            mSexIv.setBackgroundResource(R.drawable.sex_woman_icon)
        } else {
            mSexIv.visibility = View.GONE
        }

        mSrlNameTv.text = model.nicknameRemark
        mSignTv.text = model.signature

        if (model.location != null && !TextUtils.isEmpty(model.location.province)) {
            mHashMap.put(LOCATION_TAG, model.location.province)
        } else {
            mHashMap.put(LOCATION_TAG, "火星")
        }

        refreshTag()
    }

    private fun refreshTag() {
        mTags.clear()
        if (mHashMap != null) {
            if (!TextUtils.isEmpty(mHashMap.get(CHARM_TAG))) {
                mTags.add(TagModel(CHARM_TAG, mHashMap.get(CHARM_TAG)))
            }

            if (!TextUtils.isEmpty(mHashMap.get(FANS_NUM_TAG))) {
                mTags.add(TagModel(FANS_NUM_TAG, mHashMap.get(FANS_NUM_TAG)))
            }

            if (!TextUtils.isEmpty(mHashMap.get(LOCATION_TAG))) {
                mTags.add(TagModel(LOCATION_TAG, mHashMap.get(LOCATION_TAG)))
            }

        }
        mTagAdapter.setTagDatas(mTags)
        mTagAdapter.notifyDataChanged()
    }


    private fun showRelationNum(list: List<RelationNumModel>?) {
        if (list != null && list.isNotEmpty()) {
            for (mode in list) {
                if (mode.relation == UserInfoManager.RELATION.FANS.value) {
                    fansNum = mode.cnt
                }
            }
        }

        mHashMap.put(FANS_NUM_TAG, "粉丝 " + StringFromatUtils.formatTenThousand(fansNum))

        refreshTag()
    }


    private fun showReginRank(list: List<UserRankModel>?) {
        //        mMedalIv.setBackground(getResources().getDrawable(R.drawable.paihang));
        //        UserRankModel reginRankModel = new UserRankModel();
        //        UserRankModel countryRankModel = new UserRankModel();
        //        if (list != null && list.size() > 0) {
        //            for (UserRankModel model : list) {
        //                if (model.getCategory() == UserRankModel.REGION) {
        //                    reginRankModel = model;
        //                }
        //                if (model.getCategory() == UserRankModel.COUNTRY) {
        //                    countryRankModel = model;
        //                }
        //            }
        //        }
        //
        //        if (reginRankModel != null && reginRankModel.getRankSeq() != 0) {
        //            mRankText.setText(reginRankModel.getRegionDesc() + "第" + String.valueOf(reginRankModel.getRankSeq()) + "位");
        //        } else if (countryRankModel != null && countryRankModel.getRankSeq() != 0) {
        //            mRankText.setText(countryRankModel.getRegionDesc() + "第" + String.valueOf(countryRankModel.getRankSeq()) + "位");
        //        } else {
        //            mRankText.setText(getResources().getString(R.string.default_rank_text));
        //        }
    }

    private fun showUserRelation(isFriend: Boolean, isFollow: Boolean) {
        mUserInfoModel!!.isFriend = isFriend
        mUserInfoModel!!.isFollow = isFollow
        if (isFriend) {
            mFollowIv.isClickable = false
            mFollowIv.text = "互关"
            mFollowIv.background = mFollowDrawable
            mFollowIv.tag = RELATION_FOLLOWED
        } else if (isFollow) {
            mFollowIv.isClickable = false
            mFollowIv.text = "已关注"
            mFollowIv.tag = RELATION_FOLLOWED
            mFollowIv.background = mFollowDrawable
        } else {
            mFollowIv.isClickable = true
            mFollowIv.text = "关注Ta"
            mFollowIv.tag = RELATION_UN_FOLLOW
            mFollowIv.background = mUnFollowDrawable
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RelationChangeEvent) {
        if (event.useId == mUserInfoModel!!.userId) {
            showUserRelation(event.isFriend, event.isFollow)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RemarkChangeEvent) {
        if (event.userId == mUserInfoModel!!.userId) {
            mNameTv.text = mUserInfoModel!!.nicknameRemark
        }
    }

    private fun unFollow(userInfoModel: UserInfoModel?) {
        val tipsDialogView = TipsDialogView.Builder(context)
                .setTitleTip("取消关注")
                .setMessageTip("是否取消关注")
                .setConfirmTip("取消关注")
                .setCancelTip("不了")
                .setConfirmBtnClickListener(object : AnimateClickListener() {
                    override fun click(view: View) {
                        if (mDialogPlus != null) {
                            mDialogPlus!!.dismiss()
                        }
                        UserInfoManager.getInstance().mateRelation(userInfoModel!!.userId, UserInfoManager.RA_UNBUILD, userInfoModel.isFriend)
                    }
                })
                .setCancelBtnClickListener(object : AnimateClickListener() {
                    override fun click(view: View) {
                        if (mDialogPlus != null) {
                            mDialogPlus!!.dismiss()
                        }
                    }
                })
                .build()

        mDialogPlus = DialogPlus.newDialog(context!!)
                .setContentHolder(ViewHolder(tipsDialogView))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setExpanded(false)
                .setOnDismissListener { }
                .create()
        mDialogPlus!!.show()
    }

    override fun destroy() {
        super.destroy()
        mOtherPhotoWallView?.destory()
        mFeedsWallView?.destroy()
        mDialogPlus?.dismiss()
        mPersonMoreOpView?.dismiss()
        mEditRemarkDialog?.dismiss(false)
        mFeedsWallView?.destroy()
    }

    override fun onRequestSucess(hasMore: Boolean) {
        mSmartRefresh.finishRefresh()
        mSmartRefresh.finishLoadMore()
        mSmartRefresh.setEnableLoadMore(hasMore)
    }

    companion object {
        const val PERSON_CENTER_TOP_ICON = "http://res-static.inframe.mobi/app/person_center_top_bg.png"

        const val RELATION_FOLLOWED = 1 // 已关注关系
        const val RELATION_UN_FOLLOW = 2 // 未关注关系

        const val CHARM_TAG = 0            // 魅力值
        const val FANS_NUM_TAG = 1         // 粉丝数标签
        const val LOCATION_TAG = 2         // 地区标签  省
    }
}
