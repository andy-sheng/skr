package com.module.playways.grab.room.view.minigame

import android.graphics.Color
import android.view.View
import android.view.ViewStub
import android.widget.ScrollView
import android.widget.TextView

import com.alibaba.fastjson.JSON
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.ExViewStub
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.grab.room.model.GrabRoundInfoModel
import com.module.playways.grab.room.model.NewChorusLyricModel
import com.module.playways.grab.room.view.control.SelfSingCardView
import com.module.playways.room.song.model.MiniGameInfoModel
import com.zq.live.proto.Common.EMiniGamePlayType
import com.component.lyrics.LyricsManager

import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer

abstract class BaseMiniGameSelfSingCardView(viewStub: ViewStub, internal var mGrabRoomData: GrabRoomData?) : ExViewStub(viewStub) {
    val TAG = "BaseMiniGameSelfSingCardView"
    internal var mMiniGameInfoModel: MiniGameInfoModel? = null
    internal var mMiniGameSongUrl: String?=null
    var mOverListener: (()->Unit)?=null

    //    CharmsView mCharmsView;
    internal var mSvLyric: ScrollView?=null
    internal var mAvatarIv: SimpleDraweeView?=null
    internal var mFirstTipsTv: TextView?=null
    internal var mTvLyric: TextView?=null    //用来显示游戏内容
    //    SingCountDownView mSingCountDownView;

    internal var mDisposable: Disposable? = null

    override fun init(parentView: View) {
        mSvLyric = mParentView!!.findViewById(R.id.sv_lyric)
        mAvatarIv = mParentView!!.findViewById(R.id.avatar_iv)
        mFirstTipsTv = mParentView!!.findViewById(R.id.first_tips_tv)
        mTvLyric = mParentView!!.findViewById(R.id.tv_lyric)
    }


    open fun playLyric(): Boolean {
        val infoModel = mGrabRoomData?.realRoundInfo
        if (infoModel == null) {
            MyLog.w(TAG, "infoModel 是空的")
            return false
        }

        if (infoModel.music == null) {
            MyLog.w(TAG, "songModel 是空的")
            return false
        }
        //        mCharmsView.bindData(mGrabRoomData, (int) MyUserInfoManager.getInstance().getUid());
        mMiniGameInfoModel = infoModel.music.miniGame
        if (mMiniGameInfoModel == null) {
            MyLog.w(TAG, "MiniGame 是空的")
            return false
        }

        tryInflate()
        mSvLyric?.scrollTo(0, 0)
        //        int totalTs = infoModel.getSingTotalMs();
        //        mSingCountDownView.setTagTvText(mMiniGameInfoModel.getGameName());
        //        mSingCountDownView.startPlay(0, totalTs, true);

        if (infoModel.miniGameRoundInfoModels != null && infoModel.miniGameRoundInfoModels.size > 0) {
            val userInfoModel = mGrabRoomData?.getPlayerOrWaiterInfo(infoModel.miniGameRoundInfoModels[0].userID)
            if (userInfoModel != null) {
                AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(userInfoModel.avatar)
                        .setCircle(true)
                        .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                        .setBorderColor(Color.WHITE)
                        .build())
                var name = UserInfoManager.getInstance().getRemarkName(userInfoModel.userId, userInfoModel.nickname)
                if (name.length > 7) {
                    name = name.substring(0, 7)
                }
                mFirstTipsTv?.text = "【$name】先开始"
            } else {
                MyLog.w(TAG, "playLyric userInfoModel = null")
            }
        } else {
            MyLog.w(TAG, "playLyric getMINIGameRoundInfoModels = null")
        }


        if (mMiniGameInfoModel!!.gamePlayType == EMiniGamePlayType.EMGP_SONG_DETAIL.value) {
            // TODO: 2019-05-29 带歌词的
            mMiniGameSongUrl = mMiniGameInfoModel!!.songInfo.songURL
            setLyric(mTvLyric, mMiniGameSongUrl)
        } else {
            // TODO: 2019-05-29 不带歌词的,待补充
            mTvLyric?.text = mMiniGameInfoModel!!.displayGameRule
        }
        return true
    }

    protected fun setLyric(lyricTv: TextView?, lyricUrl: String?) {
        LyricsManager
                .loadGrabPlainLyric(lyricUrl)
                .subscribe({ o ->
                    lyricTv?.text = ""
                    if (U.getStringUtils().isJSON(o)) {
                        val newChorusLyricModel = JSON.parseObject(o, NewChorusLyricModel::class.java)
                        lyricTv?.append(mMiniGameInfoModel!!.displayGameRule)
                        lyricTv?.append("\n")
                        var i = 0
                        while (i < newChorusLyricModel.items.size && i < 2) {
                            lyricTv?.append(newChorusLyricModel.items[i].words)
                            if (i == 0) {
                                lyricTv?.append("\n")
                            }
                            i++
                        }
                    } else {
                        lyricTv?.append(mMiniGameInfoModel!!.displayGameRule)
                        lyricTv?.append("\n")
                        lyricTv?.append(o)
                    }
                }, { throwable -> MyLog.e(throwable) })
    }

    override fun onViewDetachedFromWindow(v: View) {
        super.onViewDetachedFromWindow(v)
        if (mDisposable != null) {
            mDisposable!!.dispose()
        }
    }

    fun destroy() {
        if (mDisposable != null) {
            mDisposable!!.dispose()
        }
    }
}
