//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.rong.imkit.manager.AudioPlayManager;
import io.rong.imkit.manager.AudioRecordManager;

public class RongKitReceiver extends BroadcastReceiver {
  public RongKitReceiver() {
  }

  public void onReceive(Context context, Intent intent) {
    if (intent != null && intent.getAction() != null && !intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
      AudioPlayManager.getInstance().stopPlay();
      AudioRecordManager.getInstance().destroyRecord();
    }

  }
}
