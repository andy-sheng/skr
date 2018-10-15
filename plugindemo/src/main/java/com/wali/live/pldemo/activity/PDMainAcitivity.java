package com.wali.live.pldemo.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
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
import com.wali.live.pldemo.R;
import com.wali.live.pldemo.receiver.ChannelReceiver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Route(path = "/plugindemo/PDMainAcitivity")
public class PDMainAcitivity extends BaseActivity {
    CommonTitleBar mTitlebar;
    RecyclerView mListRv;
    List<H> mDataList = new ArrayList<>();

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.pldemo_main_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = (CommonTitleBar) findViewById(R.id.titlebar);
        mTitlebar.getCenterTextView().setText(getResources().getString(R.string.demo_title));

        mListRv = (RecyclerView) findViewById(R.id.list_rv);

        mListRv.setLayoutManager(new LinearLayoutManager(this));
        mListRv.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pldemo_item_tv, parent, false);
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

        mDataList.add(new H("打印 flavor 渠道和版本号", new Runnable() {
            @Override
            public void run() {

                String channelName = U.getChannelUtils().getChannelNameFromBuildConfig();
                int versionCode = U.getAppInfoUtils().getVersionCode();
                U.getToastUtil().showToast(channelName + " " + versionCode + "\n"
                        + U.app().getPackageName() + "\n"
                        + PDMainAcitivity.this.getPackageName());
            }
        }));

        mDataList.add(new H("发送广播到 ChannelReceiver", new Runnable() {
            @Override
            public void run() {
                //跳到LoginActivity,要用ARouter跳
                Intent intent = new Intent();
                intent.setAction(ChannelReceiver.ACTION);
                intent.putExtra(ChannelReceiver.DATA_KEY, "from PDMainActivity");
                LocalBroadcastManager.getInstance(PDMainAcitivity.this).sendBroadcast(intent);
            }
        }));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ChannelReceiver.ACTION);

        LocalBroadcastManager.getInstance(PDMainAcitivity.this).registerReceiver(new ChannelReceiver(), intentFilter);
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
