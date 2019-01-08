package com.module.playways.rank.song.holder;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class SongCardRecycleView extends RecyclerView {
    public SongCardRecycleView(Context context) {
        super(context);
    }

    public SongCardRecycleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SongCardRecycleView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return false;
    }
}
