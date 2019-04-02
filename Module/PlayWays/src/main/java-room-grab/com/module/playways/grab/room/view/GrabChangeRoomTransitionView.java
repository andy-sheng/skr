package com.module.playways.grab.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.module.rank.R;

/**
 * 切换房间过场UI
 */
public class GrabChangeRoomTransitionView extends RelativeLayout {
    public final static String TAG = "GrabChangeRoomTransitionView";

    ExTextView mChangeRoomTipTv;
    ExImageView mChangeRoomIv;

    public GrabChangeRoomTransitionView(Context context) {
        super(context);
        init();
    }

    public GrabChangeRoomTransitionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GrabChangeRoomTransitionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_change_room_transition_view, this);
        mChangeRoomTipTv = (ExTextView) this.findViewById(R.id.change_room_tip_tv);
        mChangeRoomIv = (ExImageView) this.findViewById(R.id.change_room_iv);
        setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {

            }
        });
    }


}
