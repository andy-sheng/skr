package com.common.view.recyclerview;

import android.view.View;

public interface RecyclerOnItemClickListener<T> {
    /**
     * 并不是每个参数都用得到，按需要传递。
     * 传递方和使用方协商就好了。
     * @param view
     * @param position
     * @param model
     */
    void onItemClicked(View view, int position,T model);
}