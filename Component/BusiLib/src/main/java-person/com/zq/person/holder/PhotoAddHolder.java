package com.zq.person.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.zq.person.model.AddPhotoModel;

public class PhotoAddHolder extends RecyclerView.ViewHolder {

    ImageView mUpdatePhotoIv;

    public PhotoAddHolder(final View itemView, final RecyclerOnItemClickListener listener) {
        super(itemView);
        mUpdatePhotoIv = (ImageView)itemView.findViewById(R.id.update_photo_iv);
        itemView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (listener != null) {
                    listener.onItemClicked(itemView,0,new AddPhotoModel());
                }
            }
        });
    }

    public void bindData(int position) {

    }
}
