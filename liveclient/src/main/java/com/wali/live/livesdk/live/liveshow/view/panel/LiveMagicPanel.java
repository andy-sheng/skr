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
import com.wali.live.component.view.BasePanelContainer;
import com.wali.live.component.view.IComponentView;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.component.view.panel.BaseBottomPanel;
import com.wali.live.livesdk.R;
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
        implements View.OnClickListener, IComponentView<LiveMagicPanel.IPresenter, LiveMagicPanel.IView> {
    private static final String TAG = "LiveMagicPanel";

    @Nullable
    protected IPresenter mPresenter;

    private RelativeLayout mSubPanelView;
    private View mSplitter;
    private ViewGroup mTabContainer;

    private PanelContainer mPanelContainer;
    private boolean mIsMultiBeauty = false;

    private View mBeautyBtn;
    private View mFilterBtn;
    private View mExpressionBtn;

    private final SingleChooser mSingleChooser = new SingleChooser(
            new SingleChooser.IChooserListener() {
                @Override
                public void onItemSelected(View view) {
                    if (mPresenter == null) {
                        return;
                    }
                    int id = view.getId();
                    if (id == R.id.face_beauty) {
                        mPanelContainer.showBeautyPanel();
                    } else if (id == R.id.expression) {
                        mPanelContainer.showExpressionPanel();
                    } else if (id == R.id.filter) {
                        mPanelContainer.showFilterPanel();
                    }
                }
            });

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.magic_control_panel;
    }

    public LiveMagicPanel(@NonNull RelativeLayout parentView) {
        super(parentView);
    }

    @Override
    protected void inflateContentView() {
        super.inflateContentView();

        mSubPanelView = $(R.id.sub_panel_view);
        mSplitter = $(R.id.splitter);
        mTabContainer = $(R.id.tab_container);

        mBeautyBtn = $(R.id.face_beauty);
        mFilterBtn = $(R.id.filter);
        mExpressionBtn = $(R.id.expression);

        $click(mBeautyBtn, this);
        $click(mFilterBtn, this);
        $click(mExpressionBtn, this);

        mPanelContainer = new PanelContainer(mSubPanelView);
        mSingleChooser.setup(mTabContainer, 0);

        if (mPresenter != null) {
            mPresenter.syncPanelStatus();
        }
    }

    @Override
    public void onOrientation(boolean isLandscape) {
        super.onOrientation(isLandscape);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mContentView.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        if (mIsLandscape) {
            layoutParams.width = PANEL_WIDTH_LANDSCAPE;
        } else {
            layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        }
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
                if (magicParams == null) {
                    return;
                }
                int enableCount = 0;
                if (magicParams.isFilter()) {
                    ++enableCount;
                    mFilterBtn.setVisibility(View.VISIBLE);
                }
                if (magicParams.isExpression()) {
                    ++enableCount;
                    mExpressionBtn.setVisibility(View.VISIBLE);
                }
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
                    mBeautyBtn.setSelected(true);
                    mPanelContainer.showBeautyPanel();
                } else if (magicParams.isFilter()) {
                    mFilterBtn.setSelected(true);
                    mPanelContainer.showFilterPanel();
                } else if (magicParams.isExpression()) {
                    mExpressionBtn.setSelected(true);
                    mPanelContainer.showExpressionPanel();
                }
            }

            @Override
            public void onBeautyData(int currPosition) {
                if (mPanelContainer.mBeautyPanel != null) {
                    mPanelContainer.mBeautyPanel.onCurrPosition(currPosition);
                }
            }

            @Override
            public void onFilterData(List<FilterItemAdapter.FilterItem> filterItems) {
                if (mPanelContainer.mFilterPanel != null) {
                    mPanelContainer.mFilterPanel.mAdapter.setItemData(filterItems);
                }
            }

            @Override
            public void onFilterData(String filter, int intensity) {
                if (mPanelContainer.mFilterPanel != null) {
                    mPanelContainer.mFilterPanel.mAdapter.setCurrFilter(filter);
                    mPanelContainer.mFilterPanel.mFilterAdjuster.setVolume(intensity);
                }
            }

            @Override
            public void onExpressionData(List<FilterItemAdapter.FilterItem> filterItems, int currPosition) {
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
         * 同步美颜数据
         */
        void syncBeautyData();

        /**
         * 同步滤镜数据
         */
        void syncFilterData();

        /**
         * 同步表情数据
         */
        void syncExpressionData();

        /**
         * 设置美颜
         */
        void setBeauty(int index);

        /**
         * 设置滤镜
         */
        void setFilter(String filter);

        /**
         * 设置滤镜强度
         */
        void setFilterIntensity(int intensity);

        /**
         * 设置表情
         */
        void setExpression(int index);
    }

    public interface IView extends IViewProxy {
        /**
         * 同步美妆面板按钮可用性
         */
        void onPanelStatus(MagicParamPresenter.MagicParams magicParams);

        /**
         * 同步美颜数据
         */
        void onBeautyData(int currPosition);

        /**
         * 同步滤镜数据
         */
        void onFilterData(List<FilterItemAdapter.FilterItem> filterItems);

        /**
         * 同步滤镜数据
         */
        void onFilterData(String filter, int intensity);

        /**
         * 同步表情数据
         */
        void onExpressionData(List<FilterItemAdapter.FilterItem> filterItems, int currPosition);
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

        private void onCurrPosition(int currPosition) {
            if (mSwitchButton != null) {
                mSwitchButton.setChecked(currPosition > 0);
            } else if (mSeekBar != null) {
                mSeekBar.setPercent(mSeekBarPosIndex[currPosition]);
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
                        if (LiveMagicPanel.this.mPresenter != null) {
                            LiveMagicPanel.this.mPresenter.setBeauty(isChecked ? 1 : 0);
                        }
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
                        if (LiveMagicPanel.this.mPresenter != null) {
                            LiveMagicPanel.this.mPresenter.setBeauty(index);
                        }
                    }
                });
            }

            if (LiveMagicPanel.this.mPresenter != null) {
                LiveMagicPanel.this.mPresenter.syncBeautyData();
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
                        if (LiveMagicPanel.this.mPresenter != null) {
                            LiveMagicPanel.this.mPresenter.setFilter(filter);
                        }
                    }
                });

        private final VolumeAdjuster mFilterAdjuster = new VolumeAdjuster(
                new VolumeAdjuster.AdjusterWrapper() {
                    @Override
                    public void onChangeVolume(@IntRange(from = 0, to = 100) int volume) {
                        if (LiveMagicPanel.this.mPresenter != null) {
                            LiveMagicPanel.this.mPresenter.setFilterIntensity(volume);
                        }
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

            if (LiveMagicPanel.this.mPresenter != null) {
                LiveMagicPanel.this.mPresenter.syncFilterData();
            }
        }
    }

    // 表情子面板
    private class ExpressionPanel extends BaseBottomPanel<RecyclerView, RelativeLayout> {

        private RecyclerView mRecyclerView;

        public ExpressionPanel(@NonNull RelativeLayout parentView) {
            super(parentView);
        }

        @Override
        protected int getLayoutResId() {
            return R.layout.expression_panel;
        }

        @Override
        protected void inflateContentView() {
            super.inflateContentView();

            if (LiveMagicPanel.this.mPresenter != null) {
                LiveMagicPanel.this.mPresenter.syncExpressionData();
            }
        }
    }

    // 子面板容器
    private class PanelContainer extends BasePanelContainer<Object, IViewProxy, RelativeLayout> {

        private BeautyPanel mBeautyPanel;
        private FilterPanel mFilterPanel;
        private ExpressionPanel mExpressionPanel;

        public PanelContainer(@NonNull RelativeLayout panelContainer) {
            super(panelContainer);
            mPanelContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 空操作，覆盖基类行为，不调用hidePanel
                }
            });
        }

        @Override
        public IViewProxy getViewProxy() {
            return null;
        }

        protected void showBeautyPanel() {
            if (mBeautyPanel == null) {
                mBeautyPanel = new BeautyPanel(this.mPanelContainer);
            }
            showPanel(mBeautyPanel, false);
        }

        protected void showFilterPanel() {
            if (mFilterPanel == null) {
                mFilterPanel = new FilterPanel(this.mPanelContainer);
            }
            showPanel(mFilterPanel, false);
        }

        protected void showExpressionPanel() {
            if (mExpressionPanel == null) {
                mExpressionPanel = new ExpressionPanel(this.mPanelContainer);
            }
            showPanel(mExpressionPanel, false);
        }
    }
}
