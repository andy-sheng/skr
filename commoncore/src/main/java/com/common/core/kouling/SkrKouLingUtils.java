package com.common.core.kouling;

import android.net.Uri;
import android.text.TextUtils;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.clipboard.ClipboardUtils;
import com.common.core.account.UserAccountManager;
import com.common.core.kouling.api.KouLingServerApi;
import com.common.core.scheme.processor.ProcessResult;
import com.common.core.scheme.processor.ZqSchemeProcessorManager;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.common.ICallback;

public class SkrKouLingUtils {
    public final static String TAG = "SkrKouLingUtils";

    /**
     * t=1&u=123&r=3333
     * 为了口令比较短
     * t=1 表示type是邀请别人进入游戏
     * u=123 表示房主id
     * r=3333 表示房间id
     *
     * @param inviterId
     * @param gameId
     */
    public static void genJoinGrabGameKouling(final int inviterId, final int gameId, final ICallback callback) {
        String code = String.format("inframeskr://room/grabjoin?owner=%s&gameId=%s&ask=1", inviterId, gameId);
        KouLingServerApi kouLingServerApi = ApiManager.getInstance().createService(KouLingServerApi.class);

        ApiMethods.subscribe(kouLingServerApi.setTokenByCode(code), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("复制整段信息，打开【撕歌Skr】查看。");
                    sb.append("我开了个房间，一起来撕歌吧。");
                    sb.append("$").append(obj.getData().getString("token")).append("$");
                    sb.append("还没安装【撕歌Skr】？点击安装");
                    sb.append("http://a.app.qq.com/o/simple.jsp?pkgname=com.zq.live");
                    if (callback != null) {
                        callback.onSucess(sb.toString());
                    }
                } else {
                    if (callback != null) {
                        callback.onFailed("", obj.getErrno(), "口令生成失败");
                    }
                }
            }
        });
    }

    public static void genReqFollowKouling(final int inviterId, final String name, final ICallback callback) {
        String code = String.format("inframeskr://relation/bothfollow?inviterId=%s", inviterId);
        KouLingServerApi kouLingServerApi = ApiManager.getInstance().createService(KouLingServerApi.class);

        ApiMethods.subscribe(kouLingServerApi.setTokenByCode(code), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("复制整段信息，打开【撕歌Skr】查看。");
                    sb.append("快来撕歌关注我,我是").append(name);
                    sb.append("$").append(obj.getData().getString("token")).append("$");
                    sb.append("还没安装【撕歌Skr】？点击安装");
                    sb.append("http://a.app.qq.com/o/simple.jsp?pkgname=com.zq.live");
                    if (callback != null) {
                        callback.onSucess(sb.toString());
                    }
                } else {
                    if (callback != null) {
                        callback.onFailed("", obj.getErrno(), "口令生成失败");
                    }
                }
            }
        });
    }

    public static void tryParseScheme(String str) {
        if (!TextUtils.isEmpty(str)) {


            // 跳到 是否需要跳一个activity
//            String base64Scheme = U.getStringUtils().getLongestBase64SubString(str);
            // TODO 可能需要去服务端解析口令，解析口令的接口不做身份验证
//            MyLog.d(TAG, "tryParseScheme" + " base64Scheme=" + base64Scheme);
//            byte[] bytes = U.getBase64Utils().decode(base64Scheme);
//            try {
//                String kouling = new String(bytes, "utf-8");
            String kouling = getKoulingByStr(str);
            MyLog.d(TAG,"tryParseScheme kouling=" + kouling);
            if (!TextUtils.isEmpty(kouling)) {
                KouLingServerApi kouLingServerApi = ApiManager.getInstance().createService(KouLingServerApi.class);
                ApiMethods.subscribe(kouLingServerApi.getCodeByToken(kouling), new ApiObserver<ApiResult>() {
                    @Override
                    public void process(ApiResult obj) {
                        if (obj.getErrno() == 0) {
                            String scheme = obj.getData().getString("code");
                            if (!TextUtils.isEmpty(scheme)) {
                                // TODO这里要考虑下如果没登录怎么办，走SchemeActivity
                                if (UserAccountManager.getInstance().hasAccount()) {
                                    Uri uri = Uri.parse(scheme);
                                    ProcessResult processResult = ZqSchemeProcessorManager.getInstance().process(uri, U.getActivityUtils().getTopActivity(), false);
                                } else {
                                    ARouter.getInstance().build(RouterConstants.ACTIVITY_SCHEME)
                                            .withString("uri", scheme)
                                            .navigation();
                                }
                            }
                            ClipboardUtils.clear();
                        } else {
                        }
                    }
                });
            }
        }
        return ;
    }

    private static String parseKouling2Scheme(String kouling) {
        if (kouling != null) {
            kouling = kouling.trim();
        }
        if (!TextUtils.isEmpty(kouling)) {
            String args[] = kouling.split("&");
            int type = -1;
            if (args.length > 0 && args[0].startsWith("t=")) {
                String args1[] = args[0].split("=");
                if (args1.length == 2) {
                    type = Integer.parseInt(args1[1]);
                }
            }
            if (type == 1) {
                String userId = null;
                if (args.length > 1 && args[1].startsWith("u=")) {
                    String args1[] = args[1].split("=");
                    if (args1.length == 2) {
                        userId = args1[1];
                    }
                }
                String roomId = null;
                if (args.length > 2 && args[2].startsWith("r=")) {
                    String args1[] = args[1].split("=");
                    if (args1.length == 2) {
                        roomId = args1[1];
                    }
                }
                if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(roomId)) {
                    return String.format("inframeskr://room/grabjoin?owner=%s&gameId=%s&ask=1", userId, roomId);
                }
            }
        }
        return null;
    }

    private static String getKoulingByStr(String str) {
        if (!TextUtils.isEmpty(str)) {
            int b = str.indexOf("$");
            if (b >= 0) {
                str = str.substring(b+1);
                int e = str.indexOf("$");
                if (e >= 0) {
                    str = str.substring(0, e);
                    return str;
                }
            }
        }
        return null;
    }
}
