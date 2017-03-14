package com.wali.live.livesdk.live.liveshow.view.panel;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.base.view.RotatedSeekBar;
import com.mi.live.engine.base.GalileoConstants;
import com.wali.live.component.view.IComponentView;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.component.view.panel.BaseBottomPanel;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.component.data.StreamerPresenter;
import com.wali.live.livesdk.live.component.presenter.BaseContainerPresenter;
import com.wali.live.livesdk.live.liveshow.data.MagicParamPresenter;
import com.wali.live.livesdk.live.liveshow.presenter.adapter.FilterItemAdapter;
import com.wali.live.livesdk.live.liveshow.presenter.adapter.SingleChooser;
import com.wali.live.livesdk.live.liveshow.presenter.adapter.VolumeAdjuster;
import com.wali.live.livesdk.live.view.SwitchButton;

import java.util.List;

/**
 * Created by yangli on 2017/03/07.
 * <p>
 * Generated using create_bottom_panel.py
 *
 * @module 秀场美妆面板视图
 */
public class LiveMagicPanel extends BaseBottomPanel<LinearLayout, RelativeLayout>
        implements IComponentView<LiveMagicPanel.IPresenter, LiveMagicPanel.IView> {
    private static final String TAG = "LiveMagicPanel";

    @NonNull
    protected IPresenter mPresenter;
    @NonNull
    protected StreamerPresenter mStreamerPresenter;

    private MagicParamPresenter.MagicParams mMagicParams;

    private RelativeLayout mSubPanelView;
    private View mSplitter;
    private ViewGroup mTabContainer;

    private PanelContainer mPanelContainer;
    private boolean mIsMultiBeauty = false;

    private View mBeautyBtn;
    private View mFilterBtn;
//    private View mExpressionBtn;

    private final SingleChooser mSingleChooser = new SingleChooser(
            new SingleChooser.IChooserListener() {
                @Override
                public void onItemSelected(View view) {
                    int id = view.getId();
                    if (id == R.id.face_beauty) {
                        mPanelContainer.showBeautyPanel();
                    } else if (id == R.id.filter) {
                        mPanelContainer.showFilterPanel();
                    }
//                    else if (id == R.id.expression) {
//                        mPanelContainer.showExpressionPanel();
//                    }
                }
            });

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.magic_control_panel;
    }

    public LiveMagicPanel(
            @NonNull RelativeLayout parentView,
            @NonNull StreamerPresenter streamerPresenter) {
        super(parentView);
        mStreamerPresenter = streamerPresenter;
    }

    @Override
    protected void inflateContentView() {
        super.inflateContentView();

        mSubPanelView = $(R.id.sub_panel_view);
        mSplitter = $(R.id.splitter);
        mTabContainer = $(R.id.tab_container);

        mBeautyBtn = $(R.id.face_beauty);
        mFilterBtn = $(R.id.filter);
//        mExpressionBtn = $(R.id.expression);

        mPanelContainer = new PanelContainer(mSubPanelView);
        mSingleChooser.setup(mTabContainer, 0);

        if (mMagicParams == null) {
            mPresenter.syncPanelStatus();
        } else {
            onPanelStatus(mMagicParams);
        }
    }

    @Override
    public void onOrientation(boolean isLandscape) {
        super.onOrientation(isLandscape);
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams) mContentView.getLayoutParams();
        if (mIsLandscape) {
            layoutParams.width = PANEL_WIDTH_LANDSCAPE;
        } else {
            layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        }
        mContentView.setLayoutParams(layoutParams);
    }

    private void onPanelStatus(MagicParamPresenter.MagicParams magicParams) {
        mMagicParams = magicParams;
        int enableCount = 0;
        if (magicParams.isFilter()) {
            ++enableCount;
            mFilterBtn.setVisibility(View.VISIBLE);
        }
//        if (magicParams.isExpression()) {
//            ++enableCount;
//            mExpressionBtn.setVisibility(View.VISIBLE);
//        }
        if (magicParams.isBeauty()) {
            ++enableCount;
            mBeautyBtn.setVisibility(View.VISIBLE);
        }
        mIsMultiBeauty = magicParams.isMultiBeauty();
        if (enableCount <= 1) { // 少于两个可见，则不需显示底部Tab
            mSplitter.setVisibility(View.GONE);
            mTabContainer.setVisibility(View.GONE);
        }
        if (magicParams.isBeauty()) { // 按优先级，显示默认的子面板
            mSingleChooser.setSelection(mBeautyBtn);
            mPanelContainer.showBeautyPanel();
        } else if (magicParams.isFilter()) {
            mSingleChooser.setSelection(mFilterBtn);
            mPanelContainer.showFilterPanel();
        }
//        else if (magicParams.isExpression()) {
//            mSingleChooser.setSelection(mExpressionBtn);
//            mPanelContainer.showExpressionPanel();
//        }
    }

    @Override
    public IView getViewProxy() {
        /**
         * 局部内部类，用于Presenter回调通知该View改变状态
         */
        class ComponentView implements IView {
            @Nullable
            @Override
            public <T extends View> T getRealView() {
                return null;
            }

            @Override
            public void onPanelStatus(MagicParamPresenter.MagicParams magicParams) {
                if (magicParams != null) {
                    LiveMagicPanel.this.onPanelStatus(magicParams);
                }
            }

            @Override
            public void onFilterData(List<FilterItemAdapter.FilterItem> filterItems) {
                mPanelContainer.mFilterPanel.onFilterData(filterItems);
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
        /**
         * 同步美妆面板状态
         */
        void syncPanelStatus();

        /**
         * 同步滤镜数据
         */
        void syncFilterData();
    }

    public interface IView extends IViewProxy {
        /**
         * 同步美妆面板按钮可用性
         */
        void onPanelStatus(MagicParamPresenter.MagicParams magicParams);

        /**
         * 同步滤镜数据
         */
        void onFilterData(List<FilterItemAdapter.FilterItem> filterItems);
    }

    // 美颜子面板
    private class BeautyPanel extends BaseBottomPanel<LinearLayout, RelativeLayout> {

        private final float[] mSeekBarPosIndex = new float[]{0, 0.34f, 0.66f, 1};

        private SwitchButton mSwitchButton;
        private RotatedSeekBar mSeekBar;

        public BeautyPanel(@NonNull RelativeLayout parentView) {
            super(parentView);
        }

        @Override
        protected int getLayoutResId() {
            if (mIsMultiBeauty) {
                return R.layout.single_beauty_panel;
            } else {
                return R.layout.multi_beauty_panel;
            }
        }

        @Override
        protected void inflateContentView() {
            super.inflateContentView();

            mSwitchButton = $(R.id.switch_btn);
            if (mSwitchButton != null) {
                mSwitchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mStreamerPresenter.setBeautyLevel(mMagicParams.getBeautyLevel(isChecked ? 1 : 0));
                    }
                });
            }

            mSeekBar = $(R.id.seek_bar);
            if (mSeekBar != null) {
                mSeekBar.setOnRotatedSeekBarChangeListener(new RotatedSeekBar.OnRotatedSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(RotatedSeekBar rotatedSeekBar, float percent, boolean fromUser) {
                    }

                    @Override
                    public void onStartTrackingTouch(RotatedSeekBar rotatedSeekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(RotatedSeekBar rotatedSeekBar) {
                        int index = (int) (rotatedSeekBar.getPercent() * 6 + 1) / 2; // 滑条级别：0, 1, 2, 3
                        rotatedSeekBar.setPercent(mSeekBarPosIndex[index]);
                        mStreamerPresenter.setBeautyLevel(mMagicParams.getBeautyLevel(index));
                    }
                });
            }

            if (mSwitchButton != null) {
                mSwitchButton.setChecked(mStreamerPresenter.getBeautyLevel() != GalileoConstants.BEAUTY_LEVEL_OFF);
            } else if (mSeekBar != null) {
                mSeekBar.setPercent(mSeekBarPosIndex[mMagicParams.findBeautyPos(mStreamerPresenter.getBeautyLevel())]);
            }
        }
    }

    // 滤镜子面板
    private class FilterPanel extends BaseBottomPanel<LinearLayout, RelativeLayout> {

        private RecyclerView mRecyclerView;

        private final FilterItemAdapter mAdapter = new FilterItemAdapter(
                new FilterItemAdapter.IFilterItemListener() {
                    @Override
                    public void onItemSelected(String filter) {
                        mStreamerPresenter.setFilter(filter);
                    }
                });

        private final VolumeAdjuster mFilterAdjuster = new VolumeAdjuster(
                new VolumeAdjuster.AdjusterWrapper() {
                    @Override
                    public void onChangeVolume(@IntRange(from = 0, to = 100) int volume) {
                        mStreamerPresenter.setFilterIntensity(volume);
                    }
                }
        );

        public FilterPanel(@NonNull RelativeLayout parentView) {
            super(parentView);
        }

        @Override
        protected int getLayoutResId() {
            return R.layout.filter_panel;
        }

        @Override
        protected void inflateContentView() {
            super.inflateContentView();

            mRecyclerView = this.$(R.id.recycler_view);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(
                    this.mParentView.getContext(), LinearLayoutManager.HORIZONTAL, false));

            mFilterAdjuster.setup((ViewGroup) $(R.id.param_adjuster_view));

            if (mPresenter != null) {
                mPresenter.syncFilterData();
            }
        }

        public void onFilterData(List<FilterItemAdapter.FilterItem> filterItems) {
            mAdapter.setItemData(filterItems);
            mAdapter.setCurrFilter(mStreamerPresenter.getFilter());
            mFilterAdjuster.setVolume(mStreamerPresenter.getFilterIntensity());
        }
    }

    // 注释掉，暂时不支持表情
//    // 表情子面板
//    private class ExpressionPanel extends BaseBottomPanel<RecyclerView, RelativeLayout> {
//
//        private RecyclerView mRecyclerView;
//
//        public ExpressionPanel(@NonNull RelativeLayout parentView) {
//            super(parentView);
//        }
//
//        @Override
//        protected int getLayoutResId() {
//            return R.layout.expression_panel;
//        }
//
//        @Override
//        protected void inflateContentView() {
//            super.inflateContentView();
//
//            if (mPresenter != null) {
//                mPresenter.syncExpressionData();
//            }
//        }
//    }

    // 子面板容器
    private class PanelContainer extends BaseContainerPresenter<RelativeLayout> {

        private BeautyPanel mBeautyPanel;
        private FilterPanel mFilterPanel;
//        private ExpressionPanel mExpressionPanel;

        @Override
        protected String getTAG() {
            return TAG;
        }

        public PanelContainer(@NonNull RelativeLayout relativeLayout) {
            super(null);
            setComponentView(relativeLayout);
        }

        protected void showBeautyPanel() {
            if (mBeautyPanel == null) {
                mBeautyPanel = new BeautyPanel(mView);
            }
            showPanel(mBeautyPanel, false);
        }

        protected void showFilterPanel() {
            if (mFilterPanel == null) {
                mFilterPanel = new FilterPanel(mView);
            }
            showPanel(mFilterPanel, false);
        }

//        protected void showExpressionPanel() {
//            if (mExpressionPanel == null) {
//                mExpressionPanel = new ExpressionPanel(mView);
//            }
//            showPanel(mExpressionPanel, false);
//        }

        @Nullable
        @Override
        protected IAction createAction() {
            return null;
        }
    }
}
