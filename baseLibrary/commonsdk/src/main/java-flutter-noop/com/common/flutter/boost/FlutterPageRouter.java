package com.common.flutter.boost;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.common.log.MyLog;

import java.util.HashMap;
import java.util.Map;

public class FlutterPageRouter {
    private static final String FLUTTER_PAGE_PRE = "skrflutter://";
    public static String FLUTTER_PAGE_MANAGER_BGM = FLUTTER_PAGE_PRE + "PartyBgMusicManagerPage";
    public static String FLUTTER_PAGE_LOCAL_BGM = FLUTTER_PAGE_PRE + "PartyBgMusicLocalPage";

    public static boolean openPageByUrl(Context context, String url, Map params) {
        return false;
    }

    public static boolean openPageByUrl(Context context, String url, Map params, int requestCode) {
        return false;
    }
}
