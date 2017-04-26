package com.wali.live.common.listener;

import android.view.View;

/**
 * Created by lan on 15-11-4.
 * use for RecyclerView itemLongClick
 */
public interface OnItemLongClickListener {
    boolean onItemLongClick(View view, int position);
}