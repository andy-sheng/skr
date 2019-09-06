package com.component.busilib.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.utils.U
import com.component.busilib.R
import com.facebook.drawee.view.SimpleDraweeView

// 所有的头像
class AvatarView : ConstraintLayout {
    val mTag = "AvatarView"

    private val avatarIv: SimpleDraweeView
    private val text: TextView

    private var isCircle = true
    private var borderWidth = 0
    private var borderColor = Color.WHITE

    constructor(context: Context) : super(context) {
        initView(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(attrs)
    }

    init {
        View.inflate(context, R.layout.avatar_view_layout, this)
        avatarIv = findViewById(R.id.avatar)
        text = findViewById(R.id.text)
    }

    private fun initView(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.avatarView)
        isCircle = typedArray.getBoolean(R.styleable.avatarView_isCircle, true)
        borderWidth = typedArray.getDimensionPixelSize(R.styleable.avatarView_borderWidth, 0)
        borderColor = typedArray.getColor(R.styleable.avatarView_borderColor, Color.WHITE)
        typedArray.recycle()
    }

    fun bindData(model: UserInfoModel?) {
        text.visibility = View.VISIBLE
        AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(model?.avatar)
                .setBorderColor(borderColor)
                .setBorderWidth(borderWidth.toFloat())
                .setCircle(isCircle)
                .build())

    }

    fun bindData(model: UserInfoModel?, isOnline: Boolean) {
        text.visibility = View.VISIBLE
        AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(model?.avatar)
                .setBorderColor(borderColor)
                .setGray(isOnline)
                .setBorderWidth(borderWidth.toFloat())
                .setCircle(isCircle)
                .build())
    }

    fun setImageDrawable(drawble: Drawable) {
        text.visibility = View.GONE
        avatarIv.setImageDrawable(drawble)
    }
}