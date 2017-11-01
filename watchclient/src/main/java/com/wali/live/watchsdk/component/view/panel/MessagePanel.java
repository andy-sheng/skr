package com.wali.live.watchsdk.component.view.panel;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.base.utils.display.DisplayUtils;
import com.base.view.SymmetryTitleBar;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.component.view.panel.BaseBottomPanel;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.presenter.adapter.ConversationAdapter;

import java.util.List;

/**
 * Created by yangli on 2017/10/27.
 *
 * @module 私信面板视图
 */
public final class MessagePanel extends BaseBottomPanel<LinearLayout, RelativeLayout>
        implements IComponentView<MessagePanel.IPresenter, MessagePanel.IView> {
    private static final String TAG = "MessagePanel";

    @Nullable
    protected IPresenter mPresenter;

    private final ConversationAdapter mAdapter = new ConversationAdapter();

    private SymmetryTitleBar mTitleBar;
    private View mLeftTitleBtn;
    private ImageView mRightTitleBtn;

    private final ConversationAdapter.IConversationClickListener mConversationClickListener =
            new ConversationAdapter.IConversationClickListener() {
                @Override
                public void onItemClick(ConversationAdapter.ConversationItem item) {
                    if (mPresenter != null) {
                        mPresenter.onConversationClick(mParentView.getContext(), item);
                    }
                }
            };

    @Override
    protected final String getTAG() {
        return TAG;
    }

    @Override
    protected final int getLayoutResId() {
        return R.layout.message_panel;
    }

    @Override
    public final void setPresenter(@Nullable IPresenter iPresenter) {
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

        mLeftTitleBtn = mTitleBar.getLeftImageBtn();
        mLeftTitleBtn.setVisibility(View.GONE);
        mLeftTitleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPresenter != null) {
                    mPresenter.onBackBtnClick();
                }
            }
        });
        mRightTitleBtn = mTitleBar.getRightImageBtn();
        mRightTitleBtn.setImageResource(R.drawable.dynamic_message_icon);
        mRightTitleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPresenter != null) {
                    mPresenter.onRightBtnClick(mParentView.getContext());
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
        boolean needStart = !mIsShow;
        super.showSelf(useAnimation, isLandscape);
        if (needStart) {
            mPresenter.startPresenter();
        }
    }

    @Override
    public void hideSelf(boolean useAnimation) {
        boolean needStop = mIsShow;
        super.hideSelf(useAnimation);
        if (needStop) {
            mPresenter.stopPresenter();
        }
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
            public void onEnterFocusMode() {
                mAdapter.clearData();
                mTitleBar.setTitle(R.string.sixin_model_message);
                mLeftTitleBtn.setVisibility(View.GONE);
                mRightTitleBtn.setVisibility(View.VISIBLE);
                mRightTitleBtn.setImageResource(R.drawable.dynamic_message_icon);
            }

            @Override
            public void onEnterUnFocusMode() {
                mAdapter.clearData();
                mTitleBar.setTitle(R.string.sixin_model_unattention);
                mLeftTitleBtn.setVisibility(View.VISIBLE);
                mRightTitleBtn.setVisibility(View.GONE);
                mRightTitleBtn.setImageResource(0);
            }

            @Override
            public void onNewConversationList(List<ConversationAdapter.ConversationItem> list) {
                mAdapter.setItemData(list);
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
         * 点击右侧按钮
         */
        void onRightBtnClick(Context context);

        /**
         * 点击会话列表项
         */
        void onConversationClick(@NonNull Context context, @NonNull ConversationAdapter.ConversationItem item);
    }

    public interface IView extends IViewProxy {
        /**
         * 进入关注模式
         */
        void onEnterFocusMode();

        /**
         * 进入未关注模式
         */
        void onEnterUnFocusMode();

        /**
         * 更新整个会话列表
         */
        void onNewConversationList(List<ConversationAdapter.ConversationItem> list);
    }
}
