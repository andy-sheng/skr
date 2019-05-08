package com.module.playways.grab.room.top;

import android.content.Context;
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
    int mAnimationRes[] = new int[]{
            R.drawable.yuyin_shengwen1,
            R.drawable.yuyin_shengwen2,
            R.drawable.yuyin_shengwen3,
    };

    Handler mUiHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==MSG_START){
                setImageResource(mAnimationRes[mIndex++ % mAnimationRes.length]);
                mUiHandler.sendEmptyMessageDelayed(MSG_START,400);
            }else if(msg.what==MSG_HIDE){
                mUiHandler.removeMessages(MSG_START);
                setVisibility(GONE);
            }
        }
    };

    public SpeakingTipsAnimationView(Context context) {
        super(context);
        init();
    }

    public SpeakingTipsAnimationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void init() {

    }


    public void show(int duration) {
        setVisibility(VISIBLE);
        mUiHandler.removeMessages(MSG_START);
        mUiHandler.sendEmptyMessage(MSG_START);

        mUiHandler.removeMessages(MSG_HIDE);
        mUiHandler.sendEmptyMessageDelayed(MSG_HIDE,duration);
    }

    public void hide() {
        mUiHandler.removeCallbacksAndMessages(null);
        setVisibility(GONE);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mUiHandler.removeCallbacksAndMessages(null);
    }
}
