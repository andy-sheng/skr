package com.module.playways.party.room.actor

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.component.busilib.view.AvatarView
import com.component.person.event.ShowPersonCardEvent
import com.module.playways.R
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.party.room.event.PartySendEmojiEvent
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

// 房间内的人
class PartyMemberPanelView(context: Context) : ConstraintLayout(context), CoroutineScope by MainScope() {

    var mDialogPlus: DialogPlus? = null

    private val divider: View
    private val titleTv: TextView
    private val numTv: TextView
    private val smartRefresh: SmartRefreshLayout
    private val recyclerView: RecyclerView

    private val adapter = PartyMemberAdapter()

    private val roomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)
    var offset = 0
    val cnt = 15
    var hasMore = true

    init {
        View.inflate(context, R.layout.party_member_panel_view_layout, this)

        divider = this.findViewById(R.id.divider)
        titleTv = this.findViewById(R.id.title_tv)
        numTv = this.findViewById(R.id.num_tv)
        smartRefresh = this.findViewById(R.id.smart_refresh)
        recyclerView = this.findViewById(R.id.recycler_view)

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        smartRefresh.apply {
            setEnableLoadMore(true)
            setEnableRefresh(false)
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(false)

            setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    loadMemberListData(offset, false)
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {

                }

            })
        }

        loadMemberListData(0, true)
    }

    private fun loadMemberListData(off: Int, isClean: Boolean) {
        launch {
            val result = subscribe(RequestControl("loadMemberListData", ControlType.CancelThis)) {
                roomServerApi.getOnlineUserList(H.partyRoomData?.gameId ?: 0, off, cnt)
            }
            if (result.errno == 0) {
                hasMore = result.data.getBooleanValue("hasMore")
                offset = result.data.getIntValue("offset")
                H.partyRoomData?.onlineUserCnt = result.data.getIntValue("total")
                val list = JSON.parseArray(result.data.getString("users"), PartyPlayerInfoModel::class.java)
                addList(list, isClean)
            }
            finishRefreshOrLoadMore()
        }
    }

    private fun finishRefreshOrLoadMore() {
        smartRefresh.finishRefresh()
        smartRefresh.finishLoadMore()
        smartRefresh.setEnableLoadMore(hasMore)
    }

    private fun addList(list: List<PartyPlayerInfoModel>?, isClean: Boolean) {
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

    inner class PartyMemberAdapter : RecyclerView.Adapter<PartyMemberAdapter.PartyMemberViewHolder>() {

        var mDataList = ArrayList<PartyPlayerInfoModel>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartyMemberViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.party_member_item_view_layout, parent, false)
            return PartyMemberViewHolder(view)
        }

        override fun getItemCount(): Int {
            return mDataList.size
        }

        override fun onBindViewHolder(holder: PartyMemberViewHolder, position: Int) {
            holder.bindData(position, mDataList[position])
        }

        inner class PartyMemberViewHolder(item: View) : RecyclerView.ViewHolder(item) {

            private val avatarSdv: AvatarView = item.findViewById(R.id.avatar_sdv)
            private val nameTv: TextView = item.findViewById(R.id.name_tv)
            private val roleDescTv: TextView = item.findViewById(R.id.role_desc_tv)

            var mPos = -1
            var mModel: PartyPlayerInfoModel? = null

            init {
                item.setDebounceViewClickListener {
                    mModel?.userID?.let {
                        EventBus.getDefault().post(ShowPersonCardEvent(it))
                    }
                }
            }

            fun bindData(position: Int, model: PartyPlayerInfoModel) {
                this.mPos = position
                this.mModel = model

                avatarSdv.bindData(model.userInfo)
                nameTv.text = model.userInfo.nicknameRemark
                if (!TextUtils.isEmpty(model.getRoleDesc())) {
                    roleDescTv.text = model.getRoleDesc()
                } else {
                    roleDescTv.text = "观众"
                }

            }
        }
    }

}