package com.module.home.updateinfo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseActivity;
import com.common.base.FragmentDataListener;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.Location;
import com.common.core.myinfo.MyUserInfo;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.log.MyLog;
import com.common.upload.UploadCallback;
import com.common.upload.UploadParams;
import com.common.upload.UploadTask;
import com.common.utils.FragmentUtils;
import com.common.utils.LbsUtils;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.titlebar.CommonTitleBar;
import com.facebook.drawee.view.SimpleDraweeView;
import com.imagepicker.ImagePicker;
import com.imagepicker.fragment.ImagePickerFragment;
import com.imagepicker.model.ImageItem;
import com.imagepicker.view.CropImageView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.home.R;
import com.module.home.updateinfo.fragment.EditInfoAgeFragment;
import com.module.home.updateinfo.fragment.EditInfoNameFragment;
import com.module.home.updateinfo.fragment.EditInfoSexFragment;
import com.module.home.updateinfo.fragment.EditInfoSignFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;


// 个人资料编辑
public class EditInfoActivity extends BaseActivity implements View.OnClickListener {

    RelativeLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    RelativeLayout mEditAvatar;
    SimpleDraweeView mAvatarIv;
    RelativeLayout mEditName;
    ExTextView mNicknameTv;
    RelativeLayout mEditSign;
    ExTextView mSignTv;
    RelativeLayout mEditAge;
    ExTextView mAgeTv;
    RelativeLayout mEditSex;
    ExTextView mSexTv;
    RelativeLayout mEditLocation;
    ExTextView mLocationTv;
    ExImageView mLocationRefreshBtn;


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
        mSignTv = (ExTextView) findViewById(R.id.sign_tv);
        mAgeTv = (ExTextView) findViewById(R.id.age_tv);
        mSexTv = (ExTextView) findViewById(R.id.sex_tv);
        mLocationTv = (ExTextView) findViewById(R.id.location_tv);
        mLocationRefreshBtn = (ExImageView) findViewById(R.id.location_refresh_btn);

        initViewData();

        mEditAvatar.setOnClickListener(this);
        mEditName.setOnClickListener(this);
        mEditSign.setOnClickListener(this);
        mEditAge.setOnClickListener(this);
        mEditSex.setOnClickListener(this);
        mEditLocation.setOnClickListener(this);
        mLocationRefreshBtn.setOnClickListener(this);

        RxView.clicks(mTitlebar.getLeftTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        finish();
                    }
                });

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
        mAgeTv.setText("18-22岁");
        String sex = "未知";
        if (MyUserInfoManager.getInstance().getSex() == 1) {
            sex = "男";
        } else if (MyUserInfoManager.getInstance().getSex() == 2) {
            sex = "女";
        }
        mSexTv.setText(sex);
        mLocationTv.setText(MyUserInfoManager.getInstance().getLocationDesc());


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
        } else if (viewId == R.id.location_refresh_btn) {
            onClickLocationRefresh();
        }
    }

    //修改头像
    private void onClickAvatarContainer() {
        ImagePicker.getInstance().setParams(ImagePicker.newParamsBuilder()
                .setSelectLimit(1)
                .setCropStyle(CropImageView.Style.CIRCLE)
                .build()
        );

        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, ImagePickerFragment.class)
                .setAddToBackStack(true)
                .setHasAnimation(true)
                .setFragmentDataListener(new FragmentDataListener() {
                    @Override
                    public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object object) {
                        List<ImageItem> list = ImagePicker.getInstance().getSelectedImages();
                        if (list.size() > 0) {
                            ImageItem imageItem = list.get(0);
                            UploadTask uploadTask = UploadParams.newBuilder(imageItem.getPath())
                                    .setNeedCompress(true)
                                    .startUploadAsync(new UploadCallback() {
                                        @Override
                                        public void onProgress(long currentSize, long totalSize) {

                                        }

                                        @Override
                                        public void onSuccess(String url) {
                                            U.getToastUtil().showShort("上传成功 url:" + url);
                                            MyUserInfoManager.getInstance().updateInfo(MyUserInfoManager
                                                    .newMyInfoUpdateParamsBuilder()
                                                    .setAvatar(url)
                                                    .build(), false);
                                        }

                                        @Override
                                        public void onFailure(String msg) {

                                        }

                                    });
                        }
                    }
                })
                .build());
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
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(this, EditInfoAgeFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());
    }

    //修改性别
    private void onClickSexContainer() {
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(this, EditInfoSexFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());
    }

    private void onClickLocationRefresh() {
        mLocationTv.setText("获取位置中");
        MyUserInfoManager.getInstance().uploadLocation();
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
}
