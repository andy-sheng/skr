package com.common.core.upgrade;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.common.core.R;
import com.common.utils.U;
import com.common.view.ex.ExTextView;

public class ForceUpgradeView extends RelativeLayout {
    ExTextView mContentTv;
    ExTextView mSizeTipsTv;
    LinearLayout mOpContainer;
    ExTextView mQuitBtn;
    ExTextView mUpdateBtn;
    ExTextView mVersionTipsTv;

    RelativeLayout mDownloadContainer;
    DownloadApkProgressBar mDownloadApkProgressbar;
    ExTextView mCancelBtn;

    ExTextView mOldVersionTv;
    ExTextView mNewVersionTv;

    Listener mListener;

    public ForceUpgradeView(Context context) {
        super(context);
        init();
    }

    public ForceUpgradeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ForceUpgradeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        inflate(getContext(), R.layout.force_upgrade_view_layout, this);

        mContentTv = (ExTextView)this.findViewById(R.id.content_tv);
        mVersionTipsTv = this.findViewById(R.id.version_tips_tv);
        mSizeTipsTv = (ExTextView)this.findViewById(R.id.size_tips_tv);
        mOpContainer = (LinearLayout)this.findViewById(R.id.op_container);
        mQuitBtn = (ExTextView)this.findViewById(R.id.quit_btn);
        mUpdateBtn = (ExTextView)this.findViewById(R.id.update_btn);
        mUpdateBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onUpdateBtnClick();
                }
            }
        });
        mQuitBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onQuitBtnClick();
                }
            }
        });
        mDownloadContainer = (RelativeLayout)this.findViewById(R.id.download_container);
        mDownloadApkProgressbar = (DownloadApkProgressBar)this.findViewById(R.id.download_apk_progressbar);
        mCancelBtn = (ExTextView)this.findViewById(R.id.cancel_btn);
        mCancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onCancelBtnClick();
                }
                mOpContainer.setVisibility(VISIBLE);
                mDownloadContainer.setVisibility(GONE);
            }
        });
        mOldVersionTv = (ExTextView)this.findViewById(R.id.old_version_tv);
        mNewVersionTv = (ExTextView)this.findViewById(R.id.new_version_tv);
    }


    void setListener(Listener l){
        mListener = l;
    }

    public void updateProgress(int arg1) {
        mOpContainer.setVisibility(GONE);
        mDownloadContainer.setVisibility(VISIBLE);
        mDownloadApkProgressbar.setProgress(arg1);
    }

    public void bindData(UpgradeInfoModel upgradeInfoModel) {
        mOpContainer.setVisibility(VISIBLE);
        mDownloadContainer.setVisibility(GONE);
        mContentTv.setText(upgradeInfoModel.getUpdateContent());
        mVersionTipsTv.setText(upgradeInfoModel.getVersionName());
        mSizeTipsTv.setText("更新包"+upgradeInfoModel.getPackageSizeStr()+",建议在wifi环境下载更新");
        mUpdateBtn.setText("更新");
        mOldVersionTv.setText("v"+U.getAppInfoUtils().getVersionName());
        mNewVersionTv.setText("v"+upgradeInfoModel.getVersionName());
    }

    public void setAlreadyDownloadTips() {
        mUpdateBtn.setText("安装");
        mSizeTipsTv.setText("更新包已在wifi环境下加载完毕");
    }

    public interface Listener{
        void onUpdateBtnClick();
        void onQuitBtnClick();
        void onCancelBtnClick();
    }
}
