package com.zq.live;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.clipboard.ClipboardUtils;
import com.common.core.account.UserAccountManager;
import com.common.core.account.event.AccountEvent;
import com.common.core.kouling.SkrKouLingUtils;
import com.common.core.login.LoginActivity;
import com.common.core.share.SharePanel;
import com.common.core.share.SharePlatform;
import com.common.core.share.ShareType;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.ActivityUtils;
import com.common.utils.LogUploadUtils;
import com.common.utils.NetworkUtils;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.component.busilib.manager.BgMusicManager;
import com.component.busilib.recommend.RA;
import com.component.busilib.recommend.RAServerApi;
import com.dialog.view.TipsDialogView;
import com.module.RouterConstants;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.zq.person.photo.PhotoLocalApi;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

/**
 * 这个类放在最顶层，因为只有可能它 调用 其它
 * 其它不后悔调用它
 */
public class GlobalEventReceiver {
    public final static String TAG = "GlobalEventReceiver";

    Handler mUiHandler = new Handler(Looper.getMainLooper());

    TipsDialogView mTipsDialogView;

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
        if (UserAccountManager.getInstance().hasAccount()) {
            initABtestInfo();
        }
    }


    @Subscribe
    public void onEvent(ActivityUtils.ForeOrBackgroundChange event) {
        if (event.foreground) {
            // 检查剪贴板
            String str = ClipboardUtils.getPaste();
//            str = "dD0xJnU9MTM0MzA4OCZyPTEwMA==";
            if (!TextUtils.isEmpty(str)) {
                SkrKouLingUtils.tryParseScheme(str);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AccountEvent.LogoffAccountEvent event) {
        PhotoLocalApi.deleteAll();
        if (event.reason == AccountEvent.LogoffAccountEvent.REASON_ACCOUNT_EXPIRED) {
            MyLog.w(TAG, "LogoffAccountEvent" + " 账号已经过期，需要重新登录,跳到登录页面");
        }
        if (!UserAccountManager.getInstance().hasAccount()) {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_LOGIN)
                    .withInt(LoginActivity.KEY_REASON, LoginActivity.REASON_LOGOFF)
                    .navigation();
        }
        Activity homeActivity = U.getActivityUtils().getHomeActivity();
        if (homeActivity != null) {
            UMShareAPI.get(U.app()).deleteOauth(homeActivity, SHARE_MEDIA.WEIXIN, null);
            UMShareAPI.get(U.app()).deleteOauth(homeActivity, SHARE_MEDIA.QQ, null);
        }
        BgMusicManager.getInstance().destory();
    }

    @Subscribe
    public void onEvent(AccountEvent.SetAccountEvent event) {
        initABtestInfo();
    }

    /**
     * 获取AB test 相关的信息
     */
    private void initABtestInfo() {
        RAServerApi raServerApi = ApiManager.getInstance().createService(RAServerApi.class);
        ApiMethods.subscribe(raServerApi.getABtestInfo(RA.getTestList()), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    String vars = obj.getData().getString("vars");
                    String testList = obj.getData().getString("testList");
                    RA.setVar(vars);
                    RA.setTestList(testList);
                    if (RA.hasTestList()) {
                        HashMap map = new HashMap();
                        map.put("testList", RA.getTestList());
                        StatisticsAdapter.recordCountEvent("ra", "active", map);
                    }
                }
            }
        });
    }

    @Subscribe
    public void onEvent(LogUploadUtils.RequestOthersUploadLogSuccess event) {
        SharePanel sharePanel = new SharePanel(U.getActivityUtils().getTopActivity());
        String title = String.format("日志 id=%s,name=%s,date=%s", event.uploaderId, event.uploaderName, event.date);
        sharePanel.setShareContent(event.uploaderAvatar, title, event.extra, event.mLogUrl);
        sharePanel.share(SharePlatform.WEIXIN, ShareType.TEXT);
        MyLog.w(TAG, title + " url:" + event.mLogUrl);
        U.getToastUtil().showLong(title + "拉取成功，请将其分享给研发同学");
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NetworkUtils.NetworkChangeEvent event) {
        if (event.type == -1) {
            mUiHandler.removeCallbacksAndMessages(mNetworkChangeRunnable);
            mUiHandler.postDelayed(mNetworkChangeRunnable, 3000);
        } else {
            mUiHandler.removeCallbacks(mNetworkChangeRunnable);
        }
    }

    private Runnable mNetworkChangeRunnable = new Runnable() {
        @Override
        public void run() {
            showNetworkDisConnectDialog();
        }
    };

    private void showNetworkDisConnectDialog() {
        if (mTipsDialogView != null) {
            mTipsDialogView.dismiss(false);
        }
        mTipsDialogView  = new TipsDialogView.Builder(U.getActivityUtils().getTopActivity())
                .setMessageTip("网络异常\n请检查网络连接后重试")
                .setOkBtnTip("确认")
                .setOkBtnClickListener(new AnimateClickListener() {
                    @Override
                    public void click(View view) {
                        if (mTipsDialogView != null) {
                            mTipsDialogView.dismiss();
                        }
                    }
                })
                .build();
        mTipsDialogView.showByDialog();
    }

}
