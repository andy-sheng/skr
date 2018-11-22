package com.example.rxretrofit.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.common.rxretrofit.download.DownInfo;
import com.common.rxretrofit.download.DownLoadListener.HttpDownOnNextListener;
import com.common.rxretrofit.download.DownState;
import com.common.rxretrofit.download.HttpDownManager;
import com.common.utils.U;
import com.wali.live.moduletest.R;

public class RecycleHolder extends RecyclerView.ViewHolder {

    Button mBtnRxDown;
    Button mBtnRxPause;
    TextView mTvMsg;
    ProgressBar mProgressBar;

    DownInfo apkApi;

    public RecycleHolder(View itemView) {
        super(itemView);
        mBtnRxDown = itemView.findViewById(R.id.btn_rx_down);
        mBtnRxPause = itemView.findViewById(R.id.btn_rx_pause);
        mTvMsg = itemView.findViewById(R.id.tv_msg);
        mProgressBar = itemView.findViewById(R.id.progress_bar);

        mBtnRxDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (apkApi.getState() != DownState.FINISH) {
                    HttpDownManager.getInstance().startDown(apkApi);
                }

            }
        });
        mBtnRxPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HttpDownManager.getInstance().pause(apkApi);
            }
        });
    }

    public void bindData(DownInfo downInfo) {
        apkApi = downInfo;
        downInfo.setListener(httpProgressOnNextListener);
        mProgressBar.setMax((int) downInfo.getCountLength());
        mProgressBar.setProgress((int) downInfo.getReadLength());
        /*第一次恢复 */
        switch (downInfo.getState()) {
            case START:
                /*起始状态*/
                break;
            case PAUSE:
                mTvMsg.setText("暂停中");
                break;
            case DOWN:
                mTvMsg.setText("下载中");
                HttpDownManager.getInstance().startDown(downInfo);
                break;
            case STOP:
                mTvMsg.setText("下载停止");
                break;
            case ERROR:
                mTvMsg.setText("下載錯誤");
                break;
            case FINISH:
                mTvMsg.setText("下载完成");
                break;
        }
    }

    /*下载回调*/
    HttpDownOnNextListener<DownInfo> httpProgressOnNextListener = new HttpDownOnNextListener<DownInfo>() {
        @Override
        public void onNext(DownInfo baseDownEntity) {
            mTvMsg.setText("提示：下载完成/文件地址->" + baseDownEntity.getSavePath());
        }

        @Override
        public void onStart() {
            mTvMsg.setText("提示:开始下载");
        }

        @Override
        public void onComplete() {
            U.getToastUtil().showShort("提示：下载结束");
        }

        @Override
        public void onError(Throwable e) {
            super.onError(e);
            mTvMsg.setText("失败:" + e.toString());
        }


        @Override
        public void onPuase() {
            super.onPuase();
            mTvMsg.setText("提示:暂停");
        }

        @Override
        public void onStop() {
            super.onStop();
        }

        @Override
        public void updateProgress(long readLength, long countLength) {
            mTvMsg.setText("提示:下载中");
            mProgressBar.setMax((int) countLength);
            mProgressBar.setProgress((int) readLength);
        }
    };
}

