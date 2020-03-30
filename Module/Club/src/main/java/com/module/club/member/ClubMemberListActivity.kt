package com.module.club.member

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.userinfo.model.ClubMemberInfo
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.titlebar.CommonTitleBar
import com.component.club.model.ClubMemberInfoModel
import com.dialog.view.TipsDialogView
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

@Route(path = RouterConstants.ACTIVITY_LIST_MEMBER)
class ClubMemberListActivity : BaseActivity() {

    lateinit var titlebar: CommonTitleBar
    lateinit var refreshLayout: SmartRefreshLayout
    lateinit var contentRv: RecyclerView
    lateinit var adapter: ClubMemberListAdapter

    private var clubMemberInfo: ClubMemberInfo? = null
    private var clubID: Int = 0

    private val clubServerApi = ApiManager.getInstance().createService(ClubServerApi::class.java)
    private var offset = 0
    private var hasMore = true
    private val cnt = 15

    private var mTipsDialogView: TipsDialogView? = null
    private var mClubMemberTitleDialog: ClubMemberTitleDialog? = null

    companion object {
        const val CLUB_LIST_TITLE = 1      //家族成员
        const val CLUB_LIST_TRANSFER = 2    //转让家族列表
    }

    var type = CLUB_LIST_TITLE  //默认家族成员

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.club_member_list_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        clubMemberInfo = intent.getSerializableExtra("clubMemberInfo") as ClubMemberInfo?
        type = intent.getIntExtra("clubMemberType", CLUB_LIST_TITLE)
        if (clubMemberInfo == null) {
            finish()
        } else {
            clubID = clubMemberInfo?.club?.clubID ?: 0
        }

        titlebar = findViewById(R.id.titlebar)
        refreshLayout = findViewById(R.id.refreshLayout)
        contentRv = findViewById(R.id.content_rv)

        if (type == CLUB_LIST_TRANSFER) {
            titlebar.centerTextView.text = "转让家族"
        }

        adapter = ClubMemberListAdapter(type, clubMemberInfo?.roleType
                ?: 0, object : ClubMemberListAdapter.Listener {
            override fun onClickTransfer(position: Int, model: ClubMemberInfoModel?) {
                model?.userInfoModel?.userId?.let { userID ->
                    mTipsDialogView?.dismiss(false)
                    mTipsDialogView = TipsDialogView.Builder(this@ClubMemberListActivity)
                            .setMessageTip("确定将家族移交给 ${model?.userInfoModel?.nicknameRemark}\n移交后你将不再担任本家族族长")
                            .setConfirmTip("确定移交")
                            .setCancelTip("取消")
                            .setConfirmBtnClickListener {
                                mTipsDialogView?.dismiss()
                                transferMember(position, model, userID)
                            }
                            .setCancelBtnClickListener {
                                mTipsDialogView?.dismiss()
                            }
                            .build()
                    mTipsDialogView?.showByDialog()
                }
            }

            override fun onClickAvatar(position: Int, model: ClubMemberInfoModel?) {
                model?.userInfoModel?.userId?.let {
                    val bundle = Bundle()
                    bundle.putInt("bundle_user_id", it)
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_OTHER_PERSON)
                            .with(bundle)
                            .navigation()
                }
            }

            override fun onClickRemove(position: Int, model: ClubMemberInfoModel?) {
                model?.userInfoModel?.userId?.let { userID ->
                    mTipsDialogView?.dismiss(false)
                    mTipsDialogView = TipsDialogView.Builder(this@ClubMemberListActivity)
                            .setMessageTip("确定将${model?.userInfoModel?.nicknameRemark}移除家族吗？")
                            .setConfirmTip("移除")
                            .setCancelTip("取消")
                            .setConfirmBtnClickListener {
                                mTipsDialogView?.dismiss()
                                delMember(position, model, userID)
                            }
                            .setCancelBtnClickListener {
                                mTipsDialogView?.dismiss()
                            }
                            .build()
                    mTipsDialogView?.showByDialog()
                }
            }

            override fun onClickTitle(position: Int, model: ClubMemberInfoModel?) {
                // 等设计稿
                model?.userInfoModel?.let {
                    mClubMemberTitleDialog?.dismiss(false)
                    mClubMemberTitleDialog = ClubMemberTitleDialog(this@ClubMemberListActivity, clubMemberInfo, it, object : ClubMemberTitleDialog.Listener {
                        override fun onClickCoFounder() {
                            mClubMemberTitleDialog?.dismiss()
                            setClubMemberTitle(EClubMemberRoleType.ECMRT_CoFounder.value, position, it)
                        }

                        override fun onClickHost() {
                            mClubMemberTitleDialog?.dismiss()
                            setClubMemberTitle(EClubMemberRoleType.ECMRT_Hostman.value, position, it)
                        }

                        override fun onClickCommon() {
                            mClubMemberTitleDialog?.dismiss()
                            setClubMemberTitle(EClubMemberRoleType.ECMRT_Common.value, position, it)
                        }
                    })
                    mClubMemberTitleDialog?.showByDialog()
                }
            }
        })
        contentRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        contentRv.adapter = adapter

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
                    getClubMemberList(offset, false)
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {

                }

            })
        }

        getClubMemberList(0, true)
    }

    private fun setClubMemberTitle(role: Int, position: Int, model: UserInfoModel) {
        launch {
            val map = mapOf(
                    "userID" to model.userId,
                    "role" to role
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("setClubMemberTitle", ControlType.CancelThis)) {
                clubServerApi.setClubMemberInfo(body)
            }
            if (result.errno == 0) {
                // 设置成功，更新下身份
                val roleDesc = result.data.getString("roleDesc")
                model.clubInfo?.roleType = role
                model.clubInfo?.roleDesc = roleDesc
                adapter.notifyItemChanged(position)
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    private fun getClubMemberList(off: Int, isClean: Boolean) {
        launch {
            val result = subscribe(RequestControl("getClubMemberList", ControlType.CancelThis)) {
                clubServerApi.getClubMemberList(clubID, off, cnt)
            }
            if (result.errno == 0) {
                offset = result.data.getIntValue("offset")
                hasMore = result.data.getBooleanValue("hasMore")
                val list = JSON.parseArray(result.data.getString("items"), ClubMemberInfoModel::class.java)
                if (type == CLUB_LIST_TRANSFER) {
                    val newList = ArrayList<ClubMemberInfoModel>()
                    list?.forEach {
                        if (it.userInfoModel?.clubInfo?.isFounder() == true) {
                            // 过滤所以族长
                        } else {
                            newList.add(it)
                        }
                    }
                    addClubMemberList(newList, isClean)
                } else {
                    addClubMemberList(list, isClean)
                }
            }
            finishRefreshAndLoadMore()
        }
    }

    private fun finishRefreshAndLoadMore() {
        refreshLayout.finishRefresh()
        refreshLayout.finishLoadMore()
        refreshLayout.setEnableLoadMore(hasMore)
    }

    private fun addClubMemberList(list: List<ClubMemberInfoModel>?, clean: Boolean) {
        if (clean) {
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

    private fun transferMember(position: Int, model: ClubMemberInfoModel, userID: Int) {
        launch {
            val map = mapOf(
                    "userID" to userID
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("transferMember", ControlType.CancelThis)) {
                clubServerApi.transferClub(body)
            }
            if (result.errno == 0) {
                U.getToastUtil().showShort("转让家族成功")
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    private fun delMember(position: Int, model: ClubMemberInfoModel, userID: Int) {
        launch {
            val map = mapOf(
                    "userID" to userID
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("delClubMember", ControlType.CancelThis)) {
                clubServerApi.delClubMember(body)
            }
            if (result.errno == 0) {
                // 删除成功，更新下视图
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

    override fun destroy() {
        super.destroy()
        mClubMemberTitleDialog?.dismiss(false)
        mTipsDialogView?.dismiss(false)
    }
}