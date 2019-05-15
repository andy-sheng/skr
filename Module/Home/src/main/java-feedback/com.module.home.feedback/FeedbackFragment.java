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

import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.base.BuildConfig;
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
import com.common.statistics.TimeStatistics;
import com.common.utils.LogUploadUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;
import com.common.view.titlebar.CommonTitleBar;
import com.dialog.view.StrokeTextView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.home.R;
import com.module.home.musictest.model.Answer;
import com.xiaomi.mistatistic.sdk.MiStatInterface;
import com.zq.toast.CommonToastView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.agora.rtc.RtcEngine;
import io.reactivex.functions.Consumer;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class FeedbackFragment extends BaseFragment {

    private static int FEEDBACK_ERRO = 1;  // 反馈问题
    private static int FEEDBACK_SUGGEST = 2; // 功能建议

    CommonTitleBar mTitlebar;
    RadioGroup mButtonArea;
    RadioButton mErrorBack;
    RadioButton mFeedBack;
    NoLeakEditText mFeedbackContent;
    ExTextView mContentTextSize;
    StrokeTextView mSubmitTv;

    ProgressBar mUploadProgressBar;

    int mBefore;  // 记录之前的位置
    int mType = FEEDBACK_ERRO;

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
        mSubmitTv = (StrokeTextView) mRootView.findViewById(R.id.submit_tv);
        mUploadProgressBar = (ProgressBar) mRootView.findViewById(R.id.upload_progress_bar);


        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                //U.getSoundUtils().play(TAG, R.raw.normal_back, 500);
                U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                U.getFragmentUtils().popFragment(FeedbackFragment.this);
            }
        });

        mButtonArea.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.error_back) {
                    mType = FEEDBACK_ERRO;
                } else if (checkedId == R.id.feed_back) {
                    mType = FEEDBACK_SUGGEST;
                }
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
                        if (mType == FEEDBACK_ERRO) {
                            U.getLogUploadUtils().upload(MyUserInfoManager.getInstance().getUid(), new LogUploadUtils.Callback() {
                                @Override
                                public void onSuccess(String url) {
                                    feedback(new int[]{mType}, url);
                                }

                                @Override
                                public void onFailed() {
                                    mUploadProgressBar.setVisibility(View.GONE);
                                    U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                                            .setImage(R.drawable.touxiangshezhishibai_icon)
                                            .setText("反馈失败")
                                            .build());

                                    U.getFragmentUtils().popFragment(FeedbackFragment.this);
                                }
                            }, true);
                        } else if (mType == FEEDBACK_SUGGEST) {
                            feedback(new int[]{mType}, null);
                        } else {
                            // donothing
                        }
                    }
                });

        U.getSoundUtils().preLoad(TAG, R.raw.normal_back);
    }

    private void feedback(int[] type, String logUrl) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("createdAt", System.currentTimeMillis());
        map.put("appVer", U.getAppInfoUtils().getVersionName());
        map.put("channel", U.getChannelUtils().getChannel());
        map.put("source", 2);
        map.put("type", type);
        String content = mFeedbackContent.getText().toString().trim();
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

                    U.getFragmentUtils().popFragment(FeedbackFragment.this);
                } else {
                    mUploadProgressBar.setVisibility(View.GONE);
                    U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                            .setImage(R.drawable.touxiangshezhishibai_icon)
                            .setText(result.getErrmsg())
                            .build());

                    U.getFragmentUtils().popFragment(FeedbackFragment.this);
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
            }
        });
    }

    @Override
    public void destroy() {
        super.destroy();
        U.getSoundUtils().release(TAG);
    }

    @Override
    public boolean useEventBus() {
        return true;
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
