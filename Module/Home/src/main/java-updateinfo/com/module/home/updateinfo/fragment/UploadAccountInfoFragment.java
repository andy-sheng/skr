package com.module.home.updateinfo.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.core.avatar.AvatarUtils;
import com.common.core.login.LoginActivity;
import com.common.core.myinfo.MyUserInfo;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.MyUserInfoServerApi;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.image.fresco.BaseImageView;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.upload.UploadCallback;
import com.common.upload.UploadParams;
import com.common.upload.UploadTask;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;
import com.common.view.titlebar.CommonTitleBar;
import com.module.RouterConstants;
import com.respicker.ResPicker;
import com.respicker.fragment.ResPickerFragment;
import com.respicker.model.ImageItem;
import com.respicker.view.CropImageView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.home.R;
import com.module.home.updateinfo.UploadAccountInfoActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

public class UploadAccountInfoFragment extends BaseFragment {

    boolean isUpload = false; //当前是否是完善个人资料

    RelativeLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    BaseImageView mAvatarIv;
    NoLeakEditText mNicknameEt;
    ExTextView mNicknameHintTv;
    ExTextView mNextTv;

    @Override
    public int initView() {
        return R.layout.upload_account_info_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mAvatarIv = (BaseImageView) mRootView.findViewById(R.id.avatar_iv);
        mNicknameEt = (NoLeakEditText) mRootView.findViewById(R.id.nickname_et);
        mNicknameHintTv = (ExTextView) mRootView.findViewById(R.id.nickname_hint_tv);
        mNextTv = (ExTextView) mRootView.findViewById(R.id.next_tv);

        Bundle bundle = getArguments();
        if (bundle != null) {
            isUpload = bundle.getBoolean(UploadAccountInfoActivity.BUNDLE_IS_UPLOAD);
        }

        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance()
                .getAvatar())
                .setCircle(true)
                .setBorderColor(Color.parseColor("#0C2275"))
                .setBorderWidth(U.getDisplayUtils().dip2px(3))
                .build());

        mAvatarIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ResPicker.getInstance().setParams(ResPicker.newParamsBuilder()
                        .setSelectLimit(1)
                        .setCropStyle(CropImageView.Style.CIRCLE)
                        .build()
                );

                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), ResPickerFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .setFragmentDataListener(new FragmentDataListener() {
                            @Override
                            public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object object) {
                                ImageItem imageItem = ResPicker.getInstance().getSingleSelectedImage();
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
                                                        .build(), true);
                                            }

                                            @Override
                                            public void onFailure(String msg) {

                                            }

                                        });
                            }
                        })
                        .build());
            }
        });

        mNicknameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int length = U.getStringUtils().getStringLength(editable.toString());
                int selectionStart = mNicknameEt.getSelectionStart();
                int selectionEnd = mNicknameEt.getSelectionEnd();
                if (length > 14) {
                    editable.delete(selectionStart - 1, selectionEnd);
                    mNicknameEt.setText(editable.toString());
                    int selection = editable.length();
                    mNicknameEt.setSelection(selection);
                    mNicknameHintTv.setVisibility(View.VISIBLE);
                    mNicknameHintTv.setText("昵称不能超过7个汉字或14个英文");
                } else {
                    mNicknameHintTv.setVisibility(View.GONE);
                }
            }
        });

        RxView.clicks(mNextTv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        String nickName = mNicknameEt.getText().toString().trim();
                        checkNickName(nickName);
                    }
                });

        RxView.clicks(mTitlebar.getLeftTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        getActivity().finish();
                        ARouter.getInstance().build(RouterConstants.ACTIVITY_LOGIN)
                                .greenChannel().navigation();
                    }
                });

        if (!TextUtils.isEmpty(MyUserInfoManager.getInstance().getNickName())) {
            mNicknameEt.setText(MyUserInfoManager.getInstance().getNickName());
        }
    }

    private void checkNickName(String nickName) {
        if (TextUtils.isEmpty(nickName)) {
            U.getToastUtil().showShort("昵称为空");
            return;
        }

        MyUserInfoServerApi myUserInfoServerApi = ApiManager.getInstance().createService(MyUserInfoServerApi.class);
        ApiMethods.subscribe(myUserInfoServerApi.checkNickName(nickName), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    boolean isValid = result.getData().getBoolean("isValid");
                    String unValidReason = result.getData().getString("unValidReason");
                    if (isValid) {
                        // 昵称可用
                        Bundle bundle = new Bundle();
                        bundle.putBoolean(UploadAccountInfoActivity.BUNDLE_IS_UPLOAD, isUpload);
                        bundle.putString(UploadAccountInfoActivity.BUNDLE_UPLOAD_NICKNAME, nickName);
                        U.getFragmentUtils().addFragment(FragmentUtils
                                .newAddParamsBuilder(getActivity(), EditInfoSexFragment.class)
                                .setBundle(bundle)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .build());

//                        MyUserInfoManager.getInstance().updateInfo(MyUserInfoManager.newMyInfoUpdateParamsBuilder()
//                                .setNickName(nickName)
//                                .build(), true);
//                        U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
//                        if (MyUserInfoManager.getInstance().getSex() == 0) {
//                            // 无性别数据
//                            Bundle bundle = new Bundle();
//                            bundle.putBoolean(UploadAccountInfoActivity.BUNDLE_IS_UPLOAD, isUpload);
//                            U.getFragmentUtils().addFragment(FragmentUtils
//                                    .newAddParamsBuilder(getActivity(), EditInfoSexFragment.class)
//                                    .setBundle(bundle)
//                                    .setNotifyHideFragment(UploadAccountInfoFragment.class)
//                                    .setAddToBackStack(false)
//                                    .setHasAnimation(true)
//                                    .build());
//                        } else if (TextUtils.isEmpty(MyUserInfoManager.getInstance().getBirthday())) {
//                            // 无出生年月数据
//                            Bundle bundle = new Bundle();
//                            bundle.putBoolean(UploadAccountInfoActivity.BUNDLE_IS_UPLOAD, isUpload);
//                            U.getFragmentUtils().addFragment(FragmentUtils
//                                    .newAddParamsBuilder(getActivity(), EditInfoAgeFragment.class)
//                                    .setNotifyHideFragment(UploadAccountInfoFragment.class)
//                                    .setBundle(bundle)
//                                    .setAddToBackStack(false)
//                                    .setHasAnimation(true)
//                                    .build());
//                        } else {
//                            getActivity().finish();
//                        }
                    } else {
                        // 昵称不可用
                        mNicknameHintTv.setVisibility(View.VISIBLE);
                        mNicknameHintTv.setText(unValidReason);
                    }
                }

            }
        }, this);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvnet(MyUserInfoEvent.UserInfoChangeEvent userInfoChangeEvent) {
        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance()
                .getAvatar())
                .setCircle(true)
                .setBorderColor(Color.parseColor("#0C2275"))
                .setBorderWidth(U.getDisplayUtils().dip2px(3))
                .build());
    }


    @Override
    public boolean useEventBus() {
        return true;
    }
}
