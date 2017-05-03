package com.mi.liveassistant.michannel;

import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.proto.CommonChannelProto;

/**
 * Created by lan on 16/7/4.
 *
 * @module 频道
 * @description 频道模型工厂
 */
public class ChannelModelFactory {
    private static final String TAG = ChannelModelFactory.class.getSimpleName();

    public static ChannelViewModel getChannelViewModel(CommonChannelProto.ChannelItem protoItem) {
        int uiType = protoItem.getUiType();
        try {
            if (uiType == 7 || uiType == 10 || uiType == 11 || uiType == 12 || uiType == 13 || uiType == 15
                    || uiType == 17 || uiType == 18 || uiType == 19 || uiType == 21 || uiType == 22
                    || uiType == 25 || uiType == 26) {
                return new ChannelLiveViewModel(protoItem);
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
