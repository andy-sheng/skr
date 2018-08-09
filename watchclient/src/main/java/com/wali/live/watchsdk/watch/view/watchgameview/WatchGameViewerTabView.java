package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.widget.RelativeLayout;

import com.wali.live.watchsdk.R;

public class WatchGameViewerTabView extends RelativeLayout{
    public WatchGameViewerTabView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.watch_game_tab_viewer_layout,this);
    }

}
