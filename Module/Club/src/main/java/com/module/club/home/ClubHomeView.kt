package com.module.club.home

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.ClubInfo
import com.common.flutter.boost.FlutterBoostController
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.dialog.view.TipsDialogView
import com.module.RouterConstants
import com.module.club.ClubServerApi
import com.module.club.IClubHomeView
import com.module.club.IClubModuleService
import com.module.club.R
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import com.zq.live.proto.Notification.ClubInfoChangeMsg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ClubHomeView(context: Context) : ConstraintLayout(context), IClubHomeView, CoroutineScope by MainScope() {
    private val refreshLayout: SmartRefreshLayout
    private val recyclerView: RecyclerView
    private val adapter: ClubHomeAdapter

    private var clubServerApi = ApiManager.getInstance().createService(ClubServerApi::class.java)
    private var offset = 0
    private val cnt = 15
    private var hasMore = true

    private var lastUpdateBannerTimeMs = 0L
    private var lastUpdateClubListTimeMs = 0L

    private var mTipsDialogView: TipsDialogView? = null

    init {
        View.inflate(context, R.layout.club_home_view_layout, this)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        refreshLayout = this.findViewById(R.id.refreshLayout)
        recyclerView = this.findViewById(R.id.recycler_view)

        refreshLayout.setEnableRefresh(true)
        refreshLayout.setEnableLoadMore(hasMore)
        refreshLayout.setEnableLoadMoreWhenContentNotFull(false)
        refreshLayout.setEnableOverScrollDrag(false)
        refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                getClubListData(offset, false)
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                initData(true)
            }
        })

        adapter = ClubHomeAdapter(object : ClubHomeClickListener {
            override fun onClickSearchClub() {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_SEARCH_CLUB)
                        .navigation()
            }

            override fun onClickRankClub() {
                FlutterBoostController.openFlutterPage(getContext(),"FamilyRankPage",null)
            }

            override fun onClickCreatClub() {
                createClub()
            }

            override fun onClickClubInfo(clubInfo: ClubInfo?) {
                clubInfo?.let {
                    val clubServices = ARouter.getInstance().build(RouterConstants.SERVICE_CLUB).navigation() as IClubModuleService
                    clubServices.tryGoClubHomePage(it.clubID)
                }
            }

        })
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        adapter.updateFunction(MyUserInfoManager.myUserInfo?.clubInfo?.club)
    }

    private fun createClub() {
        launch {
            val result = subscribe(RequestControl("checkCreatePermission", ControlType.CancelThis)) {
                clubServerApi.checkCreatePermission()
            }
            if (result.errno == 0) {
                // 可以创建
                ARouter.getInstance().build(RouterConstants.ACTIVITY_CREATE_CLUB)
                        .withString("from", "create")
                        .navigation()
            } else {
                if (result.errno == 8440202) {
                    mTipsDialogView?.dismiss(false)
                    mTipsDialogView = TipsDialogView.Builder(context)
                            .setMessageTip("开通VIP特权，立即获得家族创建权限")
                            .setConfirmTip("立即开通")
                            .setCancelTip("取消")
                            .setConfirmBtnClickListener {
                                mTipsDialogView?.dismiss(false)
                                ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                                        .withString("url", ApiManager.getInstance().findRealUrlByChannel("https://app.inframe.mobi/user/newVip?title=1"))
                                        .greenChannel().navigation()
                            }
                            .setCancelBtnClickListener {
                                mTipsDialogView?.dismiss()
                            }
                            .build()
                    mTipsDialogView?.showByDialog()
                } else {
                    U.getToastUtil().showShort(result.errmsg)
                }
            }
        }
    }

    override fun initData(flag: Boolean) {
        if (flag) {
            getClubListData(0, true)
        } else {
            val now = System.currentTimeMillis()
            if ((now - lastUpdateClubListTimeMs) > 10 * 1000 * 60) {
                // 超过10分钟再去更新吧
                getClubListData(0, true)
            }
        }

    }

    override fun autoToHead() {
        recyclerView.smoothScrollToPosition(0)
    }

    override fun stopTimer() {
        //todo doNothing
    }

    override fun destory() {
        mTipsDialogView?.dismiss(false)
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        cancel()
    }

    private fun getBannerInfo() {

    }

    private fun getClubListData(off: Int, isClean: Boolean) {
        launch {
            val result = subscribe(RequestControl("loadClubListData", ControlType.CancelThis)) {
                clubServerApi.getRecommendClubList(off, cnt)
            }
            if (result.errno == 0) {
                lastUpdateClubListTimeMs = System.currentTimeMillis()
                offset = result.data.getIntValue("offset")
                hasMore = result.data.getBooleanValue("hasMore")
                val list = JSON.parseArray(result.data.getString("items"), ClubInfo::class.java)
                adapter.addClubList(list, isClean)
            }

            refreshLayout.setEnableLoadMore(hasMore)
            refreshLayout.finishRefresh()
            refreshLayout.finishLoadMore()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 1)
    fun onEvent(event: ClubInfoChangeMsg) {
        // 我自己家族信息的改变
        adapter.updateFunction(MyUserInfoManager.myUserInfo?.clubInfo?.club)
    }

}