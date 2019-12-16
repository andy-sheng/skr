package com.module.club.homepage

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.AppBarLayout
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.model.ClubInfo
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.person.view.PersonTagView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.RouterConstants
import com.module.club.ClubServerApi
import com.module.club.R
import com.module.club.homepage.room.ClubPartyRoomView
import com.module.club.homepage.view.ClubMemberView
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshHeader
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.SimpleMultiPurposeListener
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import kotlin.math.abs

@Route(path = RouterConstants.ACTIVITY_OTHER_HOMEPAGE_CLUB)
class OtherClubHomepageActivity : BaseActivity() {
    private var imageBg: SimpleDraweeView? = null
    private var smartRefresh: SmartRefreshLayout? = null
    private var container: LinearLayout? = null

    private var clubRoomView: ClubPartyRoomView? = null

    private var appbar: AppBarLayout? = null
    private var clubAvatarSdv: SimpleDraweeView? = null
    private var clubNameTv: TextView? = null
    private var clubRelationTv: TextView? = null
    private var clubTagView: PersonTagView? = null
    private var memberView: ClubMemberView? = null  //换成一个view来做

    private var clubIntroduceTitle: TextView? = null
    private var clubIntroduceContent: ExTextView? = null

    private var toolbar: Toolbar? = null
    private var toolbarLayout: RelativeLayout? = null
    private var srlNameTv: TextView? = null

    private var ivBack: ExImageView? = null
    private var applyEnterTv: ExTextView? = null

    private var lastVerticalOffset = Int.MAX_VALUE
    private var scrollDivider = U.getDisplayUtils().dip2px(150f)  // 滑到分界线的时候

    private val clubServerApi = ApiManager.getInstance().createService(ClubServerApi::class.java)
    private var clubID: Int = 0
    private var clubInfo: ClubInfo? = null

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.club_other_homepage_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        clubID = intent.getIntExtra("clubID", 0)
        if (clubID == 0) {
            finish()
        }

        imageBg = findViewById(R.id.image_bg)
        smartRefresh = findViewById(R.id.smart_refresh)
        container = findViewById(R.id.container)
        clubRoomView = findViewById(R.id.club_room_view)
        appbar = findViewById(R.id.appbar)
        clubAvatarSdv = findViewById(R.id.club_avatar_sdv)
        clubNameTv = findViewById(R.id.club_name_tv)
        clubRelationTv = findViewById(R.id.club_relation_tv)
        clubTagView = findViewById(R.id.club_tag_view)
        memberView = findViewById(R.id.member_view)
        clubIntroduceTitle = findViewById(R.id.club_introduce_title)
        clubIntroduceContent = findViewById(R.id.club_introduce_content)
        toolbar = findViewById(R.id.toolbar)
        toolbarLayout = findViewById(R.id.toolbar_layout)
        srlNameTv = findViewById(R.id.srl_name_tv)
        ivBack = findViewById(R.id.iv_back)
        applyEnterTv = findViewById(R.id.apply_enter_tv)

        adjustNotchPhone()
        initTopArea()
        initMemberArea()
        initRoomArea()
        initToolBarScroll()
        initApplyEnter()

        getHomePage()
        clubRoomView?.initData()
        memberView?.initData()

    }

    private fun adjustNotchPhone() {
        if (U.getDeviceUtils().hasNotch(this@OtherClubHomepageActivity)) {
            val layoutParams = clubAvatarSdv?.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.topMargin = layoutParams.topMargin + U.getStatusBarUtil().getStatusBarHeight(this@OtherClubHomepageActivity)
            clubAvatarSdv?.layoutParams = layoutParams
        }
    }

    private fun initTopArea() {
        ivBack?.setDebounceViewClickListener { finish() }
    }

    private fun initMemberArea() {
        memberView?.clubID = clubID
    }

    private fun initRoomArea() {
        clubRoomView?.clubID = clubID
        smartRefresh?.apply {
            setEnableRefresh(true)
            setEnableLoadMore(true)
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(true)
            setHeaderMaxDragRate(1.5f)

            setOnMultiPurposeListener(object : SimpleMultiPurposeListener() {
                internal var lastScale = 0f

                override fun onRefresh(refreshLayout: RefreshLayout) {
                    smartRefresh?.setEnableLoadMore(true)
                    getHomePage()
                    clubRoomView?.initData()
                    memberView?.initData()
                }

                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    clubRoomView?.loadMoreData {
                        finishRereshAndLoadMore()
                        smartRefresh?.setEnableLoadMore(it)
                    }
                }

                override fun onHeaderMoving(header: RefreshHeader?, isDragging: Boolean, percent: Float, offset: Int, headerHeight: Int, maxDragHeight: Int) {
                    super.onHeaderMoving(header, isDragging, percent, offset, headerHeight, maxDragHeight)
                    val scale = offset.toFloat() / U.getDisplayUtils().dip2px(300f).toFloat() + 1
                    if (abs(scale - lastScale) >= 0.01) {
                        lastScale = scale
                        imageBg?.scaleX = scale
                        imageBg?.scaleY = scale
                    }
                }
            })

        }
    }

    private fun initToolBarScroll() {
        appbar?.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            // TODO: 2019-06-23 也可以加效果，看产品怎么说
            imageBg?.translationY = verticalOffset.toFloat()
            if (lastVerticalOffset != verticalOffset) {
                lastVerticalOffset = verticalOffset
                if (verticalOffset == 0) {
                    // 展开状态
                    if (toolbar?.visibility != View.GONE) {
                        toolbar?.visibility = View.GONE
                        toolbarLayout?.visibility = View.GONE
                    }
                } else if (abs(verticalOffset) >= scrollDivider) {
                    // 完全收缩状态
                    if (toolbar?.visibility != View.VISIBLE) {
                        toolbar?.visibility = View.VISIBLE
                        toolbarLayout?.visibility = View.VISIBLE
                    }

                    if (abs(verticalOffset) >= appBarLayout.totalScrollRange) {
                        srlNameTv?.alpha = 1f
                    } else {
                        srlNameTv?.alpha = (abs(verticalOffset) - scrollDivider).toFloat() / (appBarLayout.totalScrollRange - scrollDivider).toFloat()
                    }
                } else {
                    // TODO: 2019/4/8 过程中，可以加动画，先直接显示
                    if (toolbar?.visibility != View.GONE) {
                        toolbar?.visibility = View.GONE
                        toolbarLayout?.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun initApplyEnter() {
        applyEnterTv?.setDebounceViewClickListener {
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
                    U.getToastUtil().showShort("申请成功")
                } else {
                    U.getToastUtil().showShort(result.errmsg)
                }
            }
        }
    }


    private fun getHomePage() {
        launch {
            val result = subscribe(RequestControl("getHomePage", ControlType.CancelThis)) {
                clubServerApi.getClubHomePageDetail(clubID)
            }
            if (result.errno == 0) {
                clubInfo = JSON.parseObject(result.data.getString("info"), ClubInfo::class.java)
                refreshClubUI()
            }
            finishRereshAndLoadMore()
        }
    }

    private fun refreshClubUI() {
        AvatarUtils.loadAvatarByUrl(imageBg, AvatarUtils.newParamsBuilder(clubInfo?.logo)
                .setCircle(false)
                .setBlur(true)
                .build())
        AvatarUtils.loadAvatarByUrl(clubAvatarSdv, AvatarUtils.newParamsBuilder(clubInfo?.logo)
                .setCircle(false)
                .setCornerRadius(8.dp().toFloat())
                .build())
        clubNameTv?.text = clubInfo?.name
        // todo 缺一个联系方式
        clubTagView = findViewById(R.id.club_tag_view)

        clubIntroduceContent?.text = clubInfo?.desc
        srlNameTv?.text = clubInfo?.name
    }

    private fun finishRereshAndLoadMore() {
        smartRefresh?.finishLoadMore()
        smartRefresh?.finishRefresh()
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun canSlide(): Boolean {
        return false
    }
}