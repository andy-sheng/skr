package com.module.msg.rc.rompush;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.base.BaseActivity;
import com.common.log.MyLog;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.home.IHomeService;


public class ThirdPushActivity extends BaseActivity {
    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return 0;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        handlePushMessage(getIntent());
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    /**
     * Android push 消息
     */
    private void handlePushMessage(Intent intent) {
        if (intent != null && intent.getData() != null && intent.getData().getScheme().equals("rong")) {
            //该条推送消息的内容。
            String content = intent.getData().getQueryParameter("pushContent");
            //标识该推送消息的唯一 Id。
            String id = intent.getData().getQueryParameter("pushId");
            //用户自定义参数 json 格式，解析后用户可根据自己定义的 Key 、Value 值进行业务处理。
            String extra = intent.getData().getQueryParameter("extra");
            //统计通知栏点击事件.
            Log.d("ThirdPushActivity", "--content:" + content + "--id:" + id + "---extra:" + extra);
            if (!TextUtils.isEmpty(extra)) {
                try {
                    JSONObject jsonObject = JSON.parseObject(extra);
                    String url = jsonObject.getString("url");
                    if (!TextUtils.isEmpty(url)) {
                        finish();
                        ARouter.getInstance().build("/core/SchemeSdkActivity")
                                .withString("uri", url)
                                .navigation();
                        return;
                    }
                } catch (Exception e) {
                    MyLog.e(e);
                }
            }
        }
        finish();
        if (!U.getActivityUtils().isHomeActivityExist()) {
            IHomeService channelService = (IHomeService) ARouter.getInstance().build(RouterConstants.SERVICE_HOME).navigation();
            if (channelService != null) {
                channelService.goHomeActivity(this);
                return;
            }
        }
    }
}
