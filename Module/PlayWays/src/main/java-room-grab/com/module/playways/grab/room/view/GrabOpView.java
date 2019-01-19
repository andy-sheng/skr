package com.module.playways.grab.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.utils.HandlerTaskTimer;
import com.common.view.ex.ExTextView;
import com.module.rank.R;

/**
 * 抢唱模式操作面板
 * 倒计时 抢 灭 等按钮都在上面
 */
public class GrabOpView extends RelativeLayout {

    public static final int STATUS_GRAP = 1;
    public static final int STATUS_COUNT_DOWN = 2;
    public static final int STATUS_LIGHT_OFF = 3;

    int mStatus;

    public ExTextView mDescTv;
    Listener mListener;

    public GrabOpView(Context context) {
        super(context);
        init();
    }

    public GrabOpView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GrabOpView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_op_view_layout, this);
        mDescTv = (ExTextView) this.findViewById(R.id.desc_tv);
        mDescTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStatus == STATUS_GRAP) {
                    if (mListener != null) {
                        mListener.clickGrabBtn();
                    }
                } else if (mStatus == STATUS_LIGHT_OFF) {
                    if (mListener != null) {
                        mListener.clickLightOff();
                    }
                }
            }
        });
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void playCountDown(int num) {
        // 播放 3 2 1 导唱倒计时
        mDescTv.setClickable(false);
        mStatus = STATUS_COUNT_DOWN;
        HandlerTaskTimer.newBuilder().interval(1000)
                .take(num)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        int num1 = num - integer + 1;
                        switch (num1) {
                            case 3:
                                break;
                            case 2:
                                break;
                            case 1:
                                break;
                        }
                        mDescTv.setText(num1 + "");
//                        mGrabOpBtn.setBackgroundResource(R.drawable.yanchangjiemian_dabian);
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        if (mListener != null) {
                            mListener.countDownOver();
                        }
                        // 按钮变成抢唱，且可点击
                        mDescTv.setClickable(true);
                        mDescTv.setText("抢");
                        mStatus = STATUS_GRAP;
                    }
                });
    }

    public interface Listener {
        void clickGrabBtn();

        void clickLightOff();

        void countDownOver();
    }
}
