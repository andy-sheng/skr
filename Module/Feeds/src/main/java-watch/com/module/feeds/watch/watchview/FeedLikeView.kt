package com.module.feeds.watch.watchview

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.TextView

import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.log.MyLog
import com.common.utils.SpanUtils
import com.common.utils.U
import com.module.feeds.R

/**
 * 点赞的神曲，每一项可以点击
 */
class FeedLikeView : TextView {

    var onClickNameListener: ((userID: Int) -> Unit)? = null

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }

    private fun initView() {
        movementMethod = MyLinkMovementMethod
        highlightColor = resources.getColor(R.color.transparent)
    }

    /**
     * 设置点赞的名字
     *
     * @param topics
     * @return
     */
    fun setLikeUsers(topics: List<UserInfoModel>?, likeNum: Int) {
        text = ""
        if (topics != null && topics.isNotEmpty()) {
            visibility = View.VISIBLE
            val length = topics.size
            for (i in 0 until length) {
                val clickText: String
                val feedUserInfo = topics[i]
                val name = UserInfoManager.getInstance().getRemarkName(feedUserInfo.userId, feedUserInfo.nickname)
                clickText = if (i == length - 1) {
                    name
                } else {
                    "$name、"
                }
                if (clickText.isNotBlank()) {
                    val spannableStringBuilder: SpannableStringBuilder
                    if (i == 0) {
                        spannableStringBuilder = SpanUtils()
                                .appendImage(U.getDrawable(R.drawable.feed_like_name_icon), SpanUtils.ALIGN_CENTER)
                                .append(" $clickText").setForegroundColor(Color.parseColor("#4A90E2"))
                                .setClickSpan(object : ClickableSpan() {
                                    override fun onClick(widget: View) {
                                        onClickNameListener?.invoke(feedUserInfo.userId)
                                    }

                                    override fun updateDrawState(ds: TextPaint) {
                                        ds.color = Color.parseColor("#4A90E2")
                                        ds.isUnderlineText = false
                                    }
                                })
                                .create()
                    } else {
                        spannableStringBuilder = SpanUtils()
                                .append(clickText).setForegroundColor(Color.parseColor("#4A90E2"))
                                .setClickSpan(object : ClickableSpan() {
                                    override fun onClick(widget: View) {
                                        onClickNameListener?.invoke(feedUserInfo.userId)
                                    }

                                    override fun updateDrawState(ds: TextPaint) {
                                        ds.color = Color.parseColor("#4A90E2")
                                        ds.isUnderlineText = false
                                    }
                                })
                                .create()
                    }
                    append(spannableStringBuilder)
                }
            }

            if (likeNum > 8) {
                val moreStr = "等" + likeNum + "人赞过"
                val mSpannableString = SpanUtils()
                        .append(moreStr).setForegroundColor(U.getColor(R.color.black_trans_50))
                        .create()
                append(mSpannableString)
            }
        } else {
            visibility = View.GONE
        }
    }
}

object MyLinkMovementMethod : LinkMovementMethod() {
    override fun onTouchEvent(widget: TextView?, buffer: Spannable?, event: MotionEvent?): Boolean {
        try {
            val b = super.onTouchEvent(widget, buffer, event)
            return b
        } catch (e: Exception) {
            MyLog.d("MyLinkMovementMethod", e)
        }
        return false
    }
}
