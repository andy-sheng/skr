package com.zq.live;

import android.text.TextUtils;

import com.common.clipboard.ClipboardUtils;
import com.common.core.account.UserAccountManager;
import com.common.core.account.event.AccountEvent;
import com.common.core.kouling.SkrKouLingUtils;
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
import com.common.utils.U;
import com.component.busilib.recommend.RA;
import com.component.busilib.recommend.RAServerApi;
import com.zq.person.photo.PhotoLocalApi;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;

/**
 * 这个类放在最顶层，因为只有可能它 调用 其它
 * 其它不后悔调用它
 */
public class GlobalEventReceiver {
    public final static String TAG = "GlobalEventReceiver";

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
        if(UserAccountManager.getInstance().hasAccount()){
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

    @Subscribe
    public void onEvent(AccountEvent.LogoffAccountEvent event) {
        PhotoLocalApi.deleteAll();
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
                if(obj.getErrno() == 0){
                    String vars = obj.getData().getString("vars");
                    String testList= obj.getData().getString("testList");
                    RA.setVar(vars);
                    RA.setTestList(testList);
                    if(RA.hasTestList()){
                        HashMap map = new HashMap();
                        map.put("testList", RA.getTestList());
                        StatisticsAdapter.recordCountEvent("ra","active",map);
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
}
