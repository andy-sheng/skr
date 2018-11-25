//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.eventbus.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import io.rong.eventbus.EventBus;
import io.rong.eventbus.util.ErrorDialogFragmentFactory;
import io.rong.eventbus.util.ThrowableFailureEvent;

public class ErrorDialogManager {
  public static ErrorDialogFragmentFactory<?> factory;
  protected static final String TAG_ERROR_DIALOG = "de.greenrobot.eventbus.error_dialog";
  protected static final String TAG_ERROR_DIALOG_MANAGER = "de.greenrobot.eventbus.error_dialog_manager";
  public static final String KEY_TITLE = "de.greenrobot.eventbus.errordialog.title";
  public static final String KEY_MESSAGE = "de.greenrobot.eventbus.errordialog.message";
  public static final String KEY_FINISH_AFTER_DIALOG = "de.greenrobot.eventbus.errordialog.finish_after_dialog";
  public static final String KEY_ICON_ID = "de.greenrobot.eventbus.errordialog.icon_id";
  public static final String KEY_EVENT_TYPE_ON_CLOSE = "de.greenrobot.eventbus.errordialog.event_type_on_close";

  public ErrorDialogManager() {
  }

  public static void attachTo(Activity activity) {
    attachTo(activity, false, (Bundle)null);
  }

  public static void attachTo(Activity activity, boolean finishAfterDialog) {
    attachTo(activity, finishAfterDialog, (Bundle)null);
  }

  public static void attachTo(Activity activity, boolean finishAfterDialog, Bundle argumentsForErrorDialog) {
    Object executionScope = activity.getClass();
    attachTo(activity, executionScope, finishAfterDialog, argumentsForErrorDialog);
  }

  public static void attachTo(Activity activity, Object executionScope, boolean finishAfterDialog, Bundle argumentsForErrorDialog) {
    if (factory == null) {
      throw new RuntimeException("You must set the static factory field to configure error dialogs for your app.");
    } else {
      if (isSupportActivity(activity)) {
        io.rong.eventbus.util.ErrorDialogManager.SupportManagerFragment.attachTo(activity, executionScope, finishAfterDialog, argumentsForErrorDialog);
      } else {
        io.rong.eventbus.util.ErrorDialogManager.HoneycombManagerFragment.attachTo(activity, executionScope, finishAfterDialog, argumentsForErrorDialog);
      }

    }
  }

  private static boolean isSupportActivity(Activity activity) {
    boolean isSupport = false;
    Class c = activity.getClass().getSuperclass();

    while(c != null) {
      String name = c.getName();
      if (name.equals("android.support.v4.app.FragmentActivity")) {
        isSupport = true;
      } else {
        if (name.startsWith("com.actionbarsherlock.app") && (name.endsWith(".SherlockActivity") || name.endsWith(".SherlockListActivity") || name.endsWith(".SherlockPreferenceActivity"))) {
          throw new RuntimeException("Please use SherlockFragmentActivity. Illegal activity: " + name);
        }

        if (!name.equals("android.app.Activity")) {
          c = c.getSuperclass();
          continue;
        }

        if (VERSION.SDK_INT < 11) {
          throw new RuntimeException("Illegal activity without fragment support. Either use Android 3.0+ or android.support.v4.app.FragmentActivity.");
        }
      }

      return isSupport;
    }

    throw new RuntimeException("Illegal activity type: " + activity.getClass());
  }

  protected static void checkLogException(ThrowableFailureEvent event) {
    if (factory.config.logExceptions) {
      String tag = factory.config.tagForLoggingExceptions;
      if (tag == null) {
        tag = EventBus.TAG;
      }

      Log.i(tag, "Error dialog manager received exception", event.throwable);
    }

  }

  private static boolean isInExecutionScope(Object executionScope, ThrowableFailureEvent event) {
    if (event != null) {
      Object eventExecutionScope = event.getExecutionScope();
      if (eventExecutionScope != null && !eventExecutionScope.equals(executionScope)) {
        return false;
      }
    }

    return true;
  }

  @TargetApi(11)
  public static class HoneycombManagerFragment extends Fragment {
    protected boolean finishAfterDialog;
    protected Bundle argumentsForErrorDialog;
    private EventBus eventBus;
    private Object executionScope;

    public HoneycombManagerFragment() {
    }

    public void onResume() {
      super.onResume();
      this.eventBus = io.rong.eventbus.util.ErrorDialogManager.factory.config.getEventBus();
      this.eventBus.register(this);
    }

    public void onPause() {
      this.eventBus.unregister(this);
      super.onPause();
    }

    public void onEventMainThread(ThrowableFailureEvent event) {
      if (io.rong.eventbus.util.ErrorDialogManager.isInExecutionScope(this.executionScope, event)) {
        io.rong.eventbus.util.ErrorDialogManager.checkLogException(event);
        FragmentManager fm = this.getFragmentManager();
        fm.executePendingTransactions();
        DialogFragment existingFragment = (DialogFragment)fm.findFragmentByTag("de.greenrobot.eventbus.error_dialog");
        if (existingFragment != null) {
          existingFragment.dismiss();
        }

        DialogFragment errorFragment = (DialogFragment) io.rong.eventbus.util.ErrorDialogManager.factory.prepareErrorFragment(event, this.finishAfterDialog, this.argumentsForErrorDialog);
        if (errorFragment != null) {
          errorFragment.show(fm, "de.greenrobot.eventbus.error_dialog");
        }

      }
    }

    public static void attachTo(Activity activity, Object executionScope, boolean finishAfterDialog, Bundle argumentsForErrorDialog) {
      FragmentManager fm = activity.getFragmentManager();
      io.rong.eventbus.util.ErrorDialogManager.HoneycombManagerFragment fragment = (io.rong.eventbus.util.ErrorDialogManager.HoneycombManagerFragment)fm.findFragmentByTag("de.greenrobot.eventbus.error_dialog_manager");
      if (fragment == null) {
        fragment = new io.rong.eventbus.util.ErrorDialogManager.HoneycombManagerFragment();
        fm.beginTransaction().add(fragment, "de.greenrobot.eventbus.error_dialog_manager").commit();
        fm.executePendingTransactions();
      }

      fragment.finishAfterDialog = finishAfterDialog;
      fragment.argumentsForErrorDialog = argumentsForErrorDialog;
      fragment.executionScope = executionScope;
    }
  }

  public static class SupportManagerFragment extends android.support.v4.app.Fragment {
    protected boolean finishAfterDialog;
    protected Bundle argumentsForErrorDialog;
    private EventBus eventBus;
    private boolean skipRegisterOnNextResume;
    private Object executionScope;

    public SupportManagerFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      this.eventBus = io.rong.eventbus.util.ErrorDialogManager.factory.config.getEventBus();
      this.eventBus.register(this);
      this.skipRegisterOnNextResume = true;
    }

    public void onResume() {
      super.onResume();
      if (this.skipRegisterOnNextResume) {
        this.skipRegisterOnNextResume = false;
      } else {
        this.eventBus = io.rong.eventbus.util.ErrorDialogManager.factory.config.getEventBus();
        this.eventBus.register(this);
      }

    }

    public void onPause() {
      this.eventBus.unregister(this);
      super.onPause();
    }

    public void onEventMainThread(ThrowableFailureEvent event) {
      if (io.rong.eventbus.util.ErrorDialogManager.isInExecutionScope(this.executionScope, event)) {
        io.rong.eventbus.util.ErrorDialogManager.checkLogException(event);
        android.support.v4.app.FragmentManager fm = this.getFragmentManager();
        fm.executePendingTransactions();
        android.support.v4.app.DialogFragment existingFragment = (android.support.v4.app.DialogFragment)fm.findFragmentByTag("de.greenrobot.eventbus.error_dialog");
        if (existingFragment != null) {
          existingFragment.dismiss();
        }

        android.support.v4.app.DialogFragment errorFragment = (android.support.v4.app.DialogFragment) io.rong.eventbus.util.ErrorDialogManager.factory.prepareErrorFragment(event, this.finishAfterDialog, this.argumentsForErrorDialog);
        if (errorFragment != null) {
          errorFragment.show(fm, "de.greenrobot.eventbus.error_dialog");
        }

      }
    }

    public static void attachTo(Activity activity, Object executionScope, boolean finishAfterDialog, Bundle argumentsForErrorDialog) {
      android.support.v4.app.FragmentManager fm = ((FragmentActivity)activity).getSupportFragmentManager();
      io.rong.eventbus.util.ErrorDialogManager.SupportManagerFragment fragment = (io.rong.eventbus.util.ErrorDialogManager.SupportManagerFragment)fm.findFragmentByTag("de.greenrobot.eventbus.error_dialog_manager");
      if (fragment == null) {
        fragment = new io.rong.eventbus.util.ErrorDialogManager.SupportManagerFragment();
        fm.beginTransaction().add(fragment, "de.greenrobot.eventbus.error_dialog_manager").commit();
        fm.executePendingTransactions();
      }

      fragment.finishAfterDialog = finishAfterDialog;
      fragment.argumentsForErrorDialog = argumentsForErrorDialog;
      fragment.executionScope = executionScope;
    }
  }
}
