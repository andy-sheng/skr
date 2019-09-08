package com.module.playways.battle.view

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.common.image.fresco.BaseImageView
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R


//结果页面
class SongListCardView : ExConstraintLayout {
    val TAG = "SongListCardView"
    val avatarBg: BaseImageView
    val avatarIv: BaseImageView
    val songNameTv: ExTextView
    val hasSingTv: ExTextView
    val lightCountTv: ExTextView
    val starBg: ExImageView
    val startSongList: ExTextView
    val recyclerView: RecyclerView

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

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

    }
}
