package com.wali.live.sdk.litedemo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.mi.liveassistant.barrage.callback.ChatMsgCallBack;
import com.mi.liveassistant.barrage.callback.SysMsgCallBack;
import com.mi.liveassistant.barrage.data.Message;
import com.mi.liveassistant.barrage.facade.MessageFacade;
import com.mi.liveassistant.data.model.User;
import com.mi.liveassistant.data.model.Viewer;
import com.mi.liveassistant.room.manager.live.GameLiveManager;
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
import com.wali.live.sdk.litedemo.global.GlobalData;
import com.wali.live.sdk.litedemo.topinfo.anchor.TopAnchorView;
import com.wali.live.sdk.litedemo.topinfo.viewer.TopViewerView;
import com.wali.live.sdk.litedemo.utils.KeyboardUtils;
import com.wali.live.sdk.litedemo.utils.ToastUtils;

import java.util.List;

import static com.wali.live.sdk.litedemo.MainActivity.REQUEST_MEDIA_PROJECTION;

/**
 * Created by chenyong on 2017/4/28.
 */
public class GameLiveActivity extends RxActivity implements View.OnClickListener {
    /*开播流程*/
    private GameLiveManager mLiveManager;
    private Button mGameLiveBtn;
    private boolean mIsBegin;

    /*主播信息*/
    private UserInfoManager mUserManager;
    private TopAnchorView mAnchorView;
    private long mPlayerId;
    private String mLiveId;
    private User mAnchor;

    private Intent mIntent;

    /*观众信息*/
    private ViewerInfoManager mViewerManager;
    private TopViewerView mViewerView;

    /*控制按钮*/
    private ViewGroup mOperatorView;
    private Button mMuteBtn;
    private boolean mIsMute;

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
        setContentView(R.layout.activity_game_live);

        initView();
        initManager();

        adjustView();
    }

    private void adjustView() {
        mMuteBtn.setText("静音:" + (mIsMute ? "是" : "否"));

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
        KeyboardUtils.showKeyboard(this, (EditText) mSendBarrageView.findViewById(R.id.barrage_et));
    }

    private void updateOnKeyboardHide() {
        mSendMessageBtn.setVisibility(View.VISIBLE);
        mSendBarrageView.setVisibility(View.GONE);
        mOperatorView.setVisibility(View.VISIBLE);
        KeyboardUtils.hideKeyboard(this, (EditText) mSendBarrageView.findViewById(R.id.barrage_et));
    }

    private void initView() {
        mGameLiveBtn = $(R.id.game_live_btn);
        mGameLiveBtn.setOnClickListener(this);

        mAnchorView = $(R.id.anchor_view);
        mViewerView = $(R.id.viewer_view);

        mOperatorView = $(R.id.operate_container);
        mMuteBtn = $(R.id.mute_btn);
        mMuteBtn.setOnClickListener(this);

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
        mLiveManager = new GameLiveManager(new ILiveListener() {
            @Override
            public void onEndUnexpected() {
                Log.w(TAG, "onEndUnexpected");
            }
        });

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
        Log.w(TAG, "onDestroy");
        super.onDestroy();
        mLiveManager.destroy();
        MessageFacade.getInstance().unregistCallBack();
        MessageFacade.getInstance().stopPull();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.game_live_btn:
                clickGameBtn();
                break;
            case R.id.mute_btn:
                mIsMute = !mIsMute;
                mLiveManager.muteMic(mIsMute);
                mMuteBtn.setText("静音:" + (mIsMute ? "是" : "否"));
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void clickGameBtn() {
        if (mIsBegin) {
            endLive();
        } else {
            if (mIntent == null) {
                ToastUtils.showToast("begin game live intent is null");
                startActivityForResult(
                        ((MediaProjectionManager) GlobalData.app()
                                .getSystemService(Context.MEDIA_PROJECTION_SERVICE)).createScreenCaptureIntent(),
                        REQUEST_MEDIA_PROJECTION);
                return;
            }
            beginLive();
        }
    }

    private void beginLive() {
        mLiveManager.setCaptureIntent(mIntent);
        mLiveManager.beginLive(null, "TEST", null, new ILiveCallback() {
            @Override
            public void notifyFail(int errCode) {
                ToastUtils.showToast("begin game live fail=" + errCode);
            }

            @Override
            public void notifySuccess(long playerId, String liveId) {
                ToastUtils.showToast("begin game live success");
                mIsBegin = true;
                mGameLiveBtn.setText("结束直播");

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
                ToastUtils.showToast("end game live fail=" + errCode);
            }

            @Override
            public void notifySuccess(long playerId, String liveId) {
                ToastUtils.showToast("end game live success");
                mIsBegin = false;
                mGameLiveBtn.setText("开始直播");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.w(TAG, "onActivityResult " + requestCode + " resultCode=" + resultCode + "data =" + data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_MEDIA_PROJECTION:
                    mIntent = data;
                    beginLive();
                    break;
                default:
                    break;
            }
        } else if (requestCode == REQUEST_MEDIA_PROJECTION) {
            ToastUtils.showToast("media projection forbidden");
        }
    }

    public static void openActivity(Activity activity) {
        Intent intent = new Intent(activity, GameLiveActivity.class);
        activity.startActivity(intent);
    }
}
