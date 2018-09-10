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

/**
 * Created by liuting on 18-9-10.
 */

public class WatchGameMoreTabView extends RelativeLayout implements WatchGameTabView.GameTabChildView, TwoSelectTabLayout.OnTabSelectListener {
    private LinearLayout mTopTabContainer;
    private TwoSelectTabLayout mTwoSelsetTab;
    private NestViewPager mViewPager;

    private ChannelTabPagerAdapter mPagerAdapter;

    private RoomBaseDataModel mMyRoomData;

    public WatchGameMoreTabView(Context context, WatchComponentController componentController) {
        super(context);
        mMyRoomData = componentController.getRoomBaseDataModel();
        init(context, componentController);
    }

    private void init(Context context, WatchComponentController componentController) {
        inflate(context, R.layout.watch_game_more_tab_layout, this);

        mTopTabContainer = (LinearLayout) findViewById(R.id.top_tab_container);
        mTwoSelsetTab = (TwoSelectTabLayout) findViewById(R.id.two_select_tab);
        mViewPager = (NestViewPager) findViewById(R.id.view_pager);

        mTwoSelsetTab.setOnTabSelectListener(this);



        mPagerAdapter = new ChannelTabPagerAdapter();
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setViewPagerCanScroll(false);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mTwoSelsetTab.setTabSelectedWithoutCallBack(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        mPagerAdapter.setChannelList(getTestData());
    }

    private List<? extends ChannelShow> getTestData() {
        List<ChannelShow> list = new ArrayList<>();

        ChannelShow channelShow1 = new ChannelShow();
        channelShow1.setChannelId(27);
        channelShow1.setUiType(ChannelShow.UI_TYPE_COMMAND);
        channelShow1.setUrl(MiLinkCommand.COMMAND_HOT_CHANNEL_LIST);
        list.add(channelShow1);

        ChannelShow channelShow2 = new ChannelShow();
        channelShow2.setChannelId(4001);
        channelShow2.setUiType(ChannelShow.UI_TYPE_COMMAND);
        channelShow2.setUrl(MiLinkCommand.COMMAND_HOT_CHANNEL_LIST);
        list.add(channelShow2);

        return list;
    }

    @Override
    public void select() {

    }

    @Override
    public void unselect() {

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;

    }

    @Override
    public void onLeftTabSelect() {
        if (mViewPager.getChildCount() >= 1) {
            mViewPager.setCurrentItem(0);
        }
    }

    @Override
    public void onRightTabSelect() {
        if (mViewPager.getChildCount() >= 2) {
            mViewPager.setCurrentItem(1);
        }
    }
}
