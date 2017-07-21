package com.wali.live.watchsdk.channel.viewmodel;

import com.base.log.MyLog;
import com.wali.live.proto.CommonChannelProto.ChannelItem;

/**
 * Created by lan on 16/7/4.
 *
 * @module 频道
 * @description 频道模型工厂
 */
public class ChannelModelFactory {
    private static final String TAG = ChannelModelFactory.class.getSimpleName();

    public static ChannelViewModel getChannelViewModel(ChannelItem protoItem) {
        int uiType = protoItem.getUiType();
        try {
            if (uiType == 1 || uiType == 2 || uiType == 4 || uiType == 16) {
                return new ChannelShowViewModel(protoItem);
            } else if (uiType == 3 || uiType == 5 || uiType == 9 || uiType == 24) {
                return new ChannelUserViewModel(protoItem);
            } else if (uiType == 6) {
                return new ChannelBannerViewModel(protoItem);
            } else if (uiType == 8) {
                return new ChannelTwoTextViewModel(protoItem);
            } else if (uiType == 7 || uiType == 10 || uiType == 11 || uiType == 12 || uiType == 13 || uiType == 15
                    || uiType == 17 || uiType == 18 || uiType == 19 || uiType == 21 || uiType == 22
                    || uiType == 25 || uiType == 26 || uiType == 28 || uiType == 31 || uiType == 32 || uiType == 33 || uiType == 34) {
                return new ChannelLiveViewModel(protoItem);
            } else if (uiType == 14) {
                return new ChannelSplitViewModel(protoItem);
            } else if (uiType == 20 || uiType == 27 || uiType == 30 || uiType == 35) {
                return new ChannelNavigateViewModel(protoItem);
            } else if (uiType == 23) {
                // TODO 过滤23和29样式
                // return new ChannelNoticeViewModel(protoItem);
                return null;
            } else if (uiType == 29) {
                // return new ChannelRankingViewModel(protoItem);
                return null;
            } else {
                MyLog.d(TAG, "getChannelViewModel uiType  = " + uiType + " is illegal");
                return null;
            }
        } catch (Exception e) {
            MyLog.d(TAG, "uiType  = " + uiType + " is exception\n" + e);
            return null;
        }
    }
}
