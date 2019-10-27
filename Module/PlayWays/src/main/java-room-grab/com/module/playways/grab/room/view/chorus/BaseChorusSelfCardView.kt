package com.module.playways.grab.room.view.chorus

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.view.ViewStub

import com.alibaba.fastjson.JSON
import com.common.core.userinfo.model.UserInfoModel
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.ExViewStub
import com.module.playways.R
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.grab.room.event.GrabChorusUserStatusChangeEvent
import com.module.playways.grab.room.model.ChorusRoundInfoModel
import com.module.playways.grab.room.model.GrabRoundInfoModel
import com.module.playways.grab.room.model.NewChorusLyricModel
import com.module.playways.grab.room.view.control.SelfSingCardView
import com.module.playways.room.data.H
import com.module.playways.room.song.model.SongModel
import com.component.lyrics.LyricsManager

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import java.util.ArrayList

import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer

import com.module.playways.grab.room.view.chorus.ChorusSelfLyricAdapter.ChorusLineLyricModel.GRAB_TYPE

abstract class BaseChorusSelfCardView(viewStub: ViewStub) : ExViewStub(viewStub) {
    val TAG = "ChorusSelfSingCardView"

    protected var mLyricRecycleView: RecyclerView?=null
    internal var mChorusSelfLyricAdapter: ChorusSelfLyricAdapter?=null
    internal var mSongModel: SongModel? = null

    internal var mDisposable: Disposable? = null

    internal var mLeft = DH()
    internal var mRight = DH()

    var mOverListener:(()->Unit)?=null

    protected abstract val isForVideo: Boolean

    class DH {
        var mUserInfoModel: UserInfoModel? = null
        var mChorusRoundInfoModel: ChorusRoundInfoModel? = null

        fun reset() {
            mUserInfoModel = null
            mChorusRoundInfoModel = null
        }
    }

    override fun init(parentView: View) {
        mLyricRecycleView = mParentView!!.findViewById(R.id.lyric_recycle_view)
        mLyricRecycleView?.layoutManager = LinearLayoutManager(mParentView!!.context, LinearLayoutManager.VERTICAL, false)
        mChorusSelfLyricAdapter = ChorusSelfLyricAdapter(mLeft, mRight, isForVideo)
        mLyricRecycleView?.adapter = mChorusSelfLyricAdapter
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    protected open fun playLyric(): Boolean {
        mLeft.reset()
        mRight.reset()

        if (H.isGrabRoom()) {
            val infoModel = H.grabRoomData!!.realRoundInfo
            if (infoModel != null) {
                val chorusRoundInfoModelList = infoModel.chorusRoundInfoModels
                if (chorusRoundInfoModelList != null && chorusRoundInfoModelList.size >= 2) {
                    val uid1 = chorusRoundInfoModelList[0].userID
                    val uid2 = chorusRoundInfoModelList[1].userID
                    mLeft.mUserInfoModel = H.grabRoomData!!.getPlayerOrWaiterInfo(uid1)
                    mLeft.mChorusRoundInfoModel = chorusRoundInfoModelList[0]
                    mRight.mUserInfoModel = H.grabRoomData!!.getPlayerOrWaiterInfo(uid2)
                    mRight.mChorusRoundInfoModel = chorusRoundInfoModelList[1]
                }
                mSongModel = infoModel.music
            }
        }else if(H.isMicRoom()){
            val infoModel = H.micRoomData!!.realRoundInfo
            if (infoModel != null) {
                val chorusRoundInfoModelList = infoModel.chorusRoundInfoModels
                if (chorusRoundInfoModelList != null && chorusRoundInfoModelList.size >= 2) {
                    val uid1 = chorusRoundInfoModelList[0].userID
                    val uid2 = chorusRoundInfoModelList[1].userID
                    mLeft.mUserInfoModel = H.micRoomData!!.getPlayerOrWaiterInfo(uid1)
                    mLeft.mChorusRoundInfoModel = chorusRoundInfoModelList[0]
                    mRight.mUserInfoModel = H.micRoomData!!.getPlayerOrWaiterInfo(uid2)
                    mRight.mChorusRoundInfoModel = chorusRoundInfoModelList[1]
                }
                mSongModel = infoModel.music
            }
        }
        if (mSongModel == null) {
            return false
        }

        tryInflate()
        setVisibility(View.VISIBLE)
        if (mDisposable != null && !mDisposable!!.isDisposed) {
            mDisposable!!.dispose()
        }

        mDisposable = LyricsManager
                .loadGrabPlainLyric(mSongModel!!.standLrc)
                .subscribe({ result ->
                    val lyrics = ArrayList<ChorusSelfLyricAdapter.ChorusLineLyricModel>()

                    if (U.getStringUtils().isJSON(result)) {
                        val newChorusLyricModel = JSON.parseObject(result, NewChorusLyricModel::class.java)
                        for (itemsBean in newChorusLyricModel.items) {
                            val owner = if (itemsBean.turn == 1) mLeft.mUserInfoModel else mRight.mUserInfoModel

                            if (lyrics.size > 0) {
                                val bean = lyrics[lyrics.size - 1]
                                if (bean.userInfoModel.userId == owner!!.userId) {
                                    bean.lyrics += "\n" + itemsBean.words
                                } else {
                                    lyrics.add(ChorusSelfLyricAdapter.ChorusLineLyricModel(owner, itemsBean.words, GRAB_TYPE))
                                }
                            } else {
                                lyrics.add(ChorusSelfLyricAdapter.ChorusLineLyricModel(owner, itemsBean.words, GRAB_TYPE))
                            }
                        }
                    } else {
                        if (!TextUtils.isEmpty(result)) {
                            val strings = result.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                            var turnLeft = true
                            var i = 0
                            while (i < strings.size) {
                                val owner = if (turnLeft) mLeft.mUserInfoModel else mRight.mUserInfoModel
                                turnLeft = !turnLeft
                                if (i + 1 < strings.size) {
                                    lyrics.add(ChorusSelfLyricAdapter.ChorusLineLyricModel(owner, strings[i] + "\n" + strings[i + 1], GRAB_TYPE))
                                } else {
                                    lyrics.add(ChorusSelfLyricAdapter.ChorusLineLyricModel(owner, strings[i], GRAB_TYPE))
                                }
                                i = i + 2
                            }
                        }
                    }
                    mChorusSelfLyricAdapter?.setSongModel(mSongModel)
                    mChorusSelfLyricAdapter?.computeFlag()
                    mChorusSelfLyricAdapter?.dataList = lyrics
                    // 移到顶部
                    mLyricRecycleView?.scrollToPosition(0)
                }, { throwable -> MyLog.e(TAG, "accept throwable=$throwable") })
        return true
    }

    override fun onViewAttachedToWindow(v: View) {
        super.onViewAttachedToWindow(v)
    }

    override fun onViewDetachedFromWindow(v: View) {
        super.onViewDetachedFromWindow(v)
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: GrabChorusUserStatusChangeEvent) {
        if (mParentView == null || mParentView!!.visibility == View.GONE) {
            return
        }
        if (mLeft.mUserInfoModel != null) {
            if (event.mChorusRoundInfoModel.userID == mLeft.mUserInfoModel!!.userId) {
                mLeft.mChorusRoundInfoModel = event.mChorusRoundInfoModel
            }
        }
        if (mRight.mUserInfoModel != null) {
            if (event.mChorusRoundInfoModel.userID == mRight.mUserInfoModel!!.userId) {
                mRight.mChorusRoundInfoModel = event.mChorusRoundInfoModel
            }
        }
        mChorusSelfLyricAdapter?.computeFlag()
        mChorusSelfLyricAdapter?.notifyDataSetChanged()
    }

    fun destroy() {

    }
}
