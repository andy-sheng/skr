package com.module.posts.view

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.AppCompatTextView
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog

import com.common.utils.SpanUtils
import com.common.utils.U
import com.module.posts.R
import kotlinx.android.synthetic.main.posts_red_pkg_item_layout.view.*
import kotlinx.android.synthetic.main.posts_report_activity_layout.view.*

// 做成微信那种把
class ExpandTextView : ConstraintLayout {
    private var mMaxLines = 3// TextView最大行数
    private var mContentText: String? = null
    private var listener: ExpandListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    val contentTv: TextView
    val expendTv: TextView //展开收起
    var mIsExpand = false;

    init {
        View.inflate(context, R.layout.posts_expand_text_layout, this)
        contentTv = this.findViewById(R.id.content_tv)
        expendTv = this.findViewById(R.id.expend_tv)

        expendTv.setDebounceViewClickListener {
            if ("展开".equals(expendTv.text)) {
                setOpen()
            } else {
                setClose()
            }
            listener?.onClickExpand(!mIsExpand)
        }
    }

    public fun setListener(listener: ExpandListener) {
        this.listener = listener
    }

    fun bindData(text: String?, maxLine: Int, isExpand: Boolean) {
        mMaxLines = maxLine
        mIsExpand = isExpand
        mContentText = text

        if (mIsExpand) {
            setOpen()
        } else {
            setClose()
        }
    }

    fun setClose() {
        // 展开
        contentTv.maxLines = Int.MAX_VALUE
        contentTv.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                contentTv.viewTreeObserver.removeOnPreDrawListener(this)
                val line = contentTv.lineCount
                if (line > mMaxLines) {
                    contentTv.maxLines = mMaxLines
                    expendTv.text = "展开"
                    expendTv.visibility = View.VISIBLE
                } else {
                    expendTv.visibility = View.GONE
                }
                return true
            }
        })
        contentTv.text = mContentText
    }

    fun setOpen() {
        // 收起
        contentTv.maxLines = Int.MAX_VALUE
        contentTv.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                contentTv.viewTreeObserver.removeOnPreDrawListener(this)
                val line = contentTv.lineCount
                if (line > mMaxLines) {
                    expendTv.text = "收起"
                    expendTv.visibility = View.VISIBLE
                } else {
                    expendTv.visibility = View.GONE
                }
                return true
            }
        })
        contentTv.text = mContentText
    }


    interface ExpandListener {
        fun onClickExpand(isExpand: Boolean)
    }
}
