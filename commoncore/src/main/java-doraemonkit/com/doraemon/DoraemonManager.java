package com.doraemon;

import android.view.View;

import com.common.core.R;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.didichuxing.doraemonkit.DoraemonKit;
import com.didichuxing.doraemonkit.kit.sysinfo.ExtraInfoProvider;
import com.didichuxing.doraemonkit.kit.sysinfo.SysInfoItem;
import com.module.ModuleServiceManager;
import com.module.common.ICallback;
import com.module.msg.IMsgService;

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
                //extras.add(new SysInfoItem("友盟push DeviceToken", com.common.umeng.UmengPush.getDeviceToken()));
                extras.add(new SysInfoItem("translucent_no_bug", U.app().getResources().getBoolean(R.bool.translucent_no_bug) + ""));
                extras.add(new SysInfoItem("通知栏权限", U.getPermissionUtils().checkNotification(U.app()) ? "开启" : "关闭", new DebounceViewClickListener() {
                    @Override
                    public void clickValid(View v) {
                        U.getPermissionUtils().goNotificationSettingPage();
                    }
                }));
                return extras;
            }

            @Override
            public void pullLog(String userIdStr) {
                try {
                    int userId = Integer.parseInt(userIdStr);
                    IMsgService msgService = ModuleServiceManager.getInstance().getMsgService();
                    if (msgService != null) {
                        msgService.sendSpecialDebugMessage(String.valueOf(userId), 1, "请求上传日志", new ICallback() {
                            @Override
                            public void onSucess(Object obj) {
                                U.getToastUtil().showLong("请求成功,稍等看该用户是否有返回");
                            }

                            @Override
                            public void onFailed(Object obj, int errcode, String message) {
                                U.getToastUtil().showLong("请求失败");
                            }
                        });
                    }
                } catch (Exception e) {
                    U.getToastUtil().showShort("id不对");
                }

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
