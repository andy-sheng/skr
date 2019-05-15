package com.common.matrix.display;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.common.base.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class IssuesHolder extends RecyclerView.ViewHolder {
    TextView tvTime, tvTag, tvKey, tvType, tvContent, tvIndex, tvDesc;

    public int position;

    private boolean isShow = true;

    public IssuesHolder(View itemView) {
        super(itemView);
        tvTime = itemView.findViewById(R.id.item_time);
        tvTag = itemView.findViewById(R.id.item_tag);
        tvKey = itemView.findViewById(R.id.item_key);
        tvType = itemView.findViewById(R.id.item_type);
        tvContent = itemView.findViewById(R.id.item_content);
        tvIndex = itemView.findViewById(R.id.item_index);
        tvDesc = itemView.findViewById(R.id.item_desc);
    }

    public void bind(int no, MyIssue issue) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss:SSS");
        Date date = new Date(issue.getContent().getLongValue("time"));
        tvTime.setText("IssueTime -> " + simpleDateFormat.format(date));

        if (TextUtils.isEmpty(issue.getTag())) {
            tvTag.setVisibility(View.GONE);
        } else {
            tvTag.setText("TAG -> " + issue.getTag());
        }
        if (TextUtils.isEmpty(issue.getKey())) {
            tvKey.setVisibility(View.GONE);
        } else {
            tvKey.setText("KEY -> " + issue.getKey());
        }

        if (issue.getType() == 0) {
            tvType.setVisibility(View.GONE);
        } else {
            tvType.setText("TYPE -> " + issue.getType());
        }
        tvDesc.setVisibility(View.VISIBLE);
        tvDesc.setText("DESC -> " + issue.getDesc());

        tvIndex.setText(no + "");
        tvIndex.setTextColor(getColor(position));
        if (isShow) {
            showIssue(issue);
        } else {
            hideIssue();
        }

    }

    public void showIssue(MyIssue issue) {
        tvContent.setText(ParseIssueUtil.parseIssue(issue, true));
        tvContent.setVisibility(View.VISIBLE);
        isShow = true;
    }

    public void hideIssue() {
        tvContent.setVisibility(View.GONE);
        isShow = false;
    }

    public int getColor(int index) {
        switch (index) {
            case 0:
                return Color.RED;
            case 1:
                return Color.GREEN;
            case 2:
                return Color.BLUE;
            default:
                return Color.GRAY;
        }
    }
}
