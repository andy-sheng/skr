package com.module.home.updateinfo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.base.FragmentDataListener;
import com.common.core.avatar.AvatarUtils;
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
import com.imagepicker.ImagePicker;
import com.imagepicker.fragment.ImagePickerFragment;
import com.imagepicker.model.ImageItem;
import com.imagepicker.view.CropImageView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.module.home.R;
import com.module.home.updateinfo.fragment.EditInfoSexFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;


@Route(path = RouterConstants.ACTIVITY_UPLOAD)
public class UploadAccountInfoActivity extends BaseActivity {

    public static final String UPLOAD_ACCOUNT_NICKNAME = "upload_account_nickname";
    public static final String UPLOAD_ACCOUNT_SEX = "upload_account_sex";

    RelativeLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    BaseImageView mAvatarIv;
    NoLeakEditText mNicknameEt;
    ExTextView mNicknameHintTv;
    ExTextView mNextTv;

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.upload_account_info_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) findViewById(R.id.titlebar);
        mAvatarIv = (BaseImageView) findViewById(R.id.avatar_iv);
        mNicknameEt = (NoLeakEditText) findViewById(R.id.nickname_et);
        mNicknameHintTv = (ExTextView) findViewById(R.id.nickname_hint_tv);
        mNextTv = (ExTextView) findViewById(R.id.next_tv);

        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance()
                .getAvatar())
                .setCircle(true)
                .setBorderColor(Color.parseColor("#0C2275"))
                .setBorderWidth(U.getDisplayUtils().dip2px(3))
                .build());

        mAvatarIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImagePicker.getInstance().setParams(ImagePicker.newParamsBuilder()
                        .setSelectLimit(1)
                        .setCropStyle(CropImageView.Style.CIRCLE)
                        .build()
                );

                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(UploadAccountInfoActivity.this, ImagePickerFragment.class)
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

        RxView.clicks(mNextTv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        String nickName = mNicknameEt.getText().toString().trim();
                        checkNickName(nickName);
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
                    mNicknameHintTv.setText("昵称不能超过7哥汉字或14个英文");
                } else {
                    mNicknameHintTv.setVisibility(View.GONE);
                }
            }
        });

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
                        U.getKeyBoardUtils().hideSoftInputKeyBoard(UploadAccountInfoActivity.this);
                        Bundle bundle = new Bundle();
                        bundle.putString(UPLOAD_ACCOUNT_NICKNAME, nickName);
                        U.getFragmentUtils().addFragment(FragmentUtils
                                .newAddParamsBuilder(UploadAccountInfoActivity.this, EditInfoSexFragment.class)
                                .setBundle(bundle)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .build());
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

    @Override
    public boolean canSlide() {
        return false;
    }
}
