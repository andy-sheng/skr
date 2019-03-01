package com.module.playways.grab.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.module.playways.grab.room.event.GrabMyCoinChangeEvent;
import com.module.playways.grab.room.event.SomeOneLightBurstEvent;
import com.module.rank.R;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class GrabTopView extends RelativeLayout {
    ExTextView mTvChangeRoom;
    ExTextView mTvCoin;
    OnClickChangeRoomListener mOnClickChangeRoomListener;
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

    public void setOnClickChangeRoomListener(OnClickChangeRoomListener onClickChangeRoomListener) {
        mOnClickChangeRoomListener = onClickChangeRoomListener;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabMyCoinChangeEvent event) {
        mTvCoin.setText(event.coin + "");
    }

    public void init() {
        inflate(getContext(), R.layout.grab_top_view, this);
        mTvChangeRoom = (ExTextView) findViewById(R.id.tv_change_room);
        mTvCoin = (ExTextView) findViewById(R.id.tv_coin);

        mTvChangeRoom.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if(mOnClickChangeRoomListener != null){
                    mOnClickChangeRoomListener.onClick();
                }
            }
        });
    }

    public interface OnClickChangeRoomListener{
        void onClick();
    }
}
