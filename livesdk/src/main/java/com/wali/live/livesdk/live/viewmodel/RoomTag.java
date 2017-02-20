package com.wali.live.livesdk.live.viewmodel;

import com.base.log.MyLog;
import com.wali.live.proto.Live2Proto;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by lan on 16/12/20.
 */
public class RoomTag implements Serializable {
    public static final String TAG_ID = "tag_id";
    public static final String TAG_NAME = "tag_name";
    public static final String ICON_URL = "icon_url";

    private int mTagId;
    private String mTagName;
    private String mIconUrl;

    public RoomTag(Live2Proto.TagInfo protoInfo) {
        parse(protoInfo);
    }

    public RoomTag(String jsonString) throws Exception {
        parse(jsonString);
    }

    public void parse(Live2Proto.TagInfo protoInfo) {
        if (protoInfo == null) {
            return;
        }
        mTagId = protoInfo.getTagId();
        mTagName = protoInfo.getTagName();
        mIconUrl = protoInfo.getIconUrl();
    }

    public void parse(String jsonString) throws Exception {
        JSONObject obj = new JSONObject(jsonString);
        mTagId = obj.getInt(TAG_ID);
        mTagName = obj.getString(TAG_NAME);
        mIconUrl = obj.getString(ICON_URL);
    }

    public int getTagId() {
        return mTagId;
    }

    public String getTagName() {
        return mTagName;
    }

    public String getIconUrl() {
        return mIconUrl;
    }

    public Live2Proto.TagInfo build() {
        return Live2Proto.TagInfo.newBuilder().setTagId(mTagId).setTagName(mTagName).build();
    }

    @Override
    public String toString() {
        return mTagId + "-" + mTagName + "-" + mIconUrl;
    }

    public String toJsonString() {
        try {
            JSONObject obj = new JSONObject();
            obj.put(TAG_ID, mTagId);
            obj.put(TAG_NAME, mTagName);
            obj.put(ICON_URL, mIconUrl);
            return obj.toString();
        } catch (JSONException e) {
            MyLog.e(e);
        }
        return "";
    }
}
