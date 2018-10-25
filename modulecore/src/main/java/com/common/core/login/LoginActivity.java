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

    private TextView mTestServerBtn;

    private TextView mTestLocalBtn;

    private TextView mMiTestFollow;

    private TextView mMiTestScheme;

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

        mTestServerBtn = (TextView) this.findViewById(R.id.mi_test_server);

        mTestLocalBtn = (TextView) this.findViewById(R.id.mi_test_local);

        mMiTestFollow = (TextView) this.findViewById(R.id.mi_test_follow);

        mMiTestScheme = (TextView) this.findViewById(R.id.mi_test_scheme);

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

        mTestServerBtn.setOnClickListener(new View.OnClickListener() {
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

        mTestLocalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                        long uid = MyUserInfoManager.getInstance().getMyUserInfo().getUserInfo().getUserId();
                        UserInfoManager.getInstance().getFriendsUserInfoFromDB(UserInfoManager.BOTH_FOLLOWED, true, false);
                        UserInfoManager.getInstance().getFriendsUserInfoFromDB(UserInfoManager.MY_FOLLOWING, true, false);
                        UserInfoManager.getInstance().getFriendsUserInfoFromDB(UserInfoManager.MY_FOLLOWING, false, false);
                        UserInfoManager.getInstance().getFriendsUserInfoFromDB(UserInfoManager.MY_FOLLOWER, true, false);
                        MyUserInfoManager.getInstance().getMyUserInfo();
                        emitter.onComplete();
                    }
                }).subscribeOn(Schedulers.io())
                        .subscribe();
            }
        });

        mMiTestFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final long uid = MyUserInfoManager.getInstance().getMyUserInfo().getUserInfo().getUserId();
//                UserInfoManager.getInstance().follow(uid, 490021, null)
//                UserInfoManager.getInstance().unBlock(uid, 490014)
//                UserInfoManager.getInstance().unFollow(uid, 490020)
                UserInfoManager.getInstance().block(123, 490014)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .compose(LoginActivity.this.<Integer>bindUntilEvent(ActivityEvent.DESTROY))
                        .subscribe(new Observer<Integer>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(Integer integer) {
                                MyLog.d(TAG, " integer " + integer);
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            }
        });

        mMiTestScheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_SCHEME)
                        .withString("uri", "walilive://room/join?liveid=130214_1458310748&playerid=130214&videourl=encode")
                        .greenChannel().navigation();
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
