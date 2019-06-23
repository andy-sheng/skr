package com.component.busilib.verify;

import android.content.Context;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.common.view.ex.RxLifecycleView;
import com.dialog.view.TipsDialogView;
import com.module.RouterConstants;

public class RealNameVerifyUtils {

    TipsDialogView mTipsDialogView;
    BaseFragment mF;
    RxLifecycleView mRxLifecycleView;

    public RealNameVerifyUtils(BaseFragment fragment) {
        this.mF = fragment;
    }

    public RealNameVerifyUtils(RxLifecycleView rxLifecycleView) {
        mRxLifecycleView = rxLifecycleView;
    }

    /**
     * 是否需要实名认证
     *
     * @param successCallback
     */
    public void checkJoinVideoPermission(final Runnable successCallback) {
        if (MyUserInfoManager.getInstance().isRealNameVerified()) {
            if (successCallback != null) {
                successCallback.run();
            }
            return;
        }
        VerifyServerApi grabRoomServerApi = ApiManager.getInstance().createService(VerifyServerApi.class);
        if (mRxLifecycleView != null) {
            ApiMethods.subscribe(grabRoomServerApi.checkJoinVideoRoomPermission(), new ApiObserver<ApiResult>() {
                @Override
                public void process(ApiResult obj) {
                    process2(obj, successCallback);
                }
            }, mRxLifecycleView, new ApiMethods.RequestControl("checkCreatePublicRoomPermission", ApiMethods.ControlType.CancelThis));
        } else {
            ApiMethods.subscribe(grabRoomServerApi.checkJoinVideoRoomPermission(), new ApiObserver<ApiResult>() {
                @Override
                public void process(ApiResult obj) {
                    process2(obj, successCallback);
                }
            }, mF, new ApiMethods.RequestControl("checkCreatePublicRoomPermission", ApiMethods.ControlType.CancelThis));
        }
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
                    Context context = null;
                    if (mRxLifecycleView != null) {
                        context = ((View) mRxLifecycleView).getContext();
                    } else {
                        context = mF.getContext();
                    }
                    mTipsDialogView = new TipsDialogView.Builder(context)
                            .setMessageTip("撕歌的宝贝们，两分钟完成认证，超有趣的视频玩法等着你来哦")
                            .setConfirmTip("快速认证")
                            .setCancelTip("取消")
                            .setConfirmBtnClickListener(new AnimateClickListener() {
                                @Override
                                public void click(View view) {
                                    mTipsDialogView.dismiss();
                                    ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                                            .withString("url", U.getChannelUtils().getUrlByChannel("http://app.inframe.mobi/oauth/mobile?from=video"))
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
