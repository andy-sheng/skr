package com.module.playways.songmanager.adapter

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.common.core.userinfo.UserInfoManager
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.common.view.recyclerview.DiffAdapter
import com.module.playways.R
import com.module.playways.songmanager.model.GrabWishSongModel
import com.zq.live.proto.Common.StandPlayType

class WishSongAdapter : DiffAdapter<GrabWishSongModel, RecyclerView.ViewHolder>() {

    var mChorusDrawable: Drawable = DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#7088FF"))
            .setCornersRadius(U.getDisplayUtils().dip2px(10f).toFloat())
            .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f).toFloat())
            .setStrokeColor(U.getColor(R.color.white_trans_70))
            .build()
    var mPKDrawable: Drawable = DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#E55088"))
            .setCornersRadius(U.getDisplayUtils().dip2px(10f).toFloat())
            .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f).toFloat())
            .setStrokeColor(U.getColor(R.color.white_trans_70))
            .build()
    var mMiniGameDrawable: Drawable = DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#61B14F"))
            .setCornersRadius(U.getDisplayUtils().dip2px(10f).toFloat())
            .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f).toFloat())
            .setStrokeColor(U.getColor(R.color.white_trans_70))
            .build()

    var onClickDeleteWish: ((view: View, position: Int, songModel: GrabWishSongModel?) -> Unit)? = null
    var onClickSelectWish: ((view: View, position: Int, songModel: GrabWishSongModel?) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.wish_song_item_layout, parent, false)
        return ItemHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = mDataList[position]
        val itemHolder = holder as ItemHolder
        itemHolder.bind(model, position)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    private inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var mSongNameTv: ExTextView = itemView.findViewById(R.id.song_name_tv)
        var mSingNameTv: TextView = itemView.findViewById(R.id.sing_name_tv)
        var mSongTagTv: ExTextView = itemView.findViewById(R.id.song_tag_tv)
        var mSelectTv: ExTextView = itemView.findViewById(R.id.select_tv)
        var mDeleteTv: ExTextView = itemView.findViewById(R.id.delete_tv)

        private var mSongModel: GrabWishSongModel? = null
        private var mPosition: Int = 0

        init {
            mSelectTv.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View) {
                    onClickSelectWish?.invoke(v, mPosition, mSongModel)
                }
            })

            mDeleteTv.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View) {
                    onClickDeleteWish?.invoke(v, mPosition, mSongModel)
                }
            })
        }

        fun bind(model: GrabWishSongModel, position: Int) {
            this.mSongModel = model
            this.mPosition = position

            if (model.playType == StandPlayType.PT_SPK_TYPE.value) {
                mSongTagTv.text = "PK"
                mSongTagTv.visibility = View.VISIBLE
                mSongTagTv.background = mPKDrawable
                mSongNameTv.text = "《" + model.displaySongName + "》"
            } else if (model.playType == StandPlayType.PT_CHO_TYPE.value) {
                mSongTagTv.text = "合唱"
                mSongTagTv.visibility = View.VISIBLE
                mSongTagTv.background = mChorusDrawable
                mSongNameTv.text = "《" + model.displaySongName + "》"
            } else if (model.playType == StandPlayType.PT_MINI_GAME_TYPE.value) {
                mSongTagTv.text = "双人游戏"
                mSongTagTv.visibility = View.VISIBLE
                mSongTagTv.background = mMiniGameDrawable
                mSongNameTv.text = "【" + model.itemName + "】"
            } else {
                mSongNameTv.setPadding(0, 0, 0, 0)
                mSongTagTv.visibility = View.GONE
                mSongNameTv.text = "《" + model.displaySongName + "》"
            }

            val remarkName = UserInfoManager.getInstance().getRemarkName(model.suggester!!.userId, model.suggester!!.nickname)
            mSingNameTv.text = remarkName

        }
    }
}
