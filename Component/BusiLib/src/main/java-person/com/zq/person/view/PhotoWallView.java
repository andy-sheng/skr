package com.zq.person.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.component.busilib.R;

public class PhotoWallView extends RelativeLayout {
    RecyclerView mPhotoView;


    public PhotoWallView(Context context) {
        super(context);
        init(context);
    }

    public PhotoWallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PhotoWallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.photo_wall_view_layout, this);

        mPhotoView = (RecyclerView) findViewById(R.id.photo_view);
    }
}
