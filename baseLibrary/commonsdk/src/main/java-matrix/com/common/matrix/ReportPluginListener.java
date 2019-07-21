package com.common.matrix;


import android.content.Context;
import android.content.Intent;

import com.common.log.MyLog;
import com.common.matrix.display.IssuesListActivity;
import com.common.utils.U;
import com.tencent.matrix.plugin.DefaultPluginListener;
import com.tencent.matrix.report.Issue;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportPluginListener extends DefaultPluginListener {

    public final String TAG = "Matrix性能监控";

    public ReportPluginListener(Context context) {
        super(context);
    }

    @Override
    public void onReportIssue(Issue issue) {
        super.onReportIssue(issue);
        //CrashReport.postCatchedException(new MatrixException(issue.toString()));
        if(issue.getContent().has("scene")){
            String scene = issue.getContent().optString("scene");
            if(scene.endsWith("IssuesListActivity")){
                return;
            }
        }
        /**
         * 写入本地文件
         */
        saveMonitorInfo2File(issue);
        Intent intent = new Intent(U.app(), IssuesListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        U.app().startActivity(intent);
    }


    private void saveMonitorInfo2File(Issue issue) {
        MyLog.e(TAG, "saveMonitorInfo2File" + " issue=" + issue);
        U.getFileUtils().deleteEarlyFiles(U.getAppInfoUtils().getSubDirFile("Matrix"), 10);

        com.alibaba.fastjson.JSONObject data = new com.alibaba.fastjson.JSONObject();
        data.put("type", issue.getType());
        data.put("tag", issue.getTag());
        data.put("key", issue.getKey());
        data.put("content", issue.getContent().toString());
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String fileName = formatter.format(new Date()) + ".txt";
            File dir = U.getAppInfoUtils().getSubDirFile("Matrix");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(new File(dir, fileName));
            fos.write(data.toJSONString().getBytes());
            fos.close();
        } catch (Exception e) {

        }
        return;
    }

}
