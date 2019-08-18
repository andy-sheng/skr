package com.module.playways.grab.room.top;

import android.animation.AnimatorSet;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.component.busilib.constans.GrabRoomType;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;

public class GrabTopOpView extends RelativeLayout {

    ExTextView mTvChangeRoom;

    ImageView mIvVoiceSetting;
    ImageView mCameraIv;
    View mDivider;
    ImageView mGameRuleIv;
    ImageView mFeedBackIv;

    ExTextView mExitTv;

    Listener mOnClickChangeRoomListener;
    GrabRoomData mGrabRoomData;

    AnimatorSet mAnimatorSet;  //金币加减的动画
    AnimatorSet mHzAnimatorSet;  //金币加减的动画


    public GrabTopOpView(Context context) {
        super(context);
        init();
    }

    public GrabTopOpView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GrabTopOpView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setListener(Listener onClickChangeRoomListener) {
        mOnClickChangeRoomListener = onClickChangeRoomListener;
    }

    public void init() {
        inflate(getContext(), R.layout.grab_top_op_view, this);
        mTvChangeRoom = (ExTextView) findViewById(R.id.tv_change_room);
        mCameraIv = (ImageView) findViewById(R.id.camera_iv);
        mDivider = (View) findViewById(R.id.divider);
        mGameRuleIv = (ImageView) findViewById(R.id.game_rule_iv);
        mFeedBackIv = (ImageView) findViewById(R.id.feed_back_iv);
        mExitTv = (ExTextView) findViewById(R.id.exit_tv);
        mIvVoiceSetting = (ImageView) findViewById(R.id.iv_voice_setting);

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
        if (!mGrabRoomData.isVideoRoom()) {
            mCameraIv.setVisibility(GONE);
            mDivider.setVisibility(GONE);
            mIvVoiceSetting.setBackground(U.getDrawable(R.drawable.yichangdaodi_yinyue_audio));
        }
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
