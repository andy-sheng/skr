package com.wali.live.livesdk.live.view.topinfo;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.wali.live.base.BaseEvent;
import com.wali.live.livesdk.R;
import com.wali.live.watchsdk.watchtop.view.WatchTopInfoBaseView;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by lan on 17/2/14.
 */
public class LiveTopInfoSingleView extends WatchTopInfoBaseView {
    public static final String TAG = LiveTopInfoSingleView.class.getSimpleName();

    private View mNameAndViewerNumAreaView;
    private View mManagerAreaView;

    public LiveTopInfoSingleView(Context context) {
        super(context);
        init(context, false);
    }

    public LiveTopInfoSingleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, false);
    }

    public LiveTopInfoSingleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, false);
    }

    @Override
    protected int getLayout(boolean isLandscape) {
        return R.layout.livesdk_top_info_single_view;
    }

    @Override
    protected void initParticular() {
        mAvatarLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
    }

    @Override
    protected void initView() {
        mNameAndViewerNumAreaView = findViewById(R.id.name_and_viewer_num_area);
        mManagerAreaView = findViewById(R.id.manager_area);
    }

    public void initViewUseData() {
        updateAnchorNickName();
        updateOwnerView();
        updateTicketView();
        updateViewerCountView();
        updateViewers();

        initManagerView();
    }

    @Override
    public void onUserInfoComplete() {
        updateTicketView();
        updateOwnerView();
        updateAnchorNickName();
        mLastUpdateTime = 0;
        updateViewers();
    }

    @Override
    public void updateAnchorNickName() {
        super.updateAnchorNickName();
        CharSequence text = TextUtils.ellipsize(mShowerNameTv.getText(), mShowerNameTv.getPaint(),
                mShowerNameTv.getMaxWidth(), TextUtils.TruncateAt.END);
        mShowerNameTv.setText(text);
    }

    @Override
    public void resetData() {
        MyLog.d(TAG, "resetData");
        adjustOriginalAlpha(1.0f);
        adjustOriginalVisibility(VISIBLE);
        super.resetData();
    }

    private void adjustOriginalAlpha(float alpha) {
        mOwnerBadgeIv.setAlpha(alpha);
    }

    private void adjustOriginalVisibility(int visibility) {
        mOwnerBadgeIv.setVisibility(visibility);
    }

    private void initManagerView() {
        mManagerAreaView.setVisibility(VISIBLE);
        mManagerAreaView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new BaseEvent.UserActionEvent(BaseEvent.UserActionEvent.EVENT_TYPE_REQUEST_SET_MANAGER, null, null));
            }
        });
    }

    public void onScreenOrientationChanged(boolean isLandScape) {
        super.onScreenOrientationChanged(isLandScape);
        LayoutParams lpAvatar = (RelativeLayout.LayoutParams) mAvatarRv.getLayoutParams();
        LayoutParams lpContainer = (RelativeLayout.LayoutParams) findViewById(R.id.owner_container_root).getLayoutParams();
        if (isLandScape) {
            lpAvatar.rightMargin = DisplayUtils.dip2px(AVATAR_MARGIN_RIGHT_LANDSCAPE);
            lpContainer.topMargin = DisplayUtils.dip2px(AVATAR_MARGIN_TOP_LANDSCAPE);
        } else {
            lpAvatar.rightMargin = DisplayUtils.dip2px(AVATAR_MARGIN_RIGHT_PORTRAIT);
            lpContainer.topMargin = DisplayUtils.dip2px(AVATAR_MARGIN_TOP_PORTRAIT);
        }
    }
}
