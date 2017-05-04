package com.wali.live.sdk.litedemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mi.liveassistant.avatar.AvatarUtils;
import com.mi.liveassistant.data.model.User;
import com.mi.liveassistant.room.manager.watch.WatchManager;
import com.mi.liveassistant.room.manager.watch.callback.IWatchCallback;
import com.mi.liveassistant.room.user.UserInfoManager;
import com.mi.liveassistant.room.user.callback.IUserCallback;
import com.mi.liveassistant.room.viewer.ViewerInfoManager;
import com.mi.liveassistant.room.viewer.callback.IViewerCallback;
import com.mi.liveassistant.room.viewer.model.Viewer;
import com.wali.live.sdk.litedemo.R;
import com.wali.live.sdk.litedemo.base.activity.RxActivity;
import com.wali.live.sdk.litedemo.fresco.FrescoWorker;
import com.wali.live.sdk.litedemo.fresco.image.ImageFactory;
import com.wali.live.sdk.litedemo.utils.ToastUtils;
import com.wali.live.sdk.litedemo.viewer.TopViewerView;

import java.util.List;

/**
 * Created by lan on 17/5/3.
 */
public class WatchActivity extends RxActivity {
    public static final String EXTRA_PLAYER_ID = "player_id";
    public static final String EXTRA_LIVE_ID = "live_id";

    private WatchManager mWatchManager;
    private UserInfoManager mUserManager;

    private RelativeLayout mSurfaceContainer;

    private SimpleDraweeView mAnchorDv;
    private TextView mAnchorTv;

    private long mPlayerId;
    private String mLiveId;

    private User mAnchor;

    private RecyclerView mBarrageRv;

    private ViewerInfoManager mViewerManager;
    private TopViewerView mViewerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initData();
        initView();
        initManager();
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

        mAnchorDv = $(R.id.anchor_dv);
        mAnchorTv = $(R.id.anchor_tv);

        mBarrageRv = $(R.id.barrage_rv);
        mViewerView = $(R.id.viewer_view);
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
                updateAnchorView();
            }
        });

        mViewerManager = new ViewerInfoManager();
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

    private void updateAnchorView() {
        mAnchorTv.setText(mAnchor.getNickname());

        String avatarUrl = AvatarUtils.getAvatarUrlByUid(mAnchor.getUid(), mAnchor.getAvatar());
        Log.d(TAG, "updateAnchorView avatarUrl=" + avatarUrl);
        FrescoWorker.loadImage(mAnchorDv, ImageFactory.newHttpImage(avatarUrl).setIsCircle(true).build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWatchManager.leaveLive();
    }

    public static void openActivity(Activity activity, long playerId, String liveId) {
        Intent intent = new Intent(activity, WatchActivity.class);
        intent.putExtra(EXTRA_PLAYER_ID, playerId);
        intent.putExtra(EXTRA_LIVE_ID, liveId);
        activity.startActivity(intent);
    }
}
