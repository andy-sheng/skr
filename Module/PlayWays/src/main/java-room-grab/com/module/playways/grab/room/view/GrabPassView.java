package com.module.playways.grab.room.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.module.rank.R;

/**
 * 不唱了界面
 */
public class GrabPassView extends RelativeLayout {
    public final static String TAG = "GrabPassView";
    public static final int MSG_ANIMATION_SHOW = 1;
    ExImageView mIvPass;

    Listener mListener;

    Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == MSG_ANIMATION_SHOW) {
                TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
                animation.setDuration(200);
                animation.setRepeatMode(Animation.REVERSE);
                animation.setInterpolator(new OvershootInterpolator());
                animation.setFillAfter(true);
                startAnimation(animation);
                setVisibility(VISIBLE);
            }

        }
    };

    public GrabPassView(Context context) {
        super(context);
        init();
    }

    public GrabPassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GrabPassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private void init() {
        inflate(getContext(), R.layout.grab_pass_view_layout, this);
        mIvPass = (ExImageView) findViewById(R.id.pass_iv);
        mIvPass.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.giveUp();
                }
            }
        });
    }

    public void delayShowPassView() {
        hideWithAnimation(false);
        mUiHandler.removeMessages(MSG_ANIMATION_SHOW);
        mUiHandler.sendEmptyMessageDelayed(MSG_ANIMATION_SHOW,5000);
    }

    public void passSuccess() {
        mUiHandler.removeMessages(MSG_ANIMATION_SHOW);
        hideWithAnimation(true);
    }

    public void hideWithAnimation(boolean needAnim) {
        MyLog.d(TAG, "hideWithAnimation" + " needAnim=" + needAnim);
        mUiHandler.removeMessages(MSG_ANIMATION_SHOW);
        clearAnimation();
        setVisibility(GONE);
//        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
//                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
//        animation.setDuration(needAnim ? 200 : 0);
//        animation.setRepeatMode(Animation.REVERSE);
//        animation.setInterpolator(new OvershootInterpolator());
//        animation.setFillAfter(true);
//        startAnimation(animation);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mUiHandler.removeCallbacksAndMessages(null);
    }

    public interface Listener {
        void giveUp();
    }
}
