package com.module.playways.party.room.actor

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.view.setDebounceViewClickListener
import com.common.view.ex.ExTextView
import com.component.busilib.view.AvatarView
import com.component.person.event.ShowPersonCardEvent
import com.module.playways.R
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import org.greenrobot.eventbus.EventBus

// 申请列表
class PartyApplyPanelView(context: Context) : ConstraintLayout(context) {

    var divider: View? = null
    var titleTv: TextView? = null
    var recyclerView: RecyclerView? = null

    val adapter: PartyApplyAdapter = PartyApplyAdapter()
    var mDialogPlus: DialogPlus? = null

    init {
        View.inflate(context, R.layout.party_apply_panel_view_layout, this)
        divider = this.findViewById(R.id.divider)
        titleTv = this.findViewById(R.id.title_tv)
        recyclerView = this.findViewById(R.id.recycler_view)

        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        //todo 补上拉申请列表的接口，通知下申请人数改变
    }

    /**
     * 以后tips dialog 不要在外部单独写 dialog 了。
     * 可以不
     */
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

    inner class PartyApplyAdapter : RecyclerView.Adapter<PartyApplyAdapter.PartyApplyViewHolder>() {
        var mDataList = ArrayList<UserInfoModel>()

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
            val agreeTv: ExTextView = item.findViewById(R.id.agree_tv)
            val nameTv: TextView = item.findViewById(R.id.name_tv)

            var mPos = -1
            var mModel: UserInfoModel? = null

            init {
                agreeTv.setDebounceViewClickListener {
                    // todo 同意申请 补接口
                }
                avatarSdv.setDebounceViewClickListener {
                    mModel?.userId?.let {
                        EventBus.getDefault().post(ShowPersonCardEvent(it))
                    }
                }
            }

            fun bindData(position: Int, model: UserInfoModel) {
                this.mPos = position
                this.mModel = model

            }
        }
    }
}

