package com.module.rankingmode.prepare.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.module.rankingmode.R;

/**
 * 开始匹配view
 *
 */
public class MatchStartView extends RelativeLayout {

    public MatchStartView(Context context) {
        super(context);
        init();
    }

    public MatchStartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MatchStartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.match_start_view, this);
    }
}
