package com.wali.live.sdk.litedemo;

import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;

import com.mi.liveassistant.room.callback.ICallback;
import com.mi.liveassistant.room.manager.GameLiveManager;
import com.wali.live.sdk.litedemo.account.LoginManager;
import com.wali.live.sdk.litedemo.base.activity.RxActivity;
import com.wali.live.sdk.litedemo.utils.ToastUtils;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends RxActivity implements View.OnClickListener {
    private Button mLoginBtn;
    private Button mGameLiveBtn;

    private GameLiveManager mLiveManager;
    private boolean mIsBegin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initManager();
    }

    private void initView() {
        mLoginBtn = $(R.id.login_btn);
        mLoginBtn.setOnClickListener(this);

        mGameLiveBtn = $(R.id.game_live_btn);
        mGameLiveBtn.setOnClickListener(this);
    }

    private void initManager() {
        mLiveManager = new GameLiveManager();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn:
                clickLoginBtn();
                break;
            case R.id.game_live_btn:
                clickGameBtn();
                break;
        }
    }

    private void clickLoginBtn() {
        Observable.just(0)
                .map(new Func1<Integer, String>() {
                    @Override
                    public String call(Integer integer) {
                        return LoginManager.getOAuthCode(MainActivity.this);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String code) {
                        try {
                            LoginManager.loginByMiAccountOAuth(50001, code);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    private void clickGameBtn() {
        if (mIsBegin) {
            ToastUtils.showToast("end game live ...");
            mLiveManager.endLive(new ICallback() {
                @Override
                public void notifyFail(int errCode) {
                    ToastUtils.showToast("end game live fail=" + errCode);
                }

                @Override
                public void notifySuccess() {
                    ToastUtils.showToast("end game live success");
                    mIsBegin = false;
                    mGameLiveBtn.setText("begin game live");
                }
            });
        } else {
            ToastUtils.showToast("begin game live ...");
            mLiveManager.beginLive(null, "TEST", null, new ICallback() {
                @Override
                public void notifyFail(int errCode) {
                    ToastUtils.showToast("begin game live fail=" + errCode);
                }

                @Override
                public void notifySuccess() {
                    ToastUtils.showToast("begin game live success");
                    mIsBegin = true;
                    mGameLiveBtn.setText("end game live");
                }
            });
        }
    }
}
