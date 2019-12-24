package com.module.playways.room.song.view

import android.content.Context
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.common.utils.SpanUtils
import com.common.utils.U
import com.component.lyrics.LyricsManager
import com.module.playways.R
import com.module.playways.room.song.model.SongModel
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder

class RelaySongInfoDialogView(model: SongModel, context: Context) : ConstraintLayout(context) {

    private var mDialogPlus: DialogPlus? = null

    init {
        View.inflate(context, R.layout.relay_song_info_dialog_view_layout, this)

        val songNameTv: TextView = this.findViewById(R.id.song_name_tv)
        val songAuthorTv: TextView = this.findViewById(R.id.song_author_tv)
        val contentTv: TextView = this.findViewById(R.id.content_tv)
        val startTv: TextView = this.findViewById(R.id.start_tv)

        songNameTv.text = model.itemName
        songAuthorTv.text = model.songDesc
        contentTv.text = "歌词加载中..."
        LyricsManager.loadStandardLyric(model.lyric)
                .subscribe({
                    it?.let {
                        if ((model.relaySegments?.size ?: 0) > 0) {
                            val span = SpanUtils()
                            var lyricsLine = 0   // 时间片内歌词多少句
                            val maxLines = 3     // 一个时间片最多3句歌词
                            // 第一句分割
                            var timeIndex = 0
                            var timeEnd = model.relaySegments?.get(timeIndex)
                                    ?: 0
                            span.append("【你唱】").setForegroundColor(Color.parseColor("#4DA5DB")).setFontSize(12, true).append("\n")
                            it.lyricsLineInfoList.forEachIndexed { index, lyricsLineInfo ->
                                if (lyricsLineInfo.startTime < timeEnd) {
                                    when {
                                        lyricsLine < maxLines -> span.append(lyricsLineInfo.lineLyrics).setForegroundColor(U.getColor(R.color.black_trans_80)).setFontSize(14, true).append("\n")
                                        lyricsLine == maxLines -> span.append("...")
                                        else -> {
                                            // 不用处理
                                            if (timeEnd == Int.MAX_VALUE) {
                                                return@forEachIndexed
                                            }
                                        }
                                    }
                                    lyricsLine += 1
                                } else {
                                    lyricsLine = 0   // 时间片内歌词句数清零
                                    timeIndex += 1
                                    if (timeIndex % 2 == 0) {
                                        span.append("\n").append("【你唱】").setForegroundColor(Color.parseColor("#4DA5DB")).setFontSize(12, true).append("\n")
                                    } else {
                                        span.append("\n").append("【Ta唱】").setForegroundColor(Color.parseColor("#DB4D84")).setFontSize(12, true).append("\n")
                                    }
                                    timeEnd = if (timeIndex < model.relaySegments?.size ?: 0) {
                                        model.relaySegments?.get(timeIndex) ?: 0
                                    } else {
                                        // 最后一句了
                                        Int.MAX_VALUE
                                    }
                                    span.append(lyricsLineInfo.lineLyrics).append("\n")
                                    lyricsLine += 1
                                }
                            }
                            contentTv.text = span.create()
                            contentTv.requestLayout()
                        } else {

                        }
                    }
                }, {
                })
    }

    fun showByDialog(height: Int) {
        mDialogPlus?.dismiss(false)
        mDialogPlus = DialogPlus.newDialog(context)
                .setContentHolder(ViewHolder(this))
                .setGravity(Gravity.CENTER)
                .setContentHeight(height)
                .setContentBackgroundResource(com.component.busilib.R.color.transparent)
                .setOverlayBackgroundResource(com.component.busilib.R.color.black_trans_80)
                .setMargin(U.getDisplayUtils().dip2px(16f), -1, U.getDisplayUtils().dip2px(16f), -1)
                .setExpanded(false)
                .setCancelable(true)
                .create()
        mDialogPlus?.show()
    }

    fun dismiss() {
        mDialogPlus?.dismiss()
    }

    fun dismiss(isAnimation: Boolean) {
        mDialogPlus?.dismiss(isAnimation)
    }
}