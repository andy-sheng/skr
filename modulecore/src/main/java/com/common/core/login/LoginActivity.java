package com.common.core.login;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.core.R;
import com.common.core.account.UserAccountManager;
import com.common.core.oauth.XiaoMiOAuth;
import com.common.core.upload.UploadCallBack;
import com.common.core.upload.UploadUtils;
import com.common.view.titlebar.CommonTitleBar;
import com.common.utils.U;
import com.wali.live.proto.AuthUpload.AuthType;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

@Route(path = "/core/login")
public class LoginActivity extends BaseActivity {

    private TextView mMiBtn;

    private TextView mUploadBtn;

    CommonTitleBar mTitlebar;


    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.core_login_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        mTitlebar = (CommonTitleBar) this.findViewById(R.id.titlebar);

        mTitlebar.getLeftTextView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mMiBtn = (TextView) this.findViewById(R.id.mi_btn);

        mUploadBtn = (TextView) this.findViewById(R.id.mi_upload);

        mMiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                        String code = XiaoMiOAuth.getOAuthCode(LoginActivity.this);
                        UserAccountManager.getInstance().loginByMiOauth(code);
                        emitter.onComplete();
                    }
                })
                        .subscribeOn(Schedulers.io())
                        .subscribe();
            }
        });
        boolean showToast = getIntent().getBooleanExtra("key_show_toast", false);
        if (showToast) {
            U.getActivityUtils().showSnackbar("请先登录", true);
        }

        mUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = Environment.getExternalStorageDirectory() + File.separator + "test.jpg";
                final UploadUtils.UploadParams uploadParams = new UploadUtils.UploadParams.Builder()
                        .setLocalPath(path)
                        .setType(AuthType.HEAD)
                        .setMimeType("image/jpg")
                        .build();
                Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                        UploadUtils.upload(uploadParams, new UploadCallBack() {
                            @Override
                            public void onTaskStart() {
//                                U.getToastUtil().showToast("onTaskStart");
                            }

                            @Override
                            public void onTaskCancel() {
//                                U.getToastUtil().showToast("onTaskCancel");
                            }

                            @Override
                            public void onTaskProgress(double progress) {
//                                U.getToastUtil().showToast("onTaskProgress");
                            }

                            @Override
                            public void onTaskFailure() {
//                                U.getToastUtil().showToast("onTaskFailure");
                            }

                            @Override
                            public void onTaskSuccess() {
//                                U.getToastUtil().showToast("onTaskSuccess");
                            }
                        });
                        emitter.onComplete();
                    }
                }).subscribeOn(Schedulers.io())
                        .subscribe();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
