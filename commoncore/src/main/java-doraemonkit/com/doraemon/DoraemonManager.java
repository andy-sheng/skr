package com.doraemon;

import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.core.R;
import com.common.log.MyLog;
import com.common.matrix.MatrixInit;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.didichuxing.doraemonkit.DoraemonKit;
import com.didichuxing.doraemonkit.kit.sysinfo.ExtraInfoProvider;
import com.didichuxing.doraemonkit.kit.sysinfo.SysInfoItem;
import com.module.ModuleServiceManager;
import com.module.RouterConstants;
import com.module.common.ICallback;
import com.module.msg.IMsgService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DoraemonManager {
    public static final String TAG = "DoraemonManager";

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
                extras.add(new SysInfoItem("Matrix", MatrixInit.isOpen() + "", new DebounceViewClickListener() {
                    @Override
                    public void clickValid(View v) {
                        if (MatrixInit.isOpen()) {
                            MatrixInit.goIssueList();
                        }
                    }
                }));

//                extras.add(new SysInfoItem("模拟io", "模拟", new DebounceViewClickListener() {
//                    @Override
//                    public void clickValid(View v) {
//                        writeSth();
//                        try {
//                            File f = new File("/sdcard/a.txt");
//                            byte[] buf = new byte[400];
//                            FileInputStream fis = new FileInputStream(f);
//                            int count = 0;
//                            while (fis.read(buf) != -1) {
////                MatrixLog.i(TAG, "read %d", ++count);
//                            }
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//
//                        //need to trigger gc to detect leak
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Runtime.getRuntime().gc();
//                                Runtime.getRuntime().runFinalization();
//                                Runtime.getRuntime().gc();
//                            }
//                        }).start();
//
//                    }
//                }));

                extras.add(new SysInfoItem("模拟崩溃", "模拟", new DebounceViewClickListener() {
                    @Override
                    public void clickValid(View v) {
//                        try {
//                            Boolean a = (Boolean) U.getReflectUtils().readField(MiPushClient.class, null, "isCrashHandlerSuggested");
//                            U.getToastUtil().showShort("MiPushClient.isCrashHandlerSuggested=" + a);
//                        } catch (NoSuchFieldException e) {
//                            e.printStackTrace();
//                        } catch (IllegalAccessException e) {
//                            e.printStackTrace();
//                        }

                        v = null;
                        int h = v.getHeight();
                        MyLog.e(TAG, "h=" + h);
                    }
                }));

                extras.add(new SysInfoItem("x5 内核调试", "查看", new DebounceViewClickListener() {
                    @Override
                    public void clickValid(View v) {
                        ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                                .withString("url", "http://debugtbs.qq.com").navigation();
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

    private static void writeSth() {
        try {
            File f = new File("/sdcard/a.txt");
            if (f.exists()) {
                f.delete();
            }
            byte[] data = new byte[4096];
            for (int i = 0; i < data.length; i++) {
                data[i] = 'a';
            }
            FileOutputStream fos = new FileOutputStream(f);
            for (int i = 0; i < 10; i++) {
                fos.write(data);
            }

            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showFloatIcon() {
        DoraemonKit.tryShowFloatIcon(U.getActivityUtils().getTopActivity());
    }

    public static void hideFloatIcon() {
        DoraemonKit.hideFloatIcon();
    }

}
