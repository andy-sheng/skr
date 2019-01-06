package com.module.playways.rank.room.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.image.fresco.BaseImageView;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.module.playways.rank.room.model.RoomData;
import com.module.playways.rank.room.quickmsg.QuickMsgView;
import com.module.rank.R;
import com.module.playways.rank.room.event.InputBoardEvent;
import com.module.playways.rank.room.scorebar.ScorePrograssBar2;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class TopContainerView extends RelativeLayout {
    public final static String TAG = "TopContainerView";
    ExImageView mMoreBtn;
    ScorePrograssBar2 mScoreProgressBar;
    BaseImageView mAvatarIv;
    MoreOpView mMoreOpView;
    Listener mListener;
    RoomData mRoomData;

    public TopContainerView(Context context) {
        super(context);
        init();
    }

    public TopContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.top_container_view_layout, this);
        mAvatarIv = this.findViewById(R.id.avatar_iv);
        mMoreBtn = this.findViewById(R.id.more_btn);
        mScoreProgressBar = this.findViewById(R.id.score_progress_bar);

        mMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMoreOpView == null) {
                    mMoreOpView = new MoreOpView(getContext());
                    mMoreOpView.setListener(new MoreOpView.Listener() {
                        @Override
                        public void onClostBtnClick() {
                            if (mListener != null) {
                                mListener.closeBtnClick();
                            }
                        }

                        @Override
                        public void onVoiceChange(boolean voiceOpen) {
                            // 打开或者关闭声音 只是不听别人的声音
                            if (mListener != null) {
                                mListener.onVoiceChange(voiceOpen);
                            }
                        }
                    });
                    mMoreOpView.setRoomData(mRoomData);
                }
                mMoreOpView.showAt(mMoreBtn);
            }
        });
        initSparkPrograssBar();
        loadAvatar(AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                .setCircle(true)
                .build());
    }

    private void initSparkPrograssBar() {
        mScoreProgressBar.setProgress(100);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mScoreProgressBar.setProgress(50);
            }
        }, 4000);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(InputBoardEvent event) {
        if (event.show) {
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
        }
    }

    public void loadAvatar(AvatarUtils.LoadParams params) {
        params.setCircle(true);
        AvatarUtils.loadAvatarByUrl(mAvatarIv, params);
    }

    public void setScoreProgress(int progress) {
        mScoreProgressBar.setProgress(progress);
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    public void setRoomData(RoomData roomData) {
        mRoomData = roomData;
    }

    public interface Listener {
        void closeBtnClick();

        void onVoiceChange(boolean voiceOpen);
    }
}
