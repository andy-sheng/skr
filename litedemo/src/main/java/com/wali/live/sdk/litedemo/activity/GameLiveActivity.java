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
import android.widget.Button;

import com.mi.liveassistant.barrage.callback.TextMsgCallBack;
import com.mi.liveassistant.barrage.data.Message;
import com.mi.liveassistant.barrage.facade.MessageFacade;
import com.mi.liveassistant.data.model.User;
import com.mi.liveassistant.room.manager.live.GameLiveManager;
import com.mi.liveassistant.room.manager.live.callback.ILiveCallback;
import com.mi.liveassistant.room.user.UserInfoManager;
import com.mi.liveassistant.room.user.callback.IUserCallback;
import com.wali.live.sdk.litedemo.R;
import com.wali.live.sdk.litedemo.barrage.BarrageAdapter;
import com.wali.live.sdk.litedemo.barrage.view.SendBarrageView;
import com.wali.live.sdk.litedemo.base.activity.RxActivity;
import com.wali.live.sdk.litedemo.global.GlobalData;
import com.wali.live.sdk.litedemo.topinfo.anchor.TopAnchorView;
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

    /*弹幕消息*/
    private RecyclerView mBarrageRv;
    private LinearLayoutManager mBarrageManager;
    private BarrageAdapter mBarrageAdapter;

    private TextMsgCallBack mMsgCallBack = new TextMsgCallBack() {
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
    }

    private void initView() {
        mGameLiveBtn = $(R.id.game_live_btn);
        mGameLiveBtn.setOnClickListener(this);

        mAnchorView = $(R.id.anchor_view);

        mBarrageRv = $(R.id.barrage_rv);
        mSendBarrageView = $(R.id.send_barrage_view);
        mSendBarrageView.setCallback(mSendCallback);
    }

    private void initManager() {
        mLiveManager = new GameLiveManager();
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
        Log.w(TAG,"onDestroy");
        super.onDestroy();
        mLiveManager.destroy();
        MessageFacade.getInstance().unregistCallBack(mMsgCallBack);
        MessageFacade.getInstance().stopPull();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.game_live_btn:
                clickGameBtn();
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
        ToastUtils.showToast("begin game live ...");
        mLiveManager.beginLive(null, "TEST", null, new ILiveCallback() {
            @Override
            public void notifyFail(int errCode) {
                ToastUtils.showToast("begin game live fail=" + errCode);
            }

            @Override
            public void notifySuccess(long playerId, String liveId) {
                ToastUtils.showToast("begin game live success");
                mIsBegin = true;
                mGameLiveBtn.setText("end game live");

                mPlayerId = playerId;
                mLiveId = liveId;
                initAnchor();
                initBarrageComponent();
            }
        });
    }

    private void endLive() {
        ToastUtils.showToast("end game live ...");
        mLiveManager.endLive(new ILiveCallback() {
            @Override
            public void notifyFail(int errCode) {
                ToastUtils.showToast("end game live fail=" + errCode);
            }

            @Override
            public void notifySuccess(long playerId, String liveId) {
                ToastUtils.showToast("end game live success");
                mIsBegin = false;
                mGameLiveBtn.setText("begin game live");
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
        Log.w(TAG,"initBarrageComponent");
        mBarrageManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mBarrageManager.setStackFromEnd(true);

        mBarrageRv.setLayoutManager(mBarrageManager);
        mBarrageAdapter = new BarrageAdapter();
        mBarrageRv.setAdapter(mBarrageAdapter);

        MessageFacade.getInstance().registCallBack(mMsgCallBack);
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
