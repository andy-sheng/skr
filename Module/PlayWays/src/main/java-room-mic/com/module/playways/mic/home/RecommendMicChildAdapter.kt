package com.module.playways.mic.home

import android.graphics.Color
import android.support.constraint.Group
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.log.MyLog
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.busilib.view.VoiceChartView
import com.component.level.utils.LevelConfigUtils
import com.component.person.view.CommonAudioView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R

class RecommendMicChildAdapter : RecyclerView.Adapter<RecommendMicChildAdapter.RecomChildViewHolder>() {

    var mDataList = ArrayList<RecommendUserInfo>()

    var onClickVoice: ((model: RecommendUserInfo?, position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecomChildViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mic_recommend_child_item_layout, parent, false)
        return RecomChildViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: RecomChildViewHolder, position: Int) {
        holder.bindData(mDataList[position], position)
    }

    inner class RecomChildViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        val levelIv: ImageView = item.findViewById(R.id.level_iv)
        val avatarIv: SimpleDraweeView = item.findViewById(R.id.avatar_iv)
        val nameTv: ExTextView = item.findViewById(R.id.name_tv)
        val audioView: CommonAudioView = item.findViewById(R.id.audio_view)

        var mModel: RecommendUserInfo? = null
        var mPosition: Int = 0

        init {
            audioView.setAnimateDebounceViewClickListener {
                onClickVoice?.invoke(mModel, mPosition)
            }
        }

        fun bindData(model: RecommendUserInfo, position: Int) {
            this.mModel = model
            this.mPosition = position

            if (LevelConfigUtils.getRaceCenterAvatarBg(model.userInfo?.ranking?.mainRanking
                            ?: 0) != 0) {
                levelIv.visibility = View.VISIBLE
                levelIv.background = U.getDrawable(LevelConfigUtils.getRaceCenterAvatarBg(model.userInfo?.ranking?.mainRanking
                        ?: 0))
            } else {
                levelIv.visibility = View.INVISIBLE
            }
            nameTv.text = model.userInfo?.nicknameRemark
            AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(model.userInfo?.avatar)
                    .setCircle(true)
                    .build())

            if (model.voiceInfo != null && !TextUtils.isEmpty(model.voiceInfo?.voiceURL)) {
                audioView.visibility = View.VISIBLE
                audioView.bindData(model.voiceInfo?.duration ?: 0)
            } else {
                audioView.visibility = View.GONE
            }
        }

        fun starPlay() {
            MyLog.d("RecomChildViewHolder", "starPlay")
            audioView.setPlay(true)
        }

        fun stopPlay() {
            MyLog.d("RecomChildViewHolder", "stopPlay")
            audioView.setPlay(false)
        }
    }
}