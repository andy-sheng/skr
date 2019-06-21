package com.module.playways.doubleplay.view

import android.text.TextUtils
import android.view.View
import android.view.ViewStub
import android.widget.ScrollView
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.image.fresco.BaseImageView
import com.common.utils.U
import com.common.view.ExViewStub
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.grab.room.view.video.DoubleSelfSingCardView
import com.module.playways.room.song.model.SongModel


class DoubleSingCardView(viewStub: ViewStub) : ExViewStub(viewStub) {
    val TAG = "DoubleSingCardView"
    var mSongOwnerIv: BaseImageView? = null
    var mSongNameTv: TextView? = null
    var mScrollView: ScrollView? = null
    var mNextSongTipTv: TextView? = null
    var mCutSongTv: ExTextView? = null
    lateinit var mDoubleSelfSingCardView: DoubleSelfSingCardView

    init {
        tryInflate()
    }

    override fun init(parentView: View?) {
        mSongOwnerIv = parentView?.findViewById(com.module.playways.R.id.song_owner_iv)
        mSongNameTv = parentView?.findViewById(com.module.playways.R.id.song_name_tv)
        mScrollView = parentView?.findViewById(com.module.playways.R.id.scrollView)
        mNextSongTipTv = parentView?.findViewById(com.module.playways.R.id.next_song_tip_tv)
        mCutSongTv = parentView?.findViewById(com.module.playways.R.id.cut_song_tv)
        mDoubleSelfSingCardView = DoubleSelfSingCardView(parentView)
    }

    fun playLyric(avatar: String = "", mCur: SongModel?, mNext: String?) {
        AvatarUtils.loadAvatarByUrl(mSongOwnerIv,
                AvatarUtils.newParamsBuilder(avatar)
                        .setBorderColor(U.getColor(R.color.white))
                        .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                        .setCircle(true)
                        .build())

        mSongNameTv?.text = mCur?.itemName
        mDoubleSelfSingCardView.playLyric(mCur)

        if (TextUtils.isEmpty(mNext)) {
            mNextSongTipTv?.text = "没有歌曲啦～"
            mCutSongTv?.text = "去点歌"
        } else {
            mNextSongTipTv?.text = mNext
            mCutSongTv?.text = "切歌"
        }
    }

    override fun layoutDesc(): Int {
        return R.layout.double_sing_card_view_layout
    }
}