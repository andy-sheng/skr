package com.component.busilib.verify;

import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.DeviceUtils;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.common.view.DebounceViewClickListener;
import com.dialog.view.TipsDialogView;
import com.module.RouterConstants;
import com.tencent.mm.opensdk.constants.Build;

public class SkrVerifyUtils {

    TipsDialogView mTipsDialogView;


    /**
     * 是否需要实名认证
     *
     * @param successCallback
     */
    public void checkJoinVideoPermission(final Runnable successCallback) {
        if (MyLog.isDebugLogOpen()) {
            if (successCallback != null) {
                successCallback.run();
            }
            return;
        }
        final VerifyServerApi grabRoomServerApi = ApiManager.getInstance().createService(VerifyServerApi.class);
        if (Build.SDK_INT < 21) {
            if (U.getDeviceUtils().getLevel().getValue() <= DeviceUtils.LEVEL.MIDDLE.getValue()) {
                if (mTipsDialogView != null) {
                    mTipsDialogView.dismiss();
                }
                mTipsDialogView = new TipsDialogView.Builder(U.getActivityUtils().getTopActivity())
                        .setMessageTip("你的设备性能较差，进入视频专场可能会影响体验，确定要进入么？")
                        .setConfirmTip("进入")
                        .setCancelTip("退出")
                        .setConfirmBtnClickListener(new AnimateClickListener() {
                            @Override
                            public void click(View view) {
                                ApiMethods.subscribe(grabRoomServerApi.checkJoinVideoRoomPermission(), new ApiObserver<ApiResult>() {
                                    @Override
                                    public void process(ApiResult obj) {
                                        process2(obj, successCallback);
                                    }
                                }, new ApiMethods.RequestControl("checkCreatePublicRoomPermission", ApiMethods.ControlType.CancelThis));
                                mTipsDialogView.dismiss();
                            }
                        })
                        .setCancelBtnClickListener(new AnimateClickListener() {
                            @Override
                            public void click(View view) {
                                mTipsDialogView.dismiss();
                            }
                        })
                        .build();
                mTipsDialogView.showByDialog();
                return;
            }
        }

        ApiMethods.subscribe(grabRoomServerApi.checkJoinVideoRoomPermission(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                process2(obj, successCallback);
            }
        }, new ApiMethods.RequestControl("checkCreatePublicRoomPermission", ApiMethods.ControlType.CancelThis));
    }

    /**
     * 双人房实人认证相关
     *
     * @param successCallback
     */
    public void checkJoinDoubleRoomPermission(final Runnable successCallback) {
//        if (MyLog.isDebugLogOpen()) {
//            if (successCallback != null) {
//                successCallback.run();
//            }
//            return;
//        }

        final VerifyServerApi grabRoomServerApi = ApiManager.getInstance().createService(VerifyServerApi.class);
        ApiMethods.subscribe(grabRoomServerApi.checkJoinDoubleRoomPermission(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                process2(obj, successCallback);
            }
        }, new ApiMethods.RequestControl("checkJoinDoubleRoomPermission", ApiMethods.ControlType.CancelThis));
    }

    private void process2(ApiResult obj, Runnable successCallback) {
        if (obj.getErrno() == 0) {
            boolean isAuth = obj.getData().getBooleanValue("IsAuth");
            if (successCallback != null) {
                successCallback.run();
            }
            if (isAuth) {
                MyUserInfoManager.getInstance().setRealNameVerified(true);
            }
        } else {
            if (8344161 == obj.getErrno()) {
                // 视频实名认证
                if (mTipsDialogView != null) {
                    mTipsDialogView.dismiss();
                }
                // 去实名认证
                mTipsDialogView = new TipsDialogView.Builder(U.getActivityUtils().getTopActivity())
                        .setMessageTip(obj.getErrmsg())
                        .setConfirmTip("快速认证")
                        .setCancelTip("残忍拒绝")
                        .setConfirmBtnClickListener(new AnimateClickListener() {
                            @Override
                            public void click(View view) {
                                mTipsDialogView.dismiss();
                                ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                                        .withString("url", ApiManager.getInstance().findRealUrlByChannel("http://app.inframe.mobi/oauth?from=video"))
                                        .greenChannel().navigation();
                            }
                        })
                        .setCancelBtnClickListener(new AnimateClickListener() {
                            @Override
                            public void click(View view) {
                                mTipsDialogView.dismiss();
                            }
                        })
                        .build();
                mTipsDialogView.showByDialog();
            } else if (8344304 == obj.getErrno()) {
                // 未满100首
                mTipsDialogView = new TipsDialogView.Builder(U.getActivityUtils().getTopActivity())
                        .setMessageTip(obj.getErrmsg())
                        .setOkBtnTip("确认")
                        .setOkBtnClickListener(new DebounceViewClickListener() {
                            @Override
                            public void clickValid(View v) {
                                mTipsDialogView.dismiss();
                            }
                        })
                        .build();
                mTipsDialogView.showByDialog();
            } else if (8376042 == obj.getErrno()) {
                // 双人唱聊实名认证
                if (mTipsDialogView != null) {
                    mTipsDialogView.dismiss();
                }
                // 去实名认证
                mTipsDialogView = new TipsDialogView.Builder(U.getActivityUtils().getTopActivity())
                        .setMessageTip(obj.getErrmsg())
                        .setConfirmTip("快速认证")
                        .setCancelTip("残忍拒绝")
                        .setConfirmBtnClickListener(new AnimateClickListener() {
                            @Override
                            public void click(View view) {
                                mTipsDialogView.dismiss();
                                ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                                        .withString("url", ApiManager.getInstance().findRealUrlByChannel("http://app.inframe.mobi/oauth?from=duoble"))
                                        .greenChannel().navigation();
                            }
                        })
                        .setCancelBtnClickListener(new AnimateClickListener() {
                            @Override
                            public void click(View view) {
                                mTipsDialogView.dismiss();
                            }
                        })
                        .build();
                mTipsDialogView.showByDialog();
            } else {
                U.getToastUtil().showShort(obj.getErrmsg());
            }
        }
    }
}
