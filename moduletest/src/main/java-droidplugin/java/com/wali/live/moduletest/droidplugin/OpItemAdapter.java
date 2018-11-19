package com.wali.live.moduletest.droidplugin;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.common.log.MyLog;
import com.common.utils.U;
import com.morgoo.droidplugin.pm.PluginManager;
import com.morgoo.helper.Log;
import com.wali.live.moduletest.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.morgoo.helper.compat.PackageManagerCompat.INSTALL_FAILED_NOT_SUPPORT_ABI;
import static com.morgoo.helper.compat.PackageManagerCompat.INSTALL_SUCCEEDED;

public class OpItemAdapter extends RecyclerView.Adapter {

    public final static String TAG = "OpItemAdapter";

    List<PackageData> mDataList = new ArrayList<>();
    Context mContext;

    public OpItemAdapter(Context context) {
        this.mContext = context;
    }

    public void setData(List<PackageData> dataList) {
        for (PackageData data : dataList) {
            for (PackageData p : mDataList) {
                if (!TextUtils.isEmpty(data.getSdcardPath())) {
                    if (data.getSdcardPath().equals(p.getSdcardPath())) {
                        if (p.getStatus() > data.getStatus()) {
                            data.setStatus(p.getStatus());
                        }
                    }
                }
            }
        }
        mDataList.clear();
        mDataList.addAll(dataList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.test_droidplugin_sub_item, parent, false);
        ItemHolder itemHolder = new ItemHolder(view);
        itemHolder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onInstallBtnClick(PackageData data) {
                if (data.isFromInstallView()) {
                    PackageManager pm = mContext.getPackageManager();
                    Intent intent = pm.getLaunchIntentForPackage(data.getPackageName());
                    MyLog.d(TAG, "intent:" + intent);
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    } else {
                        Log.e("DroidPlugin", "pm " + pm.toString() + " no find intent " + data.getPackageName());
                    }
                } else {
                    if (data.getStatus() == PackageData.STATUS_UNINSTALL) {
                        // 未安装
                        if (!PluginManager.getInstance().isConnected()) {
                            Toast.makeText(mContext, "插件服务正在初始化，请稍后再试。。。", Toast.LENGTH_SHORT).show();
                        }
                        try {
                            if (PluginManager.getInstance().getPackageInfo(data.getPackageName(), 0) != null) {
                                Toast.makeText(mContext, "已经安装了，不能再安装", Toast.LENGTH_SHORT).show();
                            } else {
                                //安装中
                                data.setStatus(PackageData.STATUS_INSTALLING);
                                notifyDataSetChanged();
                                tryInstall(data);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            //安装中
                            data.setStatus(PackageData.STATUS_INSTALLING);
                            notifyDataSetChanged();
                            tryInstall(data);
                        }
                    } else if (data.getStatus() == PackageData.STATUS_INSTALLING) {
                        Toast.makeText(mContext, "安装中", Toast.LENGTH_SHORT).show();
                    } else if (data.getStatus() == PackageData.STATUS_INSTALLED) {
                        Toast.makeText(mContext, "已安装", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onDeleteBtnClick(PackageData data) {
                if (data.getStatus() == PackageData.STATUS_UNINSTALL
                        || data.getStatus() == PackageData.STATUS_INSTALLING) {
                    File file = new File(data.getSdcardPath());
                    if (file.exists()) {
                        file.delete();
                        mDataList.remove(data);
                        notifyDataSetChanged();
                    }
                } else if (data.getStatus() == PackageData.STATUS_INSTALLED) {
                    // 已安装
                    if (!PluginManager.getInstance().isConnected()) {
                        Toast.makeText(mContext, "服务未连接", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            PluginManager.getInstance().deletePackage(data.getPackageName(), 0);
                            Toast.makeText(mContext, "卸载完成", Toast.LENGTH_SHORT).show();
                            data.setStatus(PackageData.STATUS_UNINSTALL);
                            notifyDataSetChanged();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        return itemHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PackageData data = mDataList.get(position);
        if (holder instanceof ItemHolder) {
            ItemHolder itemHolder = (ItemHolder) holder;
            itemHolder.bind(data);
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    void tryInstall(PackageData data) {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) {
                try {
                    final int re = PluginManager.getInstance().installPackage(data.getSdcardPath(), 0);
                    emitter.onNext(re);
                    data.setStatus(PackageData.STATUS_INSTALLED);
                } catch (RemoteException e) {
                    emitter.onNext(-99);
                    data.setStatus(PackageData.STATUS_UNINSTALL);
                }
                emitter.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer re) {
                        switch (re) {
                            case PluginManager.INSTALL_FAILED_NO_REQUESTEDPERMISSION:
                                U.getToastUtil().showShort("安装失败，文件请求的权限太多");
                                data.setStatus(PackageData.STATUS_UNINSTALL);
                                break;
                            case INSTALL_FAILED_NOT_SUPPORT_ABI:
                                U.getToastUtil().showShort("宿主不支持插件的abi环境，可能宿主运行时为64位，但插件只支持32位");
                                data.setStatus(PackageData.STATUS_UNINSTALL);
                                break;
                            case INSTALL_SUCCEEDED:
                                U.getToastUtil().showShort("安装完成");
                                data.setStatus(PackageData.STATUS_INSTALLED);
                                break;
                            case -99:
                                U.getToastUtil().showShort("binder异常");
                                data.setStatus(PackageData.STATUS_UNINSTALL);
                                break;
                        }
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    static class ItemHolder extends RecyclerView.ViewHolder {

        TextView mInfoTv;
        TextView mInstallTv;
        TextView mDeleteTv;
        PackageData mData;

        ItemClickListener mItemClickListener;

        public ItemHolder(View itemView) {
            super(itemView);
            mInfoTv = (TextView) itemView.findViewById(R.id.info_tv);
            mInstallTv = (TextView) itemView.findViewById(R.id.install_tv);
            mDeleteTv = (TextView) itemView.findViewById(R.id.delete_tv);
            mDeleteTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onDeleteBtnClick(mData);
                    }
                }
            });
            mInstallTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onInstallBtnClick(mData);
                    }
                }
            });
        }

        public void setItemClickListener(ItemClickListener listener) {
            this.mItemClickListener = listener;
        }

        public void bind(PackageData data) {
            mData = data;
            mInfoTv.setText(data.toString());
            if (data.isFromInstallView()) {
                mDeleteTv.setVisibility(View.GONE);
                mInstallTv.setText("打开");
            } else {
                mDeleteTv.setVisibility(View.VISIBLE);
                if (mData.getStatus() == PackageData.STATUS_UNINSTALL) {
                    mInstallTv.setText("未安装");
                    mDeleteTv.setText("删除");
                } else if (mData.getStatus() == PackageData.STATUS_INSTALLING) {
                    mInstallTv.setText("安装中");
                    mDeleteTv.setText("删除");
                } else if (mData.getStatus() == PackageData.STATUS_INSTALLED) {
                    // 已安装
                    mInstallTv.setText("已安装");
                    mDeleteTv.setText("卸载");
                }
            }

        }
    }

    interface ItemClickListener {
        public void onInstallBtnClick(PackageData data);

        public void onDeleteBtnClick(PackageData data);
    }
}
