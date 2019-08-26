package com.module.feeds.detail.manager

import com.common.log.MyLog
import com.common.videocache.MediaCacheManager
import com.module.feeds.watch.model.FeedSongModel
import com.module.feeds.watch.model.FeedsWatchModel
import java.util.*
import kotlin.collections.ArrayList

/**
 *  传入播放模式
 *  当前播放的歌曲
 *  总的歌曲列表
 *
 *  提供不同模式的上一首 下一首逻辑
 */
class FeedSongPlayModeManager(mode: PlayMode, cur: FeedSongModel?, originalSongList: List<FeedSongModel>?) : AbsPlayModeManager() {
    val TAG = "FeedSongPlayModeManager"
    //当前播放
    private var mCur: FeedSongModel? = null

    //原始列表
    private var mOriginalSongList = ArrayList<FeedSongModel>()

    //当前循环播放列表中的位置
    private var mOriginPosition: Int = 0

    //混淆列表
    private var mShuffleSongList = ArrayList<Pair<Int, FeedSongModel>>()

    //当前随机列表中的位置
    private var mShufflePosition: Int = 0

    //是否支持循环，即在第一首时点击上一首，则返回最后一首。在最后一首点击下一首回到第一首
    var supportCycle = true

    //默认顺序播放
    var mMode: PlayMode = PlayMode.SINGLE

    //当不支持循环播放时，当列表走到头时
    var loadMoreCallback: ((size: Int, whenOkCallback: () -> Unit) -> Unit)? = null

    init {
        mCur = cur
        if (originalSongList != null) {
            mOriginalSongList.addAll(originalSongList)
        }

//        mShuffleSongList = ArrayList(originalSongList)
//        Collections.shuffle(mShuffleSongList)
        if (mCur == null) {
            mMode = mode
        } else {
            changeMode(mode)
        }
    }

    fun setCurrentPlayModel(model: FeedSongModel?) {
        MyLog.d(TAG, "setCurrentPlayModel model = $model")
        mCur = model
        if (mMode == PlayMode.RANDOM) {
            for (i in 0 until mShuffleSongList.size) {
                if (mShuffleSongList[i].second == model) {
                    mShufflePosition = i
                    break
                }
            }
        } else {
            var f = false
            if (mOriginPosition in 0 until mOriginalSongList.size) {
                if (mOriginalSongList[mOriginPosition] == model) {
                    f = true
                }
            }
            if (!f) {
                for (i in 0 until mOriginalSongList.size) {
                    if (mOriginalSongList[i] == model) {
                        mOriginPosition = i
                        break
                    }
                }
            }
        }
        tryPreCache()
        MyLog.d(TAG, "after setCurrentPlayModel mCur = $mCur , mOriginPosition = $mOriginPosition")
    }

    fun setCurrentPlayPostion(pos: Int) {
        if (pos < mOriginalSongList.size) {
            setCurrentPlayModel(mOriginalSongList[pos])
        }
    }

    fun setOriginList(fsms: ArrayList<FeedSongModel>, clean: Boolean) {
        MyLog.d(TAG, "setOriginList fsms.size = ${fsms.size} clean=$clean")
        if (clean) {
            mOriginalSongList.clear()
        }
        mOriginalSongList.addAll(fsms)
        mShuffleSongList.clear()
        ensureHasShuffle(mCur)
        if (clean) {
            mOriginPosition = 0
            mShufflePosition = 0
        } else {

        }
    }

    override fun playState(isPlaying: Boolean) {

    }

    fun getCurPostionInOrigin(): Int {
        if (mMode == PlayMode.RANDOM) {
            return mShuffleSongList.get(mShufflePosition).first
        }

        if (mMode == PlayMode.ORDER || mMode == PlayMode.SINGLE) {
            return mOriginPosition
        }
        return 0
    }

    override fun changeMode(mode: PlayMode) {
        mMode = mode
        setCurrentPlayModel(mCur)
    }

    var temporary: FeedSongModel? = null

    fun getNextSong2(userAction: Boolean, callback: (songMode: FeedSongModel?) -> Unit, canAcceptCall: ((songMode: FeedSongModel) -> Boolean)?) {
        if (temporary == null) {
            temporary = mCur
        }

        getNextSong(userAction) {
            if (it != null) {
                if (canAcceptCall == null) {
                    temporary = null
                    callback.invoke(it)
                } else {
                    canAcceptCall?.let { call ->
                        val accept = call(it)
                        if (accept) {
                            temporary = null
                            callback.invoke(it)
                        } else {
                            getNextSong2(userAction, callback, canAcceptCall)
                        }
                    }
                }
            } else {
                setCurrentPlayModel(temporary)
                temporary = null
                callback.invoke(it)
            }
        }
    }

    override fun getNextSong(userAction: Boolean, callback: (songMode: FeedSongModel?) -> Unit) {
        MyLog.d(TAG, "getNextSong mCur = $mCur , mOriginPosition = $mOriginPosition")
        if (mOriginalSongList.isNullOrEmpty()) {
            MyLog.e(TAG, "getNextSonguserAction = $userAction, callback = $callback mOriginalSongList = null")
            return
        }
        if (mCur == null) {
            getFirstSongWhenCurNull()
            callback?.invoke(mCur)
            tryPreCache()
            return
        }

        if (mMode == PlayMode.SINGLE) {
            if (userAction) {
                if (supportCycle) {
                    mOriginPosition = (mOriginPosition + 1) % mOriginalSongList.size
                    mCur = mOriginalSongList[mOriginPosition]
                    callback?.invoke(mCur)
                    tryPreCache()
                } else {
                    if (mOriginPosition + 1 >= mOriginalSongList.size) {
                        // 拉没了
                        loadMoreCallback?.invoke(mOriginalSongList.size) {
                            //请求准备数据，数据准备好了
                            if (mOriginPosition + 1 >= mOriginalSongList.size) {
                                callback?.invoke(null)
                                tryPreCache()
                            } else {
                                mOriginPosition++
                                mCur = mOriginalSongList[mOriginPosition]
                                callback?.invoke(mCur)
                                tryPreCache()
                            }
                        }
                    } else {
                        mOriginPosition++
                        mCur = mOriginalSongList[mOriginPosition]
                        callback?.invoke(mCur)
                        tryPreCache()
                    }
                }
            } else {
                callback?.invoke(mCur)
                tryPreCache()
            }
        } else if (mMode == PlayMode.ORDER) {
            if (supportCycle) {
                mOriginPosition = (mOriginPosition + 1) % mOriginalSongList.size
                mCur = mOriginalSongList[mOriginPosition]
                callback?.invoke(mCur)
                tryPreCache()
            } else {
                if (mOriginPosition + 1 >= mOriginalSongList.size) {
                    // 拉没了
                    loadMoreCallback?.invoke(mOriginalSongList.size) {
                        //请求准备数据，数据准备好了
                        if (mOriginPosition + 1 >= mOriginalSongList.size) {
                            callback?.invoke(null)
                            tryPreCache()
                        } else {
                            mOriginPosition++
                            mCur = mOriginalSongList[mOriginPosition]
                            callback?.invoke(mCur)
                            tryPreCache()
                        }
                    }
                } else {
                    mOriginPosition++
                    mCur = mOriginalSongList[mOriginPosition]
                    callback?.invoke(mCur)
                    tryPreCache()
                }
            }
        } else if (mMode == PlayMode.RANDOM) {
            mShufflePosition += 1
            ensureHasShuffle(null)
            if (mShufflePosition >= mShuffleSongList.size) {
                mShuffleSongList.clear()
                ensureHasShuffle(mCur)
                mShufflePosition -= mShuffleSongList.size
            }
            mCur = mShuffleSongList[mShufflePosition].second
            callback?.invoke(mCur)
            tryPreCache()
        }
    }

    private fun tryPreCache() {
        if (!mOriginalSongList.isNullOrEmpty()) {
            if (mMode == PlayMode.ORDER || mMode == PlayMode.SINGLE) {
                val p = (mOriginPosition + 1) % mOriginalSongList.size
                mOriginalSongList[p].playURL?.let {
                    MediaCacheManager.preCache(it)
                }
            } else if (mMode == PlayMode.RANDOM) {
                val p = mShufflePosition + 1
                if (p in 0 until mShuffleSongList.size) {
                    mShuffleSongList[p].second.playURL?.let {
                        MediaCacheManager.preCache(it)
                    }
                }
            }
        } else {
            MyLog.e(TAG, "tryPreCache mOriginalSongList = null")
        }
    }

    fun getPreSong2(userAction: Boolean, callback: (songMode: FeedSongModel?) -> Unit, canAcceptCall: ((songMode: FeedSongModel) -> Boolean)?) {
        if (temporary == null) {
            temporary = mCur
        }

        getPreSong(userAction) {
            if (it != null) {
                if (canAcceptCall == null) {
                    temporary = null
                    callback.invoke(it)
                } else {
                    canAcceptCall?.let { call ->
                        val accept = call(it)
                        if (accept) {
                            temporary = null
                            callback.invoke(it)
                        } else {
                            getPreSong2(userAction, callback, canAcceptCall)
                        }
                    }
                }
            } else {
                setCurrentPlayModel(temporary)
                temporary = null
                callback.invoke(it)
            }
        }
    }

    override fun getPreSong(userAction: Boolean, callback: (songMode: FeedSongModel?) -> Unit) {
        MyLog.d(TAG, "getPreSong mCur = $mCur, mOriginPosition = $mOriginPosition")
        if (mOriginalSongList.isNullOrEmpty()) {
            MyLog.d(TAG, "getPreSonguserAction = $userAction, callback = $callback mOriginalSongList = null")
            return
        }
        if (mCur == null) {
            getFirstSongWhenCurNull()
            callback?.invoke(mCur)
            return
        }
        if (mMode == PlayMode.SINGLE) {
            if (userAction) {
                if (supportCycle) {
                    mOriginPosition = (mOriginPosition - 1 + mOriginalSongList.size) % mOriginalSongList.size
                    mCur = mOriginalSongList[mOriginPosition]
                    callback?.invoke(mCur)
                } else {
                    if (mOriginPosition - 1 < 0) {
                        callback?.invoke(null)
                    } else {
                        mOriginPosition--
                        mCur = mOriginalSongList[mOriginPosition]
                        callback?.invoke(mCur)
                    }
                }
            } else {
                callback?.invoke(mCur)
            }
        } else if (mMode == PlayMode.ORDER) {
            if (supportCycle) {
                mOriginPosition = (mOriginPosition - 1 + mOriginalSongList.size) % mOriginalSongList.size
                mCur = mOriginalSongList[mOriginPosition]
                callback?.invoke(mCur)
            } else {
                if (mOriginPosition - 1 < 0) {
                    callback?.invoke(null)
                } else {
                    mOriginPosition--
                    mCur = mOriginalSongList[mOriginPosition]
                    callback?.invoke(mCur)
                }
            }
        } else if (mMode == PlayMode.RANDOM) {
            // 往前播不重新混淆，一直循环
            ensureHasShuffle(mCur)
            mShufflePosition = (mShufflePosition - 1 + mShuffleSongList.size) % mShuffleSongList.size
            mCur = mShuffleSongList[mShufflePosition].second
            callback?.invoke(mCur)
        }
    }

    override fun getCurMode(): PlayMode {
        return mMode
    }

    private fun getFirstSongWhenCurNull(): FeedSongModel? {
        when (mMode) {
            PlayMode.SINGLE, PlayMode.ORDER -> {
                if (mOriginalSongList.isNotEmpty()) {
                    mCur = mOriginalSongList[0]
                    mOriginPosition = 0
                }
            }
            PlayMode.RANDOM -> {
                ensureHasShuffle(null)
                if (mShuffleSongList.isNotEmpty()) {
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
            if (!mShuffleSongList.isNullOrEmpty()) {
                if (avoidSong != null && mShuffleSongList[0].second == avoidSong) {
                    val p = mShuffleSongList.removeAt(0)
                    mShuffleSongList.add(p)
                }
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

fun add2SongPlayModeManager(mSongPlayModeManager: FeedSongPlayModeManager?, list: List<FeedsWatchModel>?, clean: Boolean) {
    val fsms = ArrayList<FeedSongModel>()
    list?.let { wms ->
        wms.forEach { wm ->
            wm.song?.let {
                fsms.add(it)
            }
        }
    }
    mSongPlayModeManager?.setOriginList(fsms, clean)
}