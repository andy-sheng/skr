package com.wali.live.watchsdk.task;

import android.os.Handler;

import com.base.global.GlobalData;

/**
 * Created by lan on 16-3-18.
 */
public abstract class TaskRunnable implements Runnable {

    Handler handler = new Handler();

    @Override
    public void run() {
        final Boolean result = doInBackground();
        handler.post(new Runnable(){
            @Override
            public void run() {
                onPostExecute(result);
            }
        });
    }

    protected abstract Boolean doInBackground(Void... params);

    protected abstract void onPostExecute(Boolean result);
}
