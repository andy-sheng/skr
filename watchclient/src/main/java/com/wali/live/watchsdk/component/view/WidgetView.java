
package com.wali.live.watchsdk.component.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.widget.RelativeLayout;

import com.base.utils.display.DisplayUtils;
import com.wali.live.component.view.IComponentView;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.proto.LiveCommonProto;
import com.wali.live.watchsdk.R;

import java.util.List;

/**
 * Created by chenyong on 2017/03/24.
 * <p>
 * Generated using create_component_view.py
 *
 * @module 运营位组件
 */
public class WidgetView extends RelativeLayout
        implements IComponentView<WidgetView.IPresenter, WidgetView.IView> {
    private static final String TAG = "WidgetView";

    private static final int PADDING = DisplayUtils.dip2px(10f);
    private static final int PADDING_LANDSCAPE_RIGHT = DisplayUtils.dip2px(46.67f);

    @Nullable
    protected IPresenter mPresenter;

    private WidgetItemView mLeftTopWiv;
    private WidgetItemView mRightTopWiv;
    private WidgetItemView mLeftBottomWiv;
    private WidgetItemView mRightBottomWiv;

    private boolean mNeedShow = true;

    public WidgetView(Context context) {
        this(context, null, 0);
    }

    public WidgetView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WidgetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.widget_view, this);
        setPadding(PADDING, PADDING >> 1, PADDING, PADDING >> 1);
    }

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    protected final <T extends View> T $$(@IdRes int resId) {
        return (T) ((ViewStub) findViewById(resId)).inflate();
    }

    protected final <T extends View> T $(View parent, @IdRes int resId) {
        return (T) parent.findViewById(resId);
    }

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    private void showWidgetView(@NonNull List<LiveCommonProto.NewWidgetItem> list) {
        for (int i = 0; i < list.size(); i++) {
            LiveCommonProto.NewWidgetItem info = list.get(i);
            switch (info.getPosition()) {
                case 0://左上角
                    setLeftTopInfo(info);
                    break;
                case 1://右上角
                    setRightTopInfo(info);
                    break;
                case 2://左下角
                    setLeftBottomInfo(info);
                    break;
                case 3://右下角
                    setRightBottomInfo(info);
                    break;
            }
        }
    }

    /**
     * 设置左上角运营位数据
     */
    private void setLeftTopInfo(LiveCommonProto.NewWidgetItem info) {
        if (mLeftTopWiv == null) {
            mLeftTopWiv = $$(R.id.left_top_vs);
            mLeftTopWiv.setWidgetPos(WidgetItemView.POS_LEFT_TOP);
            mLeftTopWiv.setPresenter(mPresenter);
        }
        mLeftTopWiv.showWidgetItem(info, mNeedShow);
    }

    /**
     * 设置右上角运营位数据
     */
    private void setRightTopInfo(LiveCommonProto.NewWidgetItem info) {
        if (mRightTopWiv == null) {
            mRightTopWiv = $$(R.id.right_top_vs);
            mRightTopWiv.setWidgetPos(WidgetItemView.POS_RIGHT_TOP);
            mRightTopWiv.setPresenter(mPresenter);
        }
        mRightTopWiv.showWidgetItem(info, mNeedShow);
    }

    /**
     * 设置左下角运营位数据
     */
    private void setLeftBottomInfo(LiveCommonProto.NewWidgetItem info) {
        if (mLeftBottomWiv == null) {
            mLeftBottomWiv = $$(R.id.left_bottom_vs);
            mLeftBottomWiv.setWidgetPos(WidgetItemView.POS_LEFT_BOTTOM);
            mLeftBottomWiv.setPresenter(mPresenter);
        }
        mLeftBottomWiv.showWidgetItem(info, mNeedShow);
    }

    /**
     * 设置右下角运营位数据
     */
    private void setRightBottomInfo(LiveCommonProto.NewWidgetItem info) {
        if (mRightBottomWiv == null) {
            mRightBottomWiv = $$(R.id.right_bottom_vs);
            mRightBottomWiv.setWidgetPos(WidgetItemView.POS_RIGHT_BOTTOM);
            mRightBottomWiv.setPresenter(mPresenter);
        }
        mRightBottomWiv.showWidgetItem(info, mNeedShow);
    }

    private void adjustWidgetView(boolean needShow) {
        if (mNeedShow != needShow) {
            mNeedShow = needShow;
            if (mNeedShow) {
                showWidgetView();
            } else {
                hideWidgetView();
            }
        }
    }

    private void hideWidgetView() {
        if (mLeftTopWiv != null) {
            mLeftTopWiv.hide();
        }
        if (mRightTopWiv != null) {
            mRightTopWiv.hide();
        }
        if (mLeftBottomWiv != null) {
            mLeftBottomWiv.hide();
        }
        if (mRightBottomWiv != null) {
            mRightBottomWiv.hide();
        }
    }

    private void showWidgetView() {
        if (mLeftTopWiv != null) {
            mLeftTopWiv.show();
        }
        if (mRightTopWiv != null) {
            mRightTopWiv.show();
        }
        if (mLeftBottomWiv != null) {
            mLeftBottomWiv.show();
        }
        if (mRightBottomWiv != null) {
            mRightBottomWiv.show();
        }
    }

    private void destroyView() {
        if (mLeftTopWiv != null) {
            mLeftTopWiv.destroyView();
        }
        if (mRightTopWiv != null) {
            mRightTopWiv.destroyView();
        }
        if (mLeftBottomWiv != null) {
            mLeftBottomWiv.destroyView();
        }
        if (mRightBottomWiv != null) {
            mRightBottomWiv.destroyView();
        }
    }

    private void onOrientation(boolean isLandscape) {
        if (isLandscape) {
            setPadding(PADDING, PADDING >> 1, PADDING_LANDSCAPE_RIGHT, PADDING >> 1);
        } else {
            setPadding(PADDING, PADDING >> 1, PADDING, PADDING >> 1);
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
                return (T) WidgetView.this;
            }

            @Override
            public void onOrientation(boolean isLandscape) {
                WidgetView.this.onOrientation(isLandscape);
            }

            @Override
            public void hideWidgetView() {
                WidgetView.this.hideWidgetView();
            }

            @Override
            public void adjustWidgetView(boolean needShow) {
                WidgetView.this.adjustWidgetView(needShow);
            }

            @Override
            public void showWidgetView(@NonNull List<LiveCommonProto.NewWidgetItem> list) {
                WidgetView.this.showWidgetView(list);
            }

            @Override
            public void destroyView() {
                WidgetView.this.destroyView();
            }

        }
        return new ComponentView();
    }

    public interface IPresenter {
        long getUid();

        String getRoomId();
    }

    public interface IView extends IViewProxy {
        void onOrientation(boolean isLandscape);

        void hideWidgetView();

        void adjustWidgetView(boolean needShow);

        void showWidgetView(@NonNull List<LiveCommonProto.NewWidgetItem> list);

        void destroyView();
    }
}
