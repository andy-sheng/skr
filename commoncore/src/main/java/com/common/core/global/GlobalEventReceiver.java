package com.common.core.global;

import android.text.TextUtils;

import com.common.clipboard.ClipboardUtils;
import com.common.core.kouling.SkrKouLingUtils;
import com.common.utils.ActivityUtils;
import com.common.utils.U;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class GlobalEventReceiver {
    private static class GlobalEventReceiverHolder {
        private static final GlobalEventReceiver INSTANCE = new GlobalEventReceiver();
    }

    private GlobalEventReceiver() {

    }

    public static final GlobalEventReceiver getInstance() {
        return GlobalEventReceiverHolder.INSTANCE;
    }

    public void register() {
        EventBus.getDefault().register(this);
    }


    @Subscribe
    public void onEvent(ActivityUtils.ForeOrBackgroundChange event) {
        if (event.foreground) {
            // 检查剪贴板
            String str = ClipboardUtils.getPaste();
            if(!TextUtils.isEmpty(str)){
                if(SkrKouLingUtils.tryParseScheme(str)){
                    ClipboardUtils.clear();
                }
            }
        }
    }
}
