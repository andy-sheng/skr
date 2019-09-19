package com.module.posts.view

import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ClickableSpan
import android.view.View
import android.view.ViewStub
import android.widget.TextView
import com.common.log.MyLog
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ExViewStub
import com.module.posts.R

class PostsWatchCommentView(viewStub: ViewStub) : ExViewStub(viewStub) {

    lateinit var commentTv: TextView
    lateinit var contentTv: TextView
    lateinit var postsAudioView: PostsCommentAudioView
    lateinit var nineGridVp: PostsNineGridLayout

    override fun init(parentView: View) {
        commentTv = parentView.findViewById(R.id.comment_tv)
        contentTv = parentView.findViewById(R.id.content_tv)
        postsAudioView = parentView.findViewById(R.id.posts_audio_view)
        nineGridVp = parentView.findViewById(R.id.nine_grid_vp)
    }

    override fun layoutDesc(): Int {
        return R.layout.posts_watch_view_item_comment_stub_layout
    }

    fun bindData(content: String) {
        tryInflate()

        if (TextUtils.isEmpty(content)) {
            // 空的内容
            contentTv.apply {
                maxLines = 1
                setSingleLine(true)
                ellipsize = TextUtils.TruncateAt.END
            }
            val contentLayoutParams = contentTv.layoutParams as ConstraintLayout.LayoutParams
            contentLayoutParams.rightToLeft = postsAudioView.id
            contentLayoutParams.constrainedWidth = true
            contentTv.layoutParams = contentLayoutParams

            val audioLayoutParams = postsAudioView.layoutParams as ConstraintLayout.LayoutParams
            audioLayoutParams.leftMargin = 4.dp()
            audioLayoutParams.rightMargin = 10.dp()
            audioLayoutParams.leftToLeft = -1
            audioLayoutParams.leftToRight = contentTv.id
            audioLayoutParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
            audioLayoutParams.topToBottom = -1
            audioLayoutParams.topToTop = contentTv.id
            audioLayoutParams.bottomToBottom = contentTv.id
            audioLayoutParams.constrainedWidth = true
            postsAudioView.layoutParams = audioLayoutParams
        } else {
            contentTv.apply {
                maxLines = 2
                setSingleLine(false)
                ellipsize = TextUtils.TruncateAt.END
            }

            val contentLayoutParams = contentTv.layoutParams as ConstraintLayout.LayoutParams
            contentLayoutParams.rightToLeft = -1
            contentLayoutParams.constrainedWidth = true
            contentTv.layoutParams = contentLayoutParams

            val audioLayoutParams = postsAudioView.layoutParams as ConstraintLayout.LayoutParams
            audioLayoutParams.leftMargin = 0
            audioLayoutParams.rightMargin = 10.dp()
            audioLayoutParams.leftToLeft = contentTv.id
            audioLayoutParams.leftToRight = -1
            audioLayoutParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
            audioLayoutParams.topToBottom = contentTv.id
            audioLayoutParams.topToTop = -1
            audioLayoutParams.bottomToBottom = -1
            audioLayoutParams.constrainedWidth = true
            postsAudioView.layoutParams = audioLayoutParams
        }

        val contentBuilder = SpanUtils()
                .append("WWWWWWWWWWWWWWWWWW:").setForegroundColor(Color.parseColor("#63C2F0"))
                .append(content).setForegroundColor(U.getColor(R.color.black_trans_50))
                .create()
        contentTv.text = contentBuilder


    }
}