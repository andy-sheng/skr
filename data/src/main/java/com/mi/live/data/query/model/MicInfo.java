package com.mi.live.data.query.model;

import com.wali.live.proto.LiveCommonProto;

/**
 * Created by chengsimin on 16/9/8.
 */
public class MicInfo {
    long micUid;
    float topx, topy, width, height;

    public long getMicUid() {
        return micUid;
    }

    public void setMicUid(long micUid) {
        this.micUid = micUid;
    }

    public float getTopx() {
        return topx;
    }

    public void setTopx(float topx) {
        this.topx = topx;
    }

    public float getTopy() {
        return topy;
    }

    public void setTopy(float topy) {
        this.topy = topy;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public static MicInfo loadFromPb(LiveCommonProto.MicInfo micInfo) {
        MicInfo micInfo1 = new MicInfo();
        micInfo1.setMicUid(micInfo.getMicuid());
        LiveCommonProto.MicSubViewPos micSubViewPos = micInfo.getSubViewPos();
        if (micSubViewPos != null) {
            micInfo1.setHeight(micSubViewPos.getHeightScale());
            micInfo1.setWidth(micSubViewPos.getWidthScale());
            micInfo1.setTopx(micSubViewPos.getTopXScale());
            micInfo1.setTopy(micSubViewPos.getTopYScale());
        }
        return micInfo1;
    }
}
