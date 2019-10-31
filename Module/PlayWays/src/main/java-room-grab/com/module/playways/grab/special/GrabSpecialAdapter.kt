package com.module.playways.grab.special

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.image.fresco.FrescoWorker
import com.common.image.model.BaseImage
import com.common.image.model.ImageFactory
import com.common.utils.U
import com.common.view.ex.ExImageView
import com.component.person.utils.StringFromatUtils
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.battle.songlist.view.BattleStarView

class GrabSpecialAdapter : RecyclerView.Adapter<GrabSpecialAdapter.GrabSpecialViewHolder>() {

    var mDataList: ArrayList<GrabTagDetailModel> = ArrayList()

    var onClickListener: ((model: GrabTagDetailModel?, position: Int) -> Unit)? = null
    var onClickRankListener: ((model: GrabTagDetailModel?, position: Int) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GrabSpecialViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grab_special_item_layout, parent, false)
        return GrabSpecialViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: GrabSpecialViewHolder, position: Int) {
        holder.bindData(position, mDataList[position])
    }

    inner class GrabSpecialViewHolder(item: View) : RecyclerView.ViewHolder(item) {


        private val specialBg: SimpleDraweeView = item.findViewById(R.id.special_bg)
        private val specialTitleSdv: SimpleDraweeView = item.findViewById(R.id.special_title_sdv)
        private val playNumTv: TextView = item.findViewById(R.id.play_num_tv)
        private val rankIv: ImageView = item.findViewById(R.id.rank_iv)
        private val rankDesc: TextView = item.findViewById(R.id.rank_desc)
        private val starView: BattleStarView = item.findViewById(R.id.star_view)
        private val lockIv: ImageView = item.findViewById(R.id.lock_iv)

        var mPos = -1
        var model: GrabTagDetailModel? = null

        init {
            item.setAnimateDebounceViewClickListener { onClickListener?.invoke(model, mPos) }
            rankIv.setAnimateDebounceViewClickListener { onClickRankListener?.invoke(model, mPos) }
            rankDesc.setAnimateDebounceViewClickListener { onClickRankListener?.invoke(model, mPos) }
        }

        fun bindData(position: Int, model: GrabTagDetailModel) {
            this.mPos = position
            this.model = model

            FrescoWorker.loadImage(specialBg, ImageFactory.newPathImage(model.cardBg?.url)
                    .setScaleType(ScalingUtils.ScaleType.FIT_XY)
                    .build<BaseImage>())
            FrescoWorker.loadImage(specialTitleSdv, ImageFactory.newPathImage(model.cardTitle?.url)
                    .setScaleType(ScalingUtils.ScaleType.FIT_START)
                    .build<BaseImage>())

            if (!TextUtils.isEmpty(model.rankInfoDesc)) {
                rankDesc.setTextColor(U.getColor(R.color.black_trans_50))
                rankDesc.text = model.rankInfoDesc
            } else {
                rankDesc.setTextColor(U.getColor(R.color.black_trans_20))
                rankDesc.text = "暂无排名"
            }
            if (!model.showPermissionLock && model.status == GrabTagDetailModel.SST_UNLOCK) {
                starView.visibility = View.VISIBLE
                lockIv.visibility = View.GONE
                starView.bindData(model.starCnt, model.starCnt)
            } else {
                starView.visibility = View.GONE
                lockIv.visibility = View.VISIBLE
            }
            playNumTv.text = "${StringFromatUtils.formatTenThousand(model.onlineUserCnt)}人在玩"
        }
    }
}