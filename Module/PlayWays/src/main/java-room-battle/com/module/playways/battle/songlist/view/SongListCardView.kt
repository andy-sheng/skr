package com.module.playways.battle.songlist.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import com.common.image.fresco.BaseImageView
import com.common.utils.dp
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.battle.songlist.adapter.SongListCardAdapter
import com.module.playways.battle.songlist.model.BattleTagModel
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder

//歌单详情页面
class SongListCardView(val model: BattleTagModel, context: Context) : ConstraintLayout(context) {
    val mTag = "SongListCardView"

    private val avatarBg: BaseImageView
    private val avatarIv: BaseImageView
    private val songNameTv: ExTextView
    private val hasSingTv: ExTextView
    private val lightCountTv: ExTextView
    private val starBg: ExImageView
    private val startSongList: ExTextView
    private val recyclerView: RecyclerView

    val adapter = SongListCardAdapter()

    private var mDialogPlus: DialogPlus? = null

    init {
        View.inflate(context, R.layout.song_list_card_view, this)
        avatarBg = this.findViewById(R.id.avatar_bg)
        avatarIv = this.findViewById(R.id.avatar_iv)
        songNameTv = this.findViewById(R.id.song_name_tv)
        hasSingTv = this.findViewById(R.id.has_sing_tv)
        lightCountTv = this.findViewById(R.id.light_count_tv)
        starBg = this.findViewById(R.id.star_bg)
        startSongList = this.findViewById(R.id.start_song_list)
        recyclerView = this.findViewById(R.id.recycler_view)

        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = adapter
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
                .setOverlayBackgroundResource(com.common.base.R.color.black_trans_80)
                .setMargin(10.dp(), 68.dp(), 10.dp(), -1)
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
}
