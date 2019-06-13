package com.module.playways.grab.room.top;

import android.animation.AnimatorSet;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.component.busilib.constans.GrabRoomType;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.view.chorus.VideoChorusSelfSingCardView;
import com.module.playways.grab.room.view.normal.view.VideoSelfSingLyricView;

public class GrabTopView extends FrameLayout {

    ExTextView mTvChangeRoom;

    ImageView mIvVoiceSetting;
    ImageView mCameraIv;
    ImageView mGameRuleIv;
    ImageView mFeedBackIv;

    ExTextView mExitTv;

    Listener mOnClickChangeRoomListener;
    GrabRoomData mGrabRoomData;

    ExImageView mIvHzIcon;

    AnimatorSet mAnimatorSet;  //金币加减的动画
    AnimatorSet mHzAnimatorSet;  //金币加减的动画

    VideoChorusSelfSingCardView mVideoChorusSelfSingCardView;
    VideoSelfSingLyricView mVideoSelfSingLyricView;
    RelativeLayout mOpContainer;

    public GrabTopView(Context context) {
        super(context);
        init();
    }

    public GrabTopView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GrabTopView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setListener(Listener onClickChangeRoomListener) {
        mOnClickChangeRoomListener = onClickChangeRoomListener;
    }

    public void init() {
        inflate(getContext(), R.layout.grab_top_view, this);
        mOpContainer = (RelativeLayout) findViewById(R.id.op_container);
        mTvChangeRoom = (ExTextView) findViewById(R.id.tv_change_room);
        mCameraIv = (ImageView) findViewById(R.id.camera_iv);
        mGameRuleIv = (ImageView) findViewById(R.id.game_rule_iv);
        mFeedBackIv = (ImageView) findViewById(R.id.feed_back_iv);
        mExitTv = (ExTextView) findViewById(R.id.exit_tv);
        mIvVoiceSetting = (ImageView) findViewById(R.id.iv_voice_setting);
        mIvHzIcon = (ExImageView) findViewById(R.id.iv_hz_icon);

        mTvChangeRoom.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mOnClickChangeRoomListener != null) {
                    mOnClickChangeRoomListener.changeRoom();
                }
            }
        });

        mIvVoiceSetting.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mOnClickChangeRoomListener != null) {
                    mOnClickChangeRoomListener.onClickVoiceAudition();
                }
            }
        });

        mCameraIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mOnClickChangeRoomListener != null) {
                    mOnClickChangeRoomListener.onClickCamera();
                }
            }
        });

        mGameRuleIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mOnClickChangeRoomListener != null) {
                    mOnClickChangeRoomListener.onClickGameRule();
                }
            }
        });

        mFeedBackIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mOnClickChangeRoomListener != null) {
                    mOnClickChangeRoomListener.onClickFeedBack();
                }
            }
        });

        mExitTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mOnClickChangeRoomListener != null) {
                    mOnClickChangeRoomListener.closeBtnClick();
                }
            }
        });
    }

    public void setRoomData(GrabRoomData modelBaseRoomData) {
        mGrabRoomData = modelBaseRoomData;

        if (mGrabRoomData.isOwner()) {
            // 是房主，肯定不能切换房间
            setChangeRoomBtnVisiable(false);
        } else {
            // 观众的话，私密房间也不能切
            if (mGrabRoomData.getRoomType() == GrabRoomType.ROOM_TYPE_SECRET ||
                    mGrabRoomData.getRoomType() == GrabRoomType.ROOM_TYPE_FRIEND) {
                setChangeRoomBtnVisiable(false);
            } else {
                setChangeRoomBtnVisiable(true);
            }
        }

        if (mGrabRoomData.getRoomType() == GrabRoomType.ROOM_TYPE_GUIDE) {
            // 新手房
            setChangeRoomBtnVisiable(false);
            mIvVoiceSetting.setVisibility(GONE);
        }
    }

    public void playModel() {

    }

    private void inflateChorusSelfCardView() {
        if (mVideoChorusSelfSingCardView == null) {
            ((ViewStub) (findViewById(R.id.chorus_lyric_view_stub))).inflate();
            mVideoChorusSelfSingCardView = findViewById(R.id.video_chorus_card_view);
        }
    }

    private void inflateVideoSelfSingLyricView() {
        if (mVideoSelfSingLyricView == null) {
            ((ViewStub) (findViewById(R.id.video_self_lyric_view_stub))).inflate();
            mVideoSelfSingLyricView = findViewById(R.id.video_self_lyric_view);
        }
    }

    /**
     * 切换房间按钮是否可见
     *
     * @param visiable
     */
    void setChangeRoomBtnVisiable(boolean visiable) {
        if (visiable) {
            mTvChangeRoom.setVisibility(VISIBLE);
        } else {
            mTvChangeRoom.setVisibility(GONE);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimatorSet != null) {
            mAnimatorSet.removeAllListeners();
            mAnimatorSet.cancel();
        }

        if (mHzAnimatorSet != null) {
            mHzAnimatorSet.removeAllListeners();
            mHzAnimatorSet.cancel();
        }
    }

    public interface Listener {
        void changeRoom();

        void closeBtnClick();

        void onVoiceChange(boolean voiceOpen);

        void onClickGameRule();

        void onClickFeedBack();

        void onClickVoiceAudition();

        void onClickCamera();
    }
}
