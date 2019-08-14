package com.component.dialog

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout

import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.callback.Callback
import com.common.core.account.UserAccountManager
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.ResponseCallBack
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.UserInfoServerApi
import com.common.core.userinfo.event.RelationChangeEvent
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.userinfo.model.UserLevelModel
import com.common.flowlayout.FlowLayout
import com.common.flowlayout.TagAdapter
import com.common.flowlayout.TagFlowLayout
import com.common.log.MyLog
import com.common.notification.event.FollowNotifyEvent
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.statistics.StatisticsAdapter
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.component.busilib.R
import com.component.busilib.view.MarqueeTextView
import com.facebook.drawee.view.SimpleDraweeView
import com.imagebrowse.ImageBrowseView
import com.imagebrowse.big.BigImageBrowseFragment
import com.imagebrowse.big.DefaultImageBrowserLoader
import com.module.ModuleServiceManager
import com.module.common.ICallback
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import com.component.level.view.NormalLevelView2
import com.zq.live.proto.Common.ESex
import com.component.person.utils.StringFromatUtils
import com.component.person.photo.adapter.PhotoAdapter
import com.component.person.photo.model.PhotoModel
import com.component.person.model.TagModel
import com.component.person.view.PersonMoreOpView

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import java.util.ArrayList
import java.util.HashMap

import com.component.person.model.RelationNumModel

class PersonInfoDialogView2 internal constructor(val mContext: Context, userID: Int, showKick: Boolean, showInvite: Boolean) : RelativeLayout(mContext) {

    val TAG = "PersonInfoDialogView2"

    internal var mUiHandler = Handler()

    lateinit var mSmartRefresh: SmartRefreshLayout
    lateinit var mOutView: View
    lateinit var mCoordinator: CoordinatorLayout

    lateinit var mAppbar: AppBarLayout
    lateinit var mToolbarLayout: CollapsingToolbarLayout
    lateinit var mUserInfoArea: ConstraintLayout
    lateinit var mAvatarIv: SimpleDraweeView
    lateinit var mMoreBtn: ExImageView

    lateinit var mLevelView: NormalLevelView2
    lateinit var mNameTv: ExTextView
    lateinit var mSexIv: ImageView
    lateinit var mSignTv: MarqueeTextView
    lateinit var mFlowlayout: TagFlowLayout

    lateinit var mFunctionArea: ConstraintLayout
    lateinit var mInviteIv: ExTextView
    lateinit var mFollowIv: ExTextView

    lateinit var mToolbar: Toolbar
    lateinit var mSrlAvatarIv: SimpleDraweeView

    lateinit var mPhotoView: RecyclerView
    lateinit var mEmptyMyPhoto: ExTextView

    private var mTags = ArrayList<TagModel>()  //标签
    private var mHashMap = HashMap<Int, String?>()

    lateinit var mTagAdapter: TagAdapter<TagModel>

    lateinit var mPhotoAdapter: PhotoAdapter

    internal var mUserId: Int = 0
    internal var mUserInfoModel = UserInfoModel()
    internal var isShowKick: Boolean = false
    internal var isFollow: Boolean = false
    internal var isFriend: Boolean = false

    var mPersonMoreOpView: PersonMoreOpView? = null

    internal val mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)

    internal var mOffset = 0
    internal var mHasMore = false
    internal var DEFAULT_CNT = 10

    internal var hasInitHeight = false
    internal var isAppBarCanScroll = true   // AppBarLayout是否可以滚动
    var lastVerticalOffset = Int.MAX_VALUE

    internal var mClickListener: PersonInfoDialog.PersonCardClickListener? = null

    private val mBackground1 = DrawableCreator.Builder()
            .setCornersRadius(U.getDisplayUtils().dip2px(16f).toFloat())
            .setSolidColor(Color.parseColor("#D0EFFF"))
            .build()

    private val mBackground2 = DrawableCreator.Builder()
            .setCornersRadius(0f, 0f, U.getDisplayUtils().dip2px(16f).toFloat(), U.getDisplayUtils().dip2px(16f).toFloat())
            .setSolidColor(Color.parseColor("#D0EFFF"))
            .build()

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

    init {
        initView()
        initData(userID, showKick, showInvite)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mUiHandler.removeCallbacksAndMessages(null)
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    fun setListener(listener: PersonInfoDialog.PersonCardClickListener) {
        this.mClickListener = listener
    }

    private fun initView() {
        View.inflate(context, R.layout.person_info_card_view_layout, this)
        initBaseContainInfo()
        initUserInfo()
        initToolBarArea()
        initPhotoArea()
        initFuncationArea()
    }

    private fun initData(userID: Int, showKick: Boolean, showInvite: Boolean) {
        mUserId = userID
        isShowKick = showKick

        // 多音和ai裁判
        if (mUserId == UserAccountManager.SYSTEM_GRAB_ID || mUserId == UserAccountManager.SYSTEM_RANK_AI) {
            isShowKick = false
            mMoreBtn.visibility = View.GONE
        }

        if (showInvite) {
            mInviteIv.visibility = View.VISIBLE
        } else {
            mInviteIv.visibility = View.GONE
        }

        // 自己卡片的处理
        if (mUserId.toLong() == MyUserInfoManager.getInstance().uid) {
            isShowKick = false
            mMoreBtn.visibility = View.GONE
            mToolbar.visibility = View.GONE
            mFunctionArea.visibility = View.GONE
        }

        if (mFunctionArea.visibility == View.VISIBLE) {
            // 重新设置mSmartRefresh的marginbottom 和背景
            mSmartRefresh.background = mBackground2
            val layoutParams = mSmartRefresh.layoutParams as RelativeLayout.LayoutParams
            layoutParams.setMargins(U.getDisplayUtils().dip2px(10f), 0, U.getDisplayUtils().dip2px(10f), U.getDisplayUtils().dip2px(70f))
            mSmartRefresh.layoutParams = layoutParams
        } else {
            mSmartRefresh.background = mBackground1
            val layoutParams = mSmartRefresh.layoutParams as RelativeLayout.LayoutParams
            layoutParams.setMargins(U.getDisplayUtils().dip2px(10f), 0, U.getDisplayUtils().dip2px(10f), U.getDisplayUtils().dip2px(10f))
            mSmartRefresh.layoutParams = layoutParams
        }

        getHomePage(mUserId)
        getPhotos(0)
    }

    private fun getHomePage(userId: Int) {
        ApiMethods.subscribe(mUserInfoServerApi.getHomePage(userId.toLong()), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val userInfoModel = JSON.parseObject(result.data!!.getString("userBaseInfo"), UserInfoModel::class.java)
                    //                    List<UserRankModel> userRankModels = JSON.parseArray(result.getData().getJSONObject("userRankInfo").getString("seqInfo"), UserRankModel.class);
                    val relationNumModes = JSON.parseArray(result.data!!.getJSONObject("userRelationCntInfo").getString("cnt"), RelationNumModel::class.java)
                    val userLevelModels = JSON.parseArray(result.data!!.getJSONObject("userScoreInfo").getString("userScore"), UserLevelModel::class.java)
                    //                    List<GameStatisModel> userGameStatisModels = JSON.parseArray(result.getData().getJSONObject("userGameStatisticsInfo").getString("statistic"), GameStatisModel.class);

                    val isFriend = result.data!!.getJSONObject("userMateInfo").getBooleanValue("isFriend")
                    val isFollow = result.data!!.getJSONObject("userMateInfo").getBooleanValue("isFollow")

                    val meiLiCntTotal = result.data!!.getIntValue("meiLiCntTotal")

                    if (isFollow) {
                        userInfoModel.isFollow = isFollow
                        userInfoModel.isFriend = isFriend
                        UserInfoManager.getInstance().insertUpdateDBAndCache(userInfoModel)
                    }
                    showUserInfo(userInfoModel)
                    showUserRelationNum(relationNumModes)
                    showUserLevel(userLevelModels)
                    showUserRelation(isFriend, isFollow)
                    showCharmsTag(meiLiCntTotal)
                }
            }
        }, mContext as BaseActivity)
    }

    @JvmOverloads
    internal fun getPhotos(offset: Int, callback: Callback<List<PhotoModel>>? = null) {
        ApiMethods.subscribe(mUserInfoServerApi.getPhotos(mUserId.toLong(), offset, DEFAULT_CNT), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult?) {
                mSmartRefresh.finishLoadMore()
                if (result != null && result.errno == 0) {
                    val list = JSON.parseArray(result.data!!.getString("pic"), PhotoModel::class.java)
                    val newOffset = result.data!!.getIntValue("offset")
                    val totalCount = result.data!!.getIntValue("totalCount")
                    if (offset == 0) {
                        addPhotos(list, newOffset, totalCount, true)
                    } else {
                        addPhotos(list, newOffset, totalCount, false)
                    }
                    callback?.onCallback(0, list)
                } else {
                    addPhotoFail()
                }
            }

            override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                addPhotoFail()
                super.onNetworkError(errorType)
            }
        })
    }

    private fun initBaseContainInfo() {
        mOutView = this.findViewById(R.id.out_view) as View
        mOutView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mClickListener != null) {
                    mClickListener!!.onClickOut()
                }
            }
        })

        mSmartRefresh = this.findViewById<View>(R.id.smart_refresh) as SmartRefreshLayout
        mCoordinator = this.findViewById<View>(R.id.coordinator) as CoordinatorLayout
        mAppbar = this.findViewById<View>(R.id.appbar) as AppBarLayout
        mToolbarLayout = this.findViewById<View>(R.id.toolbar_layout) as CollapsingToolbarLayout

        mSmartRefresh.setEnableRefresh(false)
        mSmartRefresh.setEnableLoadMore(true)
        mSmartRefresh.setEnableLoadMoreWhenContentNotFull(false)
        mSmartRefresh.setEnableOverScrollDrag(true)
        mSmartRefresh.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                getPhotos(mOffset)
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                mSmartRefresh.finishRefresh()
            }
        })

        mAppbar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (lastVerticalOffset != verticalOffset) {
                lastVerticalOffset = verticalOffset
                if (verticalOffset == 0) {
                    // 展开状态
                    if (mToolbar.visibility != View.GONE) {
                        mToolbar.visibility = View.GONE
                    }
                } else if (Math.abs(verticalOffset) >= appBarLayout.totalScrollRange - U.getDisplayUtils().dip2px(70f)) {
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
    }

    private fun initUserInfo() {
        mUserInfoArea = this.findViewById<View>(R.id.user_info_area) as ConstraintLayout
        mAvatarIv = this.findViewById<View>(R.id.avatar_iv) as SimpleDraweeView
        mMoreBtn = this.findViewById<View>(R.id.more_btn) as ExImageView
        mLevelView = this.findViewById<View>(R.id.level_view) as NormalLevelView2
        mNameTv = this.findViewById<View>(R.id.name_tv) as ExTextView
        mNameTv = this.findViewById<View>(R.id.name_tv) as ExTextView
        mSexIv = this.findViewById<View>(R.id.sex_iv) as ImageView
        mSignTv = this.findViewById<View>(R.id.sign_tv) as MarqueeTextView
        mFlowlayout = this.findViewById<View>(R.id.flowlayout) as TagFlowLayout

        mTagAdapter = object : TagAdapter<TagModel>(mTags) {
            override fun getView(parent: FlowLayout, position: Int, tagModel: TagModel): View {
                if (tagModel.type != CHARMS_TAG) {
                    val tv = LayoutInflater.from(context).inflate(R.layout.person_center_business_tag,
                            mFlowlayout, false) as ExTextView
                    tv.text = tagModel.content
                    return tv
                } else {
                    val tv = LayoutInflater.from(context).inflate(R.layout.person_card_charm_tag,
                            mFlowlayout, false) as ExTextView
                    tv.text = tagModel.content
                    return tv
                }
            }
        }
        mFlowlayout.adapter = mTagAdapter

        mAvatarIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mClickListener != null) {
                    mClickListener!!.onClickAvatar(mUserInfoModel.avatar)
                }
            }
        })

        /**
         * 这段代码不要删除，线上调试用的，可以在线拉这个人的日志调试问题
         */
        if (MyLog.isDebugLogOpen()) {
            mAvatarIv.setOnLongClickListener {
                val msgService = ModuleServiceManager.getInstance().msgService
                msgService?.sendSpecialDebugMessage(mUserId.toString(), 1, "请求上传日志", object : ICallback {
                    override fun onSucess(obj: Any) {
                        U.getToastUtil().showLong("请求成功,稍等看该用户是否有返回")
                    }

                    override fun onFailed(obj: Any, errcode: Int, message: String) {
                        U.getToastUtil().showLong("请求失败")
                    }
                })
                false
            }
        }

        mMoreBtn.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mPersonMoreOpView != null) {
                    mPersonMoreOpView!!.dismiss()
                }
                mPersonMoreOpView = PersonMoreOpView(context, mUserInfoModel.userId, false, isShowKick)
                mPersonMoreOpView!!.setListener(object : PersonMoreOpView.Listener {
                    override fun onClickRemark() {
                        if (mPersonMoreOpView != null) {
                            mPersonMoreOpView!!.dismiss()
                        }
                        if (mClickListener != null) {
                            mClickListener!!.onClickRemark(mUserInfoModel)
                        }
                    }

                    override fun onClickUnFollow() {

                    }

                    override fun onClickReport() {
                        if (mPersonMoreOpView != null) {
                            mPersonMoreOpView!!.dismiss()
                        }
                        if (mClickListener != null) {
                            mClickListener!!.onClickReport(mUserId)
                        }
                    }

                    override fun onClickKick() {
                        if (mPersonMoreOpView != null) {
                            mPersonMoreOpView!!.dismiss()
                        }
                        if (mClickListener != null) {
                            mClickListener!!.onClickKick(mUserInfoModel)
                        }
                    }

                    override fun onClickBlack(isInBlack: Boolean) {
                        if (mPersonMoreOpView != null) {
                            mPersonMoreOpView!!.dismiss()
                        }

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

    private fun initToolBarArea() {
        mToolbar = this.findViewById<View>(R.id.toolbar) as Toolbar
        mSrlAvatarIv = this.findViewById<View>(R.id.srl_avatar_iv) as SimpleDraweeView

        mToolbar.visibility = View.GONE
        mSrlAvatarIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mClickListener != null) {
                    mClickListener!!.onClickAvatar(mUserInfoModel.avatar)
                }
            }
        })
    }

    private fun initFuncationArea() {
        mFunctionArea = findViewById(R.id.function_area)
        mInviteIv = findViewById(R.id.invite_iv)
        mFollowIv = findViewById(R.id.follow_iv)

        mFollowIv.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View) {
                if (mClickListener != null) {
                    mClickListener!!.onClickFollow(mUserId, isFriend, isFollow)
                }
            }
        })

        mInviteIv.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View) {
                // 点击邀请唱聊
                StatisticsAdapter.recordCountEvent("cp", "invite2", null)
                if (mClickListener != null) {
                    mClickListener!!.onClickDoubleInvite(mUserInfoModel)
                }
            }
        })
    }

    private fun initPhotoArea() {
        mPhotoView = this.findViewById<View>(R.id.photo_view) as RecyclerView
        mEmptyMyPhoto = this.findViewById<View>(R.id.empty_my_photo) as ExTextView

        mPhotoView.isFocusableInTouchMode = false
        val gridLayoutManager = GridLayoutManager(context, 3)
        mPhotoView.layoutManager = gridLayoutManager
        mPhotoAdapter = PhotoAdapter(PhotoAdapter.TYPE_PERSON_CARD)
        mPhotoAdapter.mOnClickPhotoListener = { _, position, _ ->
            BigImageBrowseFragment.open(true, context as FragmentActivity, object : DefaultImageBrowserLoader<PhotoModel>() {
                override fun init() {

                }

                override fun load(imageBrowseView: ImageBrowseView, position: Int, item: PhotoModel) {
                    if (TextUtils.isEmpty(item.picPath)) {
                        imageBrowseView.load(item.localPath)
                    } else {
                        imageBrowseView.load(item.picPath)
                    }
                }

                override fun getInitCurrentItemPostion(): Int {
                    return position
                }

                override fun getInitList(): List<PhotoModel>? {
                    return mPhotoAdapter.dataList
                }

                override fun loadMore(backward: Boolean, position: Int, data: PhotoModel, callback: Callback<List<PhotoModel>>?) {
                    if (backward) {
                        // 向后加载
                        getPhotos(mPhotoAdapter.successNum, Callback { r, list ->
                            if (callback != null && list != null) {
                                callback.onCallback(0, list)
                            }
                        })
                    }
                }

                override fun hasMore(backward: Boolean, position: Int, data: PhotoModel): Boolean {
                    return if (backward) {
                        mHasMore
                    } else false
                }

                override fun hasMenu(): Boolean {
                    return false
                }
            })
        }
        mPhotoView.adapter = mPhotoAdapter
    }


    fun addPhotos(list: List<PhotoModel>?, offset: Int, totalCount: Int, clear: Boolean) {
        mSmartRefresh.finishLoadMore()
        this.mOffset = offset

        if (clear) {
            mPhotoAdapter.dataList!!.clear()
        }

        if (totalCount <= 0 && mUserId.toLong() == MyUserInfoManager.getInstance().uid) {
            mEmptyMyPhoto.visibility = View.VISIBLE
        }

        if (list != null && list.size != 0) {
            if (!hasInitHeight) {
                setAppBarCanScroll(true)
                val layoutParams = mSmartRefresh.layoutParams
                layoutParams.height = U.getDisplayUtils().dip2px(375f)
                mSmartRefresh.layoutParams = layoutParams
                hasInitHeight = true
            }
            mHasMore = true
            mSmartRefresh.setEnableLoadMore(true)
            mPhotoAdapter.dataList!!.addAll(list)
            mPhotoAdapter.notifyDataSetChanged()
        } else {
            mHasMore = false
            mSmartRefresh.setEnableLoadMore(false)//是否启用下加载功能
            if (mPhotoAdapter.dataList != null && mPhotoAdapter.dataList!!.size > 0) {
                // 没有更多了
            } else {
                // 没有数据
                setAppBarCanScroll(false)
            }
        }
    }

    fun addPhotoFail() {
        mSmartRefresh.finishLoadMore()
        if (mPhotoAdapter.dataList == null || mPhotoAdapter.dataList!!.size == 0) {
            setAppBarCanScroll(false)
        }
    }

    private fun setAppBarCanScroll(canScroll: Boolean) {
        if (isAppBarCanScroll == canScroll) {
            return
        }
        if (mAppbar != null && mAppbar!!.layoutParams != null) {
            val params = mAppbar!!.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = AppBarLayout.Behavior()
            behavior.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
                override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                    isAppBarCanScroll = canScroll
                    return canScroll
                }
            })
            params.behavior = behavior
            mAppbar!!.layoutParams = params
        }
    }

    fun showUserInfo(model: UserInfoModel?) {
        if (model != null) {
            mUserInfoModel = model
            AvatarUtils.loadAvatarByUrl(mAvatarIv,
                    AvatarUtils.newParamsBuilder(model.avatar)
                            .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                            .setBorderColor(Color.WHITE)
                            .setCircle(true)
                            .build())
            AvatarUtils.loadAvatarByUrl(mSrlAvatarIv,
                    AvatarUtils.newParamsBuilder(model.avatar)
                            .setBorderColor(Color.WHITE)
                            .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                            .setCircle(true)
                            .build())

            mNameTv.text = model.nicknameRemark
            mSignTv.text = model.signature
            if (model.sex == ESex.SX_MALE.value) {
                mSexIv.visibility = View.VISIBLE
                mSexIv.setBackgroundResource(R.drawable.sex_man_icon)
            } else if (model.sex == ESex.SX_FEMALE.value) {
                mSexIv.visibility = View.VISIBLE
                mSexIv.setBackgroundResource(R.drawable.sex_woman_icon)
            } else {
                mSexIv.visibility = View.GONE
            }

            if (model.location != null && !TextUtils.isEmpty(model.location.province)) {
                mHashMap.put(LOCATION_TAG, model.location.province)
            } else {
                mHashMap.put(LOCATION_TAG, "火星")
            }

            refreshTag()
        }
    }


    private fun showUserRelationNum(relationNumModes: List<RelationNumModel>?) {
        var fansNum = 0
        if (relationNumModes != null && relationNumModes.size > 0) {
            for (mode in relationNumModes) {
                if (mode.relation == UserInfoManager.RELATION.FANS.value) {
                    fansNum = mode.cnt
                }
            }
        }

        mHashMap.put(FANS_NUM_TAG, "粉丝 " + StringFromatUtils.formatTenThousand(fansNum))

        refreshTag()
    }

    private fun showCharmsTag(meiLiCntTotal: Int) {
        mHashMap.put(CHARMS_TAG, "魅力 " + StringFromatUtils.formatMillion(meiLiCntTotal))

        refreshTag()
    }

    fun showUserLevel(list: List<UserLevelModel>) {
        var mainRank = 0
        var subRank = 0
        for (userLevelModel in list) {
            if (userLevelModel.type == UserLevelModel.RANKING_TYPE) {
                mainRank = userLevelModel.score
            } else if (userLevelModel.type == UserLevelModel.SUB_RANKING_TYPE) {
                subRank = userLevelModel.score
            }
        }

        mLevelView.bindData(mainRank, subRank)
    }

    fun showUserRelation(isFriend: Boolean, isFollow: Boolean) {
        this.isFollow = isFollow
        this.isFriend = isFriend

        refreshFollow()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RelationChangeEvent) {
        if (event.useId == mUserId) {
            isFollow = event.isFollow
            isFriend = event.isFriend

            refreshFollow()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: FollowNotifyEvent) {
        if (event.mUserInfoModel != null && event.mUserInfoModel.userId == mUserId) {
            isFollow = event.mUserInfoModel.isFollow
            isFriend = event.mUserInfoModel.isFriend

            refreshFollow()
        }
    }

    // TODO: 2019/4/14 在卡片内，不提供取关功能
    private fun refreshFollow() {
        mUserInfoModel.isFollow = isFollow
        mUserInfoModel.isFriend = isFriend
        if (isFriend) {
            mFollowIv.text = "互关"
            mFollowIv.isClickable = false
            mFollowIv.background = mFollowDrawable
        } else if (isFollow) {
            mFollowIv.text = "已关注"
            mFollowIv.isClickable = false
            mFollowIv.background = mFollowDrawable
        } else {
            mFollowIv.text = "关注Ta"
            mFollowIv.isClickable = true
            mFollowIv.background = mUnFollowDrawable
        }
    }


    private fun refreshTag() {
        mTags.clear()
        if (mHashMap != null) {

            if (!TextUtils.isEmpty(mHashMap[CHARMS_TAG])) {
                mTags.add(TagModel(CHARMS_TAG, mHashMap[CHARMS_TAG]))
            }

            if (!TextUtils.isEmpty(mHashMap[FANS_NUM_TAG])) {
                mTags.add(TagModel(FANS_NUM_TAG, mHashMap[FANS_NUM_TAG]))
            }

            if (!TextUtils.isEmpty(mHashMap[LOCATION_TAG])) {
                mTags.add(TagModel(LOCATION_TAG, mHashMap[LOCATION_TAG]))
            }

        }
        mTagAdapter.setTagDatas(mTags)
        mTagAdapter.notifyDataChanged()
    }

    companion object {

        private val CHARMS_TAG = 1
        private val LOCATION_TAG = 2           //城市标签
        private val FANS_NUM_TAG = 3      //粉丝数标签
    }
}
