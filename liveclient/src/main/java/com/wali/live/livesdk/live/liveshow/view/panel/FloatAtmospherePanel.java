package com.wali.live.livesdk.live.liveshow.view.panel;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.wali.live.component.view.panel.BaseBottomPanel;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.component.data.StreamerPresenter;
import com.wali.live.livesdk.live.liveshow.data.MediaAsset;
import com.wali.live.livesdk.live.view.MoveViewGroup;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.statistics.StatisticsWorker;

/**
 * Created by yangli on 16-9-7.
 *
 * @module 氛围面板视图
 */
public class FloatAtmospherePanel extends BaseBottomPanel<MoveViewGroup, RelativeLayout>
        implements View.OnClickListener {
    private static final String TAG = "FloatAtmospherePanel";

    protected final static int PANEL_TOP_MARGIN_PORTRAIT = DisplayUtils.dip2px(210f);

    protected final static int PANEL_PIVOT_DELTA_X = DisplayUtils.dip2px(3.33f);
    protected final static int PANEL_PIVOT_DELTA_Y = DisplayUtils.dip2px(6.67f);

    @Nullable
    protected StreamerPresenter mPresenter;

    private final int mScreenWidth = GlobalData.screenWidth;
    private final int mScreenHeight = GlobalData.screenHeight;

    private int mPanelWidth = 0;
    private int mPanelHeight = 0;

    private final MediaAsset mMediaAsset = new MediaAsset();

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.close) {
            hideSelf(true);
            return;
        }
        if (mPresenter == null) {
            return;
        }
        if (id == R.id.applause) {
            notifyAtmosphere("Applause.mp3");
            StatisticsWorker.getsInstance().sendCommand(StatisticsWorker.AC_APP,
                    StatisticsKey.KEY_LIVING_SOUND_EFFECT_CLAP, 1);
        } else if (id == R.id.clown) {
            notifyAtmosphere("Funny.mp3");
            StatisticsWorker.getsInstance().sendCommand(StatisticsWorker.AC_APP,
                    StatisticsKey.KEY_LIVING_SOUND_EFFECT_FUNNY, 1);
        } else if (id == R.id.cheers) {
            notifyAtmosphere("Cheers.mp3");
            StatisticsWorker.getsInstance().sendCommand(StatisticsWorker.AC_APP,
                    StatisticsKey.KEY_LIVING_SOUND_EFFECT_CHEER_UP, 1);
        } else if (id == R.id.smile) {
            notifyAtmosphere("Laugh B.mp3");
            StatisticsWorker.getsInstance().sendCommand(StatisticsWorker.AC_APP,
                    StatisticsKey.KEY_LIVING_SOUND_EFFECT_SMILE, 1);
        } else if (id == R.id.laugh) {
            notifyAtmosphere("Laugh A.mp3");
            StatisticsWorker.getsInstance().sendCommand(StatisticsWorker.AC_APP,
                    StatisticsKey.KEY_LIVING_SOUND_EFFECT_LAUGH, 1);
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.float_atmosphere_panel;
    }

    public FloatAtmospherePanel(
            @NonNull RelativeLayout parentView,
            @Nullable StreamerPresenter presenter) {
        super(parentView);
        mPresenter = presenter;
        mMediaAsset.prepareMediaAssetAsync(parentView.getContext());
    }

    @Override
    protected void inflateContentView() {
        super.inflateContentView();

        $click(R.id.close, this);
        $click(R.id.applause, this);
        $click(R.id.clown, this);
        $click(R.id.cheers, this);
        $click(R.id.smile, this);
        $click(R.id.laugh, this);
    }

    @Override
    public void showSelf(boolean useAnimation, boolean isLandscape) {
        super.showSelf(useAnimation, isLandscape);
        mContentView.resetMoveRound(isLandscape);
    }

    private void notifyAtmosphere(String atmosphere) {
        String atmospherePath = mMediaAsset.queryFullPath(mParentView.getContext(), atmosphere);
        MyLog.w(TAG, "notifyOnAtmosphere atmosphere=" + atmosphere);
        if (!TextUtils.isEmpty(atmospherePath)) {
            mPresenter.playAtmosphere(atmospherePath);
        } else {
            MyLog.e(TAG, "notifyOnAtmosphere but atmosphere path is null");
        }
    }

    @Override
    public void onOrientation(boolean isLandscape) {
        super.onOrientation(isLandscape);
        if (mContentView.getMeasuredHeight() == 0) {
            mContentView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        }
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams) mContentView.getLayoutParams();
        MyLog.w(TAG, "width" + mContentView.getMeasuredWidth());
        if (mPanelWidth == 0 || mPanelHeight == 0) {
            mPanelWidth = Math.min(mContentView.getMeasuredWidth(), mContentView.getMeasuredHeight());
            mPanelHeight = Math.max(mContentView.getMeasuredWidth(), mContentView.getMeasuredHeight());
        }
        mContentView.resetMoveRound(isLandscape);
        if (isLandscape) {
            mContentView.setOrientation(LinearLayout.HORIZONTAL);
            layoutParams.leftMargin = mScreenHeight - mPanelWidth - mPanelHeight;
            layoutParams.topMargin = mScreenWidth - mPanelWidth;
            mContentView.setPivotX(mPanelHeight - PANEL_PIVOT_DELTA_X);
            mContentView.setPivotY(mPanelWidth / 2);
        } else {
            mContentView.setOrientation(LinearLayout.VERTICAL);
            layoutParams.leftMargin = mScreenWidth - mPanelWidth;
            layoutParams.topMargin = PANEL_TOP_MARGIN_PORTRAIT;
            mContentView.setPivotX(mPanelWidth / 2);
            mContentView.setPivotY(PANEL_PIVOT_DELTA_Y);
        }
    }
}
