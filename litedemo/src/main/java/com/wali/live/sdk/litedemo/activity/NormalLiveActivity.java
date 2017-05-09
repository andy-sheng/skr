package com.wali.live.sdk.litedemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mi.liveassistant.barrage.callback.ChatMsgCallBack;
import com.mi.liveassistant.barrage.callback.SysMsgCallBack;
import com.mi.liveassistant.barrage.data.Message;
import com.mi.liveassistant.barrage.facade.MessageFacade;
import com.mi.liveassistant.camera.CameraView;
import com.mi.liveassistant.data.model.User;
import com.mi.liveassistant.data.model.Viewer;
import com.mi.liveassistant.room.manager.live.NormalLiveManager;
import com.mi.liveassistant.room.manager.live.callback.ILiveCallback;
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
    private User mAnchor;

    /*观众信息*/
    private ViewerInfoManager mViewerManager;
    private TopViewerView mViewerView;

    /*弹幕消息*/
    private Button mSendMessageBtn;
    private RecyclerView mBarrageRv;
    private LinearLayoutManager mBarrageManager;
    private BarrageAdapter mBarrageAdapter;

    private ChatMsgCallBack mMsgCallBack = new ChatMsgCallBack() {
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

    private SysMsgCallBack mSysMsgCallBack = new SysMsgCallBack() {
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
        KeyboardUtils.showKeyboard(this, (EditText) mSendBarrageView.findViewById(R.id.barrage_et));
    }

    private void updateOnKeyboardHide() {
        mSendMessageBtn.setVisibility(View.VISIBLE);
        mSendBarrageView.setVisibility(View.GONE);
        KeyboardUtils.hideKeyboard(this, (EditText) mSendBarrageView.findViewById(R.id.barrage_et));
    }

    private void initView() {
        mCameraView = $(R.id.camera_view);
        mNormalLiveBtn = $(R.id.normal_live_btn);
        mNormalLiveBtn.setOnClickListener(this);

        mAnchorView = $(R.id.anchor_view);
        mViewerView = $(R.id.viewer_view);

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
        mLiveManager = new NormalLiveManager(mCameraView);
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
        MessageFacade.getInstance().unregistCallBack();
        MessageFacade.getInstance().stopPull();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.normal_live_btn:
                clickNormalBtn();
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
        ToastUtils.showToast("begin normal live ...");
        mLiveManager.beginLive(null, "TEST", null, new ILiveCallback() {
            @Override
            public void notifyFail(int errCode) {
                ToastUtils.showToast("begin normal live fail=" + errCode);
            }

            @Override
            public void notifySuccess(long playerId, String liveId) {
                ToastUtils.showToast("begin normal live success");
                mIsBegin = true;
                mNormalLiveBtn.setText("end normal live");

                mPlayerId = playerId;
                mLiveId = liveId;
                initAnchor();
                initBarrageComponent();
            }
        });
    }

    private void endLive() {
        ToastUtils.showToast("end normal live ...");
        mLiveManager.endLive(new ILiveCallback() {
            @Override
            public void notifyFail(int errCode) {
                ToastUtils.showToast("end normal live fail=" + errCode);
            }

            @Override
            public void notifySuccess(long playerId, String liveId) {
                ToastUtils.showToast("end normal live success");
                mIsBegin = false;
                mNormalLiveBtn.setText("begin normal live");
            }
        });
    }

    private void initAnchor() {
        mUserManager = new UserInfoManager();
        mUserManager.asyncUserByUuid(mPlayerId, new IUserCallback() {
            @Override
            public void notifyFail(int errCode) {
            }

            @Override
            public void notifySuccess(User user) {
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

        MessageFacade.getInstance().registCallBack(mLiveId, mMsgCallBack, mSysMsgCallBack);
        MessageFacade.getInstance().startPull(mLiveId);
    }

    public static void openActivity(Activity activity) {
        Intent intent = new Intent(activity, NormalLiveActivity.class);
        activity.startActivity(intent);
    }
}
