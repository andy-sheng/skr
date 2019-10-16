package com.module.playways.race.room.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alibaba.fastjson.JSON
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.busilib.view.recyclercardview.CardAdapterHelper
import com.component.lyrics.LyricsManager
import com.module.playways.R
import com.module.playways.grab.room.model.NewChorusLyricModel
import com.module.playways.race.room.model.RaceGamePlayInfo
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer

class RaceSelectSongRecyclerAdapter : RecyclerView.Adapter<RaceSelectSongRecyclerAdapter.RaceGamePlayHolder>() {
    val mRaceGamePlayInfoList = ArrayList<RaceGamePlayInfo>()
    var mIRaceSelectListener: IRaceSelectListener? = null
    private val mCardAdapterHelper = CardAdapterHelper()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RaceGamePlayHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.race_song_info_view_layout, parent, false)
        mCardAdapterHelper.onCreateViewHolder(parent, view)
        return RaceGamePlayHolder(view)
    }

    override fun getItemCount(): Int {
        return mRaceGamePlayInfoList.size
    }

    override fun onBindViewHolder(holder: RaceGamePlayHolder, position: Int) {
        mCardAdapterHelper.onBindViewHolder(holder.itemView, position, itemCount)
        holder.bindData(position, mRaceGamePlayInfoList.get(position))
    }

    fun addData(list: List<RaceGamePlayInfo>) {
        list?.let {
            if (it.size > 0) {
                val startNotifyIndex = if (mRaceGamePlayInfoList.size > 0) mRaceGamePlayInfoList.size - 1 else 0
                mRaceGamePlayInfoList.addAll(list)
                notifyItemRangeChanged(startNotifyIndex, mRaceGamePlayInfoList.size - startNotifyIndex)
            }
        }
    }

    inner class RaceGamePlayHolder : RecyclerView.ViewHolder {
        val TAG = "RaceSongInfoViewRaceSongInfoView"
        var bg: ExImageView
        var songNameTv: ExTextView
        var anchorTv: ExTextView
        var lyricView: ExTextView
        var divider: ExImageView
        var signUpTv: ExTextView
        var model: RaceGamePlayInfo? = null
        var loadLyricTask: Disposable? = null
        var pos: Int = -1

        constructor(itemView: View) : super(itemView) {
            bg = itemView.findViewById(R.id.bg)
            lyricView = itemView.findViewById(R.id.lyric_tv)
            songNameTv = itemView.findViewById(R.id.song_name_tv)
            anchorTv = itemView.findViewById(R.id.anchor_tv)
            divider = itemView.findViewById(R.id.divider)
            signUpTv = itemView.findViewById(R.id.sign_up_tv)

            signUpTv.setDebounceViewClickListener {
                mIRaceSelectListener?.onSignUp(model?.commonMusic?.itemID ?: 0, model)
            }
        }

        fun bindData(position: Int, model: RaceGamePlayInfo) {
            this.model = model
            this.pos = position

            songNameTv.text = "《${model.commonMusic?.itemName}》"
            anchorTv.text = ""
            model.commonMusic?.writer?.let {
                anchorTv.append("词/${model.commonMusic?.writer} ")
            }
            model.commonMusic?.composer?.let {
                anchorTv.append("曲/${model.commonMusic?.composer}")
            }

            lyricView.text = "歌词加载中"

            if (mIRaceSelectListener?.getSignUpItemID() ?: 0 > 0) {
                if (mIRaceSelectListener?.getSignUpItemID() == model.commonMusic?.itemID) {
                    signUpTv.isEnabled = false
                    signUpTv.text = "报名成功"
                    signUpTv.visibility = View.VISIBLE
                } else {
                    signUpTv.visibility = View.GONE
                }
            } else {
                signUpTv.isEnabled = true
                signUpTv.text = "报名"
                signUpTv.visibility = View.VISIBLE
            }

            loadLyricTask?.dispose()
            loadLyricTask = LyricsManager
                    .loadGrabPlainLyric(model.commonMusic?.standLrc)
                    .subscribe(Consumer<String> { o ->
                        lyricView.text = ""
                        if (U.getStringUtils().isJSON(o)) {
                            val newChorusLyricModel = JSON.parseObject(o, NewChorusLyricModel::class.java)
                            var i = 0
                            while (i < newChorusLyricModel.items.size && i < 2) {
                                lyricView.append(newChorusLyricModel.items[i].words)
                                if (i == 0) {
                                    lyricView.append("\n")
                                }
                                i++
                            }
                        } else {
                            lyricView.text = o
                        }
                    }, Consumer<Throwable> { throwable -> MyLog.e(TAG, throwable) })
        }
    }

    interface IRaceSelectListener {
        fun onSignUp(itemID: Int, model: RaceGamePlayInfo?)
        fun getSignUpItemID(): Int
    }
}