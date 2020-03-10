package com.component.notification

import android.content.Context
import android.text.Html
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.OvershootInterpolator
import android.view.animation.TranslateAnimation
import android.widget.RelativeLayout
import com.alibaba.android.arouter.launcher.ARouter
import com.common.image.fresco.BaseImageView
import com.common.image.fresco.FrescoWorker
import com.common.image.model.BaseImage
import com.common.image.model.ImageFactory
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.busilib.R
import com.module.RouterConstants


/**
 * 关注弹窗通知
 */
class BigGiftNotifyView : RelativeLayout {

    val TAG = "BigGiftNotifyView"

    lateinit var mBg: ExImageView
    lateinit var mGiftIv: BaseImageView
    lateinit var mEnterTv: ExTextView
    lateinit var mContentTv: ExTextView

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        View.inflate(context, com.component.busilib.R.layout.big_gift_notification_view_layout, this)
        mBg = findViewById<View>(com.component.busilib.R.id.bg) as ExImageView
        mGiftIv = findViewById<View>(com.component.busilib.R.id.gift_iv) as BaseImageView
        mEnterTv = findViewById<View>(com.component.busilib.R.id.enter_tv) as ExTextView
        mContentTv = findViewById<View>(com.component.busilib.R.id.content_tv) as ExTextView
    }

    fun bindData(schema: String, content: String, showEnter: Boolean, sourceURL: String, call: () -> Unit) {
        if (showEnter) {
            val animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)
            animation.duration = 600
            animation.repeatMode = Animation.REVERSE
            animation.interpolator = OvershootInterpolator()
            animation.fillAfter = true
            startAnimation(animation)

            mEnterTv.visibility = View.VISIBLE
            mEnterTv.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View) {
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_SCHEME)
                            .withString("uri", schema)
                            .navigation()

                    call.invoke()
                }
            })
        } else {
            mEnterTv.visibility = View.GONE
            mEnterTv.setOnClickListener(null)
        }

        FrescoWorker.loadImage(mGiftIv, ImageFactory.newPathImage(sourceURL)
                .setLoadingDrawable(U.getDrawable(R.drawable.skrer_logo))
                .setFailureDrawable(U.getDrawable(R.drawable.skrer_logo))
                .setWidth(U.getDisplayUtils().dip2px(45f))
                .setHeight(U.getDisplayUtils().dip2px(45f))
                .build<BaseImage>())

        val charSequence = Html.fromHtml(content)
        mContentTv.text = charSequence
    }
}
