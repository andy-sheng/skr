package com.module.home.feedback;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;

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
import com.common.rxretrofit.ControlType;
import com.common.rxretrofit.RequestControl;
import com.common.upload.UploadCallback;
import com.common.upload.UploadParams;
import com.common.upload.UploadTask;
import com.common.utils.LogUploadUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.titlebar.CommonTitleBar;
import com.module.home.R;
import com.respicker.ResPicker;
import com.respicker.activity.ResPickerActivity;
import com.respicker.model.ImageItem;
import com.component.person.photo.model.PhotoModel;
import com.component.report.FeedbackServerApi;
import com.component.report.view.FeedbackView;
import com.component.toast.CommonToastView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.agora.rtc.RtcEngine;
import okhttp3.MediaType;
import okhttp3.RequestBody;

//TODO 有时间一定要优化一下
public class FeedbackFragment extends BaseFragment {
    //反馈
    public static final int FEED_BACK = 0;
    //举报
    public static final int REPORT = 1;
    //版权举报
    public static final int COPY_REPORT = 2;

    CommonTitleBar mTitlebar;
    FeedbackView mFeedBackView;
    View mPlaceView;
    ProgressBar mUploadProgressBar;

    List<PhotoModel> mPhotoModelList;
    String mContent;
    List<Integer> mTypeList;
    String mLogUrl = "";
    int mActionType;
    int mTargetId;
    int mFeedID;
    int mSongID;

    Handler mHandler = new Handler(Looper.getMainLooper());
    boolean mUploading = false;

    ObjectPlayControlTemplate<PhotoModel, FeedbackFragment> mPlayControlTemplate = new ObjectPlayControlTemplate<PhotoModel, FeedbackFragment>() {
        @Override
        protected FeedbackFragment accept(PhotoModel cur) {
            if (mUploading) {
                return null;
            }
            mUploading = true;
            return FeedbackFragment.this;
        }

        @Override
        public void onStart(PhotoModel pm, FeedbackFragment personFragment2) {
            MyLog.d(getTAG(), "onStart" + "开始上传 PhotoModel=" + pm + " 队列还有 mPlayControlTemplate.getSize()=" + mPlayControlTemplate.getSize());
            execUploadPhoto(pm);
        }

        @Override
        protected void onEnd(PhotoModel pm) {
            MyLog.d(getTAG(), "onEnd" + " 上传结束 PhotoModel=" + pm);
        }
    };

    @Override
    public int initView() {
        return R.layout.feedback_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = (CommonTitleBar) getRootView().findViewById(R.id.titlebar);
        mFeedBackView = (FeedbackView) getRootView().findViewById(R.id.feed_back_view);
        mUploadProgressBar = (ProgressBar) getRootView().findViewById(R.id.upload_progress_bar);
        mPlaceView = (View) getRootView().findViewById(R.id.place_view);
        mFeedBackView.setActionType(mActionType);

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                //U.getSoundUtils().play(TAG, R.raw.normal_back, 500);
                U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                if (mActionType == COPY_REPORT) {
                    getActivity().finish();
                } else {
                    U.getFragmentUtils().popFragment(FeedbackFragment.this);
                }


            }
        });

        if (mActionType == FEED_BACK) {
            mTitlebar.getCenterTextView().setText("意见反馈");
        } else if (mActionType == COPY_REPORT) {
            mTitlebar.getCenterTextView().setText("版权举报");
        } else {
            mTitlebar.getCenterTextView().setText("举报");
        }

        mPlaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        mFeedBackView.setListener(new FeedbackView.Listener() {
            @Override
            public void onClickSubmit(List<Integer> typeList, String content, List<ImageItem> imageItemList) {
                U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                mPlaceView.setVisibility(View.VISIBLE);
                mUploadProgressBar.setVisibility(View.VISIBLE);
                if (mActionType == FEED_BACK) {
                    U.getLogUploadUtils().upload(MyUserInfoManager.INSTANCE.getUid(), new LogUploadUtils.Callback() {
                        @Override
                        public void onSuccess(String url) {
                            tryUploadPic(typeList, content, imageItemList, url);
                        }

                        @Override
                        public void onFailed() {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mPlaceView.setVisibility(View.GONE);
                                    mUploadProgressBar.setVisibility(View.GONE);
                                    U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                                            .setImage(com.component.busilib.R.drawable.touxiangshezhishibai_icon)
                                            .setText("反馈失败")
                                            .build());

                                    U.getFragmentUtils().popFragment(FeedbackFragment.this);
                                }
                            });
                        }
                    }, true);
                } else {
                    tryUploadPic(typeList, content, imageItemList, "");
                }
            }
        });

        U.getSoundUtils().preLoad(getTAG(), R.raw.normal_back);
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
                photoModel.setStatus(PhotoModel.Companion.getSTATUS_WAIT_UPLOAD());
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

    void execUploadPhoto(final PhotoModel photoModel) {
        MyLog.d(getTAG(), "execUploadPhoto" + " photoModel=" + photoModel);
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
                        MyLog.d(getTAG(), "上传成功" + " url=" + url);
                        photoModel.setStatus(PhotoModel.Companion.getSTATUS_SUCCESS());
                        photoModel.setPicPath(url);
                        checkUploadState(mPhotoModelList);
                        mUploading = false;
                        mPlayControlTemplate.endCurrent(photoModel);
                    }

                    @Override
                    public void onFailureNotInUiThread(String msg) {
                        MyLog.d(getTAG(), "上传失败" + " msg=" + msg);
                        photoModel.setStatus(PhotoModel.Companion.getSTATUS_FAILED());
                        checkUploadState(mPhotoModelList);
                        mUploading = false;
                        mPlayControlTemplate.endCurrent(photoModel);
                    }
                });
    }

    private void checkUploadState(final List<PhotoModel> imageItemList) {
        for (PhotoModel photoModel : imageItemList) {
            if (photoModel.getStatus() == PhotoModel.Companion.getSTATUS_WAIT_UPLOAD()) {
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

    @Override
    public void setData(int type, @Nullable Object data) {
        if (type == 0) {
            mActionType = (int) data;
        } else if (type == 1) {
            mTargetId = (int) data;
        } else if (type == 2) {
            mFeedID = (int) data;
        } else if (type == 3) {
            mSongID = (int) data;
        }
    }

    private void feedback(List<Integer> typeList, String content, String logUrl, List<String> picUrls) {
        MyLog.d(getTAG(), "feedback" + " typeList=" + typeList + " content=" + content + " logUrl=" + logUrl + " picUrls=" + picUrls);
        if (mActionType == FEED_BACK) {
            summitFeedback(typeList, content, logUrl, picUrls);
        } else if (mActionType == COPY_REPORT) {
            submitCopyReport(content, picUrls);
        } else {
            submitReport(typeList, content, picUrls);
        }
    }

    private void submitCopyReport(String content, List<String> picUrls) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("targetID", mTargetId);
        map.put("content", content);
        map.put("screenshot", picUrls);
        map.put("feedID", mFeedID);
        map.put("songID", mSongID);
        map.put("source", 7);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        UserInfoServerApi userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        ApiMethods.subscribe(userInfoServerApi.report(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    getActivity().finish();
                    U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                            .setImage(com.component.busilib.R.drawable.touxiangshezhichenggong_icon)
                            .setText("举报成功")
                            .build());
                } else {
                    getActivity().finish();
                    U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                            .setImage(com.component.busilib.R.drawable.touxiangshezhishibai_icon)
                            .setText("举报失败")
                            .build());
                }
            }
        }, this, new RequestControl("feedback", ControlType.CancelThis));

    }

    private void summitFeedback(List<Integer> typeList, String content, String logUrl, List<String> picUrls) {
        MyLog.d(getTAG(), "feedback" + " typeList=" + typeList + " content=" + content + " logUrl=" + logUrl + " picUrls=" + picUrls);
        HashMap<String, Object> map = new HashMap<>();
        map.put("createdAt", System.currentTimeMillis());
        map.put("appVer", U.getAppInfoUtils().getVersionName());
        map.put("channel", U.getChannelUtils().getChannel());
        map.put("source", 2);
        map.put("type", typeList);
        map.put("content", content);
        map.put("appLog", logUrl);
        map.put("screenshot", picUrls);

        FeedbackServerApi feedbackServerApi = ApiManager.getInstance().createService(FeedbackServerApi.class);
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(feedbackServerApi.feedback(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.d(getTAG(), "process" + " result=" + result);
                if (result.getErrno() == 0) {
                    mPlaceView.setVisibility(View.GONE);
                    mUploadProgressBar.setVisibility(View.GONE);
                    U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                            .setImage(com.component.busilib.R.drawable.touxiangshezhichenggong_icon)
                            .setText("反馈成功")
                            .build());

                    U.getFragmentUtils().popFragment(FeedbackFragment.this);
                } else {
                    mPlaceView.setVisibility(View.GONE);
                    mUploadProgressBar.setVisibility(View.GONE);
                    U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                            .setImage(com.component.busilib.R.drawable.touxiangshezhishibai_icon)
                            .setText(result.getErrmsg())
                            .build());

                    U.getFragmentUtils().popFragment(FeedbackFragment.this);
                }
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                super.onNetworkError(errorType);
                mPlaceView.setVisibility(View.GONE);
                mUploadProgressBar.setVisibility(View.GONE);
                U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                        .setImage(com.component.busilib.R.drawable.touxiangshezhishibai_icon)
                        .setText("反馈失败！\n请检查网络之后重试")
                        .build());
                U.getFragmentUtils().popFragment(FeedbackFragment.this);
            }
        }, new RequestControl("feedback", ControlType.CancelThis));
    }

    private void submitReport(List<Integer> typeList, String content, List<String> picUrls) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("targetID", mTargetId);
        map.put("content", content);
        map.put("screenshot", picUrls);
        map.put("type", typeList);
        map.put("source", 2);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        UserInfoServerApi userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        ApiMethods.subscribe(userInfoServerApi.report(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                            .setImage(com.component.busilib.R.drawable.touxiangshezhichenggong_icon)
                            .setText("举报成功")
                            .build());
                    U.getFragmentUtils().popFragment(FeedbackFragment.this);
                } else {
                    U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                            .setImage(com.component.busilib.R.drawable.touxiangshezhishibai_icon)
                            .setText("举报失败")
                            .build());
                    U.getFragmentUtils().popFragment(FeedbackFragment.this);
                }
            }
        }, this, new RequestControl("feedback", ControlType.CancelThis));
    }

    @Override
    public void destroy() {
        super.destroy();
        U.getSoundUtils().release(getTAG());
        mHandler.removeCallbacksAndMessages(null);
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
            sb.append("userID=").append(UserAccountManager.INSTANCE.getUuid());
            sb.append(" name=").append(MyUserInfoManager.INSTANCE.getNickName());
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
            sharePanel.setShareContent(MyUserInfoManager.INSTANCE.getAvatar(), "请将url分享给研发(只在调试模式开启)", sb.toString(), event.mUrl);
            sharePanel.share(SharePlatform.WEIXIN, ShareType.URL);
        }
    }
}
