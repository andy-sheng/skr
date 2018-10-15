package com.wali.live.watchsdk.statistics.item;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.wali.live.proto.StatisticsProto;

import org.json.JSONObject;

/**
 * Created by liuting on 18-8-22.
 * 游戏直播间下载相关统计
 */

public class GameWatchDownloadStatisticItem extends MilinkStatisticsItem {
    private String TAG = getClass().getSimpleName();

    public final static int GAME_WATCH_TYPE_CLICK = 701;
    public final static int GAME_WATCH_TYPE_DOWNLOAD = 702;
    public final static int GAME_WATCH_TYPE_EXPOSURE = 703;


    public final static int GAME_WATCH_BIZTYPE_POP_CLICK = 1; //  游戏挂件点击
    public final static int GAME_WATCH_BIZTYPE_LAND_DOWNLOAD_CLICK = 2; // 横屏下载按钮点击
    public final static int GAME_WATCH_BIZTYPE_GAME_HOME_PAGE_CLICK = 3; // 游戏主页下载按钮点击

    public final static int GAME_WATCH_BIZTYPE_START_DOWNLOAD = 1; // 开始下载
    public final static int GAME_WATCH_BIZTYPE_DOWNLOAD_COMPLETED = 2; // 完成下载

    public final static int GAME_WATCH_BIZTYPE_POP_EXPOSURE = 0; // 游戏挂件曝光

    public GameWatchDownloadStatisticItem(long date, int type, int bizType, long anchorId, long channelId, String gamePackage) {
        super(date, type);
        mCommonLog = StatisticsProto.CommonLog.newBuilder()
                .setBizType(bizType)
                .setExtStr(getExtraString(anchorId, channelId, gamePackage))
                .build();
    }

    private String getExtraString(long anchorId, long channelId, String gamePackage) {
        MyLog.d(TAG, "anchorId=" + anchorId + " channelId=" + channelId + " gamePackage=" + gamePackage);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("anchor_id", anchorId);
            if (channelId > 0) {
                jsonObject.put("channel_id", channelId);
            }
            if (!TextUtils.isEmpty(gamePackage)) {
                jsonObject.put("game_package", gamePackage);
            }
            return jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public StatisticsProto.LiveRecvFlagItem build() {
        MyLog.d(TAG, "type=" + mType + "\nbizType=" + mCommonLog.getBizType() + "\ncommonLog=" + mCommonLog.getExtStr());
        mFlagItem = StatisticsProto.LiveRecvFlagItem.newBuilder()
                .setDate(mDate)
                .setType(mType)
                .setLog(mCommonLog)
                .build();
        return mFlagItem;
    }
}
