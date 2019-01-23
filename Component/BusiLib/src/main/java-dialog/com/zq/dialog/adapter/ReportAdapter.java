package com.zq.dialog.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.common.view.recyclerview.DiffAdapter;
import com.component.busilib.R;
import com.zq.dialog.model.ReportModel;

public class ReportAdapter extends DiffAdapter<ReportModel, RecyclerView.ViewHolder> {

    RecyclerOnItemCheckListener onItemCheckListener;

    public ReportAdapter(RecyclerOnItemCheckListener onItemCheckListener) {
        this.onItemCheckListener = onItemCheckListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_item_layout, parent, false);
        ReportItemHolder viewHolder = new ReportItemHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ReportModel model = mDataList.get(position);

        ReportItemHolder reportItemHolder = (ReportItemHolder) holder;
        reportItemHolder.bind(model);

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    private class ReportItemHolder extends RecyclerView.ViewHolder {

        CheckBox mCheckBox;
        ReportModel mReportModel;

        public ReportItemHolder(View itemView) {
            super(itemView);
            mCheckBox = itemView.findViewById(R.id.checkbox);

            mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck) {
                    if (onItemCheckListener != null) {
                        onItemCheckListener.onCheckedChanged(isCheck, mReportModel);
                    }
                }
            });
        }

        public void bind(ReportModel model) {
            this.mReportModel = model;
            mCheckBox.setText(model.getText());
        }
    }

    public interface RecyclerOnItemCheckListener {
        void onCheckedChanged(boolean isCheck, ReportModel model);
    }
}
