package com.zq.person.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;

public class PhotoUpdateHolder extends RecyclerView.ViewHolder {

    ImageView mUpdatePhotoIv;

    public PhotoUpdateHolder(View itemView, RecyclerOnItemClickListener listener) {
        super(itemView);
        mUpdatePhotoIv = (ImageView)itemView.findViewById(R.id.update_photo_iv);

    }

    public void bindData(int totalNum) {
    }
}
