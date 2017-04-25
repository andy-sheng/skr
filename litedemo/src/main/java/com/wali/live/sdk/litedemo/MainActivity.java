package com.wali.live.sdk.litedemo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.mi.liveassistant.room.callback.ICallback;
import com.mi.liveassistant.room.manager.GameLiveManager;
import com.wali.live.sdk.litedemo.base.activity.RxActivity;
import com.wali.live.sdk.litedemo.utils.ToastUtils;

public class MainActivity extends RxActivity implements View.OnClickListener {
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
        mGameLiveBtn = $(R.id.game_live_btn);
        mGameLiveBtn.setOnClickListener(this);
    }

    private void initManager() {
        mLiveManager = new GameLiveManager();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.game_live_btn:
                clickGameBtn();
                break;
        }
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
