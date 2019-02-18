package com.didichuxing.doraemonkit.kit.sysinfo;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.base.BuildConfig;
import com.common.log.MyLog;
import com.common.utils.U;
import com.didichuxing.doraemonkit.DoraemonKit;
import com.didichuxing.doraemonkit.R;
import com.didichuxing.doraemonkit.ui.base.BaseFragment;
import com.dialog.list.DialogListItem;
import com.dialog.list.ListDialog;
import com.didichuxing.doraemonkit.ui.widget.recyclerview.DividerItemDecoration;
import com.didichuxing.doraemonkit.ui.widget.titlebar.HomeTitleBar;
import com.didichuxing.doraemonkit.util.DeviceUtils;
import com.didichuxing.doraemonkit.util.ExecutorUtil;
import com.didichuxing.doraemonkit.util.PermissionUtil;
import com.didichuxing.doraemonkit.util.UIUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 手机app信息
 * Created by zhangweida on 2018/6/25.
 */

public class SysInfoFragment extends BaseFragment {
    private RecyclerView mInfoList;
    private SysInfoItemAdapter mInfoItemAdapter;

    @Override
    protected int onRequestLayout() {
        return R.layout.dk_fragment_sys_info;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initData();
    }

    private void initView() {
        mInfoList = findViewById(R.id.info_list);
        HomeTitleBar titleBar = findViewById(R.id.title_bar);
        titleBar.setListener(new HomeTitleBar.OnTitleBarClickListener() {
            @Override
            public void onRightClick() {
                getActivity().finish();
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mInfoList.setLayoutManager(layoutManager);
        mInfoItemAdapter = new SysInfoItemAdapter(getContext());
        mInfoList.setAdapter(mInfoItemAdapter);
        DividerItemDecoration decoration = new DividerItemDecoration(DividerItemDecoration.VERTICAL);
        decoration.setDrawable(getResources().getDrawable(R.drawable.dk_divider));
        mInfoList.addItemDecoration(decoration);
    }

    private void initData() {
        List<SysInfoItem> sysInfoItems = new ArrayList<>();
        addAppData(sysInfoItems);
        addDeviceData(sysInfoItems);
        if (getContext().getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.M) {
            addPermissionData(sysInfoItems);
        } else {
            addPermissionDataUnreliable();
        }
        mInfoItemAdapter.setData(sysInfoItems);
    }

    private void addAppData(List<SysInfoItem> sysInfoItems) {
        PackageInfo pi = DeviceUtils.getPackageInfo(getContext());
        sysInfoItems.add(new TitleItem(getString(R.string.dk_sysinfo_app_info)));
        sysInfoItems.add(new SysInfoItem(getString(R.string.dk_sysinfo_package_name), pi.packageName));
        sysInfoItems.add(new SysInfoItem("渠道号(点击切换)", U.getChannelUtils().getChannel(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<String> channels = new ArrayList<>();
                channels.add("TEST");
                channels.add("DEV");
                channels.add("SANDBOX");
                channels.add("DEFAULT");
                List<DialogListItem> listItems = new ArrayList<>();
                for (final String channel : channels) {
                    listItems.add(new DialogListItem(channel, new Runnable() {
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
                        }
                    }));
                }
                ListDialog listDialog = new ListDialog(getContext());
                listDialog.showList(listItems);
            }
        }));
        sysInfoItems.add(new SysInfoItem(getString(R.string.dk_sysinfo_package_version_name), pi.versionName));
        sysInfoItems.add(new SysInfoItem(getString(R.string.dk_sysinfo_package_version_code), String.valueOf(pi.versionCode)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sysInfoItems.add(new SysInfoItem(getString(R.string.dk_sysinfo_package_min_sdk), String.valueOf(getContext().getApplicationInfo().minSdkVersion)));
        }
        sysInfoItems.add(new SysInfoItem(getString(R.string.dk_sysinfo_package_target_sdk), String.valueOf(getContext().getApplicationInfo().targetSdkVersion)));
        sysInfoItems.add(new SysInfoItem("BuildConfig.DEBUG", "" + BuildConfig.DEBUG));
        sysInfoItems.add(new SysInfoItem("MyLog.sForceOpenFlag", "" + MyLog.getForceOpenFlag()));
        sysInfoItems.add(new SysInfoItem("数据库调试地址", U.getAppInfoUtils().getDebugDBAddressLog()));
        sysInfoItems.add(new SysInfoItem("deviceId(参考miui唯一设备号的方法)", U.getDeviceUtils().getDeviceID()));

        ExtraInfoProvider extraInfoProvider = DoraemonKit.getExtraInfoProvider();
        if (extraInfoProvider != null) {
            List<SysInfoItem> extras = extraInfoProvider.getExtraInfo();
            for(SysInfoItem sysInfoItem:extras){
                sysInfoItems.add(sysInfoItem);
            }
        }
    }

    private void addDeviceData(List<SysInfoItem> sysInfoItems) {
        sysInfoItems.add(new TitleItem(getString(R.string.dk_sysinfo_device_info)));
        sysInfoItems.add(new SysInfoItem(getString(R.string.dk_sysinfo_brand_and_model), Build.MANUFACTURER + " " + Build.MODEL));
        sysInfoItems.add(new SysInfoItem(getString(R.string.dk_sysinfo_android_version), Build.VERSION.RELEASE + " (" + Build.VERSION.SDK_INT + ")"));
        sysInfoItems.add(new SysInfoItem(getString(R.string.dk_sysinfo_ext_storage_free), DeviceUtils.getSDCardSpace(getContext())));
        sysInfoItems.add(new SysInfoItem(getString(R.string.dk_sysinfo_rom_free), DeviceUtils.getRomSpace(getContext())));
        sysInfoItems.add(new SysInfoItem("ROOT", String.valueOf(DeviceUtils.isRoot(getContext()))));
        sysInfoItems.add(new SysInfoItem("DENSITY", String.valueOf(UIUtils.getDensity(getContext()))));
        sysInfoItems.add(new SysInfoItem(getString(R.string.dk_sysinfo_display_size), UIUtils.getWidthPixels(getContext()) + "x" + UIUtils.getRealHeightPixels(getContext())));
    }

    /**
     * 不可靠的检测权限方式
     */
    private void addPermissionDataUnreliable() {
        ExecutorUtil.execute(new Runnable() {
            @Override
            public void run() {
                final List<SysInfoItem> list = new ArrayList<>();
                list.add(new TitleItem(getString(R.string.dk_sysinfo_permission_info_unreliable)));
                list.add(new SysInfoItem(getString(R.string.dk_sysinfo_permission_location), PermissionUtil.checkLocationUnreliable(getContext()) ? "YES" : "NO"));
                list.add(new SysInfoItem(getString(R.string.dk_sysinfo_permission_sdcard), PermissionUtil.checkStorageUnreliable() ? "YES" : "NO"));
                list.add(new SysInfoItem(getString(R.string.dk_sysinfo_permission_camera), PermissionUtil.checkCameraUnreliable() ? "YES" : "NO"));
                list.add(new SysInfoItem(getString(R.string.dk_sysinfo_permission_record), PermissionUtil.checkRecordUnreliable() ? "YES" : "NO"));
                list.add(new SysInfoItem(getString(R.string.dk_sysinfo_permission_read_phone), PermissionUtil.checkReadPhoneUnreliable(getContext()) ? "YES" : "NO"));
                list.add(new SysInfoItem(getString(R.string.dk_sysinfo_permission_contact), PermissionUtil.checkReadContactUnreliable(getContext()) ? "YES" : "NO"));
                getView().post(new Runnable() {
                    @Override
                    public void run() {
                        if (SysInfoFragment.this.isDetached()) {
                            return;
                        }
                        mInfoItemAdapter.append(list);
                    }
                });
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void addPermissionData(List<SysInfoItem> sysInfoItems) {
        sysInfoItems.add(new TitleItem(getString(R.string.dk_sysinfo_permission_info)));
        String[] p1 = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        sysInfoItems.add(new SysInfoItem(getString(R.string.dk_sysinfo_permission_location), checkPermission(p1)));
        String[] p2 = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        sysInfoItems.add(new SysInfoItem(getString(R.string.dk_sysinfo_permission_sdcard), checkPermission(p2)));
        String[] p3 = {
                Manifest.permission.CAMERA
        };
        sysInfoItems.add(new SysInfoItem(getString(R.string.dk_sysinfo_permission_camera), checkPermission(p3)));
        String[] p4 = {
                Manifest.permission.RECORD_AUDIO
        };
        sysInfoItems.add(new SysInfoItem(getString(R.string.dk_sysinfo_permission_record), checkPermission(p4)));
        String[] p5 = {
                Manifest.permission.READ_PHONE_STATE
        };
        sysInfoItems.add(new SysInfoItem(getString(R.string.dk_sysinfo_permission_read_phone), checkPermission(p5)));
        String[] p6 = {
                Manifest.permission.READ_CONTACTS
        };
        sysInfoItems.add(new SysInfoItem(getString(R.string.dk_sysinfo_permission_contact), checkPermission(p6)));
    }

    private String checkPermission(String... perms) {
        return PermissionUtil.hasPermissions(getContext(), perms) ? "YES" : "NO";
    }

}
