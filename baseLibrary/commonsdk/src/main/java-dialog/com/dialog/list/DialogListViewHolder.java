package com.dialog.list;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.common.base.R;


public class DialogListViewHolder extends RecyclerView.ViewHolder {
    TextView titleTv;
    View mDivider;
    DialogListItem data;

    public DialogListViewHolder(View itemView) {
        super(itemView);
        if (itemView instanceof TextView) {
            titleTv = (TextView) itemView;
        } else {
            titleTv = (TextView) itemView.findViewById(R.id.desc_tv);
            mDivider = (View) itemView.findViewById(R.id.divider);
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

    public void bindData(DialogListItem data) {
        this.data = data;
        titleTv.setText(data.title);
    }
}
