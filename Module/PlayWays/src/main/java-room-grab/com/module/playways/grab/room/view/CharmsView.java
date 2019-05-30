package com.module.playways.grab.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.module.playways.R;

// 展示魅力值的view
public class CharmsView extends RelativeLayout {

    ImageView mCharmIv;
    TextView mCharmTv;

    public CharmsView(Context context) {
        super(context);
        init();
    }

    public CharmsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CharmsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_charms_view_layout, this);

        mCharmIv = (ImageView) findViewById(R.id.charm_iv);
        mCharmTv = (TextView) findViewById(R.id.charm_tv);
    }
}
