package com.component.busilib.verify;

import android.content.Context;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.DeviceUtils;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.common.view.ex.RxLifecycleView;
import com.dialog.view.TipsDialogView;
import com.module.RouterConstants;
import com.tencent.mm.opensdk.constants.Build;

public class RealNameVerifyUtils {

    TipsDialogView mTipsDialogView;


    /**
     * 是否需要实名认证
     *
     * @param successCallback
     */
    public void checkJoinVideoPermission(final Runnable successCallback) {
        if (MyUserInfoManager.getInstance().isRealNameVerified() || MyLog.isDebugLogOpen()) {
            if (successCallback != null) {
                successCallback.run();
            }
            return;
        }
        final VerifyServerApi grabRoomServerApi = ApiManager.getInstance().createService(VerifyServerApi.class);
        if (Build.SDK_INT < 21) {
            if (U.getDeviceUtils().getLevel().getValue() <= DeviceUtils.LEVEL.MIDDLE.getValue()) {
                mTipsDialogView = new TipsDialogView.Builder(U.getActivityUtils().getTopActivity())
                        .setMessageTip("你的设备性能较差，进入视频专场可能会影响体验，确定要进入么？")
                        .setOkBtnTip("进入")
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
                // 去实名认证
                if (mTipsDialogView == null) {
                    mTipsDialogView = new TipsDialogView.Builder(U.getActivityUtils().getTopActivity())
                            .setMessageTip("撕歌的宝贝们，两分钟完成认证\n超有趣的视频玩法等着你来哦")
                            .setConfirmTip("快速认证")
                            .setCancelTip("取消")
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
                }
                mTipsDialogView.showByDialog();
            }
            U.getToastUtil().showShort(obj.getErrmsg());
        }
    }
}
