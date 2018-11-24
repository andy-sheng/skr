package com.wali.live.modulewatch.watch.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.module.RouterConstants;
import com.common.core.channel.HostChannelManager;
import com.common.core.login.interceptor.JumpInterceptor;
import com.common.log.MyLog;
import com.common.utils.U;
import com.wali.live.modulewatch.R;
import com.wali.live.modulewatch.base.BaseComponentSdkActivity;
import com.wali.live.modulewatch.watch.fragment.BaseWatchFragment;
import com.wali.live.modulewatch.watch.fragment.WatchGameFragment;
import com.wali.live.modulewatch.watch.model.roominfo.RoomBaseDataModel;
import com.wali.live.modulewatch.watch.model.roominfo.RoomInfo;
import com.wali.live.modulewatch.watch.normal.WatchComponentController;
import com.wali.live.modulewatch.watch.assist.WatchSdkActivityInterface;

import java.util.ArrayList;

@Route(path = RouterConstants.ACTIVITY_WATCH, extras = JumpInterceptor.NO_NEED_LOGIN)
public class WatchSdkAcitivity extends BaseComponentSdkActivity implements WatchSdkActivityInterface{

    public static final String EXTRA_ROOM_INFO_LIST = "extra_room_info_list";
    public static final String EXTRA_ROOM_INFO_POSITION = "extra_room_info_position";


    private BaseWatchFragment mBaseWatchFragment;

    private ArrayList<RoomInfo> mRoomInfoList;
    private int mRoomInfoPosition;

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.watch_main_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        Intent data = getIntent();
        if (data == null){
            U.getToastUtil().showShort("missing Intent");
            MyLog.e(TAG, "Intent is null");
            return;
        }

        mRoomInfo = data.getParcelableExtra(EXTRA_ROOM_INFO);
        mRoomInfoList = data.getParcelableArrayListExtra(EXTRA_ROOM_INFO_LIST);
        mRoomInfoPosition = data.getIntExtra(EXTRA_ROOM_INFO_POSITION, 0);

        if (mRoomInfo == null && mRoomInfoList != null) {
            mRoomInfo = mRoomInfoList.get(mRoomInfoPosition);
        }
        if (mRoomInfo == null) {
            U.getToastUtil().showShort("missing RoomInfo");
            MyLog.e(TAG, "mRoomInfo is null");
            return;
        }

        // 填充 MyRoomData
        mMyRoomData.setRoomId(mRoomInfo.getLiveId());
        mMyRoomData.setUid(mRoomInfo.getPlayerId());
        mMyRoomData.setVideoUrl(mRoomInfo.getVideoUrl());
        mMyRoomData.setLiveType(mRoomInfo.getLiveType());
        mMyRoomData.setGameId(mRoomInfo.getGameId());
        Boolean enableShare = (Boolean) HostChannelManager.getInstance().get(HostChannelManager.KEY_SHARE_ENABLE);
        if (enableShare == null) {
            enableShare = false;
        }
        mMyRoomData.setEnableShare(enableShare);
        Boolean enableFollow = (Boolean) HostChannelManager.getInstance().get(HostChannelManager.KEY_FOLLOW_ENABLE);
        if (enableFollow == null) {
            enableFollow = false;
        }
        mMyRoomData.setEnableRelationChain(enableFollow);
        mMyRoomData.setChannelId(mRoomInfo.getPageChannelId());


        String tag = null;
//        if (mMyRoomData.getLiveType() != LiveManager.TYPE_LIVE_GAME
//                && mMyRoomData.getLiveType() != LiveManager.TYPE_LIVE_HUYA) {
//            mBaseWatchFragment = new WatchNormalFragment();
//            tag = WatchNormalFragment.class.getSimpleName();
//        } else {
//            mBaseWatchFragment = new WatchGameFragment();
//            tag = WatchGameFragment.class.getSimpleName();
//        }

        mBaseWatchFragment = new WatchGameFragment();
        tag = WatchGameFragment.class.getSimpleName();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.container, mBaseWatchFragment, tag);
        ft.commitAllowingStateLoss();
    }

    @Override
    protected void destroy() {
        super.destroy();
    }

    @Override
    protected void trySendDataWithServerOnce() {

    }

    @Override
    protected void tryClearData() {

    }

    @Override
    public void onKickEvent(String msg) {

    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public boolean canSlide() {
        return false;
    }

    @Override
    public RoomInfo getRoomInfo() {
        return null;
    }

    @Override
    public RoomBaseDataModel getRoomBaseData() {
        return null;
    }

    @Override
    public WatchComponentController getController() {
        return null;
    }

    @Override
    public boolean isDisplayLandscape() {
        return false;
    }
}
