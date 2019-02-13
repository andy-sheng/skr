package com.didichuxing.doraemonkit.ui.widget.dialog;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.didichuxing.doraemonkit.R;


public class DialogListViewHolder extends RecyclerView.ViewHolder {
    TextView titleTv;
    DialogListItem data;

    public DialogListViewHolder(View itemView) {
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

    public void bindData(DialogListItem data) {
        this.data = data;
        titleTv.setText(data.title);
    }
}
