package com.module.playways.grab.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.view.ex.ExTextView;
import com.module.rank.R;

/**
 * 抢唱模式操作面板
 * 倒计时 抢 灭 等按钮都在上面
 */
public class GrabOpView extends RelativeLayout {

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
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void clickGrabBtn();

        void clickLightOff();
    }
}
