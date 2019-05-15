/*
 * Tencent is pleased to support the open source community by making wechat-matrix available.
 * Copyright (C) 2018 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.common.matrix.display;

import com.alibaba.fastjson.JSONObject;
import com.tencent.matrix.report.Issue;


public class ParseIssueUtil {

    public static String parseIssue(MyIssue issue, boolean onlyShowContent) {

        StringBuilder stringBuilder = new StringBuilder();
        if (!onlyShowContent) {
            stringBuilder.append(Issue.ISSUE_REPORT_TAG).append(" : ").append(issue.getTag()).append("\n");
            stringBuilder.append(Issue.ISSUE_REPORT_TYPE).append(" : ").append(issue.getType()).append("\n");
            stringBuilder.append("key").append(" : ").append(issue.getKey()).append("\n");
        }
        stringBuilder.append("content :").append("\n");
        return pauseJsonObj(stringBuilder, issue.getContent()).toString();
    }

    static String parseStack(String stack) {

        StringBuilder stringBuilder = new StringBuilder(" ");

        String[] lines = stack.split("\n");
        for (String line : lines) {
            String[] args = line.split(",");
            if (args.length == 4) {
                //stack层级，方法id，方法执行次数，方法执行总耗时。
                stringBuilder.append("层级:").append(args[0]).append(" ");
                int method = Integer.parseInt(args[1]);
                args[1] = MethodMapUtils.get(method);
                stringBuilder.append(args[1]).append(" ");
                stringBuilder.append("次数:").append(args[2]).append(" ");
                stringBuilder.append("耗时:").append(args[3]).append("\n");
            } else {
                stringBuilder.append(line).append("\n");
            }
        }

        return stringBuilder.toString();
    }

    public static StringBuilder pauseJsonObj(StringBuilder builder, JSONObject object) {
        for (String key : object.keySet()) {
            String val = object.getString(key);
            if ("stack".equals(key)) {
                builder.append("\t").append(key).append(" : ").append(parseStack(val)).append("\n");
            } else if("stackKey".equals(key)){
                for(String args :val.split("\\|")){
                    int method = Integer.parseInt(args);
                    String methodStr = MethodMapUtils.get(method);
                    builder.append("\t").append(key).append(" : ").append(methodStr).append("\n");
                }
            }else {
                builder.append("\t").append(key).append(" : ").append(val).append("\n");
            }
        }
        return builder;
    }

}
