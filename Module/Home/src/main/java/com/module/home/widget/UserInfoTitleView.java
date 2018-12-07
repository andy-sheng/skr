package com.module.home.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import com.module.home.R;
public class UserInfoTitleView extends RelativeLayout {
    public UserInfoTitleView(Context context) {
        this(context, null);
    }

    public UserInfoTitleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserInfoTitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.user_info_title_layout, this);
    }
}
