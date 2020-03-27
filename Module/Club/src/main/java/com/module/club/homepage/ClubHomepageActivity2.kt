package com.module.club.homepage

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.model.ClubMemberInfo
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExImageView
import com.common.view.ex.ExLinearLayout
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.common.view.titlebar.CommonTitleBar
import com.common.view.viewpager.NestViewPager
import com.common.view.viewpager.SlidingTabLayout
import com.component.person.utils.StringFromatUtils
import com.component.person.view.RequestCallBack
import com.facebook.drawee.view.SimpleDraweeView
import com.module.RouterConstants
import com.module.club.ClubServerApi
import com.module.club.R
import com.module.club.homepage.view.*
import com.module.club.manage.setting.ClubManageActivity
import com.respicker.ResPicker
import com.respicker.activity.ResPickerActivity
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshHeader
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.SimpleMultiPurposeListener
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody

class ClubHomepageActivity2 : BaseActivity(), RequestCallBack {

    lateinit var title: CommonTitleBar
    lateinit var imageBg: ImageView
    lateinit var bottomBg: ImageView
    lateinit var smartRefresh: SmartRefreshLayout
    lateinit var classicsHeader: ClassicsHeader

    lateinit var container: ExLinearLayout
    lateinit var clubTab: SlidingTabLayout
    lateinit var clubVp: NestViewPager

    lateinit var appbar: AppBarLayout
    lateinit var contentLayout: CollapsingToolbarLayout
    lateinit var userInfoArea: ConstraintLayout
    lateinit var clubLogoSdv: SimpleDraweeView
    lateinit var clubHotTv: ExTextView
    lateinit var clubNameTv: TextView
    lateinit var clubIdTv: TextView
    lateinit var clubLevelTv: ExTextView
    lateinit var clubContriTv: ExTextView

    lateinit var applyTv: TextView

    lateinit var toolbar: Toolbar
    lateinit var toolbarLayout: ConstraintLayout
    lateinit var srlTitleTv: ExTextView

    lateinit var ivBack: ExImageView
    lateinit var moreBtn: ExImageView

    private val clubServerApi = ApiManager.getInstance().createService(ClubServerApi::class.java)
    private var clubMemberInfo: ClubMemberInfo? = null
    private var clubID: Int = 0
    private var isMyClub = false
    private var hasApplied = false

    private var appbarListener: AppBarLayout.OnOffsetChangedListener? = null
    private var srollDivider = U.getDisplayUtils().dip2px(20f)  // 滑到分界线的时候
    private var lastVerticalOffset = Integer.MAX_VALUE

    private var clubTabAdapter: PagerAdapter? = null
    private var clubIntroView: ClubIntroView? = null
    private var clubDynamicView: ClubDynamicView? = null
    private var clubPhotoWallView: ClubPhotoWallView? = null
    private var clubWorksView: ClubWorksView? = null
    private var clubRightOpView: ClubRightOpView? = null

    override fun useEventBus(): Boolean {
        return false
    }

    override fun canSlide(): Boolean {
        return false
    }

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.club_home_page_activity2_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        U.getStatusBarUtil().setTransparentBar(this, false)
        clubMemberInfo = intent.getSerializableExtra("clubMemberInfo") as ClubMemberInfo?
        clubID = clubMemberInfo?.club?.clubID ?: 0
        isMyClub = clubMemberInfo?.isMyClub() ?: false
        if (clubMemberInfo == null) {
            finish()
        }

        initBaseContainArea()
        initClubInfoArea()
        initSettingArea()

        initContentView()
        initAppBarScroll()

        if (isMyClub) {
            clubRightOpView?.show()
            clubRightOpView?.bindData(clubMemberInfo)
            clubRightOpView?.setPhotoClickListener {
                clubPhotoWallView?.goAddPhotoFragment()
            }
            moreBtn.visibility = View.VISIBLE
        } else {
            moreBtn.visibility = View.GONE
            hasAppliedJoin()
        }

        refreshClubInfo()
    }

    private fun hasAppliedJoin() {
        launch {
            val result = subscribe(RequestControl("hasAppliedJoin", ControlType.CancelThis)) {
                clubServerApi.hasAppliedJoin(clubID)
            }
            if (result.errno == 0) {
                hasApplied = result.data.getBooleanValue("yes")
                refreshApplyStatus()
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    private fun cancelJoinApply() {
        launch {
            val map = mapOf(
                    "clubID" to clubID
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("cancelJoinApply", ControlType.CancelThis)) {
                clubServerApi.cancelApplyJoin(body)
            }
            if (result.errno == 0) {
                hasApplied = false
                refreshApplyStatus()
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    private fun applyJoinClub() {
        launch {
            val map = mapOf(
                    "clubID" to clubID,
                    "text" to ""
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("applyJoinClub", ControlType.CancelThis)) {
                clubServerApi.applyJoinClub(body)
            }
            if (result.errno == 0) {
                hasApplied = true
                refreshApplyStatus()
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    private fun refreshClubInfo() {
        val clubInfo = clubMemberInfo?.club
        AvatarUtils.loadAvatarByUrl(clubLogoSdv, AvatarUtils.newParamsBuilder(clubInfo?.logo)
                .setCircle(false)
                .setCornerRadius(8.dp().toFloat())
                .build())
        clubNameTv.text = clubInfo?.name
        clubHotTv.text = StringFromatUtils.formatTenThousand(clubInfo?.hot ?: 0)
        clubIdTv.text = "ID: ${clubInfo?.clubID}"
        clubLevelTv.text = clubInfo?.levelDesc
        srlTitleTv.text = clubInfo?.name
    }

    private fun refreshApplyStatus() {
        applyTv.visibility = View.VISIBLE
        if (hasApplied) {
            applyTv.text = "取消申请"
            applyTv.background = DrawableCreator.Builder()
                    .setSolidColor(U.getColor(R.color.black_trans_10))
                    .setStrokeColor(Color.WHITE)
                    .setStrokeWidth(1.dp().toFloat())
                    .setCornersRadius(21.dp().toFloat())
                    .build()
        } else {
            applyTv.text = "申请加入"
            applyTv.background = DrawableCreator.Builder()
                    .setSolidColor(Color.parseColor("#FF9B9B"))
                    .setCornersRadius(21.dp().toFloat())
                    .build()
        }
    }

    private fun initAppBarScroll() {
        if (appbarListener == null) {
            appbarListener = AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                imageBg.translationY = verticalOffset.toFloat()
                if (verticalOffset < 0) {
                    bottomBg.setTranslationY(verticalOffset.toFloat())
                }
                if (lastVerticalOffset != verticalOffset) {
                    lastVerticalOffset = verticalOffset

                    val scrollLimit = appBarLayout.totalScrollRange  // 总的滑动长度
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

                        if (Math.abs(verticalOffset) >= scrollLimit) {
                            srlTitleTv.alpha = 1f
                        } else {
                            srlTitleTv.alpha = (Math.abs(verticalOffset) - srollDivider).toFloat() / (scrollLimit - srollDivider).toFloat()
                        }
                    } else {
                        if (toolbar.visibility != View.GONE) {
                            toolbar.visibility = View.GONE
                            toolbarLayout.visibility = View.GONE
                        }
                    }
                }
            }
        }
        appbar.removeOnOffsetChangedListener(appbarListener)
        lastVerticalOffset = Integer.MAX_VALUE
        appbar.addOnOffsetChangedListener(appbarListener)
    }

    private fun initBaseContainArea() {
        smartRefresh = findViewById(R.id.smart_refresh)
        classicsHeader = findViewById(R.id.classics_header)
        title = findViewById(R.id.title)

        imageBg = findViewById(R.id.image_bg)
        bottomBg = findViewById(R.id.bottom_bg)

        smartRefresh.setEnableRefresh(true)
        smartRefresh.setEnableLoadMore(true)
        smartRefresh.setEnableLoadMoreWhenContentNotFull(false)
        smartRefresh.setEnableOverScrollDrag(true)
        smartRefresh.setHeaderMaxDragRate(1.5f)
        smartRefresh.setOnMultiPurposeListener(object : SimpleMultiPurposeListener() {

            internal var lastScale = 0f

            override fun onRefresh(refreshLayout: RefreshLayout) {
                super.onRefresh(refreshLayout)
                // todo 要不要更新家族信息
                viewSelected(clubVp.currentItem, true)
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                super.onLoadMore(refreshLayout)
                loadMoreData(clubVp.currentItem)
            }

            override fun onHeaderMoving(header: RefreshHeader?, isDragging: Boolean, percent: Float, offset: Int, headerHeight: Int, maxDragHeight: Int) {
                super.onHeaderMoving(header, isDragging, percent, offset, headerHeight, maxDragHeight)
                val scale = offset.toFloat() / U.getDisplayUtils().dip2px(300f).toFloat() + 1
                if (Math.abs(scale - lastScale) >= 0.01) {
                    lastScale = scale
                    imageBg.setScaleX(scale)
                    imageBg.setScaleY(scale)
                }
                if (offset > 0) {
                    bottomBg.setTranslationY(offset.toFloat())
                }
            }
        })
    }

    private fun initClubInfoArea() {
        appbar = findViewById(R.id.appbar)
        contentLayout = findViewById(R.id.content_layout)
        userInfoArea = findViewById(R.id.user_info_area)
        clubLogoSdv = findViewById(R.id.club_logo_sdv)
        clubHotTv = findViewById(R.id.club_hot_tv)
        clubNameTv = findViewById(R.id.club_name_tv)
        clubIdTv = findViewById(R.id.club_id_tv)
        clubLevelTv = findViewById(R.id.club_level_tv)
        clubContriTv = findViewById(R.id.club_contri_tv)

        applyTv = findViewById(R.id.apply_tv)

        toolbar = findViewById(R.id.toolbar)
        toolbarLayout = findViewById(R.id.toolbar_layout)
        srlTitleTv = findViewById(R.id.srl_title_tv)


        clubContriTv.setDebounceViewClickListener {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_LIST_CLUB_RANK)
                    .withInt("clubID", clubMemberInfo?.club?.clubID ?: 0)
                    .navigation()
        }

        applyTv.setDebounceViewClickListener {
            if (hasApplied) {
                cancelJoinApply()
            } else {
                applyJoinClub()
            }
        }
    }

    private fun initSettingArea() {
        ivBack = findViewById(R.id.iv_back)
        moreBtn = findViewById(R.id.more_btn)

        ivBack.setDebounceViewClickListener { finish() }

        moreBtn.setDebounceViewClickListener {
            // 跳到设置页面
            val intent = Intent(this, ClubManageActivity::class.java)
            intent.putExtra("clubMemberInfo", clubMemberInfo)
            startActivity(intent)
        }
    }

    private fun initContentView() {
        container = findViewById(R.id.container)
        clubTab = findViewById(R.id.club_tab)
        clubVp = findViewById(R.id.club_vp)

        clubRightOpView = ClubRightOpView(findViewById(R.id.club_home_page_right_op))
        clubTab.setCustomTabView(R.layout.club_tab_view, R.id.tab_tv)
        clubTab.setSelectedIndicatorColors(Color.parseColor("#FF9B9B"))
        clubTab.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_NONE)
        clubTab.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NONE)
        clubTab.setIndicatorWidth(U.getDisplayUtils().dip2px(67f))
        clubTab.setIndicatorBottomMargin(U.getDisplayUtils().dip2px(15f))
        clubTab.setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(24f).toFloat())
        clubTab.setIndicatorCornorRadius(U.getDisplayUtils().dip2px(12f).toFloat())
        clubTabAdapter = object : PagerAdapter() {
            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                when (position) {
                    0 -> {
                        // 家族简介
                        if (clubIntroView == null) {
                            clubIntroView = ClubIntroView(this@ClubHomepageActivity2)
                        }
                        clubIntroView?.setData(clubMemberInfo)
                        clubIntroView?.loadData(false) {}
                        if (container.indexOfChild(clubIntroView) == -1) {
                            container.addView(clubIntroView)
                        }
                        return clubIntroView!!
                    }
                    1 -> {
                        // 家族动态
                        if (clubDynamicView == null) {
                            clubDynamicView = ClubDynamicView(this@ClubHomepageActivity2)
                        }
                        clubDynamicView?.clubMemberInfo = clubMemberInfo
                        if (container.indexOfChild(clubDynamicView) == -1) {
                            container.addView(clubDynamicView)
                        }
                        return clubDynamicView!!
                    }
                    2 -> {
                        // 家族相册
                        if (clubPhotoWallView == null) {
                            clubPhotoWallView = ClubPhotoWallView(this@ClubHomepageActivity2, this@ClubHomepageActivity2, clubMemberInfo)
                        }
                        if (container.indexOfChild(clubPhotoWallView) == -1) {
                            container.addView(clubPhotoWallView)
                        }
                        return clubPhotoWallView!!
                    }
                    3 -> {
                        // 家族作品
                        if (clubWorksView == null) {
                            clubWorksView = ClubWorksView(this@ClubHomepageActivity2)
                        }
                        clubWorksView?.clubMemberInfo = clubMemberInfo
                        if (container.indexOfChild(clubWorksView) == -1) {
                            container.addView(clubWorksView)
                        }
                        return clubWorksView!!
                    }
                    else -> return super.instantiateItem(container, position)
                }
            }

            override fun getCount(): Int {
                return 4
            }

            override fun getItemPosition(`object`: Any): Int {
                return PagerAdapter.POSITION_NONE
            }

            override fun getPageTitle(position: Int): CharSequence? {
                if (position == 0) {
                    return "简介"
                } else if (position == 1) {
                    return "动态"
                } else if (position == 2) {
                    return "相册"
                } else if (position == 3) {
                    return "作品"
                }
                return ""
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view === `object`
            }
        }
        clubVp.adapter = clubTabAdapter
        clubTab.setViewPager(clubVp)
        clubTabAdapter?.notifyDataSetChanged()

        clubVp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                viewSelected(position, false)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }

    override fun onRequestSucess(hasMore: Boolean) {
//        mSmartRefresh.finishRefresh()
//        mSmartRefresh.setEnableLoadMore(hasMore)
//        mSmartRefresh.finishLoadMore()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == ResPickerActivity.REQ_CODE_RES_PICK) {
            val imageItems = ResPicker.getInstance().selectedImageList
            if (clubPhotoWallView != null) {
                clubPhotoWallView?.uploadPhotoList(imageItems)
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun viewSelected(position: Int, flag: Boolean) {
        clubDynamicView?.stopPlay()
        clubWorksView?.stopPlay()
        when (position) {
            0 -> clubIntroView?.loadData(flag) {
                finishRefreshAndLoadMore()
            }
            1 -> clubDynamicView?.loadData(flag) {
                finishRefreshAndLoadMore()
            }
            2 -> clubPhotoWallView?.loadData(flag) {
                finishRefreshAndLoadMore()
            }
            3 -> clubWorksView?.loadData(flag) {
                finishRefreshAndLoadMore()
            }
        }
    }

    private fun loadMoreData(position: Int) {
        when (position) {
            0 -> clubIntroView?.loadMoreData {
                finishRefreshAndLoadMore()
            }
            1 -> clubDynamicView?.loadMoreData {
                finishRefreshAndLoadMore()
            }
            2 -> clubPhotoWallView?.loadMoreData {
                finishRefreshAndLoadMore()
            }
            3 -> clubWorksView?.loadMoreData {
                finishRefreshAndLoadMore()
            }
        }
    }

    private fun finishRefreshAndLoadMore() {
        smartRefresh.finishRefresh()
        smartRefresh.finishLoadMore()
    }

    override fun destroy() {
        super.destroy()
        clubIntroView?.destroy()
        clubDynamicView?.destroy()
        clubPhotoWallView?.destroy()
        clubWorksView?.destroy()
    }

    override fun onResume() {
        super.onResume()
        //返回页面更新未读消息数
        clubRightOpView?.updateUnreadMsgCount()
    }
}