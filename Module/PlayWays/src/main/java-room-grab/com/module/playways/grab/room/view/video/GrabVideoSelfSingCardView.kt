package com.module.playways.grab.room.view.video

import android.view.View
import android.view.ViewStub

import com.module.playways.R
import com.module.playways.RoomDataUtils
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.grab.room.view.GrabRootView
import com.module.playways.grab.room.view.chorus.VideoChorusSelfSingCardView
import com.module.playways.grab.room.view.control.SelfSingCardView
import com.module.playways.grab.room.view.minigame.VideoMiniGameSelfSingCardView
import com.module.playways.grab.room.view.normal.VideoNormalSelfSingCardView
import com.module.playways.grab.room.view.pk.VideoPkSelfSingCardView

class GrabVideoSelfSingCardView(rootView: View, internal var mRoomData: GrabRoomData) {
    internal var mVideoChorusSelfSingCardView: VideoChorusSelfSingCardView?=null
    internal var mVideoNormalSelfSingCardView: VideoNormalSelfSingCardView?=null
    internal var mVideoPkSelfSingCardView: VideoPkSelfSingCardView?=null
    internal var mVideoMiniGameSelfSingCardView: VideoMiniGameSelfSingCardView?=null

    init {
        run {
            val viewStub = rootView.findViewById<ViewStub>(R.id.grab_video_normal_self_sing_card_stub)
            mVideoNormalSelfSingCardView = VideoNormalSelfSingCardView(viewStub, rootView as GrabRootView)
        }
        run {
            val viewStub = rootView.findViewById<ViewStub>(R.id.grab_video_pk_self_sing_card_stub)
            mVideoPkSelfSingCardView = VideoPkSelfSingCardView(viewStub)
        }
        run {
            val viewStub = rootView.findViewById<ViewStub>(R.id.grab_video_chorus_self_sing_card_stub)
            mVideoChorusSelfSingCardView = VideoChorusSelfSingCardView(viewStub, rootView as GrabRootView)
        }
        run {
            val viewStub = rootView.findViewById<ViewStub>(R.id.grab_video_mini_game_self_sing_card_stub)
            mVideoMiniGameSelfSingCardView = VideoMiniGameSelfSingCardView(viewStub, mRoomData)
        }

    }

    fun setVisibility(visibility: Int) {
        if (visibility == View.GONE) {
            mVideoNormalSelfSingCardView?.setVisibility(View.GONE)
            mVideoPkSelfSingCardView?.setVisibility(View.GONE)
            mVideoChorusSelfSingCardView?.setVisibility(View.GONE)
            mVideoMiniGameSelfSingCardView?.setVisibility(View.GONE)
        } else if (visibility == View.VISIBLE) {
            when {
                mRoomData?.realRoundInfo?.isChorusRound == true -> {
                    mVideoNormalSelfSingCardView?.setVisibility(View.GONE)
                    mVideoPkSelfSingCardView?.setVisibility(View.GONE)
                    mVideoChorusSelfSingCardView?.setVisibility(View.VISIBLE)
                    mVideoMiniGameSelfSingCardView?.setVisibility(View.GONE)
                }
                mRoomData?.realRoundInfo?.isPKRound == true -> {
                    mVideoNormalSelfSingCardView?.setVisibility(View.GONE)
                    mVideoPkSelfSingCardView?.setVisibility(View.VISIBLE)
                    mVideoChorusSelfSingCardView?.setVisibility(View.GONE)
                    mVideoMiniGameSelfSingCardView?.setVisibility(View.GONE)
                }
                mRoomData?.realRoundInfo?.isMiniGameRound == true -> {
                    mVideoNormalSelfSingCardView?.setVisibility(View.GONE)
                    mVideoPkSelfSingCardView?.setVisibility(View.GONE)
                    mVideoChorusSelfSingCardView?.setVisibility(View.GONE)
                    mVideoMiniGameSelfSingCardView?.setVisibility(View.VISIBLE)
                }
                else -> {
                    mVideoNormalSelfSingCardView?.setVisibility(View.VISIBLE)
                    mVideoPkSelfSingCardView?.setVisibility(View.GONE)
                    mVideoChorusSelfSingCardView?.setVisibility(View.GONE)
                    mVideoMiniGameSelfSingCardView?.setVisibility(View.GONE)
                }
            }
        }
    }

    fun playLyric() {
        when {
            mRoomData?.realRoundInfo?.isChorusRound == true -> mVideoChorusSelfSingCardView?.playLyric()
            mRoomData?.realRoundInfo?.isPKRound == true -> mVideoPkSelfSingCardView?.playLyric()
            mRoomData?.realRoundInfo?.isMiniGameRound == true -> mVideoMiniGameSelfSingCardView?.playLyric()
            else -> mVideoNormalSelfSingCardView?.playLyric()
        }
    }

    fun destroy() {
        mVideoChorusSelfSingCardView?.destroy()
        mVideoNormalSelfSingCardView?.destroy()
        mVideoMiniGameSelfSingCardView?.destroy()
        mVideoPkSelfSingCardView?.destroy()
    }

    fun setListener(listener: ()->Unit) {
        mVideoChorusSelfSingCardView?.mOverListener = listener
        mVideoNormalSelfSingCardView?.mOverListener = listener
        mVideoMiniGameSelfSingCardView?.mOverListener = listener
        mVideoPkSelfSingCardView?.mOverListener  = listener
    }

    //    public View getRealView() {
    //        if(mVideoNormalSelfSingCardView.getVisibility()==View.VISIBLE){
    //            return mVideoNormalSelfSingCardView.getRealView();
    //        }
    //        if(mVideoChorusSelfSingCardView.getVisibility()==View.VISIBLE){
    //            return mVideoChorusSelfSingCardView.getRealView();
    //        }
    //        return null;
    //    }
}
