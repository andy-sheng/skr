package com.common.core.scheme;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.R;
import com.common.log.MyLog;
import com.module.RouterConstants;
import com.umeng.message.UmengNotifyClickActivity;

import org.android.agoo.common.AgooConstants;

public class UmengThirdPushActivity extends UmengNotifyClickActivity {
    public final static String TAG = "UmengThirdPushActivity";

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.third_push_activity);
    }


    /*
        {
            "display_type": "notification",
                "extra": {
            "uri": "inframeskr://web/fullScreen?url=http://test.app.inframe.mobi/activity/paiwei&showShare=1"
            },
            "msg_id": "uauju7t155038938593610",
                "body": {
            "after_open": "go_activity",
                    "play_lights": "false",
                    "ticker": "3点42分",
                    "play_vibrate": "false",
                    "activity": "com.common.core.scheme.SchemeSdkActivity",
                    "text": "分身乏术的方式打分水电费都十分的舒服的沙发上发呆是",
                    "title": "3点42分",
                    "play_sound": "true"
        },
            "random_min": 0
        }
        */
    @Override
    public void onMessage(Intent intent) {
        super.onMessage(intent);  //此方法必须调用，否则无法统计打开数
        String body = intent.getStringExtra(AgooConstants.MESSAGE_BODY);
        MyLog.i(TAG, body);
        JSONObject jsonObject = JSON.parseObject(body);
        if (jsonObject != null) {
            JSONObject extraJO = jsonObject.getJSONObject("extra");
            if (extraJO != null) {
                String uri = extraJO.getString("uri");
                ARouter.getInstance().build(RouterConstants.ACTIVITY_SCHEME)
                        .withString("uri", uri)
                        .navigation();
            }
        }
        finish();
    }
}
