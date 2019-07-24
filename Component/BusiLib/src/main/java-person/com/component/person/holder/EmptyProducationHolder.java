package com.component.person.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.component.busilib.R;

public class EmptyProducationHolder extends RecyclerView.ViewHolder {
    ImageView mEmptyImg;
    TextView mEmptyTxt;

    public EmptyProducationHolder(View itemView) {
        super(itemView);
        mEmptyImg = (ImageView) itemView.findViewById(R.id.empty_img);
        mEmptyTxt = (TextView) itemView.findViewById(R.id.empty_txt);
    }

    public void bindData(boolean isSelf) {
        if (isSelf) {
            mEmptyTxt.setText("可以去练歌房录制作品哦～");
        } else {
            mEmptyTxt.setText("ta还没有作品哦～");
        }
    }
}
