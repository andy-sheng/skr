package com.zq.person.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.component.busilib.R;

/**
 * 作品墙view
 */
public class ProducationWallView extends RelativeLayout {
    public ProducationWallView(Context context) {
        super(context);
        init(context);
    }

    public ProducationWallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ProducationWallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.producation_wall_view_layout, this);
    }
}
