package com.wali.live.watchsdk.lit.recycler.assist;

import android.support.v7.widget.GridLayoutManager;

/**
 * Created by lan on 15-11-23.
 *
 * @module RecyclerView assist
 * @description GridLayoutManager header specific
 */
public class HeaderSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {
    private IRecyclerChecker mChecker;
    private GridLayoutManager mLayoutManager;

    public HeaderSpanSizeLookup(IRecyclerChecker checker, GridLayoutManager layoutManager) {
        mChecker = checker;
        mLayoutManager = layoutManager;
    }

    @Override
    public int getSpanSize(int position) {
        return mChecker.isHeader(position) ? mLayoutManager.getSpanCount() : 1;
    }
}