package com.wali.live.modulechannel.adapter.holder;

import android.app.Activity;
import android.net.Uri;
import android.text.TextUtils;

import com.common.log.MyLog;
import com.wali.live.modulechannel.model.viewmodel.BaseViewModel;
import com.wali.live.modulechannel.model.viewmodel.ChannelLiveViewModel;

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
    //TODO-房间信息暂时没有
//    private ArrayList<RoomInfo> mRoomList;
    private long mChannelId;

    public JumpImpl(Activity activity) {
        mActRef = new WeakReference(activity);
        //TODO-房间信息暂时去了
//        mRoomList = new ArrayList();
    }

    public void setChannelId(long channelId) {
        mChannelId  = channelId;
    }

    public void process(List<? extends BaseViewModel> models) {
        //TODO-房间信息暂时去了
//        mRoomList.clear();
//        int position = 0;
//        for (BaseViewModel viewModel : models) {
//            if (viewModel instanceof ChannelLiveViewModel) {
//                List<ChannelLiveViewModel.BaseItem> itemDatas = ((ChannelLiveViewModel) viewModel).getItemDatas();
//                for (ChannelLiveViewModel.BaseItem item : itemDatas) {
//                    if (item instanceof ChannelLiveViewModel.LiveItem) {
//                        ChannelLiveViewModel.LiveItem liveItem = (ChannelLiveViewModel.LiveItem) item;
//                        if (liveItem.isEnterRoom()) {
//                            if (!liveItem.isContestRoom()) {
//                                RoomInfo roomInfo = liveItem.toRoomInfo();
//                                roomInfo.setPageChannelId(mChannelId);
//                                mRoomList.add(roomInfo);
//                                liveItem.setListPosition(position);
//                                position++;
//                            }
//                        }
//                    }
//                }
//            }
//        }
    }

    @Override
    public void jumpScheme(String schema) {
        //TODO-房间信息暂时去了
//        if (!TextUtils.isEmpty(schema)) {
//            Uri uri = Uri.parse(schema);
//            String host = uri.getHost();
//            String path = uri.getPath();
//            if ("room".equals(host)) {
//                if ("/join".equals(path)) {
//                    String liveId = SchemeUtils.getString(uri, "liveid");
//                    long playerId = SchemeUtils.getLong(uri, "playerid", 0);
//                    String videoUrl = SchemeUtils.getString(uri, "videourl");
//                    int liveType = SchemeUtils.getInt(uri, "type", 0);
//                    if (!TextUtils.isEmpty(liveId) && !TextUtils.isEmpty(videoUrl)) {
//                        RoomInfo roomInfo = RoomInfo.Builder.newInstance(playerId, liveId, videoUrl)
//                                .setLiveType(liveType)
//                                .setPageChannelId(mChannelId)
//                                .build();
//                        WatchSdkActivity.openActivity(mActRef.get(), roomInfo);
//                        return;
//                    }
//                }
//            }
//            SchemeSdkActivity.openActivity(mActRef.get(), uri);
//        }
    }

    @Override
    public void jumpWatchWithLiveList(int position) {
        //TODO-房间信息暂时去了
//        MyLog.d(TAG, "jumpWatchWithLiveList");
//        WatchSdkActivity.openActivity(mActRef.get(), mRoomList, position);
    }

    public static void jumpSchema(Activity activity, String url) {
        //TODO-房间信息暂时去了
//        if (activity != null && !TextUtils.isEmpty(url)) {
//            SchemeSdkActivity.openActivity(activity, Uri.parse(url));
//        }
    }

}
