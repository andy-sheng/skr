package com.module.home.updateinfo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.base.FragmentDataListener;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.log.MyLog;
import com.common.upload.UploadCallback;
import com.common.upload.UploadParams;
import com.common.upload.UploadTask;
import com.common.utils.FragmentUtils;
import com.common.utils.LbsUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.titlebar.CommonTitleBar;
import com.component.busilib.view.MarqueeTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.RouterConstants;
import com.module.home.R;
import com.module.home.updateinfo.fragment.EditInfoLocationFragment;
import com.module.home.updateinfo.fragment.EditInfoNameFragment;
import com.module.home.updateinfo.fragment.EditInfoSexFragment;
import com.module.home.updateinfo.fragment.EditInfoSignFragment;
import com.respicker.ResPicker;
import com.respicker.activity.ResPickerActivity;
import com.respicker.model.ImageItem;
import com.respicker.view.CropImageView;
import com.component.toast.CommonToastView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


// 个人资料编辑
@Route(path = RouterConstants.ACTIVITY_EDIT_INFO)
public class EditInfoActivity extends BaseActivity implements View.OnClickListener {

    public final String TAG = "EditInfoActivity";

    RelativeLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    RelativeLayout mEditAvatar;
    SimpleDraweeView mAvatarIv;
    RelativeLayout mEditName;
    ExTextView mNicknameTv;
    RelativeLayout mEditSign;
    MarqueeTextView mSignTv;
    RelativeLayout mEditAge;
    ExTextView mAgeTv;
    RelativeLayout mEditSex;
    ExTextView mSexTv;
    RelativeLayout mEditLocation;
    MarqueeTextView mLocationTv;
    ExImageView mLocationRefreshBtn;
    ProgressBar mProgressBar;

    Handler mUiHandler = new Handler(Looper.getMainLooper());

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.edit_info_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) findViewById(R.id.titlebar);
        mEditAvatar = (RelativeLayout) findViewById(R.id.edit_avatar);
        mEditName = (RelativeLayout) findViewById(R.id.edit_name);
        mEditSign = (RelativeLayout) findViewById(R.id.edit_sign);
        mEditAge = (RelativeLayout) findViewById(R.id.edit_age);
        mEditSex = (RelativeLayout) findViewById(R.id.edit_sex);
        mEditLocation = (RelativeLayout) findViewById(R.id.edit_location);

        mAvatarIv = (SimpleDraweeView) findViewById(R.id.avatar_iv);
        mNicknameTv = (ExTextView) findViewById(R.id.nickname_tv);
        mSignTv = (MarqueeTextView) findViewById(R.id.sign_tv);
        mAgeTv = (ExTextView) findViewById(R.id.age_tv);
        mSexTv = (ExTextView) findViewById(R.id.sex_tv);
        mLocationTv = (MarqueeTextView) findViewById(R.id.location_tv);
        mLocationRefreshBtn = (ExImageView) findViewById(R.id.location_refresh_btn);

        mProgressBar = findViewById(R.id.progress_bar);

        initViewData();

        mEditAvatar.setOnClickListener(this);
        mEditName.setOnClickListener(this);
        mEditSign.setOnClickListener(this);
        mEditAge.setOnClickListener(this);
        mEditSex.setOnClickListener(this);
        mEditLocation.setOnClickListener(this);
        mLocationRefreshBtn.setOnClickListener(this);

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
//                U.getSoundUtils().play(EditInfoActivity.TAG, R.raw.normal_back, 500);
                finish();
            }
        });

        U.getSoundUtils().preLoad(TAG, R.raw.normal_back);
    }

    @Override
    protected void destroy() {
        super.destroy();
        U.getSoundUtils().release(TAG);
        mUiHandler.removeCallbacksAndMessages(null);
    }

    private void initViewData() {
        AvatarUtils.loadAvatarByUrl(mAvatarIv,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .setBorderWidth(U.getDisplayUtils().dip2px(3))
                        .setBorderColor(Color.WHITE)
                        .build());
        mNicknameTv.setText(MyUserInfoManager.getInstance().getNickName());
        mSignTv.setText(MyUserInfoManager.getInstance().getSignature());
        if (TextUtils.isEmpty(MyUserInfoManager.getInstance().getAgeStageString())) {
            mAgeTv.setText("无");
        } else {
            mAgeTv.setText(MyUserInfoManager.getInstance().getAgeStageString());
        }

        String sex = "保密";
        if (MyUserInfoManager.getInstance().getSex() == 1) {
            sex = "男";
        } else if (MyUserInfoManager.getInstance().getSex() == 2) {
            sex = "女";
        }
        mSexTv.setText(sex);
        mLocationTv.setText(MyUserInfoManager.getInstance().getLocationProvince());
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.edit_avatar) {
            onClickAvatarContainer(); // 头像
        } else if (viewId == R.id.edit_name) {
            onClickNameContainer(); // 昵称
        } else if (viewId == R.id.edit_sign) {
            onClickSignContainer(); // 签名
        } else if (viewId == R.id.edit_age) {
            onClickAgeContainer(); // 年龄
        } else if (viewId == R.id.edit_sex) {
            onClickSexContainer(); // 性别
        } else if (viewId == R.id.edit_location) {
            onClickEditLocation();
        }
    }

    private void onClickEditLocation() {
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(this, EditInfoLocationFragment.class)
                        .setAddToBackStack(true)
                        .setFragmentDataListener(new FragmentDataListener() {
                            @Override
                            public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {
                                if (requestCode == 0) {
                                    mLocationTv.setText(MyUserInfoManager.getInstance().getLocationProvince());
                                }
                            }
                        })
                        .setHasAnimation(true)
                        .build());
    }

    //修改头像
    private void onClickAvatarContainer() {
        ResPicker.getInstance().setParams(ResPicker.newParamsBuilder()
                .setMultiMode(false)
                .setSelectLimit(1)
                .setCropStyle(CropImageView.Style.CIRCLE)
                .setIncludeGif(false)
                .build()
        );
        ResPickerActivity.open(this);
    }

    //修改昵称
    private void onClickNameContainer() {
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(this, EditInfoNameFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());
    }

    //修改签名
    private void onClickSignContainer() {
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(this, EditInfoSignFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());
    }

    //修改年龄
    private void onClickAgeContainer() {
        ARouter.getInstance().build(RouterConstants.ACTIVITY_EDIT_AGE)
                .withInt("from", 1)
                .navigation();
    }

    //修改性别
    private void onClickSexContainer() {
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(this, EditInfoSexFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());
    }

    @Override
    public boolean canSlide() {
        return false;
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvnet(MyUserInfoEvent.UserInfoChangeEvent userInfoChangeEvent) {
        initViewData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ResPickerActivity.REQ_CODE_RES_PICK && resultCode == Activity.RESULT_OK) {
            ImageItem imageItem = ResPicker.getInstance().getSingleSelectedImage();
            if (imageItem == null) {
                MyLog.e(TAG, new Throwable("选中的ImageItem为null"));
                return;
            }

            mProgressBar.setVisibility(View.VISIBLE);
            UploadTask uploadTask = UploadParams.newBuilder(imageItem.getPath())
                    .setNeedCompress(true)
                    .startUploadAsync(new UploadCallback() {
                        @Override
                        public void onProgressNotInUiThread(long currentSize, long totalSize) {

                        }

                        @Override
                        public void onSuccessNotInUiThread(String url) {
                            mUiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    uploadAvatarSuccess(url);
                                }
                            });

                        }

                        @Override
                        public void onFailureNotInUiThread(String msg) {
                            mUiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mProgressBar != null) {
                                        mProgressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                    });
        }
    }

    protected void uploadAvatarSuccess(String url) {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
        MyUserInfoManager.getInstance().updateInfo(MyUserInfoManager
                .newMyInfoUpdateParamsBuilder()
                .setAvatar(url)
                .build(), false, false, new MyUserInfoManager.ServerCallback() {
            @Override
            public void onSucess() {
                U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                        .setImage(R.drawable.touxiangshezhichenggong_icon)
                        .setText("设置成功")
                        .build());
            }

            @Override
            public void onFail() {

            }
        });
    }

    @Override
    public boolean resizeLayoutSelfWhenKeybordShow() {
        return true;
    }
}
