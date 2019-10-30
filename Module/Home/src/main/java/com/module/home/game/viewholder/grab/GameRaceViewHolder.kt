package com.module.home.game.viewholder.grab

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.image.fresco.FrescoWorker
import com.common.image.model.BaseImage
import com.common.image.model.ImageFactory
import com.common.utils.U
import com.common.view.ex.ExImageView
import com.component.level.utils.LevelConfigUtils
import com.component.person.model.UserRankModel
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.module.home.R
import com.module.home.game.model.GrabSpecialModel
import java.util.regex.Pattern

class GameRaceViewHolder(item: View, onClickTagListener: ((model: GrabSpecialModel?) -> Unit)?, onClickRaceRankListener: ((model: GrabSpecialModel?) -> Unit)?) : RecyclerView.ViewHolder(item) {

    val imageSdv: SimpleDraweeView = item.findViewById(R.id.image_sdv)
    val levelBg: ExImageView = item.findViewById(R.id.level_bg)
    val levelIv: ImageView = item.findViewById(R.id.level_iv)
    val levelDescTv: TextView = item.findViewById(R.id.level_desc_tv)
    val diffDescTv: TextView = item.findViewById(R.id.diff_desc_tv)

    var model: GrabSpecialModel? = null
    var pos: Int = 0

    init {
        item.setAnimateDebounceViewClickListener {
            onClickTagListener?.invoke(model)
        }

        levelBg.setAnimateDebounceViewClickListener {
            onClickRaceRankListener?.invoke(model)
        }
    }

    fun bind(position: Int, model: GrabSpecialModel, userRankModel: UserRankModel?) {
        this.model = model
        this.pos = position

        model.model?.biggest?.let {
            var lp = imageSdv.layoutParams
            // 左边12 右边6
            lp.width = U.getDisplayUtils().screenWidth / 2 - U.getDisplayUtils().dip2px(16f)
            lp.height = (it.h) * lp.width / (it.w)
            imageSdv.layoutParams = lp

            FrescoWorker.loadImage(imageSdv, ImageFactory.newPathImage(it.url)
                    .setScaleType(ScalingUtils.ScaleType.FIT_XY)
                    .build<BaseImage>())
        }

        levelDescTv.text = userRankModel?.levelDesc
        if (LevelConfigUtils.getImageResoucesLevel(userRankModel?.mainRanking ?: 0) != 0) {
            levelIv.background = U.getDrawable(LevelConfigUtils.getImageResoucesLevel(userRankModel?.mainRanking ?: 0))
        }

        when {
            userRankModel == null -> {
                // 为空
                diffDescTv.visibility = View.GONE
            }
            userRankModel.diff == 0 -> {
                // 默认按照上升显示
                diffDescTv.visibility = View.VISIBLE
                diffDescTv.text = highlight(userRankModel.text, userRankModel.highlight, true)
            }
            userRankModel.diff > 0 -> {
                diffDescTv.visibility = View.VISIBLE
                diffDescTv.text = highlight(userRankModel.text, userRankModel.highlight, true)
            }
            else -> {
                diffDescTv.visibility = View.VISIBLE
                diffDescTv.text = highlight(userRankModel.text, userRankModel.highlight, false)
            }
        }
    }

    private fun highlight(text: String, target: String, isUp: Boolean): SpannableString {
        val spannableString = SpannableString(text)
        val pattern = Pattern.compile(target)
        val matcher = pattern.matcher(text)
        while (matcher.find()) {
            val span = ForegroundColorSpan(Color.parseColor("#FF3B3C"))
            spannableString.setSpan(span, matcher.start(), matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return spannableString
    }

}