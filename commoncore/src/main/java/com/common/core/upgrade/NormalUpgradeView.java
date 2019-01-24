package com.common.core.upgrade;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.common.core.R;
import com.common.view.ex.ExTextView;

public class NormalUpgradeView extends RelativeLayout {
    ExTextView mSizeTipsTv;
    LinearLayout mOpContainer;
    ExTextView mQuitBtn;
    ExTextView mUpdateBtn;
    ExTextView mVersionTipsTv;

    RelativeLayout mDownloadContainer;
    DownloadApkProgressBar mDownloadApkProgressbar;
    ExTextView mCancelBtn;

    Listener mListener;

    public NormalUpgradeView(Context context) {
        super(context);
        init();
    }

    public NormalUpgradeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NormalUpgradeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        inflate(getContext(), R.layout.normal_upgrade_view_layout, this);

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
        mVersionTipsTv.setText(upgradeInfoModel.getVersionName());
        mSizeTipsTv.setText("更新包"+upgradeInfoModel.getPackageSizeStr()+",建议在wifi环境下载更新");
        mUpdateBtn.setText("更新");
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
