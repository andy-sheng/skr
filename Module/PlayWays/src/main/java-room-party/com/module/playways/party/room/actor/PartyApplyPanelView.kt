package com.module.playways.party.room.actor

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.component.busilib.view.AvatarView
import com.component.person.event.ShowPersonCardEvent
import com.module.playways.R
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.party.room.model.PartyPlayerInfoModel
import com.module.playways.room.data.H
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus

// 申请列表
class PartyApplyPanelView(context: Context) : ConstraintLayout(context), CoroutineScope by MainScope() {

    private var divider: View? = null
    private var titleTv: TextView? = null
    private val smartRefresh: SmartRefreshLayout
    private var recyclerView: RecyclerView? = null

    var adapter: PartyApplyAdapter? = null
    var mDialogPlus: DialogPlus? = null

    private val roomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)
    var offset = 0
    val cnt = 15
    var hasMore = true

    init {
        View.inflate(context, R.layout.party_apply_panel_view_layout, this)
        divider = this.findViewById(R.id.divider)
        titleTv = this.findViewById(R.id.title_tv)
        smartRefresh = this.findViewById(R.id.smart_refresh)
        recyclerView = this.findViewById(R.id.recycler_view)

        recyclerView?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = PartyApplyAdapter(object : Listener {
            override fun onClickRefuse(position: Int, model: PartyPlayerInfoModel?) {
                refuseGetSeat(position, model)
            }

            override fun onClickAgree(position: Int, model: PartyPlayerInfoModel?) {
                allowGetSeat(position, model)
            }
        })
        recyclerView?.adapter = adapter

        smartRefresh.apply {
            setEnableLoadMore(true)
            setEnableRefresh(false)
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(false)

            setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    loadApplyListData(offset, false)
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {

                }

            })
        }

        loadApplyListData(0, true)
    }

    private fun refuseGetSeat(position: Int, model: PartyPlayerInfoModel?) {
        model?.userID?.let {
            launch {
                val map = mutableMapOf(
                        "applyUserID" to it,
                        "roomID" to H.partyRoomData?.gameId
                )
                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                val result = subscribe(RequestControl("allowGetSeat", ControlType.CancelThis)) {
                    roomServerApi.refuseGetSeat(body)
                }
                if (result.errno == 0) {
                    // 只用拒绝即可，不用操作
                    // UI需要删除这个view 更新UI
                    remove(position, model)
                } else {
                    U.getToastUtil().showShort(result.errmsg)
                    if (result.errno == 8348003 || result.errno == 8348027 || result.errno == 8348028) {
                        //用户已经离开房间啦～
                        //对方已取消上麦申请
                        //对方已经在麦席上
                        remove(position, model)
                    }
                }
            }
        }
    }

    private fun allowGetSeat(position: Int, model: PartyPlayerInfoModel?) {
        model?.userID?.let {
            launch {
                val map = mutableMapOf(
                        "applyUserID" to it,
                        "roomID" to H.partyRoomData?.gameId
                )
                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                val result = subscribe(RequestControl("allowGetSeat", ControlType.CancelThis)) {
                    roomServerApi.allowGetSeat(body)
                }
                if (result.errno == 0) {
                    // 只用同意即可，不用操作
                    // UI需要删除这个view 更新UI
                    remove(position, model)
                } else {
                    U.getToastUtil().showShort(result.errmsg)
                    if (result.errno == 8348003 || result.errno == 8348005 || result.errno == 8348025) {
                        //用户已经离开房间啦～
                        //玩家不在申请中
                        // 已成功上麦
                        remove(position, model)
                    }
                }
            }
        }

    }

    fun remove(position: Int, model: PartyPlayerInfoModel) {
        adapter?.mDataList?.remove(model)
        adapter?.notifyItemRemoved(position)//注意这里
        if (position != adapter?.mDataList?.size) {
            adapter?.notifyItemRangeChanged(position, (adapter?.mDataList?.size
                    ?: 0) - position)
        }
    }

    private fun loadApplyListData(off: Int, isClean: Boolean) {
        launch {
            val result = subscribe(RequestControl("loadApplyListData", ControlType.CancelThis)) {
                roomServerApi.getApplyList(H.partyRoomData?.gameId ?: 0, off, cnt)
            }
            if (result.errno == 0) {
                hasMore = result.data.getBooleanValue("hasMore")
                offset = result.data.getIntValue("offset")
                H.partyRoomData?.applyUserCnt = result.data.getIntValue("total")
                val list = JSON.parseArray(result.data.getString("users"), PartyPlayerInfoModel::class.java)
                addList(list, isClean)
            }
            finishRefreshOrLoadMore()
        }
    }

    private fun addList(list: List<PartyPlayerInfoModel>?, isClean: Boolean) {
        if (isClean) {
            adapter?.mDataList?.clear()
            if (!list.isNullOrEmpty()) {
                adapter?.mDataList?.addAll(list)
            }
            adapter?.notifyDataSetChanged()
        } else {
            if (!list.isNullOrEmpty()) {
                val size = adapter?.mDataList?.size ?: 0
                adapter?.mDataList?.addAll(list)
                val newSize = adapter?.mDataList?.size ?: 0
                adapter?.notifyItemRangeInserted(size, newSize - size)
            }
        }
    }

    private fun finishRefreshOrLoadMore() {
        smartRefresh.finishRefresh()
        smartRefresh.finishLoadMore()
        smartRefresh.setEnableLoadMore(hasMore)
    }


    fun showByDialog() {
        showByDialog(true)
    }

    fun showByDialog(canCancel: Boolean) {
        mDialogPlus?.dismiss(false)
        mDialogPlus = DialogPlus.newDialog(context)
                .setContentHolder(ViewHolder(this))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(com.common.base.R.color.transparent)
                .setOverlayBackgroundResource(com.common.base.R.color.transparent)
                .setExpanded(false)
                .setCancelable(canCancel)
                .create()
        mDialogPlus?.show()
    }

    fun dismiss() {
        mDialogPlus?.dismiss()
    }

    fun dismiss(isAnimation: Boolean) {
        mDialogPlus?.dismiss(isAnimation)
    }

    fun destory() {
        cancel()
    }


    inner class PartyApplyAdapter(var listener: Listener) : RecyclerView.Adapter<PartyApplyAdapter.PartyApplyViewHolder>() {
        var mDataList = ArrayList<PartyPlayerInfoModel>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartyApplyViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.party_apply_item_view_layout, parent, false)
            return PartyApplyViewHolder(view)
        }

        override fun getItemCount(): Int {
            return mDataList.size
        }

        override fun onBindViewHolder(holder: PartyApplyViewHolder, position: Int) {
            holder.bindData(position, mDataList[position])
        }

        inner class PartyApplyViewHolder(item: View) : RecyclerView.ViewHolder(item) {

            val avatarSdv: AvatarView = item.findViewById(R.id.avatar_sdv)
            val refuseTv: ExTextView = item.findViewById(R.id.refuse_tv)
            val agreeTv: ExTextView = item.findViewById(R.id.agree_tv)
            val nameTv: TextView = item.findViewById(R.id.name_tv)

            var mPos = -1
            var mModel: PartyPlayerInfoModel? = null

            init {
                agreeTv.setDebounceViewClickListener {
                    listener?.onClickAgree(mPos, mModel)
                }

                avatarSdv.setDebounceViewClickListener {
                    mModel?.userInfo?.userId?.let {
                        EventBus.getDefault().post(ShowPersonCardEvent(it))
                    }
                }

                refuseTv.setDebounceViewClickListener {
                    listener?.onClickRefuse(mPos, mModel)
                }
            }

            fun bindData(position: Int, model: PartyPlayerInfoModel) {
                this.mPos = position
                this.mModel = model

                avatarSdv.bindData(model.userInfo)
                nameTv.text = model.userInfo.nicknameRemark
            }
        }
    }

    interface Listener {
        fun onClickAgree(position: Int, model: PartyPlayerInfoModel?)

        fun onClickRefuse(position: Int, model: PartyPlayerInfoModel?)
    }
}

