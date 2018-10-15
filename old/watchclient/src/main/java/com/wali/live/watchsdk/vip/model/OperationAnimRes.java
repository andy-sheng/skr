package com.wali.live.watchsdk.vip.model;

import com.base.log.MyLog;
import com.wali.live.proto.OperationActivityProto;


public class OperationAnimRes {
    private int resourceId = 0;
    private String resourceUrl;


    public static OperationAnimRes loadFromPB(OperationActivityProto.EffectResourcesItem resource) {
        OperationAnimRes res = new OperationAnimRes();

        res.setResourceId(resource.getResourceId());
        res.setResourceUrl(resource.getResourceUrl());

        MyLog.d("OperationAnimRes", "loadFromPB resource=" + res.toString());
        return res;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    @Override
    public String toString() {
        return "OperationAnimRes{" +
                "resourceId=" + resourceId +
                ", resourceUrl='" + resourceUrl + '\'' +
                '}';
    }
}
