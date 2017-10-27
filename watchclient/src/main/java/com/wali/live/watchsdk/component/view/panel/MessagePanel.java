package com.wali.live.watchsdk.component.view.panel;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.base.view.BackTitleBar;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.component.view.panel.BaseBottomPanel;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.presenter.adapter.ConversationAdapter;

/**
 * Created by yangli on 2017/10/27.
 *
 * @module 私信面板视图
 */
public class MessagePanel extends BaseBottomPanel<LinearLayout, RelativeLayout>
        implements View.OnClickListener, IComponentView<MessagePanel.IPresenter, MessagePanel.IView> {
    private static final String TAG = "MessagePanel";

    @Nullable
    protected IPresenter mPresenter;

    private BackTitleBar mTitleBar;
    private ConversationAdapter mAdapter;

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.message_panel;
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public MessagePanel(@NonNull RelativeLayout parentView) {
        super(parentView);
    }

    @Override
    protected void inflateContentView() {
        super.inflateContentView();

        mTitleBar = $(R.id.title_bar);
        RecyclerView recyclerView = $(R.id.recycler_view);
    }

    @Override
    public IView getViewProxy() {
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) mContentView;
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
    }

    public interface IView extends IViewProxy {
    }
}
