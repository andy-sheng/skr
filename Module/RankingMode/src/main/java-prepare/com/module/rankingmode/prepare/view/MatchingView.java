package com.module.rankingmode.prepare.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.module.rankingmode.R;

/**
 * 匹配中view
 */
public class MatchingView extends RelativeLayout {
    public MatchingView(Context context) {
        super(context);
        init();
    }

    public MatchingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MatchingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.match_ing_view, this);
    }
}
