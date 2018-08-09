package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.graphics.Color;
import android.widget.RelativeLayout;

import com.wali.live.watchsdk.R;

public class WatchGameChatTabView extends RelativeLayout{
    public WatchGameChatTabView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.watch_game_tab_chat_layout,this);
        setBackgroundColor(Color.WHITE);
    }

}
