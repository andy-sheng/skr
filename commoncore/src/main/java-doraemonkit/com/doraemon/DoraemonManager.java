package com.doraemon;

import android.content.Context;

import com.common.core.R;
import com.common.umeng.UmengInit;
import com.common.umeng.UmengPush;
import com.common.utils.U;
import com.didichuxing.doraemonkit.DoraemonKit;
import com.didichuxing.doraemonkit.kit.sysinfo.ExtraInfoProvider;
import com.didichuxing.doraemonkit.kit.sysinfo.SysInfoItem;
import com.didichuxing.doraemonkit.ui.FloatIconPage;
import com.didichuxing.doraemonkit.ui.UniversalActivity;
import com.didichuxing.doraemonkit.ui.base.FloatPageManager;
import com.didichuxing.doraemonkit.ui.base.PageIntent;
import com.engine.agora.AgoraEngineAdapter;

import java.util.ArrayList;
import java.util.List;

public class DoraemonManager {
    public final static String TAG = "DoraemonManager";

    public static void init() {
        DoraemonKit.install(U.app());
//        // H5任意门功能需要，非必须
//        DoraemonKit.setWebDoorCallback(new WebDoorManager.WebDoorCallback() {
//            @Override
//            public void overrideUrlLoading(String s) {
//                // 使用自己的H5容器打开这个链接
//                Log.d(TAG,"overrideUrlLoading" + " s=" + s);
//
//            }
//        });
        DoraemonKit.setExtraInfoProvider(new ExtraInfoProvider() {
            @Override
            public List<SysInfoItem> getExtraInfo() {
                List<SysInfoItem> extras = new ArrayList<>();
                extras.add(new SysInfoItem("友盟push DeviceToken", UmengPush.getDeviceToken()));
                extras.add(new SysInfoItem("translucent_has_bug", U.app().getResources().getBoolean(R.bool.translucent_has_bug)+""));
                return extras;
            }
        });
    }

    public static void showFloatIcon() {
        DoraemonKit.tryShowFloatIcon(U.getActivityUtils().getTopActivity());
    }

    public static void hideFloatIcon() {
        DoraemonKit.hideFloatIcon();
    }

}
