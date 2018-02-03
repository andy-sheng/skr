package com.wali.live.watchsdk.contest.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.base.mvp.specific.RxRelativeLayout;
import com.base.utils.toast.ToastUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.contest.manager.ContestDownloadManager;
import com.wali.live.watchsdk.contest.manager.ContestDownloadManager.State;
import com.wali.live.watchsdk.contest.manager.IContestDownloadView;
import com.wali.live.watchsdk.contest.model.AdvertisingItemInfo;
import com.wali.live.watchsdk.contest.model.DownloadItemInfo;
import com.wali.live.watchsdk.contest.presenter.ContestAdvertisingPresenter;

/**
 * Created by wanglinzhang on 2018/2/1.
 */
public class AdvertisingView extends RxRelativeLayout implements View.OnClickListener, IContestDownloadView {
    private TextView mTitleTv;
    private TextView mStatusTv;
    private BaseImageView mIconIv;

    private ProgressBar mDownloadBar;

    private AdvertisingItemInfo mModel;
    private State mState;

    private ContestDownloadManager mDownloadManager;
    private ContestAdvertisingPresenter mPresenter;
    private String mContestID;

    public AdvertisingView(Context context) {
        super(context, null);
        initView(context);
    }

    public AdvertisingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
        initView(context);
    }

    public AdvertisingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        inflate(context, R.layout.advertising_show_view, this);

        mTitleTv = $(R.id.advertising_title_tv);
        mIconIv = $(R.id.advertising_icon_iv);
        mStatusTv = $(R.id.status_tip_tv);
        mStatusTv.setOnClickListener(this);
    }

    public void init(ContestAdvertisingPresenter presenter, ContestDownloadManager manager, String contestID) {
        if (presenter == null || manager == null) {
            return;
        }
        mPresenter = presenter;
        mDownloadManager = manager;
        mContestID = contestID;
        mModel = mPresenter.getRevivalCardActInfo().get(0);
        DownloadItemInfo downloadItemInfo = new DownloadItemInfo(mModel.getDownloadUrl(), mModel.getPackageName(), mModel.getName());
        mDownloadManager.addTask(downloadItemInfo);
        mDownloadManager.initState();
        updateView();
    }

    private void updateView() {
        mTitleTv.setText(mModel.getTitle());
        FrescoWorker.loadImage(mIconIv, ImageFactory.newHttpImage(mModel.getIconUrl()).build());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.status_tip_tv) {
            if (mState == State.Idle && mModel.hasDownloadCard()) {
                mModel.setDownloadCard(false);
                mPresenter.addRevivalCardAct(1, mContestID, mModel.getPackageName());
            } else if (mState == State.InstallSuccess && mModel.hasOpenCard()) {
                mModel.setOpenCard(false);
                mPresenter.addRevivalCardAct(2, mContestID, mModel.getPackageName());
            }
            mDownloadManager.doNext();
        }
    }

    @Override
    public void processChanged(int percent) {
        if (mState == State.StartDownload) {
            mStatusTv.setText(percent + "%");
        }
    }

    @Override
    public void statusChanged(State state) {
        mState = state;
        if (mState == State.Idle) {
            if (mModel.hasDownloadCard()) {
                mStatusTv.setText(R.string.contest_advertising_download_with_card);
            } else {
                mStatusTv.setText(R.string.contest_advertising_download);
            }
        } else if (mState == State.InstallSuccess) {
            if (mModel.hasOpenCard()) {
                mStatusTv.setText(R.string.contest_advertising_open_with_card);
            } else {
                mStatusTv.setText(R.string.contest_advertising_open);
            }
        } else if (mState == State.DownloadSuccess) {
            mStatusTv.setText(R.string.contest_advertising_install);
            mDownloadManager.doNext();
        } else if (mState == State.DownloadFailed) {
            mStatusTv.setText(R.string.contest_advertising_download);
            ToastUtils.showCallToast(this.getContext(), "下载失败");
        } else if (mState == State.InstallFailed) {
            ToastUtils.showCallToast(this.getContext(), "安装失败");
        } else if (mState == State.Launch) {
            mStatusTv.setText(R.string.contest_advertising_open);
        }
    }
}
