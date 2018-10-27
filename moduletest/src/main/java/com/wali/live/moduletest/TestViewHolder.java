package com.wali.live.moduletest;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class TestViewHolder extends RecyclerView.ViewHolder {
    TextView titleTv;
    H data;

    public TestViewHolder(View itemView) {
        super(itemView);
        if (itemView instanceof TextView) {
            titleTv = (TextView) itemView;
        } else {
            titleTv = (TextView) itemView.findViewById(R.id.desc_tv);
        }
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data != null) {
                    data.op.run();
                }
            }
        });
    }

    public void bindData(H data) {
        this.data = data;
        titleTv.setText(data.title);
    }
}
