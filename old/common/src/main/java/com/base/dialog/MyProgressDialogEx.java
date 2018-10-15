package com.base.dialog;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * Created by chengsimin on 16/3/28.
 */
public class MyProgressDialogEx {

    private com.base.dialog.MyProgressDialog mDialog;
    private Handler mMainHandler;
    private long mShowTimestamp;
    private OnCancelListener mOnCancelListener;

    public MyProgressDialogEx(Context act) {
        mDialog = new com.base.dialog.MyProgressDialog(act);
        mDialog.setCancelable(false);
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    public static MyProgressDialogEx createProgressDialog(Context act) {
        return new MyProgressDialogEx(act);
    }

    private boolean mHasCancelIntend = false;

    /**
     * 最多显示这么多毫秒
     *
     * @param most
     */
    public void show(long most) {
        if (mHasCancelIntend) {
            mHasCancelIntend = false;
            mMainHandler.removeCallbacks(mCancelRunnable);
        }

        mDialog.show();
        mShowTimestamp = System.currentTimeMillis();
        if (most > 0) {
            mMainHandler.postDelayed(mCancelRunnable, most);
            mHasCancelIntend = true;
        }
    }

    private Runnable mCancelRunnable = new Runnable() {
        @Override
        public void run() {
            if (null != mDialog && mDialog.isShowing()) {
                mDialog.cancel();
                if (mOnCancelListener != null) {
                    mOnCancelListener.onCancel();
                }
            }
        }
    };

    /**
     * 至少显示这么多毫秒
     *
     * @param least
     */
    public void hide(long least) {
        if (mHasCancelIntend) {
            mHasCancelIntend = false;
            mMainHandler.removeCallbacks(mCancelRunnable);
        }
        long left = least - (System.currentTimeMillis() - mShowTimestamp);
        if (left <= 0) {
            mCancelRunnable.run();
        } else {
            mMainHandler.postDelayed(mCancelRunnable, left);
            mHasCancelIntend = true;
        }
    }

    public void setMessage(String message) {
        mDialog.setMessage(message);
    }

    public void setOnCancelListener(OnCancelListener listener) {
        mOnCancelListener = listener;
    }

    public interface OnCancelListener {
        void onCancel();
    }
}
