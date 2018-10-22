package com.wali.live.modulechannel.helper;

import android.net.Uri;
import android.text.TextUtils;


/**
 * Created by lan on 16/7/15.
 *
 * @module 频道
 * @description ViewModel辅助类：判断scheme的类型
 */
public class ModelHelper {
    /**
     * 判断是否是直播
     */
    public static boolean isLiveScheme(String uriStr) {
        //TOdo-暂时注释了
//        Uri uri = Uri.parse(uriStr);
//        String scheme = uri.getScheme();
//        if (!TextUtils.isEmpty(scheme) && scheme.equals(SchemeConstants.SCHEME_WALILIVE)) {
//            String host = uri.getHost();
//            if (!TextUtils.isEmpty(host) && host.equals(SchemeConstants.HOST_ROOM)) {
//                String path = uri.getPath();
//                if (!TextUtils.isEmpty(path) && path.equals(SchemeConstants.PATH_JOIN)) {
//                    return true;
//                }
//            }
//        }
        return false;
    }

    public static boolean isContestScheme(String uriStr) {
        //TOdo-暂时注释了
//        Uri uri = Uri.parse(uriStr);
//        boolean isContest = uri.getBooleanQueryParameter(SchemeConstants.PARAM_IS_CONTEST, false);
//        return isContest;
        return false;
    }

    /**
     * 判断是否是回放
     */
    public static boolean isPlaybackScheme(String uriStr) {
        //TOdo-暂时注释了
//        Uri uri = Uri.parse(uriStr);
//        String scheme = uri.getScheme();
//        if (!TextUtils.isEmpty(scheme) && scheme.equals(SchemeConstants.SCHEME_WALILIVE)) {
//            String host = uri.getHost();
//            if (!TextUtils.isEmpty(host) && host.equals(SchemeConstants.HOST_PLAYBACK)) {
//                String path = uri.getPath();
//                if (!TextUtils.isEmpty(path) && path.equals(SchemeConstants.PATH_JOIN)) {
//                    return true;
//                }
//            }
//        }
        return false;
    }
}
