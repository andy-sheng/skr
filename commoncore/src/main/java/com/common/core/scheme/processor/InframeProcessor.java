package com.common.core.scheme.processor;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.core.scheme.SchemeConstants;
import com.common.core.scheme.SchemeUtils;
import com.common.log.MyLog;
import com.module.RouterConstants;

import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by lan on 16/10/26.
 *
 * @module scheme
 * @description Walilive的Uri的逻辑代码
 */
public class InframeProcessor implements ISchemeProcessor {
    private static final String TAG = SchemeConstants.LOG_PREFIX + "InframeProcessor";

    @Override
    public boolean accept(Uri uri) {
        //inframesker://game/match?from=h5
        //inframesker://person/homepage?from=h5
        //其中scheme为inframesker, host为com.zp.live, relativePath为match, query为from=h5.
        String scheme = uri.getScheme();
        MyLog.w(TAG, "process scheme=" + scheme);
        if (TextUtils.isEmpty(scheme)) {
            return false;
        }

        final String authority = uri.getAuthority();
        MyLog.w(TAG, "process authority=" + authority);
        if (TextUtils.isEmpty(authority)) {
            return false;
        }

        if (SchemeConstants.SCHEME_INFRAMESKER.equals(scheme)) {
            return true;
        }

        return false;
    }

    public boolean process(@NonNull Uri uri, @NonNull BaseActivity activity) {
        final String authority = uri.getAuthority();
        MyLog.w(TAG, "process authority=" + authority);
        if (TextUtils.isEmpty(authority)) {
            return false;
        }

        MyLog.d(TAG, "process authority=" + authority);
        switch (authority) {
            case SchemeConstants.HOST_SHARE:
                processShareUrl(uri);
                return true;
            case SchemeConstants.HOST_WEB:
                processWebUrl(uri);
                return true;
        }

        activity.finish();
        return false;
    }

    private void processShareUrl(Uri uri) {

    }

    private void processWebUrl(Uri uri) {
        String path = uri.getPath();
        if (TextUtils.isEmpty(path)) {
            MyLog.d(TAG, "processWebUrl path is empty");
            return;
        }

        if (SchemeConstants.PATH_FULL_SCREEN.equals(path)) {
            try {
                Map<String, String> map = splitQuery(uri);
                if (map == null || !map.containsKey("url")) {
                    MyLog.d(TAG, "processWebUrl url is empty");
                    return;
                }

                String url = SchemeUtils.getString(uri, SchemeConstants.PARAM_URL);
                ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                        .withString("url", url)
                        .greenChannel().navigation();
            } catch (UnsupportedEncodingException e) {
                MyLog.e(TAG, e);
            }
        } else {

        }
    }

    public static Map<String, String> splitQuery(Uri url) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String query = url.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }
}
