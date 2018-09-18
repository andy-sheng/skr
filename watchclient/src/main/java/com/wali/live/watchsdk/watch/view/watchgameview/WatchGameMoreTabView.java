package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.base.view.NestViewPager;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.adapter.ChannelTabPagerAdapter;
import com.wali.live.watchsdk.channel.list.model.ChannelShow;
import com.wali.live.watchsdk.component.WatchComponentController;

import java.util.ArrayList;
import java.util.List;

import static com.wali.live.watchsdk.channel.data.ChannelDataStore.GAME_WATCH_CHANNEL_FROM_PORTRAIT;

/**
 * Created by liuting on 18-9-10.
 */

public class WatchGameMoreTabView extends WatchGameMoreLiveView implements WatchGameTabView.GameTabChildView{
    protected LinearLayout mTopTabContainer;

    public WatchGameMoreTabView(Context context, WatchComponentController componentController) {
        super(context, componentController);
    }

    @Override
    protected void inflateLayout(Context context) {
        inflate(context, R.layout.watch_game_more_tab_layout, this);
    }

    @Override
    protected void init(Context context, WatchComponentController componentController) {
        super.init(context, componentController);
        mTopTabContainer = (LinearLayout) findViewById(R.id.top_tab_container);
    }

    @Override
    protected int getLiveReqFrom() {
        return GAME_WATCH_CHANNEL_FROM_PORTRAIT;
    }

    @Override
    public void select() {

    }

    @Override
    public void unselect() {

    }

    @Override
    public void stopView() {
        destroyView();
    }
}
