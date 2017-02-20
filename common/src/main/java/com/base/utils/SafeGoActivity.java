package com.base.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.base.global.GlobalData;
import com.base.version.VersionCheckTask;

/**
 * Created by chengsimin on 2016/12/22.
 */

public class SafeGoActivity {
    public final static String TAG = SafeGoActivity.class.getSimpleName();
    // 跳转
    public static boolean go(Context activity, Intent intent) {
        Log.d(TAG, "uri:" + intent.getDataString());
        if (intent.resolveActivity(GlobalData.app().getPackageManager()) != null) {
            try {
                activity.startActivity(intent);
            }catch (Exception e){
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    // 跳转
    public static void goCheckUpdateWhenFailed(Context activity, Intent intent) {
      if(!go(activity,intent)){
          VersionCheckTask.checkUpdate((Activity) activity);
      }
    }
}
