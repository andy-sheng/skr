package com.common.core.kouling;

import android.net.Uri;
import android.text.TextUtils;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.clipboard.ClipboardUtils;
import com.common.core.account.UserAccountManager;
import com.common.core.kouling.api.KouLingServerApi;
import com.common.core.myinfo.MyUserInfoManager;
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

    public static void genDoubleJoinGrabGameKouling(final int inviterId, final int gameId,int mediaType, final ICallback callback){
        String code = String.format("inframeskr://room/doublejoin?owner=%s&gameId=%s&ask=1&mediaType=%s", inviterId, gameId,mediaType);
        KouLingServerApi kouLingServerApi = ApiManager.getInstance().createService(KouLingServerApi.class);

        ApiMethods.subscribe(kouLingServerApi.setTokenByCode(code), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    if (callback != null) {
                        callback.onSucess(obj.getData().getString("token"));
                    }
                } else {
                    if (callback != null) {
                        callback.onFailed("", obj.getErrno(), "口令生成失败");
                    }
                }
            }
        });
    }

    public static String genJoinDoubleGameKouling(String kouling) {
        StringBuilder sb = new StringBuilder();
        sb.append("【复制消息 打开撕歌skr】").append("\n");
        String name = MyUserInfoManager.getInstance().getNickName();
        sb.append(name).append(" 邀你一起加入ta的双人畅聊房间，一起边唱边聊～").append("\n");
        sb.append("——————————").append("\n");
        sb.append("房间口令:").append("$").append(kouling).append("$").append("\n");
        sb.append("撕歌skr 下载地址:http://a.app.icon_qq.com/o/simple.jsp?pkgname=com.zq.live").append("\n");
        if (MyLog.isDebugLogOpen()) {
            sb.append("仅debug才显示本条,不同环境口令不互通,环境=").append(U.getChannelUtils().getChannel());
        }
        return sb.toString();
    }


    public static void genNormalJoinGrabGameKouling(final int inviterId, final int gameId,int mediaType, final ICallback callback) {
        String code = String.format("inframeskr://room/grabjoin?owner=%s&gameId=%s&ask=1&mediaType=%s", inviterId, gameId,mediaType);
        KouLingServerApi kouLingServerApi = ApiManager.getInstance().createService(KouLingServerApi.class);

        ApiMethods.subscribe(kouLingServerApi.setTokenByCode(code), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    if (callback != null) {
                        callback.onSucess(obj.getData().getString("token"));
                    }
                } else {
                    if (callback != null) {
                        callback.onFailed("", obj.getErrno(), "口令生成失败");
                    }
                }
            }
        });
    }

    public static String genJoinGrabGameKouling(String kouling) {
        StringBuilder sb = new StringBuilder();
        sb.append("【复制消息 打开撕歌skr】").append("\n");
        String name = MyUserInfoManager.getInstance().getNickName();
//                    if (!TextUtils.isEmpty(name)) {
//                        name = name.replaceAll("\\$", "");
//                    }
        sb.append(name).append(" 在 撕歌skr 创建了个嗨唱包房 ，邀你一起来嗨呀。等你喔~").append("\n");
        sb.append("——————————").append("\n");
        sb.append("房间口令:").append("$").append(kouling).append("$").append("\n");
        sb.append("撕歌skr 下载地址:http://a.app.icon_qq.com/o/simple.jsp?pkgname=com.zq.live").append("\n");
        if (MyLog.isDebugLogOpen()) {
            sb.append("仅debug才显示本条,不同环境口令不互通,环境=").append(U.getChannelUtils().getChannel());
        }
        return sb.toString();
    }

    public static void genNormalReqFollowKouling(final int inviterId, final ICallback callback) {
        String code = String.format("inframeskr://relation/bothfollow?inviterId=%s", inviterId);
        KouLingServerApi kouLingServerApi = ApiManager.getInstance().createService(KouLingServerApi.class);

        ApiMethods.subscribe(kouLingServerApi.setTokenByCode(code), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    if (callback != null) {
                        callback.onSucess(obj.getData().getString("token"));
                    }
                } else {
                    if (callback != null) {
                        callback.onFailed("", obj.getErrno(), "口令生成失败");
                    }
                }
            }
        });
    }

    public static String genReqFollowKouling(String kouling) {
        StringBuilder sb = new StringBuilder();
        sb.append("【复制消息 打开撕歌skr】").append("\n");
        String name = MyUserInfoManager.getInstance().getNickName();
//                    if (!TextUtils.isEmpty(name)) {
//                        name = name.replaceAll("\\$", "");
//                    }
        sb.append(name).append(" 想添加你为好友，来 撕歌skr 一起嗨唱躁不停，等你喔~").append("\n");
        sb.append("——————————").append("\n");
        sb.append("邀请口令:").append("$").append(kouling).append("$").append("\n");
        sb.append("撕歌skr 下载地址:http://a.app.icon_qq.com/o/simple.jsp?pkgname=com.zq.live").append("\n");
        if (MyLog.isDebugLogOpen()) {
            sb.append("仅debug才显示本条,不同环境口令不互通,环境=").append(U.getChannelUtils().getChannel());
        }

        return sb.toString();
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
            MyLog.d(TAG, "tryParseScheme kouling=" + kouling);
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
        return;
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
            int e = str.lastIndexOf("$");
            if (e >= 0) {
                str = str.substring(0, e);
                int b = str.lastIndexOf("$");
                if (b >= 0) {
                    str = str.substring(b + 1);
                    return str;
                }
            }
        }
        return null;
    }
}
