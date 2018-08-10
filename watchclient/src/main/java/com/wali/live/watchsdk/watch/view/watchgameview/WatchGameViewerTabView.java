package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.widget.RelativeLayout;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.WatchComponentController;

public class WatchGameViewerTabView extends RelativeLayout{

    public WatchGameViewerTabView(Context context, WatchComponentController componentController) {
        super(context);
        init(context, componentController);
    }

    private void init(Context context, WatchComponentController componentController) {
        inflate(context, R.layout.watch_game_tab_viewer_layout,this);
    }

}
