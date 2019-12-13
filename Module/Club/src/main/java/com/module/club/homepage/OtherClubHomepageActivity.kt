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
import com.common.base.BaseActivity
import com.common.core.view.setDebounceViewClickListener
import com.common.utils.U
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.person.view.PersonTagView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.RouterConstants
import com.module.club.R
import com.module.club.homepage.view.ClubMemberView
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshHeader
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.SimpleMultiPurposeListener
import kotlin.math.abs

@Route(path = RouterConstants.ACTIVITY_OTHER_HOMEPAGE_CLUB)
class OtherClubHomepageActivity : BaseActivity() {
    private var imageBg: SimpleDraweeView? = null
    private var smartRefresh: SmartRefreshLayout? = null
    private var container: LinearLayout? = null
    private var recyclerView: RecyclerView? = null //换成一个view来做
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
    private var moreBtn: ExImageView? = null
    private var applyEnterTv: ExTextView? = null

    private var lastVerticalOffset = Int.MAX_VALUE
    private var scrollDivider = U.getDisplayUtils().dip2px(150f)  // 滑到分界线的时候

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.club_other_homepage_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        imageBg = findViewById(R.id.image_bg)
        smartRefresh = findViewById(R.id.smart_refresh)
        container = findViewById(R.id.container)
        recyclerView = findViewById(R.id.recycler_view)
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
        moreBtn = findViewById(R.id.more_btn)
        applyEnterTv = findViewById(R.id.apply_enter_tv)

        adjustNotchPhone()

        ivBack?.setDebounceViewClickListener { finish() }

        moreBtn?.setDebounceViewClickListener {
            //todo 待补全
        }

        smartRefresh?.apply {
            setEnableRefresh(true)
            setEnableLoadMore(true)
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(true)
            setHeaderMaxDragRate(1.5f)

            setOnMultiPurposeListener(object : SimpleMultiPurposeListener() {
                internal var lastScale = 0f

                override fun onRefresh(refreshLayout: RefreshLayout) {

                }

                override fun onLoadMore(refreshLayout: RefreshLayout) {

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

    private fun adjustNotchPhone() {
        if (U.getDeviceUtils().hasNotch(this@OtherClubHomepageActivity)) {
            val layoutParams = clubAvatarSdv?.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.topMargin = layoutParams.topMargin + U.getStatusBarUtil().getStatusBarHeight(this@OtherClubHomepageActivity)
            clubAvatarSdv?.layoutParams = layoutParams
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }
}