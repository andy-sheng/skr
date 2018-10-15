package com.wali.live.moduletest.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.callback.NavigationCallback;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.titlebar.CommonTitleBar;
import com.wali.live.moduletest.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Route(path = "/test/TestSdkActivity")
public class TestSdkActivity extends BaseActivity {
    CommonTitleBar mTitlebar;
    RecyclerView mListRv;
    List<H> mDataList = new ArrayList<>();

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.test_main_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = (CommonTitleBar) findViewById(R.id.titlebar);
        mListRv = (RecyclerView) findViewById(R.id.list_rv);

        mListRv.setLayoutManager(new LinearLayoutManager(this));
        mListRv.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.test_item_tv, parent, false);
                TestHolder testHolder = new TestHolder(view);
                return testHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                if (holder instanceof TestHolder) {
                    TestHolder testHolder = (TestHolder) holder;
                    testHolder.bindData(mDataList.get(position));
                }
            }

            @Override
            public int getItemCount() {
                return mDataList.size();
            }
        });

        mDataList.add(new H("插件间跳转测试", new Runnable() {
            @Override
            public void run() {
                //跳到LoginActivity,要用ARouter跳
                ARouter.getInstance().build("/channel/ChannelSdkActivity").navigation(TestSdkActivity.this, new NavigationCallback() {
                    @Override
                    public void onFound(Postcard postcard) {
                        MyLog.d(TAG, "onFound" + " postcard=" + postcard);
                    }

                    @Override
                    public void onLost(Postcard postcard) {
                        MyLog.d(TAG, "onLost" + " postcard=" + postcard);
                    }

                    @Override
                    public void onArrival(Postcard postcard) {
                        MyLog.d(TAG, "onArrival" + " postcard=" + postcard);
                    }

                    @Override
                    public void onInterrupt(Postcard postcard) {
                        MyLog.d(TAG, "onInterrupt" + " postcard=" + postcard);
                    }
                });
            }
        }));


        mDataList.add(new H("VirtualApk load 测试", new Runnable() {
            @Override
            public void run() {
                String pluginPath = Environment.getExternalStorageDirectory().getAbsolutePath().concat("/Test.apk");
                File plugin = new File(pluginPath);
//                try {
//                    // load 会导致 Applicaiton 加载两次，看原理
//                    com.didi.virtualapk.PluginManager.PluginManager.getInstance(U.app()).loadPlugin(plugin);
//                    U.getToastUtil().showToast("load 成功");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    U.getToastUtil().showToast("load 失败");
//                }
            }
        }));

        mDataList.add(new H("VirtualApk 跳转 测试", new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setClassName("com.wali.live.pldemo", "com.wali.live.pldemo.activity.PDMainAcitivity");
                startActivity(intent);
            }
        }));

        mDataList.add(new H("DroidPlugin 专项调试", new Runnable() {
            @Override
            public void run() {
                ARouter.getInstance().build("/test/DroidPluginTestAcitivity").greenChannel().navigation(TestSdkActivity.this, new NavigationCallback() {
                    @Override
                    public void onFound(Postcard postcard) {

                    }

                    @Override
                    public void onLost(Postcard postcard) {
                        U.getToastUtil().showToast("请确认 gradle.properties 中 droidpluginEnable 的开关是否打开");
                    }

                    @Override
                    public void onArrival(Postcard postcard) {

                    }

                    @Override
                    public void onInterrupt(Postcard postcard) {

                    }
                });
            }
        }));

        mDataList.add(new H("Replugin 专项调试", new Runnable() {
            @Override
            public void run() {
                ARouter.getInstance().build("/test/RepluginTestAcitivity").greenChannel().navigation(TestSdkActivity.this, new NavigationCallback() {
                    @Override
                    public void onFound(Postcard postcard) {

                    }

                    @Override
                    public void onLost(Postcard postcard) {
                        U.getToastUtil().showToast("请确认 gradle.properties 中 repluginEnable 的开关是否打开");
                    }

                    @Override
                    public void onArrival(Postcard postcard) {

                    }

                    @Override
                    public void onInterrupt(Postcard postcard) {

                    }
                });
            }
        }));
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    static class H {
        public String title;
        public Runnable op;

        public H(String title, Runnable op) {
            this.title = title;
            this.op = op;
        }
    }

    static class TestHolder extends RecyclerView.ViewHolder {

        TextView titleTv;
        H data;

        public TestHolder(View itemView) {
            super(itemView);

            titleTv = (TextView) itemView.findViewById(R.id.desc_tv);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (data != null) {
                        data.op.run();
                    }
                }
            });
        }

        public void bindData(H data) {
            this.data = data;
            titleTv.setText(data.title);
        }
    }
}
