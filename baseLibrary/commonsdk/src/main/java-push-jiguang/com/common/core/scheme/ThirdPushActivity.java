package com.common.core.scheme;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.base.BaseActivity;
import com.common.log.MyLog;

import cn.jpush.android.api.JPushInterface;

public class ThirdPushActivity extends BaseActivity {
    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return 0;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        handleOpenClick();
    }

    /**
     * 处理点击事件，当前启动配置的Activity都是使用
     * Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
     * 方式启动，只需要在onCreat中调用此方法进行处理
     */
    private void handleOpenClick() {
        MyLog.d(TAG, "用户点击打开了通知");
        String data = null;
        //获取华为平台附带的jpush信息
        if (getIntent().getData() != null) {
            data = getIntent().getData().toString();
        }

        //获取fcm或oppo平台附带的jpush信息
        if (TextUtils.isEmpty(data) && getIntent().getExtras() != null) {
            data = getIntent().getExtras().getString("JMessageExtra");
        }

        MyLog.w(TAG, "msg content is " + String.valueOf(data));
        if (TextUtils.isEmpty(data)) return;
        JSONObject jsonObject = JSON.parseObject(data);
        String msgId = jsonObject.getString("msg_id");
        byte whichPushSDK = (byte) jsonObject.getByte("rom_type");
        String title = jsonObject.getString("n_title");
        String content = jsonObject.getString("n_content");
        String extras = jsonObject.getString("n_extras");
        StringBuilder sb = new StringBuilder();
        sb.append("msgId:");
        sb.append(String.valueOf(msgId));
        sb.append("\n");
        sb.append("title:");
        sb.append(String.valueOf(title));
        sb.append("\n");
        sb.append("content:");
        sb.append(String.valueOf(content));
        sb.append("\n");
        sb.append("extras:");
        sb.append(String.valueOf(extras));
        sb.append("\n");
        sb.append("platform:");
        sb.append(getPushSDKName(whichPushSDK));
//            mTextView.setText(sb.toString());
        //上报点击事件
        JPushInterface.reportNotificationOpened(this, msgId, whichPushSDK);

    }

    private String getPushSDKName(byte whichPushSDK) {
        String name;
        switch (whichPushSDK) {
            case 0:
                name = "jpush";
                break;
            case 1:
                name = "xiaomi";
                break;
            case 2:
                name = "huawei";
                break;
            case 3:
                name = "meizu";
                break;
            case 4:
                name = "oppo";
                break;
            case 8:
                name = "fcm";
                break;
            default:
                name = "jpush";
        }
        return name;
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
