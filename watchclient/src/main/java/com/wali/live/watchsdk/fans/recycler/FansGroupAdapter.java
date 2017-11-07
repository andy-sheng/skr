package com.wali.live.watchsdk.fans.recycler;

import android.view.ViewGroup;

import com.wali.live.watchsdk.lit.recycler.adapter.EmptyRecyclerAdapter;
import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;

/**
 * Created by lan on 2017/11/7.
 */
public class FansGroupAdapter extends EmptyRecyclerAdapter {
    @Override
    protected int getDataCount() {
        return 0;
    }

    @Override
    protected BaseHolder onCreateHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    protected void onBindHolder(BaseHolder holder, int position) {

    }

    @Override
    protected int getItemType(int position) {
        return 0;
    }
}
