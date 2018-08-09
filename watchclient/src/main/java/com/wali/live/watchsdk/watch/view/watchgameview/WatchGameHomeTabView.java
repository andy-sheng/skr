package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.widget.RelativeLayout;

import com.wali.live.watchsdk.R;

public class WatchGameHomeTabView extends RelativeLayout{
    public WatchGameHomeTabView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.watch_game_tab_home_layout,this);
    }

}
