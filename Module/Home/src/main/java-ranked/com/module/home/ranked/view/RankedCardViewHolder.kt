package com.module.home.ranked.view

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.common.view.recyclerview.RecyclerOnItemClickListener
import com.facebook.drawee.view.SimpleDraweeView
import com.module.home.R
import com.module.home.ranked.model.RankHomeCardModel

import com.module.home.ranked.model.RankHomeCardModel.Companion.DUAN_RANK_TYPE
import com.module.home.ranked.model.RankHomeCardModel.Companion.POPULAR_RANK_TYPE
import com.module.home.ranked.model.RankHomeCardModel.Companion.REWARD_RANK_TYPE

class RankedCardViewHolder(itemView: View, listener: RecyclerOnItemClickListener<RankHomeCardModel>?) : RecyclerView.ViewHolder(itemView) {

    var mContainer: ConstraintLayout
    var mAvatarIv: SimpleDraweeView
    var mNameTv: TextView
    var mDescTv: TextView

    internal var mModel: RankHomeCardModel? = null
    internal var position: Int = 0

    init {
        mContainer = itemView.findViewById(R.id.container)
        mAvatarIv = itemView.findViewById(R.id.avatar_iv)
        mNameTv = itemView.findViewById(R.id.name_tv)
        mDescTv = itemView.findViewById(R.id.desc_tv)

        mContainer.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View) {
                listener?.onItemClicked(view, position, mModel)
            }
        })
    }

    fun bindData(model: RankHomeCardModel, position: Int) {
        this.mModel = model
        this.position = position


        when (mModel?.rankType) {
            POPULAR_RANK_TYPE -> {
                mContainer.background = U.getDrawable(R.drawable.renqirukou_bj)
                mContainer.visibility = View.VISIBLE
            }
            DUAN_RANK_TYPE -> {
                mContainer.background = U.getDrawable(R.drawable.duanweirukou_bj)
                mContainer.visibility = View.VISIBLE
            }
            REWARD_RANK_TYPE -> {
                mContainer.background = U.getDrawable(R.drawable.dashangrukou_bj)
                mContainer.visibility = View.VISIBLE
            }
            else -> {
                mContainer.visibility = View.GONE
            }
        }

        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().avatar)
                .setBorderColor(U.getColor(R.color.white))
                .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                .setCircle(true)
                .build())
        mNameTv.text = MyUserInfoManager.getInstance().nickName
        mDescTv.text = mModel?.desc
    }
}
