package com.dialog.list;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.common.base.R;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;


public class DialogListViewHolder extends RecyclerView.ViewHolder {
    ExTextView titleTv;
    DialogListItem data;

    public DialogListViewHolder(View itemView) {
        super(itemView);
        if (itemView instanceof ExTextView) {
            titleTv = (ExTextView) itemView;
        } else {
            titleTv = (ExTextView) itemView.findViewById(R.id.desc_tv);
        }
        itemView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
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
