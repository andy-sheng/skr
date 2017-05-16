package com.wali.live.sdk.litedemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import com.mi.liveassistant.barrage.callback.IChatMsgCallBack;
import com.mi.liveassistant.barrage.callback.ISysMsgCallBack;
import com.mi.liveassistant.barrage.data.Message;
import com.mi.liveassistant.barrage.facade.MessageFacade;
import com.mi.liveassistant.camera.CameraView;
import com.mi.liveassistant.data.model.LiteUser;
import com.mi.liveassistant.data.model.Viewer;
import com.mi.liveassistant.room.manager.live.NormalLiveManager;
import com.mi.liveassistant.room.manager.live.callback.ILiveCallback;
import com.mi.liveassistant.room.manager.live.callback.ILiveListener;
import com.mi.liveassistant.room.user.UserInfoManager;
import com.mi.liveassistant.room.user.callback.IUserCallback;
import com.mi.liveassistant.room.viewer.ViewerInfoManager;
import com.mi.liveassistant.room.viewer.callback.IViewerListener;
import com.wali.live.sdk.litedemo.R;
import com.wali.live.sdk.litedemo.barrage.BarrageAdapter;
import com.wali.live.sdk.litedemo.barrage.view.SendBarrageView;
import com.wali.live.sdk.litedemo.base.activity.RxActivity;
import com.wali.live.sdk.litedemo.topinfo.anchor.TopAnchorView;
import com.wali.live.sdk.litedemo.topinfo.viewer.TopViewerView;
import com.wali.live.sdk.litedemo.utils.KeyboardUtils;
import com.wali.live.sdk.litedemo.utils.ToastUtils;

import java.util.List;

/**
 * Created by chenyong on 2017/4/28.
 */

public class NormalLiveActivity extends RxActivity implements View.OnClickListener {
    /*开播流程*/
    private NormalLiveManager mLiveManager;
    private CameraView mCameraView;
    private Button mNormalLiveBtn;
    private boolean mIsBegin;

    /*主播信息*/
    private UserInfoManager mUserManager;
    private TopAnchorView mAnchorView;
    private long mPlayerId;
    private String mLiveId;
    private LiteUser mAnchor;

    /*观众信息*/
    private ViewerInfoManager mViewerManager;
    private TopViewerView mViewerView;

    /*控制按钮*/
    private ViewGroup mOperatorView;
    private Button mVolumeBtn;
    private Button mSwitchBtn;
    private Button mSplashBtn;
    private Button mBeautyBtn;

    private SeekBar mVolumeSb;

    private boolean mIsBeauty = true;

    /*弹幕消息*/
    private Button mSendMessageBtn;
    private RecyclerView mBarrageRv;
    private LinearLayoutManager mBarrageManager;
    private BarrageAdapter mBarrageAdapter;

    private IChatMsgCallBack mMsgCallBack = new IChatMsgCallBack() {
        @Override
        public void handleMessage(final List<Message> list) {
            mBarrageRv.post(new Runnable() {
                @Override
                public void run() {
                    mBarrageAdapter.addMessageList(list);
                    mBarrageRv.smoothScrollToPosition(mBarrageAdapter.getItemCount() - 1);
                }
            });
        }
    };

    private ISysMsgCallBack mSysMsgCallBack = new ISysMsgCallBack() {
        @Override
        public void handleMessage(final List<Message> list) {
            mBarrageRv.post(new Runnable() {
                @Override
                public void run() {
                    mBarrageAdapter.addMessageList(list);
                    mBarrageRv.smoothScrollToPosition(mBarrageAdapter.getItemCount() - 1);
                }
            });
        }
    };

    private SendBarrageView mSendBarrageView;
    private SendBarrageView.ISendCallback mSendCallback = new SendBarrageView.ISendCallback() {
        @Override
        public void send(String message) {
            ToastUtils.showToast("send=" + message);
            MessageFacade.getInstance().sendTextMessageAsync(message, mLiveId, mPlayerId);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_live);

        initView();
        initManager();

        adjustView();
    }

    private void adjustView() {
        mBeautyBtn.setText(mIsBeauty ? "美颜:开" : "美颜:关");
        mSplashBtn.setText(mLiveManager.isFlashLight() ? "闪光:开" : "闪光:关");
        mSplashBtn.setEnabled(mLiveManager.isBackCamera());
        mSwitchBtn.setText(mLiveManager.isBackCamera() ? "后置" : "前置");

        mVolumeBtn.setText("音量:" + mLiveManager.getVoiceVolume());
        mVolumeSb.setProgress(mLiveManager.getVoiceVolume());

        KeyboardUtils.assistActivity(this, new KeyboardUtils.OnKeyboardChangedListener() {
            @Override
            public void onKeyboardShow() {
            }

            @Override
            public void onKeyboardHide() {
                updateOnKeyboardHide();
            }
        });
    }

    private void updateOnKeyboardShow() {
        mSendMessageBtn.setVisibility(View.GONE);
        mSendBarrageView.setVisibility(View.VISIBLE);
        mOperatorView.setVisibility(View.GONE);
        mVolumeSb.setVisibility(View.GONE);
        KeyboardUtils.showKeyboard(this, (EditText) mSendBarrageView.findViewById(R.id.barrage_et));
    }

    private void updateOnKeyboardHide() {
        mSendMessageBtn.setVisibility(View.VISIBLE);
        mSendBarrageView.setVisibility(View.GONE);
        mOperatorView.setVisibility(View.VISIBLE);
        KeyboardUtils.hideKeyboard(this, (EditText) mSendBarrageView.findViewById(R.id.barrage_et));
    }

    private void initView() {
        mCameraView = $(R.id.camera_view);
        mNormalLiveBtn = $(R.id.normal_live_btn);
        mNormalLiveBtn.setOnClickListener(this);

        mAnchorView = $(R.id.anchor_view);
        mViewerView = $(R.id.viewer_view);

        mOperatorView = $(R.id.operate_container);
        mVolumeBtn = $(R.id.volume_btn);
        mVolumeBtn.setOnClickListener(this);
        mSwitchBtn = $(R.id.switch_btn);
        mSwitchBtn.setOnClickListener(this);
        mSplashBtn = $(R.id.splash_btn);
        mSplashBtn.setOnClickListener(this);
        mBeautyBtn = $(R.id.beauty_btn);
        mBeautyBtn.setOnClickListener(this);

        mVolumeSb = $(R.id.volume_sb);
        mVolumeSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mVolumeBtn.setText("音量:" + mVolumeSb.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mLiveManager.setVoiceVolume(mVolumeSb.getProgress());
            }
        });
        mVolumeSb.setVisibility(View.GONE);

        mBarrageRv = $(R.id.barrage_rv);
        mSendBarrageView = $(R.id.send_barrage_view);
        mSendBarrageView.setCallback(mSendCallback);

        mSendMessageBtn = $(R.id.send_message_view);
        mSendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateOnKeyboardShow();
            }
        });
    }

    private void initManager() {
        mLiveManager = new NormalLiveManager(mCameraView, new ILiveListener() {
            @Override
            public void onEndUnexpected(int errCode, String errMsg) {
                Log.w(TAG, "onEndUnexpected errCode" + errCode);
            }
        });
        mUserManager = new UserInfoManager();

        mViewerManager = new ViewerInfoManager();
        mViewerManager.registerListener(mLiveManager, new IViewerListener() {
            @Override
            public void update(final List<Viewer> list) {
                mViewerView.post(new Runnable() {
                    @Override
                    public void run() {
                        mViewerView.updateViewerView(list);
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLiveManager.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLiveManager.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLiveManager.destroy();
        MessageFacade.getInstance().unregisterCallBack();
        MessageFacade.getInstance().stopPull();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.normal_live_btn:
                clickNormalBtn();
                break;
            case R.id.beauty_btn:
                mIsBeauty = !mIsBeauty;
                mLiveManager.enableVideoSmooth(mIsBeauty);
                mBeautyBtn.setText(mIsBeauty ? "美颜:开" : "美颜:关");
                break;
            case R.id.splash_btn:
                mLiveManager.enableFlashLight(!mLiveManager.isFlashLight());
                mSplashBtn.setText(mLiveManager.isFlashLight() ? "闪光:开" : "闪光:关");
                break;
            case R.id.switch_btn:
                mLiveManager.switchCamera();
                mSwitchBtn.setText(mLiveManager.isBackCamera() ? "后置" : "前置");
                if (mLiveManager.isBackCamera()) {
                    mSplashBtn.setEnabled(true);
                } else {
                    mSplashBtn.setEnabled(false);
                    if (mLiveManager.isFlashLight()) {
                        mLiveManager.enableFlashLight(false);
                        mSplashBtn.setText("闪光:关");
                    }
                }
                break;
            case R.id.volume_btn:
                if (mVolumeSb.getVisibility() == View.VISIBLE) {
                    mVolumeSb.setVisibility(View.GONE);
                } else {
                    mVolumeSb.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    private void clickNormalBtn() {
        if (mIsBegin) {
            endLive();
        } else {
            beginLive();
        }
    }

    private void beginLive() {
        mLiveManager.beginLive(null, "TEST", null, new ILiveCallback() {
            @Override
            public void notifyFail(int errCode) {
                ToastUtils.showToast("begin normal live fail=" + errCode);
            }

            @Override
            public void notifySuccess(long playerId, String liveId) {
                ToastUtils.showToast("begin normal live success");
                mIsBegin = true;
                mNormalLiveBtn.setText("结束直播");

                mPlayerId = playerId;
                mLiveId = liveId;
                initAnchor();
                initBarrageComponent();
            }
        });
    }

    private void endLive() {
        mLiveManager.endLive(new ILiveCallback() {
            @Override
            public void notifyFail(int errCode) {
                ToastUtils.showToast("end normal live fail=" + errCode);
            }

            @Override
            public void notifySuccess(long playerId, String liveId) {
                ToastUtils.showToast("end normal live success");
                mIsBegin = false;
                mNormalLiveBtn.setText("开始直播");
            }
        });
    }

    private void initAnchor() {
        mUserManager = new UserInfoManager();
        mUserManager.getUserByUuid(mPlayerId, new IUserCallback() {
            @Override
            public void notifyFail(int errCode) {
            }

            @Override
            public void notifySuccess(LiteUser user) {
                mAnchor = user;
                mAnchorView.updateAnchor(mAnchor);
            }
        });
    }

    private void initBarrageComponent() {
        Log.w(TAG, "initBarrageComponent");
        mBarrageManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mBarrageManager.setStackFromEnd(true);

        mBarrageRv.setLayoutManager(mBarrageManager);
        mBarrageAdapter = new BarrageAdapter();
        mBarrageRv.setAdapter(mBarrageAdapter);

        MessageFacade.getInstance().registerCallBack(mLiveId, mMsgCallBack, mSysMsgCallBack);
        MessageFacade.getInstance().startPull(mLiveId);
    }

    public static void openActivity(Activity activity) {
        Intent intent = new Intent(activity, NormalLiveActivity.class);
        activity.startActivity(intent);
    }
}
