package com.component.busilib.verify;

import android.app.Activity;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.rxretrofit.ControlType;
import com.common.rxretrofit.RequestControl;
import com.common.utils.DeviceUtils;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.common.view.DebounceViewClickListener;
import com.dialog.view.TipsDialogView;
import com.module.RouterConstants;
import com.module.home.IHomeService;
import com.orhanobut.dialogplus.DialogPlus;
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
                                }, new RequestControl("checkCreatePublicRoomPermission", ControlType.CancelThis));
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
        }, new RequestControl("checkCreatePublicRoomPermission", ControlType.CancelThis));
    }

    /**
     * 是否达到语音房门槛
     *
     * @param successCallback
     */
    public void checkJoinAudioPermission(int tagId, final Runnable successCallback) {
        if (MyLog.isDebugLogOpen()) {
            if (successCallback != null) {
                successCallback.run();
            }
            return;
        }
        if (tagId == 44 || tagId == 45 || tagId == 47 || tagId == 48 || tagId == 40) {
            // 这些专场不用权限校验就能进
            if (successCallback != null) {
                successCallback.run();
            }
            return;
        }
        final VerifyServerApi grabRoomServerApi = ApiManager.getInstance().createService(VerifyServerApi.class);
        ApiMethods.subscribe(grabRoomServerApi.checkJoinAudioRoomPermission(tagId), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                process2(obj, successCallback);
            }
        }, new RequestControl("checkCreatePublicRoomPermission", ControlType.CancelThis));
    }

    /**
     * 双人房实人认证相关
     *
     * @param successCallback
     */
    public void checkJoinDoubleRoomPermission(final Runnable successCallback) {
        if (MyLog.isDebugLogOpen()) {
            if (successCallback != null) {
                successCallback.run();
            }
            return;
        }

        final VerifyServerApi grabRoomServerApi = ApiManager.getInstance().createService(VerifyServerApi.class);
        ApiMethods.subscribe(grabRoomServerApi.checkJoinDoubleRoomPermission(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                process2(obj, successCallback);
            }
        }, new RequestControl("checkJoinDoubleRoomPermission", ControlType.CancelThis));
    }

    public void checkAgeSettingState(final Runnable successCallback) {
        if (MyUserInfoManager.getInstance().hasAgeStage()) {
            if (successCallback != null) {
                successCallback.run();
            }
            return;
        }
        IHomeService channelService = (IHomeService) ARouter.getInstance().build(RouterConstants.SERVICE_HOME).navigation();
        channelService.goEditAgeActivity(successCallback);
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
                        .setConfirmTip("立即认证")
                        .setCancelTip("放弃")
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
            } else if (8344304 == obj.getErrno()
                    || 8344306 == obj.getErrno()
                    || 8344307 == obj.getErrno()
            ) {
//                8344304; //参与抢唱并完成100首歌曲表演，即可解锁视频专场
//                8344306; //参与抢唱并完成100首歌曲表演，即可解锁怀旧金曲专场
//                8344307; //获得100个爆灯，即可解锁唱将专场
                if (mTipsDialogView != null) {
                    mTipsDialogView.dismiss();
                }
                Activity activity = U.getActivityUtils().getTopActivity();
                // 未满100首
                if (DialogPlus.hasDialogShow(activity)) {
                    U.getToastUtil().showShort(obj.getErrmsg());
                } else {
                    mTipsDialogView = new TipsDialogView.Builder(activity)
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
                }
            } else if (8376042 == obj.getErrno()) {
                // 双人唱聊实名认证
                if (mTipsDialogView != null) {
                    mTipsDialogView.dismiss();
                }
                // 去实名认证
                mTipsDialogView = new TipsDialogView.Builder(U.getActivityUtils().getTopActivity())
                        .setMessageTip(obj.getErrmsg())
                        .setConfirmTip("立即认证")
                        .setCancelTip("放弃")
                        .setConfirmBtnClickListener(new AnimateClickListener() {
                            @Override
                            public void click(View view) {
                                mTipsDialogView.dismiss();
                                ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                                        .withString("url", ApiManager.getInstance().findRealUrlByChannel("http://app.inframe.mobi/oauth?from=double"))
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
