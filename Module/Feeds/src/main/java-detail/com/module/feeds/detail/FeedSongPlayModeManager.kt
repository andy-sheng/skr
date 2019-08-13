package com.module.feeds.detail

import com.module.feeds.watch.model.FeedSongModel
import java.util.*
import kotlin.collections.ArrayList

/**
 *  传入播放模式
 *  当前播放的歌曲
 *  总的歌曲列表
 *
 *  提供不同模式的上一首 下一首逻辑
 */
class FeedSongPlayModeManager(mode: PlayMode, cur: FeedSongModel?, originalSongList: List<FeedSongModel>) {
    private var mCur: FeedSongModel? = null

    private var mOriginalSongList = ArrayList<FeedSongModel>()

    //当前循环播放列表中的位置
    private var mOriginPosition: Int = 0

    private var mShuffleSongList = ArrayList<Pair<Int, FeedSongModel>>()

    //当前随机列表中的位置
    private var mShufflePosition: Int = 0

    //默认顺序播放
    private var mMode: PlayMode = PlayMode.SINGLE

    init {
        mCur = cur
        mMode = mode
        mOriginalSongList.addAll(originalSongList)

//        mShuffleSongList = ArrayList(originalSongList)
//        Collections.shuffle(mShuffleSongList)

        mCur?.let {
            changeMode(mMode)
        }
    }

    fun setCurrentPlayModel(mode: FeedSongModel?) {
        mCur = mode
        mOriginalSongList.forEachIndexed { index, feedSongModel ->
            if (feedSongModel == mode) {
                mOriginPosition = index
                return@forEachIndexed
            }
        }
        mShuffleSongList.forEachIndexed { index, pair ->
            if (pair.second == mode) {
                mShufflePosition = index
                return@forEachIndexed
            }
        }
    }

    fun changeMode(mode: PlayMode) {
        val lastMode = mMode
        mMode = mode
        if (mCur == null) {
            return
        }
        if (mMode == PlayMode.RANDOM) {
            // 新的模式为随机，且之前的模式为顺序或者单曲
            ensureHasShuffle(null)
            mShuffleSongList.forEachIndexed { index, pair ->
                if (pair.second == mCur) {
                    mShufflePosition = index
                    return@forEachIndexed
                }
            }
        }

        if (mMode == PlayMode.ORDER || mMode == PlayMode.SINGLE) {
            // 新的模式为随机，且之前的模式为顺序或者单曲
            mOriginPosition = mShuffleSongList.get(mShufflePosition).first
        }
    }

    fun getNextSong(userAction: Boolean): FeedSongModel? {
        if (mCur == null) {
            return getFirstSongWhenCurNull()
        }

        if (mMode == PlayMode.SINGLE) {
            if (userAction) {
                mOriginPosition = (mOriginPosition + 1) % mOriginalSongList.size
                mCur = mOriginalSongList[mOriginPosition]
                return mCur
            } else {
                return mCur
            }
        } else if (mMode == PlayMode.ORDER) {
            mOriginPosition = (mOriginPosition + 1) % mOriginalSongList.size
            mCur = mOriginalSongList[mOriginPosition]
            return mCur
        } else if (mMode == PlayMode.RANDOM) {
            mShufflePosition += 1
            //ensureHasShuffle(null)
            if (mShufflePosition >= mShuffleSongList.size) {
                mShuffleSongList.clear()
                ensureHasShuffle(mCur)
                mShufflePosition -= mShuffleSongList.size
            }
            mCur = mShuffleSongList[mShufflePosition].second
            return mCur
        }

        return FeedSongModel()
    }

    fun getPreSong(userAction: Boolean): FeedSongModel? {
        if (mCur == null) {
            return getFirstSongWhenCurNull()
        }

        if (mMode == PlayMode.SINGLE) {
            if (userAction) {
                mOriginPosition = (mOriginPosition - 1 + mOriginalSongList.size) % mOriginalSongList.size
                mCur = mOriginalSongList[mOriginPosition]
                return mCur
            } else {
                return mCur
            }
        } else if (mMode == PlayMode.ORDER) {
            mOriginPosition = (mOriginPosition - 1 + mOriginalSongList.size) % mOriginalSongList.size
            mCur = mOriginalSongList[mOriginPosition]
            return mCur
        } else if (mMode == PlayMode.RANDOM) {
            // 往前播不重新混淆，一直循环
            ensureHasShuffle(mCur)
            mShufflePosition = (mShufflePosition - 1 + mShuffleSongList.size) % mShuffleSongList.size
            mCur = mShuffleSongList[mShufflePosition].second
            return mCur
        }

        return FeedSongModel()
    }


    private fun getFirstSongWhenCurNull(): FeedSongModel? {
        when (mMode) {
            PlayMode.SINGLE, PlayMode.ORDER -> {
                if (!mOriginalSongList.isEmpty()) {
                    mCur = mOriginalSongList[0]
                    mOriginPosition = 0
                }
            }
            PlayMode.RANDOM -> {
                ensureHasShuffle(null)
                if (!mShuffleSongList.isEmpty()) {
                    mCur = mShuffleSongList[0].second
                    mShufflePosition = 0
                }
            }
        }
        return mCur
    }

    private fun ensureHasShuffle(avoidSong: FeedSongModel?) {
        if (mShuffleSongList.isEmpty()) {
            // 如果没混淆,混淆一下
            mOriginalSongList.forEachIndexed { index, feedSongModel ->
                mShuffleSongList.add(Pair(index, feedSongModel))
            }
            Collections.shuffle(mShuffleSongList)
            //将列表混淆一下 避免本次的第一首歌 排在下一首的首位
            if (avoidSong != null && mShuffleSongList[0].second == avoidSong) {
                val p = mShuffleSongList.removeAt(0)
                mShuffleSongList.add(p)
            }
        }
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