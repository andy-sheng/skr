package com.zq.report.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.share.SharePanel;
import com.common.core.share.SharePlatform;
import com.common.core.share.ShareType;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.KeyboardEvent;
import com.common.utils.LogUploadUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.component.busilib.R;
import com.zq.report.FeedbackServerApi;
import com.zq.report.view.FeedbackView;
import com.zq.toast.CommonToastView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

import io.agora.rtc.RtcEngine;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.zq.report.view.FeedbackView.FEEDBACK_ERRO;
import static com.zq.report.view.FeedbackView.FEEDBACK_SUGGEST;

/**
 * 快速一键反馈
 */
public class QuickFeedbackFragment extends BaseFragment {

    View mPlaceHolderView;
    RelativeLayout mContainer;
    FeedbackView mFeedBackView;
    View mPlaceView;
    ProgressBar mUploadProgressBar;

    @Override
    public int initView() {
        return R.layout.quick_feedback_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mPlaceHolderView = (View) mRootView.findViewById(R.id.place_holder_view);
        mContainer = (RelativeLayout) mRootView.findViewById(R.id.container);
        mFeedBackView = (FeedbackView) mRootView.findViewById(R.id.feed_back_view);
        mPlaceView = (View) mRootView.findViewById(R.id.place_view);
        mUploadProgressBar = (ProgressBar) mRootView.findViewById(R.id.upload_progress_bar);

        mFeedBackView.setListener(new FeedbackView.Listener() {
            @Override
            public void onClickSubmit(final int type, final String content) {
                U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                mUploadProgressBar.setVisibility(View.VISIBLE);
                if (type == FEEDBACK_ERRO) {
                    U.getLogUploadUtils().upload(MyUserInfoManager.getInstance().getUid(), new LogUploadUtils.Callback() {
                        @Override
                        public void onSuccess(String url) {
                            feedback(new int[]{type}, content, url);
                        }

                        @Override
                        public void onFailed() {
                            mUploadProgressBar.setVisibility(View.GONE);
                            U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                                    .setImage(R.drawable.touxiangshezhishibai_icon)
                                    .setText("反馈失败")
                                    .build());

                            U.getFragmentUtils().popFragment(QuickFeedbackFragment.this);
                        }
                    }, true);
                } else if (type == FEEDBACK_SUGGEST) {
                    feedback(new int[]{type}, content, null);
                } else {
                    // donothing
                }
            }
        });

        mPlaceView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().popFragment(QuickFeedbackFragment.this);
            }
        });
    }

    private void feedback(int[] type, String content, String logUrl) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("createdAt", System.currentTimeMillis());
        map.put("appVer", U.getAppInfoUtils().getVersionName());
        map.put("channel", U.getChannelUtils().getChannel());
        map.put("source", 1);
        map.put("type", type);
        map.put("content", content);
        map.put("appLog", logUrl);

        FeedbackServerApi feedbackServerApi = ApiManager.getInstance().createService(FeedbackServerApi.class);
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(feedbackServerApi.feedback(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    mUploadProgressBar.setVisibility(View.GONE);
                    U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                            .setImage(R.drawable.touxiangshezhichenggong_icon)
                            .setText("反馈成功")
                            .build());

                    U.getFragmentUtils().popFragment(QuickFeedbackFragment.this);
                } else {
                    mUploadProgressBar.setVisibility(View.GONE);
                    U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                            .setImage(R.drawable.touxiangshezhishibai_icon)
                            .setText(result.getErrmsg())
                            .build());

                    U.getFragmentUtils().popFragment(QuickFeedbackFragment.this);
                }
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                super.onNetworkError(errorType);
                mUploadProgressBar.setVisibility(View.GONE);
                U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                        .setImage(R.drawable.touxiangshezhishibai_icon)
                        .setText("反馈失败！\n请检查网络之后重试")
                        .build());
                U.getFragmentUtils().popFragment(QuickFeedbackFragment.this);
            }
        });
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(KeyboardEvent event) {
        MyLog.d(TAG, "onEvent" + " event=" + event);
        switch (event.eventType) {
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_HIDDEN: {
                mPlaceHolderView.getLayoutParams().height = event.keybordHeight;
                mPlaceHolderView.setLayoutParams(mPlaceHolderView.getLayoutParams());
                break;
            }
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE: {
                mPlaceHolderView.getLayoutParams().height = event.keybordHeight;
                mPlaceHolderView.setLayoutParams(mPlaceHolderView.getLayoutParams());
                break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LogUploadUtils.UploadLogEvent event) {
        if (event.mIsSuccess && MyLog.isDebugLogOpen()) {
            // 尝试跳到微信
            U.getToastUtil().showLong("调试模式，请通过微信将反馈分享给研发");
            SharePanel sharePanel = new SharePanel(getActivity());
            StringBuilder sb = new StringBuilder();
            sb.append("userID=").append(UserAccountManager.getInstance().getUuid());
            sb.append(" name=").append(MyUserInfoManager.getInstance().getNickName());
            sb.append(" ts=").append(U.getDateTimeUtils().formatTimeStringForDate(System.currentTimeMillis()));
//            sb.append("屏宽:").append(U.getDisplayUtils().getScreenWidth())
//                    .append(" 屏高:").append(U.getDisplayUtils().getScreenHeight())
//                    .append(" 手机高:").append(U.getDisplayUtils().getPhoneHeight())
//                    .append(" density:").append(U.getDisplayUtils().getDensity())
//                    .append(" densityDpi:").append(U.getDisplayUtils().getDensityDpi())
//                    .append("\n");
//            sb.append("是否开启了虚拟导航键：").append(U.getDeviceUtils().hasNavigationBar()).append(" 虚拟导航键高度:")
//                    .append(U.getDeviceUtils().getVirtualNavBarHeight())
//                    .append("\n");
//            sb.append("最小宽度为 px/(dpi/160)=").append((U.getDisplayUtils().getPhoneWidth() / (U.getDisplayUtils().getDensityDpi() / 160))).append("dp").append("\n");
//            sb.append("当前手机适用的资源文件夹是").append(U.app().getResources().getString(R.string.values_from)).append("\n");
//            sb.append("android.os.Build.VERSION.SDK_INT:").append(android.os.Build.VERSION.SDK_INT).append("\n");
            sb.append(" version:").append(U.getAppInfoUtils().getVersionName())
                    .append(" 渠道号:").append(U.getChannelUtils().getChannel())
                    .append(" Mylog.debugOpen:").append(MyLog.isDebugLogOpen());
            sb.append(" 手机型号:").append(U.getDeviceUtils().getProductModel());
            sb.append(" 手机厂商:").append(U.getDeviceUtils().getProductBrand());

//            sb.append("deviceId(参考miui唯一设备号的方法):").append(U.getDeviceUtils().getDeviceID()).append("\n");
            sb.append("agora sdk version:").append(RtcEngine.getSdkVersion());
            sharePanel.setShareContent(MyUserInfoManager.getInstance().getAvatar(), "请将url分享给研发(只在调试模式开启)", sb.toString(), event.mUrl);
            sharePanel.share(SharePlatform.WEIXIN, ShareType.URL);
        }
    }
}
