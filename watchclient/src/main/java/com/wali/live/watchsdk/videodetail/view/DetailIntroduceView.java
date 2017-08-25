package com.wali.live.watchsdk.videodetail.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.view.EmptyView;

/**
 * Created by zyh on 2017/06/06.
 *
 * @module 详情页底下的详情view
 */
public class DetailIntroduceView extends RelativeLayout {
    private static final String TAG = "DetailIntroduceView";
    private RecyclerView mRecyclerView;
    private IntroduceAdapter mIntroduceAdapter;

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    protected final void $click(View view, OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    public DetailIntroduceView(Context context) {
        this(context, null, 0);
    }

    public DetailIntroduceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DetailIntroduceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.introduce_layout, this);
        mRecyclerView = $(R.id.recycler_view);
        mIntroduceAdapter = new IntroduceAdapter();
        mRecyclerView.setAdapter(mIntroduceAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    public void setData(String title, String desc) {
        mIntroduceAdapter.setData(title, desc);
    }

    public static class IntroduceAdapter extends RecyclerView.Adapter<IntroduceAdapter.BaseAdapter> {
        String mTitle = "";
        String mDesc = "";

        @Override
        public BaseAdapter onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feeds_detail_vedio_info_view, null);
            return new BaseAdapter(view);
        }

        public void setData(String title, String desc) {
            mTitle = title;
            mDesc = desc;
            notifyDataSetChanged();
        }

        @Override
        public void onBindViewHolder(BaseAdapter holder, int position) {
            if (holder != null) {
                if (!TextUtils.isEmpty(mDesc)) {
                    holder.mContentTv.setText(mDesc);
                } else if (!TextUtils.isEmpty(mTitle)) {
                    holder.mContentTv.setText(mTitle);
                } else {
                    holder.mContentTv.setVisibility(GONE);
                    holder.mEmptyView.setVisibility(VISIBLE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return 1;
        }

        class BaseAdapter extends RecyclerView.ViewHolder {
            private EmptyView mEmptyView;
            private TextView mContentTv;

            public BaseAdapter(View itemView) {
                super(itemView);
                mEmptyView = (EmptyView) itemView.findViewById(R.id.empty_view);
                mContentTv = (TextView) itemView.findViewById(R.id.content_tv);
            }
        }
    }
}
