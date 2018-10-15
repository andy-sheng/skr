package com.wali.live.sdk.litedemo.base.activity;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by lan on 2017/3/29.
 */
public class BaseActivity extends AppCompatActivity {
    protected final String TAG = getTAG();

    protected String getTAG() {
        return getClass().getSimpleName();
    }

    protected <V extends View> V $(int id) {
        return (V) findViewById(id);
    }
}
