package com.wali.live.watchsdk.sixin.view;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.mi.live.data.user.User;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.sixin.recycler.SixinMessageAdapter;

/**
 * Created by lan on 16-2-23.
 */
@Deprecated
public class SixinComposeMessageView extends RelativeLayout {
    public final static int PAGE_MESSAGE_COUNT = 10; //分页加载十个消息

    private SwipeRefreshLayout mRefreshLayout;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;

    public SixinMessageAdapter mMessageAdapter;

    private User mTargetUser;

    public SixinComposeMessageView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public SixinComposeMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public SixinComposeMessageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        inflate(context, R.layout.sixin_compose_recycler_layout, this);
        mRecyclerView = (RecyclerView) this.findViewById(R.id.recycler_view);
        mRefreshLayout = (SwipeRefreshLayout) this.findViewById(R.id.swipe_refresh_layout);
        initContentView();
    }

    private void initContentView() {
        mMessageAdapter = new SixinMessageAdapter();
        mRecyclerView.setAdapter(mMessageAdapter);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        RecyclerView.ItemAnimator animator = mRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
    }
}


