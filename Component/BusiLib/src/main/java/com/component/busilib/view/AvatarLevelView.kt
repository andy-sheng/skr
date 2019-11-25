package com.component.busilib.view

import android.content.Context
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.model.UserInfoModel
import com.common.utils.U
import com.common.utils.dp
import com.component.busilib.R
import com.component.level.utils.LevelConfigUtils
import com.facebook.drawee.view.SimpleDraweeView

// todo 段位控件，包含段位和头像
class AvatarLevelView : ConstraintLayout {

    constructor(context: Context) : super(context) {
        initView(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(attrs)
    }

    private val avatar: SimpleDraweeView
    private val levelBg: ImageView
    private var avatarSize = 0

    init {
        View.inflate(context, R.layout.avatar_level_view_layout, this)

        avatar = this.findViewById(R.id.avatar)
        levelBg = this.findViewById(R.id.level_bg)
    }

    private fun initView(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.avatarLevel)
        avatarSize = typedArray.getDimensionPixelSize(R.styleable.avatarLevel_avatarSize, 58.dp())
        typedArray.recycle()
    }

    fun bindData(model: UserInfoModel?) {
        model?.let {
            bindData(model.avatar, model.ranking.mainRanking)
        }
    }

    fun bindData(avatarUrl: String, mainRanking: Int) {
        if (LevelConfigUtils.getImageResoucesLevel(mainRanking) != 0) {
            levelBg.background = U.getDrawable(LevelConfigUtils.getImageResoucesLevel(mainRanking))
        }

        val layoutParams = avatar.layoutParams
        layoutParams.width = avatarSize
        layoutParams.height = avatarSize
        avatar.layoutParams = layoutParams
        AvatarUtils.loadAvatarByUrl(avatar, AvatarUtils.newParamsBuilder(avatarUrl)
                .setCircle(true)
                .build())
    }

}