package com.common.rxretrofit;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

public class ApiResult implements Serializable {
    int errno;
    String errmsg;
    String traceId;
    String data;

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public JSONObject getData() {
        if (TextUtils.isEmpty(data)) {
            return null;
        }
        JSONObject jsonObject = JSON.parseObject(data);
        return jsonObject;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ApiResult{" +
                "errno=" + errno +
                ", errmsg='" + errmsg + '\'' +
                ", traceId='" + traceId + '\'' +
                '}';
    }
}
