package com.zq.person.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.view.ex.ExTextView;
import com.component.busilib.R;

public class PhotoHeadHolder extends RecyclerView.ViewHolder {

    ExTextView mPhotoNumTv;

    public PhotoHeadHolder(View itemView) {
        super(itemView);
        mPhotoNumTv = (ExTextView) itemView.findViewById(R.id.photo_num_tv);
    }

    public void bindData(int totalNum) {
        mPhotoNumTv.setText("个人相册（" + totalNum + "）");
    }
}
