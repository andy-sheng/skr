package com.zq.dialog;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;

import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.zq.dialog.adapter.ReportAdapter;
import com.zq.dialog.model.ReportModel;

import java.util.ArrayList;
import java.util.List;

public class ReportDialogView extends RelativeLayout {

    public static final int FORM_RANK = 1;
    public static final int FORM_PERSON = 2;

    List<ReportModel> mReportModels = new ArrayList<>(); //数据源
    List<ReportModel> mSelectModels = new ArrayList<>(); //已选项

    ExTextView mSubmitTv;

    RecyclerView mRecyclerView;
    ReportAdapter mReportAdapter;
    NoLeakEditText mReportContent;

    public ReportDialogView(Context context, int mode) {
        super(context);
        init(mode);
    }

    private void init(final int mode) {
        inflate(getContext(), R.layout.report_dialog_view, this);

        mSubmitTv = (ExTextView) findViewById(R.id.submit_tv);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mReportContent = (NoLeakEditText) findViewById(R.id.report_content);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mReportAdapter = new ReportAdapter(new ReportAdapter.RecyclerOnItemCheckListener() {
            @Override
            public void onCheckedChanged(boolean isCheck, ReportModel model) {
                if (isCheck) {
                    mSelectModels.add(model);
                } else {
                    mSelectModels.remove(model);
                }
            }
        });

        if (mode == FORM_RANK) {
            mReportModels = getRankReportList();
        } else if (mode == FORM_PERSON) {
            mReportModels = getPersonReportList();
        }

        mReportAdapter.setDataList(mReportModels);
        mRecyclerView.setAdapter(mReportAdapter);
        mReportAdapter.notifyDataSetChanged();
    }

    public List<ReportModel> getSelectReportModels() {
        return mSelectModels;
    }

    public String getSubmitContent() {
        return mReportContent.getText().toString().trim();
    }

    private List<ReportModel> getRankReportList() {
        List<ReportModel> rankList = new ArrayList<>();
        rankList.add(new ReportModel(5, "违规作弊"));
        rankList.add(new ReportModel(6, "恶意乱唱"));
        rankList.add(new ReportModel(7, "冒充官方"));
        rankList.add(new ReportModel(2, "侮辱谩骂"));
        rankList.add(new ReportModel(3, "色情低俗"));
        return rankList;
    }

    private List<ReportModel> getPersonReportList() {
        List<ReportModel> personList = new ArrayList<>();
        personList.add(new ReportModel(1, "骗子，有欺诈行为"));
        personList.add(new ReportModel(2, "侮辱谩骂"));
        personList.add(new ReportModel(3, "色情低俗"));
        personList.add(new ReportModel(4, "头像、昵称违规"));
        return personList;
    }

}
