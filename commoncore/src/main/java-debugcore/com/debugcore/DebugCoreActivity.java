package com.debugcore;

import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.base.BuildConfig;
import com.common.core.R;
import com.common.log.MyLog;
import com.common.statistics.TimeStatistics;
import com.common.utils.U;
import com.common.view.titlebar.CommonTitleBar;
import com.module.RouterConstants;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ListHolder;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import io.agora.rtc.RtcEngine;

@Route(path = RouterConstants.ACTIVITY_DEBUG_CORE_ACTIVITY)
public class DebugCoreActivity extends BaseActivity {

    CommonTitleBar mTitlebar;
    RecyclerView mListRv;
    RecyclerView.Adapter mAdapter;
    List<DebugData> mDataList = new ArrayList<>();

    DialogPlus mShowMsgDialog;
    DialogPlus mChannelListDialog;

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.debug_core_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = (CommonTitleBar) this.findViewById(R.id.titlebar);
        mTitlebar.getLeftImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mListRv = (RecyclerView) this.findViewById(R.id.list_rv);
        mListRv.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.debug_item_tv, parent, false);
                DebugViewHolder testHolder = new DebugViewHolder(view);
                return testHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                if (holder instanceof DebugViewHolder) {
                    DebugViewHolder testHolder = (DebugViewHolder) holder;
                    testHolder.bindData(mDataList.get(position));
                }
            }

            @Override
            public int getItemCount() {
                return mDataList.size();
            }
        };
        mListRv.setAdapter(mAdapter);
        loadData();
    }

    private void loadData() {
        mDataList.clear();
        mDataList.add(new DebugData("基本信息", new Runnable() {
            @Override
            public void run() {

                StringBuilder sb = new StringBuilder();
                sb.append("数据库调试地址:").append(U.getAppInfoUtils().getDebugDBAddressLog()).append("\n");
                sb.append("屏宽:").append(U.getDisplayUtils().getScreenWidth())
                        .append(" 屏高:").append(U.getDisplayUtils().getScreenHeight())
                        .append(" 手机高:").append(U.getDisplayUtils().getPhoneHeight())
                        .append(" density:").append(U.getDisplayUtils().getDensity())
                        .append(" densityDpi:").append(U.getDisplayUtils().getDensityDpi())
                        .append("\n");
                sb.append("是否开启了虚拟导航键：").append(U.getDeviceUtils().hasNavigationBar()).append(" 虚拟导航键高度:")
                        .append(U.getKeyBoardUtils().getVirtualNavBarHeight())
                        .append("\n");
                sb.append("最小宽度为 px/(dpi/160)=").append((U.getDisplayUtils().getPhoneWidth() / (U.getDisplayUtils().getDensityDpi() / 160))).append("dp").append("\n");
                sb.append("当前手机适用的资源文件夹是").append(U.app().getResources().getString(R.string.values_from)).append("\n");
                sb.append("android.os.Build.VERSION.SDK_INT:").append(android.os.Build.VERSION.SDK_INT).append("\n");
                sb.append("手机型号:").append(U.getDeviceUtils().getProductModel()).append("\n");
                sb.append("手机厂商:").append(U.getDeviceUtils().getProductBrand()).append("\n");
                sb.append("渠道号:").append(U.getChannelUtils().getChannel()).append(" debug:").append(BuildConfig.DEBUG).append("\n");
                sb.append("日志标记:").append("isDebugLogOpen:").append(MyLog.isDebugLogOpen())
                        .append(" MyLog.getForceOpenFlag:").append(MyLog.getForceOpenFlag())
                        .append(" MyLog.getCurrentLogLevel:").append(MyLog.getCurrentLogLevel())
                        .append("\n");
                sb.append("deviceId(参考miui唯一设备号的方法):").append(U.getDeviceUtils().getDeviceID()).append("\n");
                sb.append("是否开启了打印方法执行时间: switch:").append(TimeStatistics.getSwitch()).append(" dt:").append(TimeStatistics.sDt).append("\n");
                sb.append("agora sdk version:").append(RtcEngine.getSdkVersion()).append("\n");
                sb.append("是否插着有线耳机:").append(U.getDeviceUtils().getWiredHeadsetPlugOn()).append("\n");
                sb.append("是否插着蓝牙耳机:").append(U.getDeviceUtils().getBlueToothHeadsetOn()).append("\n");

                showMessage(sb.toString());
            }
        }));

        mDataList.add(new DebugData("修改强制调试标记位" + MyLog.getForceOpenFlag(), new Runnable() {
            @Override
            public void run() {
                if (MyLog.getForceOpenFlag()) {
                    MyLog.setForceOpenFlag(false);
                    U.getToastUtil().showShort("MyLog.sForceOpenFlag 变为false");
                    loadData();
                } else {
                    MyLog.setForceOpenFlag(true);
                    U.getToastUtil().showShort("MyLog.sForceOpenFlag 变为true");
                    loadData();
                }
            }
        }));

        mDataList.add(new DebugData("修改渠道，当前为" + U.getChannelUtils().getChannel(), new Runnable() {
            @Override
            public void run() {
                final List<String> channels = new ArrayList<>();
                channels.add("TEST");
                channels.add("DEV");
                channels.add("SANDBOX");
                channels.add("DEFAULT");
                List<DebugData> debugDataList = new ArrayList<>();
                for (final String channel : channels) {
                    debugDataList.add(new DebugData(channel, new Runnable() {
                        @Override
                        public void run() {
                            U.getChannelUtils().setChannelNameFromBuildConfig(channel);
                            U.getToastUtil().showShort("请重启app");
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Process.killProcess(Process.myPid());
                                }
                            }, 1000);

                            if (mChannelListDialog != null) {
                                mChannelListDialog.dismiss();
                            }
                            loadData();
                        }
                    }));
                }
                showList(debugDataList);
            }
        }));

        mAdapter.notifyDataSetChanged();
    }

    private void showMessage(String msg) {
        if (mShowMsgDialog != null) {
            mShowMsgDialog.dismiss();
        }
        ShowMsgView tipsDialogView = new ShowMsgView(this);
        tipsDialogView.bindData(msg);
        mShowMsgDialog = DialogPlus.newDialog(this)
                .setContentHolder(new ViewHolder(tipsDialogView))
                .setGravity(Gravity.CENTER)
                .setCancelable(true)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setExpanded(false)
                .create();
        mShowMsgDialog.show();
    }

    private void showList(final List<DebugData> list) {
        BaseAdapter baseAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public Object getItem(int position) {
                return list.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = null;
                if (convertView == null) {
                    v = LayoutInflater.from(DebugCoreActivity.this).inflate(
                            R.layout.debug_item_tv, parent, false);
                    DebugViewHolder vh = new DebugViewHolder(v);
                    vh.titleTv.setTextColor(U.getColor(R.color.green));
                    v.setTag(vh);
                } else {
                    v = convertView;
                }
                DebugViewHolder vh = (DebugViewHolder) v.getTag();
                DebugData debugData = list.get(position);
                vh.bindData(debugData);
                return v;
            }
        };
        mChannelListDialog = DialogPlus.newDialog(DebugCoreActivity.this)
                .setContentHolder(new ListHolder())
                .setAdapter(baseAdapter)
                .setGravity(Gravity.CENTER)
                .setCancelable(true)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setExpanded(false)
                .create();
        mChannelListDialog.show();
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
