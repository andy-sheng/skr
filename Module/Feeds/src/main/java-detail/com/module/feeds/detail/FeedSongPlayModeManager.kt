package com.module.feeds.detail

import com.module.feeds.watch.model.FeedSongModel
import java.util.*
import kotlin.collections.ArrayList

class FeedSongPlayModeManager(mode: PlayMode, cur: FeedSongModel?, originalSongList: List<FeedSongModel>) {
    var mCur: FeedSongModel? = null
    var mOriginalSongList: ArrayList<FeedSongModel>
    var mShuffleSongList: ArrayList<FeedSongModel>

    //默认顺序播放
    var mMode: PlayMode = PlayMode.SINGLE
    //当前位置
    var mPosition: Int = 0

    init {
        mCur = cur
        mMode = mode
        mOriginalSongList = ArrayList(originalSongList)
        mShuffleSongList = ArrayList(originalSongList)
        Collections.shuffle(mShuffleSongList)

        mCur?.let {
            changeMode(mMode)
        }
    }

    fun changeMode(mode: PlayMode) {
        mMode = mode
        if (mCur == null) {
            mPosition = 0
            return
        }

        when (mMode) {
            PlayMode.SINGLE, PlayMode.ORDER -> {
                for ((index, value) in mOriginalSongList.withIndex()) {
                    if (value.feedID == mCur!!.feedID) {
                        mPosition = index
                    }
                }
            }

            PlayMode.RANDOM -> {
                for ((index, value) in mShuffleSongList.withIndex()) {
                    if (value.feedID == mCur!!.feedID) {
                        mPosition = index
                    }
                }
            }
        }
    }

    fun getPreSong(userAction: Boolean): FeedSongModel? {
        if (mCur == null) {
            return getFirstSongWhenCurNull()
        }

        if (mMode == PlayMode.SINGLE) {
            if (userAction) {
                mPosition--
                if (mPosition < 0) {
                    mPosition = mPosition + mOriginalSongList.size
                }

                mCur = mOriginalSongList[mPosition]
                return mCur
            } else {
                return mCur
            }
        } else if (mMode == PlayMode.ORDER) {
            mPosition--
            if (mPosition < 0) {
                mPosition = mPosition + mOriginalSongList.size
            }

            mCur = mOriginalSongList[mPosition]
            return mCur
        } else if (mMode == PlayMode.RANDOM) {
            mPosition--
            if (mPosition < 0) {
                mPosition = mPosition + mShuffleSongList.size
            }

            mCur = mShuffleSongList[mPosition]
            return mCur
        }

        return FeedSongModel()
    }

    fun getNextSong(userAction: Boolean): FeedSongModel? {
        if (mCur == null) {
            return getFirstSongWhenCurNull()
        }

        if (mMode == PlayMode.SINGLE) {
            if (userAction) {
                mPosition++
                if (mPosition >= mOriginalSongList.size) {
                    mPosition = mPosition - mOriginalSongList.size
                }

                mCur = mOriginalSongList[mPosition]
                return mCur
            } else {
                return mCur
            }
        } else if (mMode == PlayMode.ORDER) {
            mPosition++
            if (mPosition >= mOriginalSongList.size) {
                mPosition = mPosition - mOriginalSongList.size
            }

            mCur = mOriginalSongList[mPosition]
            return mCur
        } else if (mMode == PlayMode.RANDOM) {
            mPosition++
            if (mPosition >= mShuffleSongList.size) {
                mPosition = mPosition - mShuffleSongList.size
            }

            mCur = mShuffleSongList[mPosition]
            return mCur
        }

        return FeedSongModel()
    }

    private fun getFirstSongWhenCurNull(): FeedSongModel? {
        when (mMode) {
            PlayMode.SINGLE, PlayMode.ORDER -> {
                mCur = mOriginalSongList[0]
                mPosition = 0
            }
            PlayMode.RANDOM -> {
                mCur = mShuffleSongList[0]
                mPosition = 0
            }
        }

        return mCur
    }

    enum class PlayMode(val model: Int) {
        //单曲播放
        SINGLE(0),
        //顺序播放
        ORDER(1),
        //随机播放
        RANDOM(2);

        val value: Int
            get() = model
    }
}