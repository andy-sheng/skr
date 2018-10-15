package com.wali.live.moduletest.replugin;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.common.log.MyLog;
import com.common.utils.U;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.model.PluginInfo;
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
                    Intent repluginIntent = RePlugin.createIntent(data.getPackageName(), "com.wali.live.pldemo.activity.PDMainAcitivity");
                    MyLog.d(TAG, "intent:" + repluginIntent);
                    if (repluginIntent != null) {
                        RePlugin.startActivity(mContext, repluginIntent);
                    }
                } else {
                    if (data.getStatus() == PackageData.STATUS_UNINSTALL) {
                        tryInstall(data);
                    } else if (data.getStatus() == PackageData.STATUS_INSTALLING) {
                        Toast.makeText(mContext, "安装中", Toast.LENGTH_SHORT).show();
                    } else if (data.getStatus() == PackageData.STATUS_INSTALLED) {
                        if (data.getVersionCode() >= data.getOldVersionCode()) {
                            tryInstall(data);
                        } else {
                            Toast.makeText(mContext, "已安装", Toast.LENGTH_SHORT).show();
                        }
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
                    RePlugin.uninstall(data.getPackageName());
                    Toast.makeText(mContext, "卸载完成", Toast.LENGTH_SHORT).show();
                    data.setStatus(PackageData.STATUS_UNINSTALL);
                    notifyDataSetChanged();
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
        // 未安装
        if (!new File(data.getSdcardPath()).exists()) {
            U.getToastUtil().showToast("文件不存在");
            return;
        }
        data.setStatus(PackageData.STATUS_INSTALLING);
        notifyDataSetChanged();
        Observable.create(new ObservableOnSubscribe<PluginInfo>() {
            @Override
            public void subscribe(ObservableEmitter<PluginInfo> emitter) {
                PluginInfo re = RePlugin.install(data.getSdcardPath());
                if (re != null) {
                    data.setStatus(PackageData.STATUS_INSTALLED);
                    data.setOldVersionCode(data.getVersionCode());
                } else {
                    data.setStatus(PackageData.STATUS_UNINSTALL);
                }
                emitter.onNext(re);
                emitter.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<PluginInfo>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(PluginInfo re) {
                        if (re != null) {
                            U.getToastUtil().showToast("安装完成");
                        } else {
                            U.getToastUtil().showToast("安装失败");
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
                mDeleteTv.setText("卸载");
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
                    if (mData.getVersionCode() >= mData.getOldVersionCode()) {
                        mInstallTv.setText("更新/覆盖");
                    } else {
                        mInstallTv.setText("已安装");
                    }
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
