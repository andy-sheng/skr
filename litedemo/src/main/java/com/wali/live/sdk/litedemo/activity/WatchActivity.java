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
import android.widget.RelativeLayout;

import com.mi.liveassistant.barrage.callback.ChatMsgCallBack;
import com.mi.liveassistant.barrage.callback.SysMsgCallBack;
import com.mi.liveassistant.barrage.data.Message;
import com.mi.liveassistant.barrage.facade.MessageFacade;
import com.mi.liveassistant.data.model.User;
import com.mi.liveassistant.data.model.Viewer;
import com.mi.liveassistant.room.manager.watch.WatchManager;
import com.mi.liveassistant.room.manager.watch.callback.IWatchCallback;
import com.mi.liveassistant.room.user.UserInfoManager;
import com.mi.liveassistant.room.user.callback.IUserCallback;
import com.mi.liveassistant.room.viewer.ViewerInfoManager;
import com.mi.liveassistant.room.viewer.callback.IViewerCallback;
import com.mi.liveassistant.room.viewer.callback.IViewerListener;
import com.wali.live.sdk.litedemo.R;
import com.wali.live.sdk.litedemo.barrage.BarrageAdapter;
import com.wali.live.sdk.litedemo.barrage.view.SendBarrageView;
import com.wali.live.sdk.litedemo.barrage.view.SendBarrageView.ISendCallback;
import com.wali.live.sdk.litedemo.base.activity.RxActivity;
import com.wali.live.sdk.litedemo.topinfo.anchor.TopAnchorView;
import com.wali.live.sdk.litedemo.topinfo.viewer.TopViewerView;
import com.wali.live.sdk.litedemo.utils.KeyboardUtils;
import com.wali.live.sdk.litedemo.utils.ToastUtils;

import java.util.List;

/**
 * Created by lan on 17/5/3.
 */
public class WatchActivity extends RxActivity {
    public static final String EXTRA_PLAYER_ID = "player_id";
    public static final String EXTRA_LIVE_ID = "live_id";

    /*观看流程*/
    private WatchManager mWatchManager;
    private RelativeLayout mSurfaceContainer;
    private String mLiveId;

    /*主播信息*/
    private UserInfoManager mUserManager;
    private TopAnchorView mAnchorView;
    private long mPlayerId;
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
    private ISendCallback mSendCallback = new ISendCallback() {
        @Override
        public void send(String message) {
            ToastUtils.showToast("send=" + message);
            MessageFacade.getInstance().sendTextMessageAsync(message, mLiveId, mPlayerId);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);

        initData();
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

    private void initData() {
        Intent data = getIntent();
        if (data == null) {
            finish();
            return;
        }
        mPlayerId = data.getLongExtra(EXTRA_PLAYER_ID, 0);
        mLiveId = data.getStringExtra(EXTRA_LIVE_ID);
    }

    private void initView() {
        mSurfaceContainer = $(R.id.surface_container);

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
        mWatchManager = new WatchManager();
        mWatchManager.setContainerView(mSurfaceContainer);
        mWatchManager.enterLive(mPlayerId, mLiveId, new IWatchCallback() {
            @Override
            public void notifyFail(int errCode) {
                ToastUtils.showToast("enter live fail=" + errCode);
            }

            @Override
            public void notifySuccess() {
                ToastUtils.showToast("enter live success");

                initBarrageComponent();
            }
        });

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

        mViewerManager = new ViewerInfoManager();
        mViewerManager.registerListener(mWatchManager, new IViewerListener() {
            @Override
            public void update(List<Viewer> list) {
                ToastUtils.showToast("viewerList=" + list.size());
            }

            @Override
            public void updateManually() {
                ToastUtils.showToast("viewerList manually");
            }
        });
        mViewerManager.asyncViewerList(mPlayerId, mLiveId, new IViewerCallback() {
            @Override
            public void notifyFail(int errCode) {
            }

            @Override
            public void notifySuccess(List<Viewer> list) {
                mViewerView.updateViewerView(list);
            }
        });
    }

    private void initBarrageComponent() {
        mBarrageManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mBarrageManager.setStackFromEnd(true);

        mBarrageRv.setLayoutManager(mBarrageManager);
        mBarrageAdapter = new BarrageAdapter();
        mBarrageRv.setAdapter(mBarrageAdapter);

        MessageFacade.getInstance().registCallBack(mLiveId, mMsgCallBack, mSysMsgCallBack);
        MessageFacade.getInstance().startPull(mLiveId);
    }

    @Override
    protected void onDestroy() {
        Log.w(TAG, "onDestroy");
        super.onDestroy();
        MessageFacade.getInstance().unregistCallBack();
        MessageFacade.getInstance().stopPull();
        mWatchManager.leaveLive();
    }

    public static void openActivity(Activity activity, long playerId, String liveId) {
        Intent intent = new Intent(activity, WatchActivity.class);
        intent.putExtra(EXTRA_PLAYER_ID, playerId);
        intent.putExtra(EXTRA_LIVE_ID, liveId);
        activity.startActivity(intent);
    }
}
