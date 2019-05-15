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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.base.BaseActivity;
import com.common.base.R;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.titlebar.CommonTitleBar;
import com.tencent.matrix.report.Issue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class IssuesListActivity extends BaseActivity {

    RecyclerView mRecyclerView;

    IssuesAdapter mIssuesAdapter;

    CommonTitleBar mTitleBar;



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        load();
    }

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.activity_issue_list;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mIssuesAdapter = new IssuesAdapter();
        mRecyclerView.setAdapter(mIssuesAdapter);

        mTitleBar = (CommonTitleBar)findViewById(R.id.title_bar);
        mTitleBar.getRightTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFileUtils().deleteAllFiles(U.getAppInfoUtils().getSubDirPath("Matrix"));
                load();
            }
        });
        load();
    }

    void load(){
        Observable.create(new ObservableOnSubscribe<List<MyIssue>>() {

            @Override
            public void subscribe(ObservableEmitter<List<MyIssue>> emitter) throws Exception {
                MethodMapUtils.loadMethod();
                List<MyIssue> list = new ArrayList<>();
                File dir = U.getAppInfoUtils().getSubDirFile("Matrix");
                final File[] listFiles = dir.listFiles();
                Arrays.sort(listFiles, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        if (f1.lastModified() > f2.lastModified()) {
                            return -1;
                        } else if (f1.lastModified() < f2.lastModified()) {
                            return 1;
                        }
                        return 0;
                    }
                });
                for(File file :listFiles){
                    MyIssue issue = load(file);
                    if (issue != null) {
                        list.add(issue);
                    }
                }
                emitter.onNext(list);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<MyIssue>>() {
                    @Override
                    public void accept(List<MyIssue> issues) throws Exception {
                        mIssuesAdapter.setDataList(issues);
                    }
                });
    }

    MyIssue load(File file){
        try {
            FileInputStream fin = new FileInputStream(file);
            if (fin != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(fin);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                inputStreamReader.close();

                JSONObject  jsonObject = ((JSONObject)JSON.parse(stringBuilder.toString()));
                MyIssue issue = new MyIssue();
                JSONObject content = jsonObject.getJSONObject("content");
                issue.setContent(content);
                issue.setKey(jsonObject.getString("key"));
                issue.setTag(jsonObject.getString("tag"));
                issue.setType(jsonObject.getIntValue("type"));
                return issue;
            }
        } catch (Exception e) {
            MyLog.e(e);
        }
        return null;
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
