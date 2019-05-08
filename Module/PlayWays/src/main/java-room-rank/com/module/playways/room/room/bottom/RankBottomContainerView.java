package com.module.playways.room.room.bottom;

import android.content.Context;
import android.util.AttributeSet;

import com.module.playways.room.room.view.BottomContainerView;
import com.module.playways.R;

public class RankBottomContainerView extends BottomContainerView {
    public RankBottomContainerView(Context context) {
        super(context);
    }

    public RankBottomContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getLayout() {
        return R.layout.bottom_container_view_layout;
    }


}
