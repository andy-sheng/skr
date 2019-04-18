package com.module.home.persenter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.account.UserAccountManager;
import com.common.core.global.event.ShowDialogInHomeEvent;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.home.MainPageSlideApi;
import com.module.home.R;
import com.module.home.model.RedPkgTaskModel;
import com.module.home.view.GetRedPkgCashView;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class RedPkgPresenter extends RxLifeCyclePresenter {
    public final static String TAG = "RedPkgPresenter";
    public final static String RED_PKG_TASK_ID = "1";

    public static final String PREF_KEY_RED_PKG_SHOW = "red_pkg_show";

    MainPageSlideApi mMainPageSlideApi;
    DialogPlus mRedPkgView;

    boolean mIsHasReq = false;

    public RedPkgPresenter() {
        mMainPageSlideApi = ApiManager.getInstance().createService(MainPageSlideApi.class);
    }

    public void checkRedPkg() {
        //showGetCashView(1,"ssss");
        if (mIsHasReq) {
            MyLog.d(TAG, "has checkRedPkg");
            return;
        }

        if (!UserAccountManager.getInstance().hasAccount()) {
            MyLog.w(TAG, "no account");
            return;
        }

        if (U.getPreferenceUtils().getSettingBoolean(PREF_KEY_RED_PKG_SHOW, false)) {
            MyLog.w(TAG, "checkRedPkg 展示过一次就不展示了");
            mIsHasReq = true;
            return;
        }

        ApiMethods.subscribe(mMainPageSlideApi.checkNewBieTask(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.d(TAG, "process" + " result=" + result.getErrno());
                if (result.getErrno() == 0) {
                    mIsHasReq = true;
                    RedPkgTaskModel redPkgTaskModel = JSONObject.parseObject(result.getData().getString("task"), RedPkgTaskModel.class);
                    if (redPkgTaskModel != null && !redPkgTaskModel.isDone()) {
                        showGetCashView(Float.parseFloat(redPkgTaskModel.getRedbagExtra().getCash()), redPkgTaskModel.getDeepLink());
                    } else {
                        MyLog.w(TAG, "checkRedPkg redPkgTaskModel is null or redPkgTaskModel is done,traceid is " + result.getTraceId());
                    }
                } else {
                    MyLog.d(TAG, "checkRedPkg failed, " + " result=" + result.getTraceId());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, e);
            }
        }, this);
    }


    public void showGetCashView(float cash, String scheme) {
        if (mRedPkgView != null) {
            mRedPkgView.dismiss();
        }

        Activity activity = U.getActivityUtils().getTopActivity();
        GetRedPkgCashView getRedPkgCashView = new GetRedPkgCashView(activity);
        TextView tvCash = getRedPkgCashView.findViewById(R.id.tv_cash);
        tvCash.setText("" + cash);

        if (mRedPkgView == null) {
            mRedPkgView = DialogPlus.newDialog(activity)
                    .setContentHolder(new ViewHolder(getRedPkgCashView))
                    .setGravity(Gravity.CENTER)
                    .setContentBackgroundResource(R.color.transparent)
                    .setOverlayBackgroundResource(R.color.black_trans_80)
                    .setExpanded(false)
                    .setCancelable(true)
                    .setOnDismissListener(new OnDismissListener() {
                        @Override
                        public void onDismiss(@NonNull DialogPlus dialog) {
                            U.getPreferenceUtils().setSettingBoolean(PREF_KEY_RED_PKG_SHOW, true);
                        }
                    })
                    .setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(@NonNull DialogPlus dialog, @NonNull View view) {
                            if (view.getId() == R.id.tv_ruzhang) {
                                ARouter.getInstance().build(RouterConstants.ACTIVITY_SCHEME)
                                        .withString("uri", scheme)
                                        .navigation();
                            }

                            mRedPkgView.dismiss();
                        }
                    })
                    .create();
        }

        EventBus.getDefault().post(new ShowDialogInHomeEvent(mRedPkgView, 30));
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mRedPkgView != null) {
            mRedPkgView.dismiss();
        }
    }
}
