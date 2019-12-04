package com.module.playways.relay.match.adapter

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
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
        val singSelf: TextView = item.findViewById(R.id.sing_self)
        val singSelfContent: TextView = item.findViewById(R.id.sing_self_content)
        val singOther: TextView = item.findViewById(R.id.sing_other)
        val singOtherContent: TextView = item.findViewById(R.id.sing_other_content)
        val startTv: TextView = item.findViewById(R.id.start_tv)

        var mPos = -1
        var mModel: ExSongModel? = null

        init {
            startTv.setDebounceViewClickListener {
                listener?.selectSong(mPos, mModel?.songModel)
            }
        }

        fun bindData(position: Int, model: ExSongModel) {
            this.mPos = position
            this.mModel = model

            songNameTv.text = model.songModel?.itemName
            songAuthorTv.text = model.songModel?.songDesc
            if (!TextUtils.isEmpty(model.firstSingLyric)) {
                singSelfContent.text = model.firstSingLyric
                singOtherContent.text = model.secondSingLyric
            } else {
                LyricsManager.loadStandardLyric(model.songModel?.lyric)
                        .subscribe({
                            it?.let {
                                if ((model.songModel?.relaySegments?.size ?: 0) > 0) {
                                    var split = model.songModel?.relaySegments?.get(0) ?: 0
                                    var firstSingLyric = StringBuilder()
                                    var secondSingLyric = StringBuilder()
                                    var firstNum = 0
                                    var secondNum = 0
                                    for (l in it.lyricsLineInfoList) {
                                        if (l.startTime < split && firstNum < 4) {
                                            firstSingLyric.append(l.lineLyrics).append("\n")
                                            firstNum++
                                            if (firstNum == 4) {
                                                firstSingLyric.append("...").append("\n")
                                            }
                                        }
                                        if (l.startTime > split && secondNum < 2) {
                                            secondSingLyric.append(l.lineLyrics).append("\n")
                                            secondNum++
                                            if (secondNum == 2) {
                                                secondSingLyric.append("...").append("\n")
                                            }
                                        }
                                        if (secondNum > 2) {
                                            break
                                        }
                                    }
                                    model.firstSingLyric = firstSingLyric.toString()
                                    model.secondSingLyric = secondSingLyric.toString()
                                    singSelfContent.text = model.firstSingLyric
                                    singOtherContent.text = model.secondSingLyric
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
        var firstSingLyric = ""
        var secondSingLyric = ""
        var songModel: SongModel? = null

        constructor(songModel: SongModel?) {
            this.songModel = songModel
        }
    }
}