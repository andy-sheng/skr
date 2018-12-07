package com.module.rankingmode.prepare.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.module.rankingmode.R;

/**
 * 　匹配成功view
 */
public class MatchSucessView extends RelativeLayout {

    public MatchSucessView(Context context) {
        super(context);
        init();
    }

    public MatchSucessView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MatchSucessView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.match_sucess_view, this);

    }
}
