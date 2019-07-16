package com.module.playways.grab.room.top;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.module.playways.R;

/**
 * 房主说话时底部 点点点 动画view
 */
public class SpeakingTipsAnimationView extends AppCompatImageView {
    public final static String TAG = "SpeakingDotAnimationView";

    static final int MSG_START = 2;
    static final int MSG_HIDE = 1;

    int mIndex = 0;
    int mType = 0;

    int mAnimationRes[] = new int[]{
            R.drawable.yuyin_shengwen1,
            R.drawable.yuyin_shengwen2,
            R.drawable.yuyin_shengwen3,
    };

    int mMsgAnimationRes[] = new int[]{
            R.drawable.msg_yuyin_1,
            R.drawable.msg_yuyin_2,
            R.drawable.msg_yuyin_3
    };

    Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_START) {
                if (mType == 1) {
                    setImageResource(mMsgAnimationRes[mIndex++ % mMsgAnimationRes.length]);
                } else {
                    setImageResource(mAnimationRes[mIndex++ % mAnimationRes.length]);
                }
                mUiHandler.sendEmptyMessageDelayed(MSG_START, 400);
            } else if (msg.what == MSG_HIDE) {
                mUiHandler.removeMessages(MSG_START);
                if (mType == 1) {
                    setImageResource(R.drawable.msg_yuyin_3);
                } else {
                    setVisibility(GONE);
                }

            }
        }
    };

    public SpeakingTipsAnimationView(Context context) {
        super(context);
        init(context, null);
    }

    public SpeakingTipsAnimationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    void init(Context context, @Nullable AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.speaking);
        mType = typedArray.getInt(R.styleable.speaking_animationtype, 0);
        typedArray.recycle();

        if (mType == 1) {
            setVisibility(VISIBLE);
            setImageResource(R.drawable.msg_yuyin_3);
        }
    }


    public void show(int duration) {
        setVisibility(VISIBLE);
        mUiHandler.removeMessages(MSG_START);
        mUiHandler.sendEmptyMessage(MSG_START);

        mUiHandler.removeMessages(MSG_HIDE);
        mUiHandler.sendEmptyMessageDelayed(MSG_HIDE, duration);
    }

    public void hide() {
        mUiHandler.removeCallbacksAndMessages(null);
        if (mType == 1) {
            setImageResource(R.drawable.msg_yuyin_3);
        } else {
            setVisibility(GONE);
        }

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mUiHandler.removeCallbacksAndMessages(null);
    }
}
