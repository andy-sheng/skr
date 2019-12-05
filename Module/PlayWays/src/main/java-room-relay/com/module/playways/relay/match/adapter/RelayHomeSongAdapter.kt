package com.module.playways.relay.match.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.utils.SpanUtils
import com.common.utils.U
import com.component.busilib.view.recyclercardview.CardAdapterHelper
import com.component.lyrics.LyricsManager
import com.component.lyrics.LyricsReader
import com.module.playways.R
import com.module.playways.room.song.model.SongModel
import io.reactivex.Observable
import io.reactivex.Observer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RelayHomeSongAdapter : RecyclerView.Adapter<RelayHomeSongAdapter.RelaySongViewHolder>() {

    var listener: RelayHomeListener? = null
    var mDataList = ArrayList<ExSongModel>()
    private val cardAdapterHelper = CardAdapterHelper(8, 12)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelaySongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.relay_song_card_item_layout, parent, false)
        cardAdapterHelper.onCreateViewHolder(parent, view)
        return RelaySongViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: RelaySongViewHolder, position: Int) {
        cardAdapterHelper.onBindViewHolder(holder.itemView, position, itemCount, listener?.getRecyclerViewPosition() == position
                || mDataList.size == 1)
        holder.bindData(position, mDataList[position])
    }

    fun addData(list: List<SongModel>?) {
        if (list?.isNotEmpty() == true) {
            val startNotifyIndex = if (mDataList.size > 0) mDataList.size - 1 else 0
            for (sm in list) {
                mDataList.add(ExSongModel(sm))
            }

            notifyItemRangeChanged(startNotifyIndex, mDataList.size - startNotifyIndex)
        }
    }

    inner class RelaySongViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        val songNameTv: TextView = item.findViewById(R.id.song_name_tv)
        val songAuthorTv: TextView = item.findViewById(R.id.song_author_tv)
        val contentTv: TextView = item.findViewById(R.id.content_tv)
        val startTv: TextView = item.findViewById(R.id.start_tv)

        var mPos = -1
        var mModel: ExSongModel? = null

        init {
            startTv.setDebounceViewClickListener {
                listener?.selectSong(mPos, mModel?.songModel)
            }
        }

        @SuppressLint("CheckResult")
        fun bindData(position: Int, model: ExSongModel) {
            this.mPos = position
            this.mModel = model

            songNameTv.text = model.songModel?.itemName
            songAuthorTv.text = model.songModel?.songDesc
            if (!TextUtils.isEmpty(model.content)) {
                contentTv.text = model.content
                contentTv.requestLayout()
            } else {
                LyricsManager.loadStandardLyric(model.songModel?.lyric)
                        .subscribe({
                            it?.let {
                                if ((model.songModel?.relaySegments?.size ?: 0) > 0) {
                                    val span = SpanUtils()
                                    var lyricsLine = 0   // 时间片内歌词多少句
                                    val maxLines = 3     // 一个时间片最多3句歌词
                                    // 第一句分割
                                    var timeIndex = 0
                                    var timeEnd = model.songModel?.relaySegments?.get(timeIndex)
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
                                            timeEnd = if (timeIndex < model.songModel?.relaySegments?.size ?: 0) {
                                                model.songModel?.relaySegments?.get(timeIndex) ?: 0
                                            } else {
                                                // 最后一句了
                                                Int.MAX_VALUE
                                            }
                                            span.append(lyricsLineInfo.lineLyrics).append("\n")
                                            lyricsLine += 1
                                        }
                                    }
                                    model.content = span.create()
                                    contentTv.text = model.content
                                    contentTv.requestLayout()
                                } else {

                                }
                            }
                        }, {
                        })
            }
        }
    }

    interface RelayHomeListener {
        fun getRecyclerViewPosition(): Int
        fun selectSong(position: Int, model: SongModel?)
    }

    class ExSongModel {
        var content: SpannableStringBuilder? = null
        var songModel: SongModel? = null

        constructor(songModel: SongModel?) {
            this.songModel = songModel
        }
    }
}