//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.eventbus.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import io.rong.eventbus.EventBus;
import io.rong.imkit.R;

public class ErrorDialogFragments {
  public static int ERROR_DIALOG_ICON = 0;
  public static Class<?> EVENT_TYPE_ON_CLICK;

  public ErrorDialogFragments() {
  }

  public static Dialog createDialog(Context context, Bundle arguments, OnClickListener onClickListener) {
    Builder builder = new Builder(context);
    builder.setTitle(arguments.getString("de.greenrobot.eventbus.errordialog.title"));
    builder.setMessage(arguments.getString("de.greenrobot.eventbus.errordialog.message"));
    if (ERROR_DIALOG_ICON != 0) {
      builder.setIcon(ERROR_DIALOG_ICON);
    }

    builder.setPositiveButton(R.string.rc_confirm, onClickListener);
    return builder.create();
  }

  public static void handleOnClick(DialogInterface dialog, int which, Activity activity, Bundle arguments) {
    if (EVENT_TYPE_ON_CLICK != null) {
      Object event;
      try {
        event = EVENT_TYPE_ON_CLICK.newInstance();
      } catch (Exception var6) {
        throw new RuntimeException("Event cannot be constructed", var6);
      }

      EventBus eventBus = ErrorDialogManager.factory.config.getEventBus();
      eventBus.post(event);
    }

    boolean finish = arguments.getBoolean("de.greenrobot.eventbus.errordialog.finish_after_dialog", false);
    if (finish && activity != null) {
      activity.finish();
    }

  }

  public static class Support extends DialogFragment implements OnClickListener {
    public Support() {
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
      return io.rong.eventbus.util.ErrorDialogFragments.createDialog(this.getActivity(), this.getArguments(), this);
    }

    public void onClick(DialogInterface dialog, int which) {
      io.rong.eventbus.util.ErrorDialogFragments.handleOnClick(dialog, which, this.getActivity(), this.getArguments());
    }
  }

  @TargetApi(11)
  public static class Honeycomb extends android.app.DialogFragment implements OnClickListener {
    public Honeycomb() {
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
      return io.rong.eventbus.util.ErrorDialogFragments.createDialog(this.getActivity(), this.getArguments(), this);
    }

    public void onClick(DialogInterface dialog, int which) {
      io.rong.eventbus.util.ErrorDialogFragments.handleOnClick(dialog, which, this.getActivity(), this.getArguments());
    }
  }
}
