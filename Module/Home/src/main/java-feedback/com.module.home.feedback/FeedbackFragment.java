package com.module.home.feedback;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.common.base.BaseFragment;
import com.common.base.BuildConfig;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.share.SharePanel;
import com.common.core.share.SharePlatform;
import com.common.core.share.ShareType;
import com.common.log.MyLog;
import com.common.statistics.TimeStatistics;
import com.common.utils.LogUploadUtils;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;
import com.common.view.titlebar.CommonTitleBar;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.home.R;
import com.xiaomi.mistatistic.sdk.MiStatInterface;
import com.zq.toast.CommonToastView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import io.agora.rtc.RtcEngine;
import io.reactivex.functions.Consumer;

public class FeedbackFragment extends BaseFragment {

    CommonTitleBar mTitlebar;
    RadioGroup mButtonArea;
    RadioButton mErrorBack;
    RadioButton mFeedBack;
    NoLeakEditText mFeedbackContent;
    ExTextView mContentTextSize;
    ExTextView mSubmitTv;

    ProgressBar mUploadProgressBar;

    int mBefore;  // 记录之前的位置

    @Override
    public int initView() {
        return R.layout.feedback_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mButtonArea = (RadioGroup) mRootView.findViewById(R.id.button_area);
        mErrorBack = (RadioButton) mRootView.findViewById(R.id.error_back);
        mFeedBack = (RadioButton) mRootView.findViewById(R.id.feed_back);
        mFeedbackContent = (NoLeakEditText) mRootView.findViewById(R.id.feedback_content);
        mContentTextSize = (ExTextView) mRootView.findViewById(R.id.content_text_size);
        mSubmitTv = (ExTextView) mRootView.findViewById(R.id.submit_tv);
        mUploadProgressBar = (ProgressBar) mRootView.findViewById(R.id.upload_progress_bar);


        RxView.clicks(mTitlebar.getLeftTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                        U.getFragmentUtils().popFragment(FeedbackFragment.this);
                    }
                });


        mFeedbackContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mBefore = i;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int length = editable.length();
                mContentTextSize.setText("" + length + "/200");
                int selectionEnd = mFeedbackContent.getSelectionEnd();
                if (length > 200) {
                    editable.delete(mBefore, selectionEnd);
                    mFeedbackContent.setText(editable.toString());
                    int selection = editable.length();
                    mFeedbackContent.setSelection(selection);
                }
            }
        });

        RxView.clicks(mSubmitTv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                        mUploadProgressBar.setVisibility(View.VISIBLE);
                        U.getLogUploadUtils().upload(MyUserInfoManager.getInstance().getUid());
                    }
                });

    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LogUploadUtils.UploadLogEvent event) {
        mUploadProgressBar.setVisibility(View.GONE);
        U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(getContext())
                .setImage(event.mIsSuccess ? R.drawable.touxiangshezhichenggong_icon : R.drawable.touxiangshezhishibai_icon)
                .setText(event.mIsSuccess ? "反馈成功" : "反馈失败")
                .build());

        U.getFragmentUtils().popFragment(FeedbackFragment.this);
        if (event.mIsSuccess && MyLog.isDebugLogOpen()) {
            // 尝试跳到微信
            U.getToastUtil().showLong("调试模式，请通过微信将反馈分享给研发");
            SharePanel sharePanel = new SharePanel(getActivity());
            StringBuilder sb = new StringBuilder();
            sb.append("userID=").append(UserAccountManager.getInstance().getUuid());
            sb.append(" name=").append(MyUserInfoManager.getInstance().getNickName());
            sb.append(" ts=").append(U.getDateTimeUtils().formatTimeStringForDate(System.currentTimeMillis())).append("\n");
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
            sb.append("手机型号:").append(U.getDeviceUtils().getProductModel()).append("\n");
            sb.append("手机厂商:").append(U.getDeviceUtils().getProductBrand()).append("\n");
            sb.append("渠道号:").append(U.getChannelUtils().getChannel()).append(" debug:").append(BuildConfig.DEBUG).append("\n");
//            sb.append("deviceId(参考miui唯一设备号的方法):").append(U.getDeviceUtils().getDeviceID()).append("\n");
            sb.append("agora sdk version:").append(RtcEngine.getSdkVersion()).append("\n");
            sharePanel.setShareContent(MyUserInfoManager.getInstance().getAvatar(), "请将url分享给研发(只在调试模式开启)", sb.toString(), event.mUrl);
            sharePanel.share(SharePlatform.WEIXIN, ShareType.URL);
        }
    }
}
