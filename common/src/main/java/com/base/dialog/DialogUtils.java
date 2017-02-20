package com.base.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;

import com.base.global.GlobalData;

/**
 * Created by lan on 15-12-28.
 */
public class DialogUtils {
    public static void showNormalDialog(final Activity activity, final int titleResId, final int messageResId,
                                        final int positiveResId, final int negativeResId,
                                        final IDialogCallback positiveCallback, final IDialogCallback negativeCallback) {
        String title = "";
        String message = "";
        if (titleResId > 0) {
            title = GlobalData.app().getResources().getString(titleResId);
        }
        if (messageResId > 0) {
            message = GlobalData.app().getResources().getString(messageResId);
        }
        showNormalDialog(activity, title, message, positiveResId, negativeResId, positiveCallback, negativeCallback);
    }

    public static void showNormalDialog(final Activity activity, final String title, final String message,
                                        final int positiveResId, final int negativeResId,
                                        final IDialogCallback positiveCallback, final IDialogCallback negativeCallback) {
        if (null != activity && !activity.isFinishing()) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                showDialog(activity, title, message, positiveResId, negativeResId, positiveCallback, negativeCallback);
            } else {
                new Handler(Looper.getMainLooper())
                        .post(new Runnable() {
                            @Override
                            public void run() {
                                showDialog(activity, title, message, positiveResId, negativeResId, positiveCallback, negativeCallback);
                            }
                        });
            }
        }
    }

    public static void showCancelableDialog(final Context activity, final int titleResId, final int messageResId,
                                            final int positiveResId, final int negativeResId,
                                            final IDialogCallback positiveCallback, final IDialogCallback negativeCallback) {
        String title = "";
        String message = "";
        if (titleResId > 0) {
            title = GlobalData.app().getResources().getString(titleResId);
        }
        if (messageResId > 0) {
            message = GlobalData.app().getResources().getString(messageResId);
        }
        showCancelableDialog(activity, title, message, positiveResId, negativeResId, positiveCallback, negativeCallback);
    }

    public static void showCancelableDialog(final Context activity, final String title, final String message,
                                            final int positiveResId, final int negativeResId,
                                            final IDialogCallback positiveCallback, final IDialogCallback negativeCallback) {
        MyAlertDialog.Builder builder = new MyAlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setCancelable(false);
//        builder.setCancelableOnTouchOutSide(false);
        builder.setMessage(message);
        if (positiveResId > 0) {
            builder.setPositiveButton(positiveResId, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (positiveCallback != null) {
                        positiveCallback.process(dialogInterface, i);
                    }
                    dialogInterface.dismiss();
                }
            });
        }
        if (negativeResId > 0) {
            builder.setNegativeButton(negativeResId, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (negativeCallback != null) {
                        negativeCallback.process(dialogInterface, i);
                    }
                    dialogInterface.dismiss();
                }
            });
        }
        builder.setAutoDismiss(false).show();
    }

    private static void showDialog(final Activity activity, final String title, final String message,
                                   final int positiveResId, final int negativeResId,
                                   final IDialogCallback positiveCallback, final IDialogCallback negativeCallback) {
        MyAlertDialog.Builder builder = new MyAlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        if (positiveResId > 0) {
            builder.setPositiveButton(positiveResId, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (positiveCallback != null) {
                        positiveCallback.process(dialogInterface, i);
                    }
                }
            });
        }
        if (negativeResId > 0) {
            builder.setNegativeButton(negativeResId, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (negativeCallback != null) {
                        negativeCallback.process(dialogInterface, i);
                    }
                }
            });
        }
        builder.setAutoDismiss(true).show();
    }

    public static MyAlertDialog showAlertDialog(final Activity activity, final CharSequence title, final CharSequence message, final CharSequence btnTxt) {
        MyAlertDialog.Builder builder = new MyAlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(btnTxt, null);
        MyAlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    public static void showGlobalDialog(final Activity activity, int titleResId, int messageResId, int positiveResId, int negativeResId, final IDialogCallback positiveCallback, final IDialogCallback negativeCallback,MyAlertDialog.DismissCallBack dismissCallBack) {
        String title = "";
        String message = "";
        if (titleResId > 0) {
            title = GlobalData.app().getResources().getString(titleResId);
        }
        if (messageResId > 0) {
            message = GlobalData.app().getResources().getString(messageResId);
        }
        MyAlertDialog.Builder builder = new MyAlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setCancelable(false);
//        builder.setCancelableOnTouchOutSide(false);
        builder.setMessage(message);
        if (positiveResId > 0) {
            builder.setPositiveButton(positiveResId, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (positiveCallback != null) {
                        positiveCallback.process(dialogInterface, i);
                    }
                    dialogInterface.dismiss();
                }
            });
        }
        if (negativeResId > 0) {
            builder.setNegativeButton(negativeResId, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (negativeCallback != null) {
                        negativeCallback.process(dialogInterface, i);
                    }
                    dialogInterface.dismiss();
                }
            });
        }
        builder.setDismissCallBack(dismissCallBack);
        builder.setAutoDismiss(false).showGlobal();
    }

    public interface IDialogCallback {
        void process(DialogInterface dialogInterface, int i);
    }
}
