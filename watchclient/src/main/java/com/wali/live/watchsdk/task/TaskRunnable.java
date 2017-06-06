package com.wali.live.watchsdk.task;

import android.os.Handler;

/**
 * Created by lan on 16-3-18.
 */
public abstract class TaskRunnable implements Runnable {
    private Handler mHandler = new Handler();

    @Override
    public void run() {
        final Boolean result = doInBackground();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onPostExecute(result);
            }
        });
    }

    protected abstract Boolean doInBackground(Void... params);

    protected abstract void onPostExecute(Boolean result);
}
