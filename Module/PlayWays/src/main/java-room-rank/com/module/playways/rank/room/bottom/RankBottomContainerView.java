package com.module.playways.rank.room.bottom;

import android.content.Context;
import android.util.AttributeSet;

import com.module.playways.rank.room.view.BottomContainerView;
import com.module.rank.R;

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
