package com.base.utils;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.ResultReceiver;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;

import com.base.log.MyLog;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * 反射方法调用系统未公开的API
 *
 * @author ROBIN.LIU
 */
public class WLReflect {

    public final static String TAG = "WLReflect";
    
    /**
     * ref http://wiki.n.miui.com/pages/viewpage.action?pageId=47102590
     */
    public static boolean isAppCompatible(Context context, String pkgName) {
        try {
            Class<?> cls = Class.forName("miui.security.appcompatibility.AppCompatibilityManager");
            Class<?>[] arrayOfClass = new Class[2];
            arrayOfClass[0] = Context.class;
            arrayOfClass[1] = String.class;
            Method method = cls.getMethod("isAppCompatible", arrayOfClass);
            method.setAccessible(true);

            Object[] arrayOfObject = new Object[2];
            arrayOfObject[0] = context;
            arrayOfObject[1] = pkgName;
            return (Boolean) method.invoke(null, arrayOfObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static final int INSTALL_FAILED_INSUFFICIENT_STORAGE = -4;//PackageManager隐藏的常量

    /**
     * 在miui v5系统中，(DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE); 返回的就是
     * MiuiDownloadManager，但考虑到单反版的移植，故使用了反射的方法实现暂停下载。
     *
     * @param dm
     * @param ids
     */
    public static void pauseDownload(DownloadManager dm, long[] ids) {
        try {
            if (!dm.getClass().getName().equalsIgnoreCase("android.app.DownloadManager")) {
                return;
            }
            Class<?> miuidownlaod = Class.forName("android.app.DownloadManager");
            Method pauseDownload = miuidownlaod.getMethod("pauseDownload", ids.getClass());
            pauseDownload.setAccessible(true);
            pauseDownload.invoke(dm, ids);
        } catch (Exception e) {
            MyLog.w(TAG,e);
        }
    }

    /**
     * 在miui v5系统中，(DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE); 返回的就是
     * MiuiDownloadManager，但考虑到单反版的移植，故使用了反射的方法实现重启下载。
     *
     * @param dm
     * @param ids
     */
    public static void restartDownload(DownloadManager dm, long[] ids) {
        try {
            if (!dm.getClass().getName().equalsIgnoreCase("android.app.DownloadManager")) {
                return;
            }
            Class<?> miuidownlaod = Class.forName("android.app.DownloadManager");
            Method pauseDownload = miuidownlaod.getMethod("restartDownload", ids.getClass());
            pauseDownload.setAccessible(true);
            pauseDownload.invoke(dm, ids);
        } catch (Exception e) {
            MyLog.w(TAG,e);
        }
    }

    /**
     * 在miui v5系统中，(DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE); 返回的就是
     * MiuiDownloadManager，但考虑到单反版的移植，故使用了反射的方法实现恢复下载。
     *
     * @param dm
     * @param ids
     */
    public static void resumeDownload(DownloadManager dm, long[] ids) {
        try {
            if (!dm.getClass().getName().equalsIgnoreCase("android.app.DownloadManager")) {
                MyLog.w("DownloadControll", "equalsIgnoreCase");
                return;
            }
            Class<?> miuidownlaod = Class.forName("android.app.DownloadManager");
            Method resumeDownload = miuidownlaod.getMethod("resumeDownload", ids.getClass());
            resumeDownload.setAccessible(true);

            resumeDownload.invoke(dm, ids);
            MyLog.w("DownloadControll", "call resumeDonwload");
        } catch (Exception e) {
            MyLog.w(TAG,e);
        }
    }


    /**
     *
     */
    public static void setDefaultExecutor() {
        Method localMethod;
        Class<?> sp;
        try {
            sp = Class.forName("android.os.AsyncTask");
            localMethod = sp.getMethod("setDefaultExecutor", Executor.class);
            localMethod.setAccessible(true);
            Field f = sp.getField("THREAD_POOL_EXECUTOR");
            f.setAccessible(true);
            Executor exec = (Executor) f.get(null);//静态变量?
            localMethod.invoke(null, exec);//静态方法为null
        } catch (Throwable e) {
            MyLog.w(TAG,e);
        }
    }

    /**
     * 将输入法界面调出来
     *
     * @param inputManager
     * @param flags
     * @param resultReceiver
     */
    public static void showSoftInputUnchecked(InputMethodManager inputManager,
                                              int flags, ResultReceiver resultReceiver) {
        Class<?>[] arrayOfClass = new Class[2];
        arrayOfClass[0] = int.class;
        arrayOfClass[1] = ResultReceiver.class;

        Object[] arrayOfObject = new Object[2];
        arrayOfObject[0] = 0;
        arrayOfObject[1] = resultReceiver;

        Method localMethod;
        try {
            localMethod = inputManager.getClass().getMethod(
                    "showSoftInputUnchecked", arrayOfClass);
            localMethod.setAccessible(true);
            localMethod.invoke(inputManager, arrayOfObject);
        } catch (Exception e) {
            MyLog.w(TAG,e);
        }
    }

    /**
     * @param view
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @return
     */
    public static boolean setFrame(View view, int left, int top, int right, int bottom) {
        Class<?>[] arrayOfClass = new Class[4];
        arrayOfClass[0] = int.class;
        arrayOfClass[1] = int.class;
        arrayOfClass[2] = int.class;
        arrayOfClass[3] = int.class;

        Object[] arrayOfObject = new Object[4];
        arrayOfObject[0] = left;
        arrayOfObject[1] = top;
        arrayOfObject[2] = right;
        arrayOfObject[3] = bottom;

        Method localMethod;
        boolean ret = false;
        try {
            Class<?> cls = Class.forName("android.view.View");
            localMethod = cls.getDeclaredMethod(
                    "setFrame", arrayOfClass);
            localMethod.setAccessible(true);
            ret = (Boolean) localMethod.invoke(view, arrayOfObject);
        } catch (Exception e) {
            MyLog.w(TAG,e);
        }

        return ret;
    }

    public static void setViewPrivateVar(View view,
                                         String fieldName, int value) {
        try {
            Class<?> cls = Class.forName("android.view.View");
            Field f = cls.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(view, value);
        } catch (Exception e) {
            MyLog.w(TAG,e);
        }
    }

    public static int getViewPrivateVar(View view,
                                        String fieldName) {
        try {
            Class<?> cls = Class.forName("android.view.View");
            Field f = cls.getDeclaredField(fieldName);
            f.setAccessible(true);
            return (Integer) f.get(view);
        } catch (Exception e) {
            MyLog.w(TAG,e);
        }
        return -1;
    }

    /**
     * @param listView
     * @param position
     * @param offset
     */
    public static void smoothScrollToPositionFromTop(ListView listView,
                                                     int position, int offset) {
        Class<?>[] arrayOfClass = new Class[2];
        arrayOfClass[0] = int.class;
        arrayOfClass[1] = int.class;

        Object[] arrayOfObject = new Object[2];
        arrayOfObject[0] = position;
        arrayOfObject[1] = offset;
        try {
            Method localMethod = listView.getClass().getMethod(
                    "smoothScrollToPositionFromTop",
                    arrayOfClass);
            localMethod.invoke(listView, arrayOfObject);
        } catch (Exception e) {
            MyLog.w(TAG,e);
        }
    }

//	/**
//	 * 清除selector区域
//	 * @param listView
//	 */
//	public static void clearSelector(AbsListView listView){
//		Class<?> cls;
//		try {
//			cls = Class.forName("android.widget.AbsListView");
//			Field f = cls.getDeclaredField("mSelectorRect");
//			f.setAccessible(true);
//			Rect rect = new Rect(0, 0, 0, 0);
//			f.set(listView, rect);
//		} catch (ClassNotFoundException e) {
//			if(IConfig.DEBUG) MyLog.w(TAG,e);
//		} catch (NoSuchFieldException e) {
//			if(IConfig.DEBUG) MyLog.w(TAG,e);
//		} catch (IllegalArgumentException e) {
//			if(IConfig.DEBUG) MyLog.w(TAG,e);
//		} catch (IllegalAccessException e) {
//			if(IConfig.DEBUG) MyLog.w(TAG,e);
//		}
//	}
//
//	public static void setCalendarViewShown(DatePicker d, boolean shown){
//		Method localMethod;
//		Class<?> sp;
//		try {
//			sp = Class.forName("android.widget.DatePicker");
//			localMethod = sp.getMethod("setCalendarViewShown", boolean.class);
//			localMethod.setAccessible(true);
//			localMethod.invoke(d, shown);
//		} catch (ClassNotFoundException e) {
//			MyLog.w(TAG,e);
//		} catch (NoSuchMethodException e) {
//			MyLog.w(TAG,e);
//		} catch (IllegalArgumentException e) {
//			MyLog.w(TAG,e);
//		} catch (IllegalAccessException e) {
//			MyLog.w(TAG,e);
//		} catch (InvocationTargetException e) {
//			MyLog.w(TAG,e);
//		}
//	}
//
//	public static void setSpinnersShown(DatePicker d, boolean shown){
//		Method localMethod;
//		Class<?> sp;
//		try {
//			sp = Class.forName("android.widget.DatePicker");
//			localMethod = sp.getMethod("setSpinnersShown", boolean.class);
//			localMethod.setAccessible(true);
//			localMethod.invoke(d, shown);
//		} catch (ClassNotFoundException e) {
//			MyLog.w(TAG,e);
//		} catch (NoSuchMethodException e) {
//			MyLog.w(TAG,e);
//		} catch (IllegalArgumentException e) {
//			MyLog.w(TAG,e);
//		} catch (IllegalAccessException e) {
//			MyLog.w(TAG,e);
//		} catch (InvocationTargetException e) {
//			MyLog.w(TAG,e);
//		}
//	}
//
//	/**
//	 * 其构造函数是HIDE 没办法只能用反射了
//	 * @param resolver
//	 * @param packageName
//	 * @return
//	 */
//	public static DownloadManager createDownloadManager(ContentResolver resolver,
//			String packageName){
//		DownloadManager downloadManager = null;
//		try {
//			Constructor<DownloadManager> construct = DownloadManager.class
//					.getConstructor(ContentResolver.class, String.class);
//			if (construct != null) {
//				downloadManager = construct.newInstance(resolver, packageName);
//			}
//		} catch (NoSuchMethodException e) {
//			MyLog.w(TAG,e);
//		} catch (IllegalArgumentException e) {
//			MyLog.w(TAG,e);
//		} catch (InstantiationException e) {
//			MyLog.w(TAG,e);
//		} catch (IllegalAccessException e) {
//			MyLog.w(TAG,e);
//		} catch (InvocationTargetException e) {
//			MyLog.w(TAG,e);
//		}
//
//		return downloadManager;
//	}


//    /**
//     *  用反射的方法获取XiaomiUserInfo。
//     *  同时兼容了cUid存在和不存在两种情形
//     * @return
//     */
//    public static XiaomiUserInfo getXiaoMiUserInfo(String uid, String cuid,
//            String token, String security)
//    {
//        XiaomiUserInfo ret = null;
//
//
//        Class<?>[] arrayOfClass = new Class[3];
//        arrayOfClass[0] = String.class;
//        arrayOfClass[1] = String.class;
//        arrayOfClass[2] = String.class;
//
//        Object[] arrayOfObject = new Object[3];
//        arrayOfObject[0] = uid;
//        arrayOfObject[1] = token;
//        arrayOfObject[2] = security;
//
//
//        Class<?>[] arrayOfClassWithCuid = new Class[4];
//        arrayOfClassWithCuid[0] = String.class;
//        arrayOfClassWithCuid[1] = String.class;
//        arrayOfClassWithCuid[2] = String.class;
//        arrayOfClassWithCuid[3] = String.class;
//
//        Object[] arrayOfObjectWithCuid = new Object[4];
//        arrayOfObjectWithCuid[0] = uid;
//        arrayOfObjectWithCuid[1] = cuid;
//        arrayOfObjectWithCuid[2] = token;
//        arrayOfObjectWithCuid[3] = security;
//
//
//        Method localMethod;
//        Class<?> sp;
//        try {
//            sp = Class.forName("com.xiaomi.accountsdk.account.XMPassport");
//            if(TextUtils.isEmpty(cuid))
//            {
//                localMethod = sp.getMethod("getXiaomiUserInfo",arrayOfClass);
//                localMethod.setAccessible(true);
//                Object object = localMethod.invoke(sp,arrayOfObject);
//                if(object instanceof  XiaomiUserInfo)
//                {
//                    ret = (XiaomiUserInfo)object;
//                }
//            }
//            else
//            {
//                localMethod = sp.getMethod("getXiaomiUserInfo",arrayOfClassWithCuid);
//                localMethod.setAccessible(true);
//                Object object = localMethod.invoke(sp,arrayOfObjectWithCuid);
//                if(object instanceof  XiaomiUserInfo)
//                {
//                    ret = (XiaomiUserInfo)object;
//                }
//            }
//        } catch (Throwable e) {
//            MyLog.w(TAG,e);
//        }
//
//        return ret;
//    }

    /*
    //用反射的方式实现 notification.extraNotification.setMessageCount(updateNumber);
    * **/
    public static void setExtraNotificationMessageCount(Notification notification, int count) {
        try {
            Field f = notification.getClass().getDeclaredField("extraNotification");
            f.setAccessible(true);
            Class<?> extraNoti = Class.forName("android.app.MiuiNotification");
            Method m = extraNoti.getDeclaredMethod("setMessageCount", int.class);
            m.setAccessible(true);
            m.invoke(f.get(notification), count);
        } catch (Exception ex) {
            MyLog.w(TAG,ex);
        }
    }

    /*
     * //用反射的方式实现 notification.extraNotification.getMessageCount();
     **/
    public static int getExtraNotificationMessageCount(Notification notification) {
        int ret = 0;
        try {
            Field f = notification.getClass().getDeclaredField("extraNotification");
            f.setAccessible(true);
            Class<?> extraNoti = Class.forName("android.app.MiuiNotification");
            Method m = extraNoti.getDeclaredMethod("getMessageCount");
            m.setAccessible(true);
            Object obj = m.invoke(f.get(notification));
            if (obj instanceof Integer) {
                Integer integer = (Integer) obj;
                ret = integer.intValue();
            }
        } catch (Exception ex) {
            MyLog.w(TAG,ex);
        }
        return ret;
    }

    //DownloadManager提供的文件总是公共可访问的，没有权限问题，因此使用hide方法绕过Android N必须使用content协议的限制
    public static void tryEnableFileAccess(DownloadManager dm) {
        if (!(Build.VERSION.SDK_INT >= 24)) {
            return;
        }
        try {
            if (!dm.getClass().getName().equalsIgnoreCase("android.app.DownloadManager")) {
                return;
            }
            Class<?> miuidownlaod = Class.forName("android.app.DownloadManager");
            Method method = miuidownlaod.getMethod("setAccessFilename", boolean.class);
            method.setAccessible(true);
            method.invoke(dm, true);
        } catch (Exception e) {
            MyLog.w(TAG,e);
        }
    }

    public static void setDownloadManagerRequestFileSize(Request request, long size) {
        Method m;
        try {
            m = request.getClass().getDeclaredMethod("setFileSize", long.class);
            m.setAccessible(true);
            m.invoke(request, size);
        } catch (Exception e) {
            MyLog.w(TAG,e);
        }
    }

    public static void setDownloadManagerRequestFileIconUri(Request request, Uri uri) {
        Method m;
        try {
            m = request.getClass().getDeclaredMethod("setFileIconUri", Uri.class);
            m.setAccessible(true);
            m.invoke(request, uri);
        } catch (Exception e) {
            MyLog.w(TAG,e);
        }
    }

    public static void setDownloadManagerRequestApkPackageName(Request request, String packageName) {
        Method m;
        try {
            m = request.getClass().getDeclaredMethod("setApkPackageName", String.class);
            m.setAccessible(true);
            m.invoke(request, packageName);
        } catch (Exception e) {
            MyLog.w(TAG,e);
        }
    }

    public static void requestPermission(Activity act, String[] permissions, int requestCode) {
        if (null == act || null == permissions || permissions.length == 0) {
            return;
        }
        try {
            Class<?>[] arrayOfClass = new Class[2];
            arrayOfClass[0] = String[].class;
            arrayOfClass[1] = int.class;

            Object[] arrayOfObject = new Object[2];
            arrayOfObject[0] = permissions;
            arrayOfObject[1] = requestCode;

            act.getClass().getMethod("requestPermissions", arrayOfClass).invoke(act, arrayOfObject);
        } catch (Exception e) {
        }
    }

    public static int isPermissionGranted(Activity act, String permission) {
        try {
            Class<?>[] arrayOfClass = new Class[1];
            arrayOfClass[0] = String.class;

            Object[] arrayOfObject = new Object[1];
            arrayOfObject[0] = permission;
            Object obj = act.getClass().getMethod("checkSelfPermission", arrayOfClass).invoke(act, arrayOfObject);
            if (obj instanceof Integer) {
                Integer integerObj = (Integer) obj;
                return integerObj.intValue();
            }
            return PackageManager.PERMISSION_DENIED;
        } catch (Exception e) {
        }
        return PackageManager.PERMISSION_GRANTED;
    }
}
