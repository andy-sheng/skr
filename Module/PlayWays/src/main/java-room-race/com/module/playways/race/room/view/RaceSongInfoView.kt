package com.module.playways.race.room.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R

class RaceSongInfoView : ConstraintLayout {
    var bg: ExImageView
    var songNameTv: ExTextView
    var anchorTv: ExTextView
    var divider: ExImageView
    var signUpTv: ExTextView

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.race_song_info_view_layout, this)

        bg = rootView.findViewById(R.id.bg)
        songNameTv = rootView.findViewById(R.id.song_name_tv)
        anchorTv = rootView.findViewById(R.id.anchor_tv)
        divider = rootView.findViewById(R.id.divider)
        signUpTv = rootView.findViewById(R.id.sign_up_tv)
    }
}