package com.module.posts.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.support.v7.widget.AppCompatTextView
import android.text.DynamicLayout
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import com.common.log.MyLog
import com.common.utils.SpanUtils
import com.common.utils.U
import com.module.posts.R
import kotlinx.android.synthetic.main.posts_report_activity_layout.view.*

class SpannableTextView : AppCompatTextView {

    var isFirstLayout = true

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    var listener: (() -> Unit)? = null

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (nicknameRemark?.isNotEmpty() == true && layout.lineCount >= maxLines && isFirstLayout) {
            isFirstLayout = false
            // 总共可以显示多少个字符串
            val endLastLine = layout.getLineEnd(maxLines - 1)
            var nickNameStr = "$nicknameRemark:"
            var contentStr = this.content
            var leftContentLength = endLastLine - nickNameStr.length
            if (leftContentLength > 0 && contentStr!!.length > leftContentLength) {
                // 截取字符串给...留位置
                if (leftContentLength - 3 >= 0) {
                    var totalLength = 0
                    var index = 0
                    for (i in 0..2) {
                        val char = contentStr.substring(leftContentLength - 1 - i, leftContentLength - i)
                        index = i
                        totalLength += U.getStringUtils().getStringUTF8Length(char)
                        if (totalLength >= 3) {
                            break
                        }
                    }
                    contentStr = contentStr.substring(0, leftContentLength - (index + 1)) + "..."
                }
            }

            val contentBuilder = SpanUtils()
                    .append(nickNameStr)
                    .setForegroundColor(Color.parseColor("#63C2F0"))
                    .setClickSpan(object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            listener?.invoke()

                        }

                        override fun updateDrawState(ds: TextPaint) {
                            ds.color = Color.parseColor("#63C2F0")
                            ds.isUnderlineText = false
                        }
                    })
                    .append(contentStr!!)
                    .setForegroundColor(U.getColor(R.color.black_trans_50))
                    .create()
            text = contentBuilder
        }

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    var nicknameRemark: String? = null
    var content: String? = null

    fun bindData(nicknameRemark: String?, content: String?) {
        this.isFirstLayout = true
        this.nicknameRemark = nicknameRemark
        this.content = content
        var nickNameStr = "$nicknameRemark:"
        var contentStr = this.content
        val contentBuilder = SpanUtils()
                .append(nickNameStr)
                .setForegroundColor(Color.parseColor("#63C2F0"))
                .setClickSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        listener?.invoke()

                    }

                    override fun updateDrawState(ds: TextPaint) {
                        ds.color = Color.parseColor("#63C2F0")
                        ds.isUnderlineText = false
                    }
                })
                .append(contentStr!!)
                .setForegroundColor(U.getColor(R.color.black_trans_50))
                .create()
        text = contentBuilder
    }

}