package com.module.posts.view

import android.content.Context
import android.graphics.Color
import android.os.Build
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

import com.common.utils.SpanUtils

class ExpandTextView : AppCompatTextView {

    private var originText: String? = null// 原始内容文本
    private var initWidth = 0// TextView可展示宽度
    private var mMaxLines = 3// TextView最大行数
    private var SPAN_CLOSE: SpannableStringBuilder? = null// 收起的文案(颜色处理)
    private var SPAN_EXPAND: SpannableStringBuilder? = null// 展开的文案(颜色处理)
    private val TEXT_EXPAND = "  展开>"
    private val TEXT_CLOSE = "  <收起"

    private var listener: ExpandListener? = null

    constructor(context: Context) : super(context) {
        initCloseEnd()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initCloseEnd()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initCloseEnd()
    }

    public fun setListener(listener: ExpandListener) {
        this.listener = listener
    }

    /**
     * 设置TextView可显示的最大行数
     *
     * @param maxLines 最大行数
     */
    override fun setMaxLines(maxLines: Int) {
        this.mMaxLines = maxLines
        super.setMaxLines(maxLines)
    }

    /**
     * 初始化TextView的可展示宽度
     *
     * @param width
     */
    fun initWidth(width: Int) {
        initWidth = width
    }

    /**
     * 收起的文案(颜色处理)初始化
     */
    private fun initCloseEnd() {
        if (SPAN_CLOSE == null) {
            val content = TEXT_EXPAND
            SPAN_CLOSE = SpanUtils()
                    .append(content).setClickSpan(object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            setExpandText(originText)
                            listener?.onClickExpand(true)
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            ds.color = Color.parseColor("#63C2F0")
                            ds.isUnderlineText = false
                        }
                    })
                    .setSpans(0, content.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                    .create()
        }
    }

    /**
     * 展开的文案(颜色处理)初始化
     */
    private fun initExpandEnd() {
        if (SPAN_EXPAND == null) {
            val content = TEXT_CLOSE
            SPAN_EXPAND = SpanUtils().append(content).setClickSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    setCloseText(originText)
                    listener?.onClickExpand(false)
                }

                override fun updateDrawState(ds: TextPaint) {
                    ds.color = Color.parseColor("#63C2F0")
                    ds.isUnderlineText = false
                }
            })
                    .setSpans(0, content.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                    .create()
        }
    }

    fun setCloseText(text: CharSequence?) {
        super@ExpandTextView.setMaxLines(mMaxLines)
        if (SPAN_CLOSE == null) {
            initCloseEnd()
        }
        var appendShowAll = false// true 不需要展开收起功能， false 需要展开收起功能
        originText = text!!.toString()

        // SDK >= 16 可以直接从xml属性获取最大行数
        var maxLines = 0
        maxLines = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getMaxLines()
        } else {
            mMaxLines
        }
        var workingText = StringBuilder(originText).toString()
        if (maxLines != -1) {
            val layout = createWorkingLayout(workingText)
            if (layout.lineCount > maxLines) {
                //获取一行显示字符个数，然后截取字符串数
                workingText = originText!!.substring(0, layout.getLineEnd(maxLines - 1)).trim { it <= ' ' }// 收起状态原始文本截取展示的部分
                val showText = originText!!.substring(0, layout.getLineEnd(maxLines - 1)).trim { it <= ' ' } + "..." + SPAN_CLOSE
                var layout2 = createWorkingLayout(showText)
                // 对workingText进行-1截取，直到展示行数==最大行数，并且添加 SPAN_CLOSE 后刚好占满最后一行
                while (layout2.lineCount > maxLines) {
                    val lastSpace = workingText.length - 1
                    if (lastSpace == -1) {
                        break
                    }
                    workingText = workingText.substring(0, lastSpace)
                    layout2 = createWorkingLayout("$workingText...$SPAN_CLOSE")
                }
                appendShowAll = true
                workingText = "$workingText..."
            }
        }

        setText(workingText)
        if (appendShowAll) {
            // 必须使用append，不能在上面使用+连接，否则spannable会无效
            append(SPAN_CLOSE)
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    fun setExpandText(text: String?) {
        super@ExpandTextView.setMaxLines(Integer.MAX_VALUE)
        initExpandEnd()
        val layout1 = createWorkingLayout(text)
        val layout2 = createWorkingLayout(text!! + TEXT_CLOSE)
        // 展示全部原始内容时 如果 TEXT_CLOSE 需要换行才能显示完整，则直接将TEXT_CLOSE展示在下一行
        if (layout2.lineCount > layout1.lineCount) {
            setText(originText + "\n")
        } else {
            setText(originText)
        }
        append(SPAN_EXPAND)
        movementMethod = LinkMovementMethod.getInstance()
    }

    //返回textview的显示区域的layout，该textview的layout并不会显示出来，只是用其宽度来比较要显示的文字是否过长
    private fun createWorkingLayout(workingText: String?): Layout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            StaticLayout(workingText, paint, initWidth - paddingLeft - paddingRight,
                    Layout.Alignment.ALIGN_NORMAL, lineSpacingMultiplier, lineSpacingExtra, false)
        } else {
            StaticLayout(workingText, paint, initWidth - paddingLeft - paddingRight,
                    Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false)
        }
    }

    interface ExpandListener {
        fun onClickExpand(isExpand: Boolean)
    }
}
