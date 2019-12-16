package com.module.club.apply

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.titlebar.CommonTitleBar
import com.module.RouterConstants
import com.module.club.ClubServerApi
import com.module.club.R
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import com.zq.live.proto.Common.EClubMemberRoleType
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody


@Route(path = RouterConstants.ACTIVITY_LIST_APPLY_CLUB)
class ClubApplyListActivity : BaseActivity() {

    lateinit var titlebar: CommonTitleBar
    lateinit var refreshLayout: SmartRefreshLayout
    lateinit var contentRv: RecyclerView
    lateinit var adapter: ClubApplyListAdapter

    private val clubServerApi = ApiManager.getInstance().createService(ClubServerApi::class.java)
    private var offset = 0
    private var hasMore = true
    private val cnt = 15

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.club_apply_list_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        titlebar = findViewById(R.id.titlebar)
        refreshLayout = findViewById(R.id.refreshLayout)
        contentRv = findViewById(R.id.content_rv)

        var hasManage = false
        if (MyUserInfoManager.myUserInfo?.clubInfo?.roleType == EClubMemberRoleType.ECMRT_Founder.value
                || MyUserInfoManager.myUserInfo?.clubInfo?.roleType == EClubMemberRoleType.ECMRT_CoFounder.value) {
            hasManage = true
        }
        adapter = ClubApplyListAdapter(hasManage, object : ClubApplyListAdapter.Listener {
            override fun onClickAgree(position: Int, model: ClubApplyInfoModel?) {
                auditMemberJoin(position, model, true)
            }

            override fun onClickRefuse(position: Int, model: ClubApplyInfoModel?) {
                auditMemberJoin(position, model, false)
            }

            override fun onCLickAvatar(position: Int, model: ClubApplyInfoModel?) {
                model?.user?.userId?.let {
                    val bundle = Bundle()
                    bundle.putInt("bundle_user_id", it)
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_OTHER_PERSON)
                            .with(bundle)
                            .navigation()
                }
            }
        })

        titlebar.leftTextView.setDebounceViewClickListener {
            finish()
        }

        refreshLayout.apply {
            setEnableLoadMore(true)
            setEnableRefresh(false)
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(false)

            setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    getApplyList(offset, false)
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {

                }

            })
        }

        getApplyList(0, true)
    }

    private fun getApplyList(off: Int, isClean: Boolean) {
        launch {
            val result = subscribe(RequestControl("getClubMemberList", ControlType.CancelThis)) {
                clubServerApi.getApplyMemberList(off, cnt, 1)
            }
            if (result.errno == 0) {
                offset = result.data.getIntValue("offset")
                hasMore = result.data.getBooleanValue("hasMore")
                val list = JSON.parseArray(result.data.getString("items"), ClubApplyInfoModel::class.java)
                addApplyList(list, isClean)
            }
            finishRefreshAndLoadMore()
        }
    }

    private fun addApplyList(list: List<ClubApplyInfoModel>?, isClean: Boolean) {
        if (isClean) {
            adapter.mDataList.clear()
            if (!list.isNullOrEmpty()) {
                adapter.mDataList.addAll(list)
            }
            adapter.notifyDataSetChanged()
        } else {
            if (!list.isNullOrEmpty()) {
                val size = adapter.mDataList.size
                adapter.mDataList.addAll(list)
                val newSize = adapter.mDataList.size
                adapter.notifyItemRangeInserted(size, newSize - size)
            }
        }
    }

    private fun finishRefreshAndLoadMore() {
        refreshLayout.finishLoadMore()
        refreshLayout.finishRefresh()
        refreshLayout.setEnableLoadMore(hasMore)
    }

    private fun auditMemberJoin(position: Int, model: ClubApplyInfoModel?, isApprove: Boolean) {
        // 1 未审核 2审核通过 3 审核不通过
        launch {
            val map = mapOf(
                    "applyID" to (model?.user?.userId ?: 0),
                    "auditStatus" to (if (isApprove) 2 else 3)
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("auditMemberJoin", ControlType.CancelThis)) {
                clubServerApi.auditMemberJoin(body)
            }
            if (result.errno == 0) {
                // 审核完成
                adapter.mDataList.remove(model)
                adapter.notifyItemRemoved(position)//注意这里
                if (position != adapter.mDataList.size) {
                    adapter.notifyItemRangeChanged(position, adapter.mDataList.size - position)
                }
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun canSlide(): Boolean {
        return false
    }
}