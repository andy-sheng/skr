package com.module.home.relation.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.RelativeLayout;

import com.module.home.R;
import com.module.home.relation.adapter.RelationAdapter;

public class RelationView extends RelativeLayout {

    public static final int FRIENDS_MODE = 1;  // 好友
    public static final int FANS_MODE = 2;   // 粉丝
    public static final int FOLLOWS_MODE = 3;  // 关注

    private int mode = FRIENDS_MODE;

    RecyclerView mRecyclerView;
    RelationAdapter mRelationAdapter;

    public RelationView(Context context, int mode) {
        super(context);
        init(context, mode);
    }

    private void init(Context context, int mode) {
        inflate(context, R.layout.relation_view, this);
        this.mode = mode;

        mRecyclerView = (RecyclerView) this.findViewById(R.id.recycler_view);
        mRelationAdapter = new RelationAdapter(mode);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mRelationAdapter);

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
}
