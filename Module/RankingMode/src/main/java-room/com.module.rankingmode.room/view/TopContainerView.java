package com.module.rankingmode.room.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.image.fresco.BaseImageView;
import com.common.view.ex.ExImageView;
import com.module.rankingmode.R;
import com.module.rankingmode.room.scorebar.ScorePrograssBar;
import com.module.rankingmode.room.event.InputBoardEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class TopContainerView extends RelativeLayout {
    public final static String TAG = "TopContainerView";
    ExImageView mCloseBtn;
    ScorePrograssBar mScoreProgressBar;
    BaseImageView mAvatarIv;

    Listener mListener;

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
        mCloseBtn = this.findViewById(R.id.close_btn);
        mScoreProgressBar = this.findViewById(R.id.score_progress_bar);

        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.closeBtnClick();
                }
            }
        });
        initSparkPrograssBar();
        loadAvatar(AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                .build());
    }

    private void initSparkPrograssBar() {
        mScoreProgressBar.setProgress(100);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mScoreProgressBar.setProgress(50);
            }
        }, 10000);
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
        AvatarUtils.loadAvatarByUrl(mAvatarIv, params);
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    public interface Listener {
        void closeBtnClick();
    }
}
