package com.wali.live.watchsdk.income.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.internal.LoadingLayout;
import com.wali.live.watchsdk.R;


/**
 * Created by liuyanyan on 16/3/5.
 */
public class PullToRefreshRecycleView extends PullToRefreshBase<RecyclerView> {
    public PullToRefreshRecycleView(Context context) {
        super(context);
    }

    public PullToRefreshRecycleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullToRefreshRecycleView(Context context, Mode mode) {
        super(context, mode);
    }

    public PullToRefreshRecycleView(Context context, Mode mode, AnimationStyle animStyle) {
        super(context, mode, animStyle);
    }


    @Override
    public Orientation getPullToRefreshScrollDirection() {
        return Orientation.VERTICAL;
    }

    @Override
    protected RecyclerView createRefreshableView(Context context, AttributeSet attrs) {
        RecyclerView rv = new RecyclerView(context, attrs);
        rv.setId(R.id.recycleview);
        return rv;
    }

    @Override
    protected boolean isReadyForPullEnd() {
        LinearLayoutManager mLinearLayoutManager = (LinearLayoutManager)getRefreshableView().getLayoutManager();

        final int lastItemPosition = getRefreshableView().getAdapter().getItemCount() - 1;
        final int lastVisiblePosition = mLinearLayoutManager.findLastVisibleItemPosition();

        if (lastVisiblePosition >= lastItemPosition - 1) {
            final int childIndex = lastVisiblePosition - mLinearLayoutManager.findFirstVisibleItemPosition();
            final View lastVisibleChild = getRefreshableView().getChildAt(childIndex);
            if (lastVisibleChild != null) {
                return lastVisibleChild.getBottom() <= getRefreshableView().getBottom();
            }
        }
        return false;
    }

    @Override
    protected boolean isReadyForPullStart() {
        View view = getRefreshableView().getChildAt(0);

        if (null != view) {
            return view.getTop() >= getRefreshableView().getTop();
        }
        return false;
    }

    public LoadingLayout getHeadLayout(){
        return getHeaderLayout();
    }
}
