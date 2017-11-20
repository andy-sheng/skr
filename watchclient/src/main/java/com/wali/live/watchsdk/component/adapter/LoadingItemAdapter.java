package com.wali.live.watchsdk.component.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.adapter.FansMemberAdapter;

/**
 * Created by yangli on 2017/11/17.
 */
public abstract class LoadingItemAdapter<ITEM, HOLDER extends ClickItemAdapter.BaseHolder, LISTENER>
        extends ClickItemAdapter<ITEM, HOLDER, LISTENER> {

    protected static final int ITEM_TYPE_FOOTER = -1;

    protected final FansMemberAdapter.FooterItem mFooterItem = new FansMemberAdapter.FooterItem();

    @Override
    protected HOLDER newViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_TYPE_FOOTER: {
                View view = mInflater.inflate(R.layout.fans_member_foot_item, parent, false);
                return (HOLDER) new FooterHolder(view);
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(HOLDER holder, int position) {
        if (holder instanceof FooterHolder) {
            holder.bindView(mFooterItem, null);
        } else {
            super.onBindViewHolder(holder, position);
        }
    }

    @Override
    public final int getItemViewType(int position) {
        return mItems.size() == position ? ITEM_TYPE_FOOTER : 0;
    }

    @Override
    public int getItemCount() {
        return mItems.size() + 1;
    }

    protected final void updateLoadingState(int state) {
        if (mFooterItem.getState() == state) {
            return;
        }
        mFooterItem.setState(state);
        notifyItemChanged(mItems.size());
    }

    public final void hideLoading() {
        updateLoadingState(FooterItem.STATE_HIDDEN);
    }

    public final void showLoading() {
        updateLoadingState(FooterItem.STATE_LOADING);
    }

    public final void onLoadingDone(boolean hasMore) {
        if (hasMore) {
            updateLoadingState(FooterItem.STATE_DONE);
        } else {
            updateLoadingState(FooterItem.STATE_NO_MORE);
        }
    }

    public final void onLoadingFailed() {
        updateLoadingState(FooterItem.STATE_FAILED);
    }

    public static class FooterItem {
        public static final int STATE_HIDDEN = 0;
        public static final int STATE_LOADING = 1;
        public static final int STATE_DONE = 2;
        public static final int STATE_NO_MORE = 3;
        public static final int STATE_FAILED = 4;

        private int state;

        public final int getState() {
            return state;
        }

        public final void setState(int state) {
            this.state = state;
        }
    }

    protected static class FooterHolder extends ClickItemAdapter.BaseHolder<FooterItem, Object> {
        private TextView mStatusView;

        public FooterHolder(View view) {
            super(view);
            mStatusView = $(R.id.status_view);
        }

        @Override
        public void bindView(FooterItem item, Object listener) {
            itemView.setVisibility(View.VISIBLE);
            switch (item.state) {
                case FooterItem.STATE_LOADING:
                    mStatusView.setText(R.string.vfan_member_loading);
                    break;
                case FooterItem.STATE_DONE:
                    mStatusView.setText(R.string.loading_tips_done);
                    break;
                case FooterItem.STATE_NO_MORE:
                    mStatusView.setText(R.string.loading_tips_no_more);
                    break;
                case FooterItem.STATE_FAILED:
                    mStatusView.setText(R.string.loading_tips_failed);
                    break;
                default:
                    itemView.setVisibility(View.GONE);
                    break;
            }
        }
    }

}
