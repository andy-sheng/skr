package com.wali.live.livesdk.live.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.log.MyLog;
import com.wali.live.livesdk.R;

/**
 * Created by lan on 16/2/27.
 */
public class CountDownView extends RelativeLayout {
    private static final String TAG = CountDownView.class.getSimpleName();

    private static final int NUMBER_COUNT_DOWN = 3;

    private TextView mNumberTv;
    private int mNumber = NUMBER_COUNT_DOWN;

    public CountDownView(Context context) {
        super(context);
        init(context);
    }

    public CountDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CountDownView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.count_down_view, this);

        initViews();
    }

    private void initViews() {
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mNumberTv = (TextView) findViewById(R.id.number_tv);
    }

    public void startCountDown() {
        if (mNumber <= 0) {
            setVisibility(View.GONE);
            MyLog.d(TAG, "CountDownView GONE");
            return;
        }

        mNumberTv.setScaleX(10f);
        mNumberTv.setScaleY(10f);
        mNumberTv.animate().scaleX(3f).scaleY(3f).setStartDelay(100).setDuration(250).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                MyLog.d(TAG, "startCountDown onAnimationEnd");
                mNumberTv.animate().scaleX(4f).scaleY(4f).setDuration(150).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if ((--mNumber) > 0) {
                            MyLog.d(TAG, "--------CountDownView GONE");
                            mNumberTv.setVisibility(View.GONE);
                            mNumberTv.setText(String.valueOf(mNumber));
                            startCountDown();
                        } else {
                            setVisibility(View.GONE);
                            MyLog.d(TAG, "CountDownView GONE");
                        }
                    }
                });
            }

            @Override
            public void onAnimationStart(Animator animation) {
                MyLog.d(TAG, "startCountDown onAnimationStart");
                if (mNumber == NUMBER_COUNT_DOWN) {
                    setVisibility(View.VISIBLE);
                    MyLog.d(TAG, "CountDownView VISIBLE");
                    mNumberTv.setText(String.valueOf(mNumber));
                }
                mNumberTv.setVisibility(View.VISIBLE);
            }
        });
    }
}
