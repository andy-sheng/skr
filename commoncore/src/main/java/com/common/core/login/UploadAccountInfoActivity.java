package com.common.core.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.base.FragmentDataListener;
import com.common.core.R;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.image.fresco.BaseImageView;
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

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

import static com.common.core.login.LoginActivity.KEY_ORIGIN_PATH;


@Route(path = RouterConstants.ACTIVITY_UPLOAD)
public class UploadAccountInfoActivity extends BaseActivity {

    RelativeLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    BaseImageView mAvatarIv;
    NoLeakEditText mNicknameEt;
    ExTextView mSubmitTv;

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.core_upload_account_info_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) findViewById(R.id.titlebar);
        mAvatarIv = (BaseImageView) findViewById(R.id.avatar_iv);
        mNicknameEt = (NoLeakEditText) findViewById(R.id.nickname_et);
        mSubmitTv = (ExTextView) findViewById(R.id.submit_tv);

        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance()
                .getAvatar())
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
                                                    MyUserInfoManager.getInstance().updateInfo(null, -1, null, url);
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

        RxView.clicks(mSubmitTv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        String nickName = mNicknameEt.getText().toString().trim();
                        if (!TextUtils.isEmpty(nickName)) {
                            MyUserInfoManager.getInstance().updateInfo(nickName, -1, null, null);
                        } else {
                            U.getToastUtil().showShort("上传个人资料成功");
                        }
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvnet(MyUserInfoEvent.UserInfoChangeEvent userInfoChangeEvent) {
        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance()
                .getAvatar())
                .build());

        if (!TextUtils.isEmpty(MyUserInfoManager.getInstance().getNickName())) {
            U.getToastUtil().showShort("昵称不为空");
//            Bundle bundle = getIntent().getExtras();
//            if (bundle != null) {
//                String path = bundle.getString(KEY_ORIGIN_PATH);
//                if (!TextUtils.isEmpty(path)) {
//                    // 跳转到原页面，并带上参数
//                    ARouter.getInstance().build(path).with(bundle).navigation();
//                }
//            }

            finish();
        }

    }

    @Override
    public boolean useEventBus() {
        return true;
    }
}
