package com.zq.report.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.anim.ObjectPlayControlTemplate;
import com.common.base.BaseFragment;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.share.SharePanel;
import com.common.core.share.SharePlatform;
import com.common.core.share.ShareType;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.upload.UploadCallback;
import com.common.upload.UploadParams;
import com.common.upload.UploadTask;
import com.common.utils.KeyboardEvent;
import com.common.utils.LogUploadUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExRelativeLayout;
import com.component.busilib.R;
import com.respicker.ResPicker;
import com.respicker.activity.ResPickerActivity;
import com.respicker.model.ImageItem;
import com.zq.person.model.PhotoModel;
import com.zq.report.FeedbackServerApi;
import com.zq.report.view.FeedbackView;
import com.zq.toast.CommonToastView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.agora.rtc.RtcEngine;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.zq.person.model.PhotoModel.STATUS_FAILED;
import static com.zq.person.model.PhotoModel.STATUS_SUCCESS;
import static com.zq.person.model.PhotoModel.STATUS_WAIT_UPLOAD;

/**
 * 快速一键反馈
 */
public class QuickFeedbackFragment extends BaseFragment {
    public final static String TAG = "QuickFeedbackFragment";
    //反馈
    public static final int FEED_BACK = 0;
    //举报
    public static final int REPORT = 1;

    public static final int FROM_RANK_ROOM = 1;  //标记来源
    public static final int FROM_GRAB_ROOM = 2;
    public static final int FROM_DOUBLE_ROOM = 3;

    private int mFrom;  //标记举报来源

    ExRelativeLayout mContainer;
    FeedbackView mFeedBackView;
    View mPlaceView;
    ProgressBar mUploadProgressBar;
    List<PhotoModel> mPhotoModelList;
    String mContent;
    List<Integer> mTypeList;
    String mLogUrl = "";
    int mFeedBackViewHeight;
    int mTopMargin;
    int mActionType;
    int mTargetId;

    Handler mHandler = new Handler(Looper.getMainLooper());

    ObjectPlayControlTemplate<PhotoModel, QuickFeedbackFragment> mPlayControlTemplate = new ObjectPlayControlTemplate<PhotoModel, QuickFeedbackFragment>() {
        @Override
        protected QuickFeedbackFragment accept(PhotoModel cur) {
            return QuickFeedbackFragment.this;
        }

        @Override
        public void onStart(PhotoModel pm, QuickFeedbackFragment personFragment2) {
            MyLog.d(TAG, "onStart" + "开始上传 PhotoModel=" + pm + " 队列还有 mPlayControlTemplate.getSize()=" + mPlayControlTemplate.getSize());
            execUploadPhoto(pm);
        }

        @Override
        protected void onEnd(PhotoModel pm) {
            MyLog.d(TAG, "onEnd" + " 上传结束 PhotoModel=" + pm);
        }
    };

    @Override
    public int initView() {
        return R.layout.quick_feedback_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mContainer = mRootView.findViewById(R.id.container);
        mFeedBackView = mRootView.findViewById(R.id.feed_back_view);
        mPlaceView = mRootView.findViewById(R.id.place_view);
        mUploadProgressBar = mRootView.findViewById(R.id.upload_progress_bar);
        mFeedBackView.setActionType(mActionType);

        mFeedBackView.setListener(new FeedbackView.Listener() {
            @Override
            public void onClickSubmit(final List<Integer> typeList, final String content, final List<ImageItem> imageItemList) {
                U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                mUploadProgressBar.setVisibility(View.VISIBLE);
                if (mActionType == FEED_BACK) {
                    U.getLogUploadUtils().upload(MyUserInfoManager.getInstance().getUid(), new LogUploadUtils.Callback() {
                        @Override
                        public void onSuccess(String url) {
                            tryUploadPic(typeList, content, imageItemList, url);
                        }

                        @Override
                        public void onFailed() {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mUploadProgressBar.setVisibility(View.GONE);
                                    U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                                            .setImage(R.drawable.touxiangshezhishibai_icon)
                                            .setText("反馈失败")
                                            .build());

                                    U.getFragmentUtils().popFragment(QuickFeedbackFragment.this);
                                }
                            });
                        }
                    }, true);
                } else {
                    tryUploadPic(typeList, content, imageItemList, "");
                }
            }
        });

        mPlaceView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().popFragment(QuickFeedbackFragment.this);
            }
        });

        mFeedBackView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mFeedBackView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mFeedBackViewHeight = mFeedBackView.getHeight();
                mTopMargin = U.getDisplayUtils().getScreenHeight() - mFeedBackViewHeight - U.getStatusBarUtil().getStatusBarHeight(getContext()) - U.getDisplayUtils().dip2px(24);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mContainer.getLayoutParams();
                layoutParams.setMargins(U.getDisplayUtils().dip2px(16), mTopMargin, U.getDisplayUtils().dip2px(16), 0);
                mPlaceView.getLayoutParams().height = mTopMargin;
                mRootView.requestLayout();
            }
        });
    }

    private void tryUploadPic(List<Integer> typeList, String content, List<ImageItem> imageItemList, String logUrl) {
        mLogUrl = logUrl;
        mTypeList = typeList;
        mContent = content;

        if (imageItemList != null && imageItemList.size() > 0) {
            List<PhotoModel> list = new ArrayList<>();
            for (ImageItem imageItem : imageItemList) {
                PhotoModel photoModel = new PhotoModel();
                photoModel.setLocalPath(imageItem.getPath());
                photoModel.setStatus(STATUS_WAIT_UPLOAD);
                list.add(photoModel);
                mPlayControlTemplate.add(photoModel, true);
            }

            mPhotoModelList = list;
        } else {
            feedback(typeList, content, mLogUrl, new ArrayList<String>());
        }
    }

    @Override
    public boolean onActivityResultReal(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == ResPickerActivity.REQ_CODE_RES_PICK) {
            List<ImageItem> imageItems = ResPicker.getInstance().getSelectedImageList();
            if (mFeedBackView != null) {
                mFeedBackView.uploadPhotoList(imageItems);
            }
            return true;
        }
        return super.onActivityResultReal(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    void execUploadPhoto(final PhotoModel photoModel) {
        MyLog.d(TAG, "execUploadPhoto" + " photoModel=" + photoModel);
        UploadTask uploadTask = UploadParams.newBuilder(photoModel.getLocalPath())
                .setNeedCompress(true)
                .setNeedMonitor(true)
                .setFileType(UploadParams.FileType.audit)
                .startUploadAsync(new UploadCallback() {
                    @Override
                    public void onProgressNotInUiThread(long currentSize, long totalSize) {

                    }

                    @Override
                    public void onSuccessNotInUiThread(String url) {
                        MyLog.d(TAG, "上传成功" + " url=" + url);
                        photoModel.setStatus(STATUS_SUCCESS);
                        photoModel.setPicPath(url);
                        checkUploadState(mPhotoModelList);
                    }

                    @Override
                    public void onFailureNotInUiThread(String msg) {
                        MyLog.d(TAG, "上传失败" + " msg=" + msg);
                        photoModel.setStatus(STATUS_FAILED);
                        checkUploadState(mPhotoModelList);
                    }
                });
    }

    private void checkUploadState(final List<PhotoModel> imageItemList) {
        for (PhotoModel photoModel : imageItemList) {
            if (photoModel.getStatus() == STATUS_WAIT_UPLOAD) {
                return;
            }
        }

        ArrayList<String> picUrls = new ArrayList<>();
        for (PhotoModel photoModel : imageItemList) {
            if (!TextUtils.isEmpty(photoModel.getPicPath())) {
                picUrls.add(photoModel.getPicPath());
            }
        }

        feedback(mTypeList, mContent, mLogUrl, picUrls);
    }

    private void feedback(List<Integer> typeList, String content, String logUrl, List<String> picUrls) {
        MyLog.d(TAG, "feedback" + " typeList=" + typeList + " content=" + content + " logUrl=" + logUrl + " picUrls=" + picUrls);
        if (mActionType == FEED_BACK) {
            summitFeedback(typeList, content, logUrl, picUrls);
        } else {
            submitReport(typeList, content, picUrls);
        }
    }

    private void summitFeedback(List<Integer> typeList, String content, String logUrl, List<String> picUrls) {
        MyLog.d(TAG, "feedback" + " typeList=" + typeList + " content=" + content + " logUrl=" + logUrl + " picUrls=" + picUrls);
        HashMap<String, Object> map = new HashMap<>();
        map.put("createdAt", System.currentTimeMillis());
        map.put("appVer", U.getAppInfoUtils().getVersionName());
        map.put("channel", U.getChannelUtils().getChannel());
        if (mFrom == FROM_RANK_ROOM || mFrom == FROM_GRAB_ROOM) {
            map.put("source", 1);
        } else if (mFrom == FROM_DOUBLE_ROOM) {
            map.put("source", 3);
        }
        map.put("type", typeList);
        map.put("content", content);
        map.put("appLog", logUrl);
        map.put("screenshot", picUrls);

        FeedbackServerApi feedbackServerApi = ApiManager.getInstance().createService(FeedbackServerApi.class);
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(feedbackServerApi.feedback(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.d(TAG, "process" + " result=" + result);
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

    private void submitReport(List<Integer> typeList, String content, List<String> picUrls) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("targetID", mTargetId);
        map.put("content", content);
        map.put("screenshot", picUrls);
        map.put("type", typeList);
        if (mFrom == FROM_RANK_ROOM || mFrom == FROM_GRAB_ROOM) {
            map.put("source", 1);
        } else if (mFrom == FROM_DOUBLE_ROOM) {
            map.put("source", 3);
        }

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        UserInfoServerApi userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        ApiMethods.subscribe(userInfoServerApi.report(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                            .setImage(R.drawable.touxiangshezhichenggong_icon)
                            .setText("举报成功")
                            .build());
                    U.getFragmentUtils().popFragment(QuickFeedbackFragment.this);
                } else {
                    U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                            .setImage(R.drawable.touxiangshezhishibai_icon)
                            .setText("举报失败")
                            .build());
                    U.getFragmentUtils().popFragment(QuickFeedbackFragment.this);
                }
            }
        }, this);
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        if (type == 0) {
            mFrom = (int) data;
        } else if (type == 1) {
            mActionType = (int) data;
        } else if (type == 2) {
            mTargetId = (int) data;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(KeyboardEvent event) {
        MyLog.d(TAG, "onEvent" + " event=" + event);
        // 注意内容高度
        switch (event.eventType) {
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_HIDDEN: {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mContainer.getLayoutParams();
                layoutParams.setMargins(U.getDisplayUtils().dip2px(16), mTopMargin, U.getDisplayUtils().dip2px(16), 0);
                mPlaceView.getLayoutParams().height = mTopMargin;
                mRootView.requestLayout();
                break;
            }
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE: {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mContainer.getLayoutParams();
                layoutParams.setMargins(U.getDisplayUtils().dip2px(16), mTopMargin - event.keybordHeight, U.getDisplayUtils().dip2px(16), 0);
                mPlaceView.getLayoutParams().height = mTopMargin - event.keybordHeight < 0 ? 0 : mTopMargin - event.keybordHeight;
                mRootView.requestLayout();
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


    @Override
    public void destroy() {
        super.destroy();
        U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
        mHandler.removeCallbacksAndMessages(null);
    }
}
