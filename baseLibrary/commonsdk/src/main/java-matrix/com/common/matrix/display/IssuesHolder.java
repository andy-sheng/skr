package com.common.matrix.display;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.common.base.R;
import com.tencent.matrix.report.Issue;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    public void readMappingFile(Map<Integer, String> methoMap) {
//        BufferedReader reader = null;
//        String tempString = null;
//        try {
//            reader = new BufferedReader(new FileReader(methodFilePath));
//            while ((tempString = reader.readLine()) != null) {
//                String[] contents = tempString.split(",");
//                methoMap.put(Integer.parseInt(contents[0]), contents[2].replace('\n', ' '));
//            }
//            reader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (reader != null) {
//                try {
//                    reader.close();
//                } catch (IOException e1) {
//                }
//            }
//        }
    }


    public void showIssue(MyIssue issue) {
        String key = "stack";
        if (issue.getContent().containsKey(key)) {
            try {
                String stack = issue.getContent().getString(key);
                Map<Integer, String> map = new HashMap<>();
                readMappingFile(map);

                if (map.size() > 0) {
                    StringBuilder stringBuilder = new StringBuilder(" ");

                    String[] lines = stack.split("\n");
                    for (String line : lines) {
                        String[] args = line.split(",");
                        int method = Integer.parseInt(args[1]);
                        boolean isContainKey = map.containsKey(method);
                        if (!isContainKey) {
                            System.out.print("error!!!");
                            continue;
                        }

                        args[1] = map.get(method);
                        stringBuilder.append(args[0]);
                        stringBuilder.append(",");
                        stringBuilder.append(args[1]);
                        stringBuilder.append(",");
                        stringBuilder.append(args[2]);
                        stringBuilder.append(",");
                        stringBuilder.append(args[3] + "\n");
                    }

                    issue.getContent().remove(key);
                    issue.getContent().put(key, stringBuilder.toString());
                }

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }

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
