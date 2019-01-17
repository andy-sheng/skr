package com.module.playways.grab.room.top;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.common.view.ex.ExLinearLayout;
import com.module.rank.R;

import java.util.ArrayList;

public class GrabTopRv extends ExLinearLayout {
    private ArrayList<GrabTopModel> mDataList;

    public GrabTopRv(Context context) {
        super(context);
    }

    public GrabTopRv(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GrabTopRv(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initData(ArrayList<GrabTopModel> dataList) {
        mDataList = dataList;
        int i;
        for (i = 0; i < mDataList.size(); i++) {
            GrabTopModel grabTopModel = mDataList.get(i);
            View view = this.getChildAt(i);
            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.grab_top_view_holder_layout, this, false);
                GrabTopViewHolder grabTopViewHolder = new GrabTopViewHolder(view);
                grabTopModel.setViewHolder(grabTopViewHolder);
                view.setTag(grabTopViewHolder);
                LinearLayout.LayoutParams lp = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
                lp.weight = 1;
                addView(view, lp);
            }
            view.setVisibility(VISIBLE);
            GrabTopViewHolder viewHolder = (GrabTopViewHolder) view.getTag();
            viewHolder.bindData(grabTopModel, i);
        }
        for (; i < this.getChildCount(); i++) {
            View view = this.getChildAt(i);
            view.setVisibility(GONE);
        }
    }


    public void lightOn(int fromId) {
        int index = mDataList.indexOf(new GrabTopModel(fromId));
        if (index >= 0) {
            GrabTopModel grabTopModel = mDataList.get(index);
            grabTopModel.setStatus(GrabTopModel.STATUS_LIGHT_ON);
            grabTopModel.getViewHolder().bindData(grabTopModel, index);
        }
    }

    public void lightOff(int fromId) {

    }
}
