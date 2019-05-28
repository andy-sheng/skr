package com.module.home.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.module.home.R;

public class GetRedPkgCashView extends RelativeLayout {
    public GetRedPkgCashView(Context context) {
        super(context);
        init();
    }

    public GetRedPkgCashView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GetRedPkgCashView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.get_red_pkg_cash_view_layout, this);

    }
}
