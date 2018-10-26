package com.common.core.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.callback.NavigationCallback;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.core.R;
import com.common.core.RouterConstants;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.oauth.XiaoMiOAuth;
import com.common.core.userinfo.UserInfo;
import com.common.core.userinfo.UserInfoManager;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.titlebar.CommonTitleBar;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.wali.live.proto.User.GetHomepageResp;
import com.wali.live.proto.User.GetUserInfoByIdRsp;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@Route(path = RouterConstants.ACTIVITY_LOGIN)
public class LoginActivity extends BaseActivity {

    private TextView mMiBtn;

    private TextView mTestBtn;

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

        mTestBtn = (TextView) this.findViewById(R.id.mi_test);


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

        mTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                        long uid = MyUserInfoManager.getInstance().getMyUserInfo().getUserInfo().getUserId();
                        UserInfoManager.getInstance().syncFollowerListFromServer(uid, 5, 0);
                        UserInfoManager.getInstance().syncFollowingFromServer(uid, 5, 0, true, true);
                        UserInfoManager.getInstance().syncBockerListFromServer(uid, 5, 0);
                        MyUserInfoManager.getInstance().init();
                        UserInfoManager.getInstance().getHomepageByUuid(490020, true, new UserInfoManager.UserInfoPageCallBack() {
                            @Override
                            public boolean onGetLocalDB(UserInfo userInfo) {
                                MyLog.d("onGetLocal" + userInfo.toString());
                                return false;
                            }

                            @Override
                            public boolean onGetServer(GetHomepageResp rsp) {
                                MyLog.d("onGetServer" + rsp.toString());
                                return false;
                            }
                        });
                        UserInfoManager.getInstance().getUserInfoByUuid(490021, new UserInfoManager.UserInfoCallBack() {
                            @Override
                            public boolean onGetLocalDB(UserInfo userInfo) {
                                MyLog.d("onGetLocal" + userInfo.toString());
                                return false;
                            }

                            @Override
                            public boolean onGetServer(GetUserInfoByIdRsp rsp) {
                                MyLog.d("onGetServer" + rsp.toString());
                                return false;
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
