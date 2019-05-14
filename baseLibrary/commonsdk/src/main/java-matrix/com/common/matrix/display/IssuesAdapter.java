package com.common.matrix.display;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.base.R;
import com.common.view.recyclerview.DiffAdapter;
import com.tencent.matrix.report.Issue;

public class IssuesAdapter extends DiffAdapter<Issue,IssuesHolder> {

    @Override
    public IssuesHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_issue_list, parent, false);
        return new IssuesHolder(view);
    }


    @Override
    public void onBindViewHolder(final  IssuesHolder holder, int position) {
        Issue is =  mDataList.get(position);
        holder.bind(mDataList.size()-position,is);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

}
