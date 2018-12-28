package com.module.home.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;

import com.common.base.FragmentDataListener;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.image.fresco.BaseImageView;

import com.common.upload.UploadCallback;
import com.common.upload.UploadParams;
import com.common.upload.UploadTask;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;

import com.component.busilib.fragment.OtherPersonFragment;
import com.imagepicker.ImagePicker;
import com.imagepicker.fragment.ImagePickerFragment;
import com.imagepicker.model.ImageItem;
import com.imagepicker.view.CropImageView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.module.home.R;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;


public class PersonFragment extends BaseFragment {

    RelativeLayout mPersonMainContainner;
    BaseImageView mAvatarIv;
    ExTextView mShareTv;
    ExTextView mSettingTv;
    ExTextView mNameTv;
    ExTextView mSignTv;
    RelativeLayout mFriends;
    ExTextView mFriendsNumTv;
    RelativeLayout mFans;
    ExTextView mFansNumTv;
    RelativeLayout mFollows;
    ExTextView mFollowsNumTv;
    RelativeLayout mMedalLayout;
    ExImageView mAuditionRoomTv;
    ExImageView mMusicTestTv;

    @Override
    public int initView() {
        return R.layout.person_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mPersonMainContainner = (RelativeLayout) mRootView.findViewById(R.id.person_main_containner);
        mAvatarIv = (BaseImageView) mRootView.findViewById(R.id.avatar_iv);
        mShareTv = (ExTextView) mRootView.findViewById(R.id.share_tv);
        mSettingTv = (ExTextView) mRootView.findViewById(R.id.setting_tv);
        mNameTv = (ExTextView) mRootView.findViewById(R.id.name_tv);
        mSignTv = (ExTextView) mRootView.findViewById(R.id.sign_tv);
        mFriends = (RelativeLayout) mRootView.findViewById(R.id.friends);
        mFriendsNumTv = (ExTextView) mRootView.findViewById(R.id.friends_num_tv);
        mFans = (RelativeLayout) mRootView.findViewById(R.id.fans);
        mFansNumTv = (ExTextView) mRootView.findViewById(R.id.fans_num_tv);
        mFollows = (RelativeLayout) mRootView.findViewById(R.id.follows);
        mFollowsNumTv = (ExTextView) mRootView.findViewById(R.id.follows_num_tv);
        mMedalLayout = (RelativeLayout) mRootView.findViewById(R.id.medal_layout);
        mAuditionRoomTv = (ExImageView) mRootView.findViewById(R.id.audition_room_tv);
        mMusicTestTv = (ExImageView) mRootView.findViewById(R.id.music_test_tv);

        initViewData();


        RxView.clicks(mSettingTv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getFragmentUtils().addFragment(
                                FragmentUtils.newAddParamsBuilder(getActivity(), SettingFragment.class)
                                        .setAddToBackStack(true)
                                        .setHasAnimation(true)
                                        .build());
                    }
                });


        RxView.clicks(mMedalLayout)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getFragmentUtils().addFragment(
                                FragmentUtils.newAddParamsBuilder(getActivity(), OtherPersonFragment.class)
                                        .setAddToBackStack(true)
                                        .setHasAnimation(true)
                                        .build());
                    }
                });


        RxView.clicks(mMusicTestTv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        // TODO: 2018/12/27  只做test
                        ARouter.getInstance().build(RouterConstants.ACTIVITY_UPLOAD)
                                .greenChannel().navigation();
                    }
                });

        RxView.clicks(mAvatarIv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        // TODO: 2018/12/28 可能会加上一个大图预览的功能
                        ImagePicker.getInstance().setParams(ImagePicker.newParamsBuilder()
                                .setSelectLimit(1)
                                .setCropStyle(CropImageView.Style.CIRCLE)
                                .build()
                        );

                        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), ImagePickerFragment.class)
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
                                                            MyUserInfoManager.getInstance().updateInfo(  MyUserInfoManager.newMyInfoUpdateParamsBuilder()
                                                                    .setAvatar(url)
                                                                    .build());
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
                });

    }

    private void initViewData() {
        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance()
                .getAvatar())
                .setCircle(true)
                .setBorderColor(Color.parseColor("#33A4E1"))
                .setBorderWidth(U.getDisplayUtils().dip2px(3))
                .build());
        mNameTv.setText(MyUserInfoManager.getInstance().getNickName());
        mSignTv.setText(MyUserInfoManager.getInstance().getSignature());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvnet(MyUserInfoEvent.UserInfoChangeEvent userInfoChangeEvent) {
        initViewData();
    }

    @Override
    public boolean useEventBus() {
        return true;
    }


}
