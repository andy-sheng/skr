package com.module.playways.grab.special

import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.utils.dp
import com.common.view.ex.ExImageView
import com.common.view.ex.drawable.DrawableCreator
import com.module.playways.R
import com.module.playways.battle.songlist.view.BattleStarView

class GrabSpecialAdapter : RecyclerView.Adapter<GrabSpecialAdapter.GrabSpecialViewHolder>() {

    var mDataList: ArrayList<GrabTagDetailModel> = ArrayList()

    var onClickListener: ((model: GrabTagDetailModel?, position: Int) -> Unit)? = null

    val bgDrawable1 = DrawableCreator.Builder()
            .setGradientColor(Color.parseColor("#FFBEDC"), Color.parseColor("#FF8AB6"), Color.parseColor("#FF8AB6"))
            .setGradientAngle(315)
            .setCornersRadius(8.dp().toFloat())
            .build()


    val bgDrawable2 = DrawableCreator.Builder()
            .setGradientColor(Color.parseColor("#FFE293"), Color.parseColor("#FFC15A"), Color.parseColor("#FFC15A"))
            .setGradientAngle(315)
            .setCornersRadius(8.dp().toFloat())
            .build()


    val bgDrawable3 = DrawableCreator.Builder()
            .setGradientColor(Color.parseColor("#A2E8FF"), Color.parseColor("#69CCFE"), Color.parseColor("#69CCFE"))
            .setGradientAngle(315)
            .setCornersRadius(8.dp().toFloat())
            .build()


    val bgDrawable4 = DrawableCreator.Builder()
            .setGradientColor(Color.parseColor("#BFEFD2"), Color.parseColor("#8ADBA6"), Color.parseColor("#8ADBA6"))
            .setGradientAngle(315)
            .setCornersRadius(8.dp().toFloat())
            .build()


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

        val background: ConstraintLayout = item.findViewById(R.id.background)
        val specialNameTv: TextView = item.findViewById(R.id.special_name_tv)
        val maskIv: ExImageView = item.findViewById(R.id.mask_iv)
        val champainIv: ImageView = item.findViewById(R.id.champain_iv)
        val rankDesc: TextView = item.findViewById(R.id.rank_desc)
        val starView: BattleStarView = item.findViewById(R.id.star_view)
        val lockIv: ImageView = item.findViewById(R.id.lock_iv)

        var mPos = -1
        var model: GrabTagDetailModel? = null

        init {
            item.setAnimateDebounceViewClickListener { onClickListener?.invoke(model, mPos) }
        }

        fun bindData(position: Int, model: GrabTagDetailModel) {
            this.mPos = position
            this.model = model

            when (position % 4) {
                1 -> background.setBackground(bgDrawable2)
                2 -> background.setBackground(bgDrawable3)
                3 -> background.setBackground(bgDrawable4)
                else -> background.setBackground(bgDrawable1)
            }
            specialNameTv.text = model.tagName
            rankDesc.text = model.rankInfoDesc
            if (model.status == GrabTagDetailModel.SST_UNLOCK) {
                starView.visibility = View.VISIBLE
                lockIv.visibility = View.GONE
                starView.bindData(model.starCnt, model.starCnt)
            } else {
                starView.visibility = View.GONE
                lockIv.visibility = View.VISIBLE
            }
        }
    }
}