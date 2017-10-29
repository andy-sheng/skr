package com.wali.live.watchsdk.component.view.panel;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.base.activity.BaseActivity;
import com.base.utils.display.DisplayUtils;
import com.base.view.BackTitleBar;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.component.view.panel.BaseBottomPanel;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.presenter.adapter.ConversationAdapter;
import com.wali.live.watchsdk.sixin.PopComposeMessageFragment;

import java.util.List;

/**
 * Created by yangli on 2017/10/27.
 *
 * @module 私信面板视图
 */
public class MessagePanel extends BaseBottomPanel<LinearLayout, RelativeLayout>
        implements IComponentView<MessagePanel.IPresenter, MessagePanel.IView> {
    private static final String TAG = "MessagePanel";

    @Nullable
    protected IPresenter mPresenter;

    private final ConversationAdapter mAdapter = new ConversationAdapter();

    private BackTitleBar mTitleBar;

    private final ConversationAdapter.IConversationClickListener mConversationClickListener =
            new ConversationAdapter.IConversationClickListener() {
                @Override
                public void onItemClick(ConversationAdapter.ConversationItem item) {
                    PopComposeMessageFragment.open((BaseActivity) mParentView.getContext(), item.getUser(), true);
                }
            };

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
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
        mAdapter.setClickListener(mConversationClickListener);
    }

    @Override
    protected void inflateContentView() {
        super.inflateContentView();
        mTitleBar = $(R.id.title_bar);
        mTitleBar.setTitle(R.string.sixin_model_message);
        mTitleBar.getBackBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPresenter != null) {
                    mPresenter.onBackBtnClick();
                }
            }
        });
        RecyclerView recyclerView = $(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(mParentView.getContext(),
                LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void showSelf(boolean useAnimation, boolean isLandscape) {
        if (!mIsShow && mPresenter != null) {
            mPresenter.startPresenter();
            mPresenter.syncAllConversions();
        }
        super.showSelf(useAnimation, isLandscape);
    }

    @Override
    public void hideSelf(boolean useAnimation) {
        if (mIsShow && mPresenter != null) {
            mPresenter.stopPresenter();
        }
        super.hideSelf(useAnimation);
    }

    @Override
    protected void orientSelf() {
        ViewGroup.LayoutParams layoutParams = mContentView.getLayoutParams();
        if (mIsLandscape) {
            layoutParams.width = PANEL_WIDTH_LANDSCAPE;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = DisplayUtils.dip2px(320);
        }
        mContentView.setLayoutParams(layoutParams);
    }

    @Override
    public IView getViewProxy() {
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) mContentView;
            }

            @Override
            public void onNewConversationList(List<ConversationAdapter.ConversationItem> list) {
                mAdapter.setItemData(list);
            }

            @Override
            public void onNewConversationUpdate(int index, ConversationAdapter.ConversationItem item) {
                if (item == null) {
                    mAdapter.notifyItemChanged(index);
                } else {
                    mAdapter.insertItemData(index, item);
                }
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {

        void startPresenter();

        void stopPresenter();

        /**
         * 点击返回按钮
         */
        void onBackBtnClick();

        /**
         * 获取关注模式下的全部会话列表
         */
        void syncAllConversions();
    }

    public interface IView extends IViewProxy {
        /**
         * 更新整个会话列表
         */
        void onNewConversationList(List<ConversationAdapter.ConversationItem> list);

        /**
         * 更新单个会话
         */
        void onNewConversationUpdate(int index, ConversationAdapter.ConversationItem item);
    }
}
