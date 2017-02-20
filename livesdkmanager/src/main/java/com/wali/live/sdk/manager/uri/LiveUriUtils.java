package com.wali.live.sdk.manager.uri;

import android.net.Uri;

import static com.wali.live.sdk.manager.MiLiveSdkController.SCHEMA_APP;
import static com.wali.live.sdk.manager.MiLiveSdkController.SCHEMA_SDK;

/**
 * Created by chengsimin on 2016/12/8.
 */

public class LiveUriUtils {

    public static Uri turnAppUri(Uri uri){
        String schema = uri.getScheme();
        String uristr = uri.toString();
        uristr = uristr.replaceFirst(schema,SCHEMA_APP);
        return Uri.parse(uristr);
    }

    public static Uri turnSdkUri(Uri uri){
        String schema = uri.getScheme();
        String uristr = uri.toString();
        uristr = uristr.replaceFirst(schema,SCHEMA_SDK);
        return Uri.parse(uristr);
    }

}
