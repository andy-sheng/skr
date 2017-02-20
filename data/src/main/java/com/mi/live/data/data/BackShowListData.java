package com.mi.live.data.data;

import com.base.log.MyLog;
import com.mi.live.data.api.JSONable;
import com.wali.live.proto.LiveShowProto;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yurui on 2/25/16.
 */
public class BackShowListData implements JSONable {

    private final static String TAG = "BackShowListData";
    // LiveShow.proto里的BackInfo.replay_type
    public static final int REPLAY_TYPE_PUBLIC = 0;
    public static final int REPLAY_TYPE_PRIVATE = 1;
    public static final int REPLAY_TYPE_TOKEN = 2;
    public static final int REPLAY_TYPE_TICKET = 3;

    public long uId;                 //用户id
    public String userNickname;      //昵称
    public long avatar;              //头像 timestamp
    public String baId;              //回放id
    public int viewerCnt;            //观众数
    public String url;               //回放地址
    public long startTime;           //直播开始时间
    public long endTime;             //直播结束时间
    public String liveTitle;         //直播标题
    public String shareUrl;         //回放分享地址
    public String coverUrl;         //直播封面
    public String addr;             //地点

    public BackShowListData() {
    }

    public BackShowListData(LiveShowProto.UserShow userShow, LiveShowProto.BackInfo backInfo) {
        if (userShow != null) {
            uId = userShow.getUId();
            userNickname = userShow.getNickname();
            avatar = userShow.getAvatar();
        }

        if (backInfo != null) {
            baId = backInfo.getBaId();
            viewerCnt = backInfo.getViewerCnt();
            url = backInfo.getUrl();
            startTime = backInfo.getStartTime();
            endTime = backInfo.getEndTime();
            liveTitle = backInfo.getBaTitle();
            shareUrl = backInfo.getShareUrl();
            addr = backInfo.getAddr();
            coverUrl = backInfo.getCoverUrl();
        }
    }

    /**
     * 设置UserShow
     *
     * @param userShow
     */
    public void setUserShow(LiveShowProto.UserShow userShow) {
        if (userShow == null) {
            return;
        }
        uId = userShow.getUId();
        userNickname = userShow.getNickname();
        avatar = userShow.getAvatar();


    }


    public static List<Object> parseBackShowList(LiveShowProto.BackShow backShow) {
        LiveShowProto.UserShow userShow = backShow.getUser();
        List<Object> list = new ArrayList<>();
        for (LiveShowProto.BackInfo backInfo : backShow.getInfosList()) {
            BackShowListData item = new BackShowListData(userShow, backInfo);
            list.add(item);
        }
        return list;
    }

    public static List<BackShowListData> parseBackShowDataList(LiveShowProto.BackShow backShow) {
        LiveShowProto.UserShow userShow = backShow.getUser();
        List<BackShowListData> list = new ArrayList<BackShowListData>();
        for (LiveShowProto.BackInfo backInfo : backShow.getInfosList()) {
            BackShowListData item = new BackShowListData(userShow, backInfo);
            list.add(item);
        }
        return list;
    }

    //JSONable interface funcs begins ***************************************

    @Override
    public JSONObject serialToJSON() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("backshowlistdata_uid", uId);
            jsonObject.put("backshowlistdata_usernickname", userNickname);
            jsonObject.put("backshowlistdata_avatar", avatar);
            jsonObject.put("backshowlistdata_baId", baId);
            jsonObject.put("backshowlistdata_viewercnt", viewerCnt);
            jsonObject.put("backshowlistdata_url", url);
            jsonObject.put("backshowlistdata_starttime", startTime);
            jsonObject.put("backshowlistdata_endtime", endTime);
            jsonObject.put("backshowlistdata_livetitle", liveTitle);
            jsonObject.put("backshowlistdata_shareurl", shareUrl);
            jsonObject.put("backshowlistdata_addr", addr);
            jsonObject.put("backshowlistdata_coverurl", coverUrl);

            return jsonObject;
        } catch (JSONException e) {
            MyLog.e(TAG, e);
        }

        return jsonObject;
    }

    @Override
    public void serialFromJSON(JSONObject jsonObject) {
        if (jsonObject == null) {
            return;
        }

        this.uId = jsonObject.optInt("backshowlistdata_uid", 0);
        this.userNickname = jsonObject.optString("backshowlistdata_usernickname", "");
        this.avatar = jsonObject.optLong("backshowlistdata_avatar", 0);
        this.baId = jsonObject.optString("backshowlistdata_baId", "");
        this.viewerCnt = jsonObject.optInt("backshowlistdata_viewercnt", 0);
        this.url = jsonObject.optString("backshowlistdata_url", "");
        this.startTime = jsonObject.optLong("backshowlistdata_starttime", 0);
        this.endTime = jsonObject.optLong("backshowlistdata_endtime", 0);
        this.liveTitle = jsonObject.optString("backshowlistdata_livetitle", "");
        this.shareUrl = jsonObject.optString("backshowlistdata_shareurl", "");
        this.addr = jsonObject.optString("backshowlistdata_addr", "");
        this.coverUrl = jsonObject.optString("backshowlistdata_coverurl", "");
    }

    //JSONable interface funcs ends ******************************************

}
