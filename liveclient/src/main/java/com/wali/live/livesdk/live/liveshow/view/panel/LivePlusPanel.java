package com.wali.live.livesdk.live.liveshow.view.panel;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.base.global.GlobalData;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.common.gift.adapter.viewHolder.decoration.BaseItemDecoration;
import com.wali.live.componentwrapper.view.panel.BaseBottomPanel;
import com.wali.live.livesdk.R;
import com.wali.live.watchsdk.component.presenter.adapter.PlusItemAdapter;

import java.util.List;

/**
 * Created by yangli on 2017/03/09.
 * <p>
 * Generated using create_bottom_panel.py
 *
 * @module 秀场直播加面板视图
 */
public class LivePlusPanel extends BaseBottomPanel<RecyclerView, RelativeLayout>
        implements View.OnClickListener, IComponentView<LivePlusPanel.IPresenter, LivePlusPanel.IView> {
    private static final String TAG = "LivePlusPanel";

    private static final int MAX_COUNT_IN_LINE = 4;

    @Nullable
    protected IPresenter mPresenter;

    private PlusItemAdapter mAdapter;

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public void onClick(View v) {
        if (mPresenter == null) {
            return;
        }
        int id = v.getId();
        if (id == R.id.atmosphere_btn) {
            mPresenter.showAtmosphereView();
        } else if (id == R.id.comment_btn) {
            mPresenter.showInputView();
        } else if (id == R.id.envelope_btn) {
            mPresenter.showEnvelopeView();
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.plus_control_panel;
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public LivePlusPanel(@NonNull RelativeLayout parentView) {
        super(parentView);
        mAdapter = new PlusItemAdapter(GlobalData.screenWidth / MAX_COUNT_IN_LINE);
    }

    @Override
    protected void inflateContentView() {
        super.inflateContentView();

        RecyclerView recyclerView = $(R.id.recycler_view);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(mContentView.getContext(), MAX_COUNT_IN_LINE));
        mAdapter.setOnClickListener(this);
        recyclerView.addItemDecoration(new PlusItemDecoration(MAX_COUNT_IN_LINE));

        if (mPresenter != null) {
            mPresenter.syncPlusBtnConfig();
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
                return (T) mContentView;
            }

            @Override
            public void onPlusBtnConfig(List<PlusItemAdapter.PlusItem> plusItems) {
                mAdapter.setPlusData(plusItems);
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
        /**
         * 显示氛围界面
         */
        void showAtmosphereView();

        /**
         * 显示输入界面
         */
        void showInputView();

        /**
         * 显示发送包界面
         */
        void showEnvelopeView();

        /**
         * 同步直播加按钮可见性配置
         */
        void syncPlusBtnConfig();
    }

    public interface IView extends IViewProxy<View> {
        /**
         * 更新直播加按钮列表
         */
        void onPlusBtnConfig(List<PlusItemAdapter.PlusItem> plusItems);
    }

    private static class PlusItemDecoration extends BaseItemDecoration {
        public static final int GRID_LIST = 4;

        public PlusItemDecoration(int orientation) {
            super(orientation);
        }

        protected int getResColorId() {
            return R.color.color_white_trans_20;
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            if (mOrientation == GRID_LIST) {
                drawGrid(c, parent, state);
            } else {
                super.onDraw(c, parent, state);
            }
        }

        private void drawGrid(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
            int childSize = parent.getChildCount();
            if (childSize == 0) {
                return;
            }
            int rowSize = 4;
            int lineSize = (childSize - 1) / rowSize + 1;

            // 获取单个子view的宽高
            View child = parent.getChildAt(0);
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
            int width = child.getRight() + mlp.rightMargin;
            int height = child.getBottom() + mlp.bottomMargin;

            // 获取父view的边界
            final int left = parent.getPaddingLeft();
            final int right = parent.getMeasuredWidth() - parent.getPaddingRight();
            final int top = parent.getPaddingTop();
            final int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();

            // 画lineSize - 1条横线
            for (int i = 1; i < lineSize; i++) {
                int t = top + height * i;
                canvas.drawRect(left, t, right, t + mItemSize, mPaint);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (mOrientation == GRID_LIST) {
                // nothing to do
            } else {
                super.getItemOffsets(outRect, view, parent, state);
            }
        }
    }
}
