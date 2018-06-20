package com.wali.live.watchsdk.channel.holder.listener;

import android.app.Activity;
import android.net.Uri;
import android.text.TextUtils;

import com.base.log.MyLog;
import com.wali.live.watchsdk.channel.viewmodel.BaseViewModel;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;
import com.wali.live.watchsdk.income.income.UserIncomeActivity;
import com.wali.live.watchsdk.scheme.SchemeSdkActivity;
import com.wali.live.watchsdk.watch.WatchSdkActivity;
import com.wali.live.watchsdk.watch.model.RoomInfo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 16-7-19.
 *
 * @module 频道
 * @description 适配器内的跳转实现工具类
 */
public class JumpImpl implements JumpListener {
    private static final String TAG = JumpImpl.class.getSimpleName();

    private WeakReference<Activity> mActRef;
    private ArrayList<RoomInfo> mRoomList;

    public JumpImpl(Activity activity) {
        mActRef = new WeakReference(activity);
        mRoomList = new ArrayList();
    }

    public void process(List<? extends BaseViewModel> models) {
        mRoomList.clear();
        int position = 0;
        for (BaseViewModel viewModel : models) {
            if (viewModel instanceof ChannelLiveViewModel) {
                List<ChannelLiveViewModel.BaseItem> itemDatas = ((ChannelLiveViewModel) viewModel).getItemDatas();
                for (ChannelLiveViewModel.BaseItem item : itemDatas) {
                    if (item instanceof ChannelLiveViewModel.LiveItem) {
                        ChannelLiveViewModel.LiveItem liveItem = (ChannelLiveViewModel.LiveItem) item;
                        if (liveItem.isEnterRoom()) {
                            if (!liveItem.isContestRoom()) {
                                RoomInfo roomInfo = liveItem.toRoomInfo();
                                mRoomList.add(roomInfo);
                                liveItem.setListPosition(position);
                                position++;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void jumpScheme(String uri) {
        if (!TextUtils.isEmpty(uri)) {
            SchemeSdkActivity.openActivity(mActRef.get(), Uri.parse(uri));
        }
    }

    @Override
    public void jumpWatchWithLiveList(int position) {
        MyLog.d(TAG, "jumpWatchWithLiveList");
        WatchSdkActivity.openActivity(mActRef.get(), mRoomList, position);
    }

    public static void jumpSchema(Activity activity, String url) {
        if (activity != null && !TextUtils.isEmpty(url)) {
            SchemeSdkActivity.openActivity(activity, Uri.parse(url));
        }
    }

}
