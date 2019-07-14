package com.module.playways.songmanager.adapter

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.common.view.recyclerview.DiffAdapter
import com.dialog.view.TipsDialogView
import com.component.busilib.friends.SpecialModel
import com.module.playways.R
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.OnClickListener
import com.orhanobut.dialogplus.OnDismissListener
import com.orhanobut.dialogplus.ViewHolder

class GrabTagsAdapter : DiffAdapter<SpecialModel, RecyclerView.ViewHolder>() {
    var onClickItem: ((specialModel: SpecialModel?) -> Unit)? = null
    var onDismissDialog: (() -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grab_song_tag_item_layout, parent, false)
        return ItemHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = mDataList[position]

        val reportItemHolder = holder as ItemHolder
        reportItemHolder.bind(model)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    private inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mTvSelectedTag: ExTextView
        var mSpecialModel: SpecialModel? = null

        init {
            mTvSelectedTag = itemView.findViewById(R.id.tv_selected_tag)
            mTvSelectedTag.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View) {
                    onDismissDialog?.invoke()
                    val tipsDialogView = TipsDialogView.Builder(itemView.context)
                            .setMessageTip("确认切换为 " + mSpecialModel?.tagName + " 歌单吗？\n当前待下发歌单内的所有歌曲将会被重置")
                            .setConfirmTip("确认切换")
                            .setCancelTip("取消")
                            .build()

                    DialogPlus.newDialog(itemView.context)
                            .setContentHolder(ViewHolder(tipsDialogView))
                            .setGravity(Gravity.BOTTOM)
                            .setContentBackgroundResource(R.color.transparent)
                            .setOverlayBackgroundResource(R.color.black_trans_80)
                            .setExpanded(false)
                            .setOnClickListener { dialog, view ->
                                if (view.id == R.id.confirm_tv) {
                                    dialog.dismiss()
                                    onClickItem?.invoke(mSpecialModel)
                                }

                                if (view.id == R.id.cancel_tv) {
                                    dialog.dismiss()
                                }
                            }
                            .setOnDismissListener { }
                            .create().show()
                }
            })
        }

        fun bind(model: SpecialModel) {
            this.mSpecialModel = model

            var color = Color.parseColor("#68ABD3")
            if (!TextUtils.isEmpty(model.bgColor)) {
                color = Color.parseColor(model.bgColor)
            }

            mTvSelectedTag.text = model.tagName
            mTvSelectedTag.setTextColor(color)
        }
    }
}