package com.wali.live.watchsdk.component.presenter.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wali.live.watchsdk.R;

/**
 * Created by chenyong on 2017/4/26.
 */

public class ShareItemAdapter extends PlusItemAdapter {

    public ShareItemAdapter(int itemWidth, int itemHeight) {
        super(itemWidth, itemHeight);
    }

    @Override
    public PlusHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mInflater == null) {
            mInflater = LayoutInflater.from(parent.getContext());
        }
        View view = mInflater.inflate(R.layout.share_item, null);
        return new PlusHolder(view, mItemWidth, mItemHeight, R.id.share_btn);
    }
}
