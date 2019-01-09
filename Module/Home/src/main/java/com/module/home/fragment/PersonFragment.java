package com.module.home.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.UserRankModel;
import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;
import com.common.upload.UploadCallback;
import com.common.upload.UploadParams;
import com.common.upload.UploadTask;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.imagepicker.ImagePicker;
import com.imagepicker.fragment.ImagePickerFragment;
import com.imagepicker.model.ImageItem;
import com.imagepicker.view.CropImageView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.module.home.R;
import model.RelationNumMode;
import com.module.home.persenter.PersonCorePresenter;
import com.module.home.view.IPersonView;
import com.module.rank.IRankingModeService;
import com.zq.level.view.NormalLevelView;
import com.zq.relation.fragment.RelationFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;


public class PersonFragment extends BaseFragment implements IPersonView {

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
    ExTextView mLevelTv;
    ExTextView mRankTv;
    NormalLevelView mLevelView;

    ExImageView mAuditionRoomTv;
    ExImageView mMusicTestTv;

    PersonCorePresenter mPersonCorePresenter;

    @Override
    public int initView() {
        return R.layout.person_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        MyLog.d(TAG, "initData" + " savedInstanceState=" + savedInstanceState);
        initTopView();
        initMedalView();
        initAudioView();
        initMusicTest();

        initViewData();

        mPersonCorePresenter = new PersonCorePresenter(this);
        addPresent(mPersonCorePresenter);
        mPersonCorePresenter.getRelationNum((int) MyUserInfoManager.getInstance().getUid());
        mPersonCorePresenter.getReginRank((int) MyUserInfoManager.getInstance().getUid());
        mPersonCorePresenter.getHomePage((int) MyUserInfoManager.getInstance().getUid());
    }

    private void initTopView() {
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
                                                            MyUserInfoManager.getInstance().updateInfo(MyUserInfoManager.newMyInfoUpdateParamsBuilder()
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

        RxView.clicks(mFriends)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        // 好友，双向关注
                        Bundle bundle = new Bundle();
                        bundle.putInt(RelationFragment.FROM_PAGE_KEY, RelationFragment.FROM_FRIENDS);
                        U.getFragmentUtils().addFragment(
                                FragmentUtils.newAddParamsBuilder(getActivity(), RelationFragment.class)
                                        .setBundle(bundle)
                                        .setAddToBackStack(true)
                                        .setHasAnimation(true)
                                        .build());

                    }
                });

        RxView.clicks(mFans)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        // 粉丝，我关注的
                        Bundle bundle = new Bundle();
                        bundle.putInt(RelationFragment.FROM_PAGE_KEY, RelationFragment.FROM_FANS);
                        U.getFragmentUtils().addFragment(
                                FragmentUtils.newAddParamsBuilder(getActivity(), RelationFragment.class)
                                        .setBundle(bundle)
                                        .setAddToBackStack(true)
                                        .setHasAnimation(true)
                                        .build());
                    }
                });

        RxView.clicks(mFollows)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        // 关注, 关注我的
                        Bundle bundle = new Bundle();
                        bundle.putInt(RelationFragment.FROM_PAGE_KEY, RelationFragment.FROM_FOLLOW);
                        U.getFragmentUtils().addFragment(
                                FragmentUtils.newAddParamsBuilder(getActivity(), RelationFragment.class)
                                        .setBundle(bundle)
                                        .setAddToBackStack(true)
                                        .setHasAnimation(true)
                                        .build());
                    }
                });
    }

    private void initMedalView() {
        mMedalLayout = (RelativeLayout) mRootView.findViewById(R.id.medal_layout);
        mLevelTv = (ExTextView) mRootView.findViewById(R.id.level_tv);
        mRankTv = (ExTextView) mRootView.findViewById(R.id.rank_tv);
        mLevelView = (NormalLevelView) mRootView.findViewById(R.id.level_view);
        mLevelView.bindData(2, 3, 5, 3);

        RxView.clicks(mMedalLayout).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) {
                IRankingModeService iRankingModeService = (IRankingModeService) ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation();
                Class<BaseFragment> baseFragment = (Class<BaseFragment>) iRankingModeService.getData(0, null);
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder((BaseActivity) getContext(), baseFragment)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .setFragmentDataListener(new FragmentDataListener() {
                            @Override
                            public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {

                            }
                        })
                        .build());
            }
        });

    }

    private void initAudioView() {
        mAuditionRoomTv = (ExImageView) mRootView.findViewById(R.id.audition_room_tv);
        RxView.clicks(mAuditionRoomTv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        ARouter.getInstance().build(RouterConstants.ACTIVITY_AUDIOROOM)
                                .withBoolean("selectSong", true)
                                .navigation();
                    }
                });
    }

    private void initMusicTest() {
        mMusicTestTv = (ExImageView) mRootView.findViewById(R.id.music_test_tv);
        RxView.clicks(mMusicTestTv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        // 暂时先不做
//                        U.getFragmentUtils().addFragment(
//                                FragmentUtils.newAddParamsBuilder(getActivity(), MusicTestFragment.class)
//                                        .setAddToBackStack(true)
//                                        .setHasAnimation(true)
//                                        .build());
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


    @Override
    public void showRelationNum(List<RelationNumMode> list) {
        for (RelationNumMode mode : list) {
            if (mode.getRelation() == UserInfoManager.RELATION_FRIENDS) {
                mFriendsNumTv.setText(String.valueOf(mode.getCnt()));
            } else if (mode.getRelation() == UserInfoManager.RELATION_FANS) {
                mFansNumTv.setText(String.valueOf(mode.getCnt()));
            } else if (mode.getRelation() == UserInfoManager.RELATION_FOLLOW) {
                mFollowsNumTv.setText(String.valueOf(mode.getCnt()));
            }
        }
    }

    @Override
    public void showReginRank(List<UserRankModel> list) {
        UserRankModel reginRankModel = new UserRankModel();
        if (list != null && list.size() > 0) {
            for (UserRankModel model : list) {
                if (model.getCategoy() == UserRankModel.REGION) {
                    reginRankModel = model;
                }
            }
        }

        if (reginRankModel != null) {
            mRankTv.setText(reginRankModel.getRegionDesc() + "荣耀榜" + String.valueOf(reginRankModel.getSeq()) + "位");
        } else {
            mRankTv.setText("您当前还没有排名信息哦～");
        }
    }
}
