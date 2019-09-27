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
import java.lang.reflect.Field

class SpannableTextView : AppCompatTextView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    var listener:(()->Unit)? = null

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (nicknameRemark?.isNotEmpty() == true) {
            var lineMaxNum = getLineMaxNumber("$nicknameRemark:$content", paint, width.toFloat())
            MyLog.d("SpannableTextView", "onLayout nicknameRemark = $nicknameRemark, content = $content lineMaxNum=$lineMaxNum width=$width")
            var nickNameStr = "$nicknameRemark:"
            var contentStr = this.content
            var leftContentLength = maxLines * lineMaxNum - nickNameStr.length
            if (leftContentLength > 0 && contentStr!!.length > leftContentLength) {
                if (leftContentLength - 3 >= 0) {
                    contentStr = contentStr.substring(0, leftContentLength-1) + "..."
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
        this.nicknameRemark = nicknameRemark
        this.content = content
        if (width >= 0) {
            var lineMaxNum = getLineMaxNumber("$nicknameRemark:$content", paint, width.toFloat())
            MyLog.d("SpannableTextView", "bindData nicknameRemark = $nicknameRemark, content = $content lineMaxNum=$lineMaxNum width=$width")
            text = "$nicknameRemark:$content"
        } else {
            MyLog.d("SpannableTextView", "bindData width<=0")
        }
    }

    private fun getLineMaxNumber(text: String, paint: TextPaint, maxWidth: Float): Int {
        var textWidth = paint.measureText(text)
        var width = textWidth / text.length
        return (maxWidth / width).toInt()
    }

}