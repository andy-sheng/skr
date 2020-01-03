package com.common.flutter.boost;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.log.MyLog;
import com.idlefish.flutterboost.containers.BoostFlutterActivity;

import java.util.HashMap;
import java.util.Map;

public class FlutterPageRouter {

    static {
        BoostInit.init();
    }

    private static final String FLUTTER_PAGE_PRE = "skrflutter://";
    public static String FLUTTER_PAGE_MANAGER_BGM = FLUTTER_PAGE_PRE + "PartyBgMusicManagerPage";
    public static String FLUTTER_PAGE_LOCAL_BGM = FLUTTER_PAGE_PRE + "PartyBgMusicLocalPage";


    public static boolean openPageByUrl(Context context, String url, Map params) {
        return openPageByUrl(context, url, params, 0);
    }

    public static boolean openPageByUrl(Context context, String url, Map params, int requestCode) {
        MyLog.d("FlutterBoost", "openPageByUrl url=" + url + " params=" + params + " requestCode=" + requestCode);
        Uri uri = Uri.parse(url);

        // 这里也可能启动原生的界面
        if (uri.getScheme().startsWith("skrflutter")) {
            try {
                Intent intent = BoostFlutterActivity.withNewEngine().url(uri.getHost()).params(params)
                        .backgroundMode(BoostFlutterActivity.BackgroundMode.opaque).build(context);
                if (context instanceof Activity) {
                    Activity activity = (Activity) context;
                    activity.startActivityForResult(intent, requestCode);
                } else {
                    context.startActivity(intent);
                }
                return true;
//            } else if (url.startsWith(FLUTTER_FRAGMENT_PAGE_URL)) {
//                context.startActivity(new Intent(context, FlutterFragmentPageActivity.class));
//                return true;
//            } else if (url.startsWith(NATIVE_PAGE_URL)) {
//                context.startActivity(new Intent(context, NativePageActivity.class));
//                return true;
            } catch (Throwable t) {
                return false;
            }
        } else if (uri.getScheme().startsWith("skrnative")) {
            Postcard post = ARouter.getInstance().build(uri.getPath());
            if (params != null) {
                for (Object key : params.keySet()) {
                    Object v = params.get(key);
                    if (v instanceof String) {
                        post = post.withString((String) key, (String) v);
                    } else if (v instanceof Integer) {
                        post = post.withInt((String) key, (int) v);
                    }
                }
            }
            if (context instanceof Activity) {
                post.navigation((Activity) context, requestCode);
            } else {
                post.navigation();
            }
        }
        return false;
    }
}
