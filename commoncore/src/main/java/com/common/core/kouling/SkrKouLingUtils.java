package com.common.core.kouling;

import android.net.Uri;
import android.text.TextUtils;

import com.common.clipboard.ClipboardUtils;
import com.common.core.scheme.processor.ProcessResult;
import com.common.core.scheme.processor.ZqSchemeProcessorManager;
import com.common.log.MyLog;
import com.common.utils.U;

import java.io.UnsupportedEncodingException;

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
    public static void genJoinGameKouling(int inviterId, int gameId) {
        StringBuilder sb = new StringBuilder();
        sb.append("复制整段信息，打开【撕歌Skr】查看。");
        sb.append("我开了个房间，一起来撕歌吧。");
        String info = String.format("t=1&u=%d&r=%d", inviterId, gameId);
        try {
            info = U.getBase64Utils().encode(info.getBytes("utf-8"));
            sb.append(info);
        } catch (UnsupportedEncodingException e) {
            sb.append(info);
        }
        sb.append("我开了个房间，一起来撕歌吧。");
        sb.append("还没安装【撕歌Skr】？点击安装");
        sb.append("http://a.app.qq.com/o/simple.jsp?pkgname=com.zq.live");
        ClipboardUtils.setCopy(sb.toString());
        U.getToastUtil().showLong("已复制到粘贴板，快去微信或QQ发送给好友吧");
    }

    public static boolean tryParseScheme(String str) {
        if (!TextUtils.isEmpty(str)) {
            // 跳到 是否需要跳一个activity
            String base64Scheme = U.getStringUtils().getLongestBase64SubString(str);
            MyLog.d(TAG, "tryParseScheme" + " base64Scheme=" + base64Scheme);
            byte[] bytes = U.getBase64Utils().decode(base64Scheme);
            try {
                String kouling = new String(bytes, "utf-8");
                String scheme = parseKouling2Scheme(kouling);
                MyLog.d(TAG, "tryParseScheme" + " scheme=" + scheme);
                if (!TextUtils.isEmpty(scheme)) {
                    Uri uri = Uri.parse(scheme);
                    ProcessResult processResult = ZqSchemeProcessorManager.getInstance().process(uri, U.getActivityUtils().getTopActivity(), false);
                    if (processResult != ProcessResult.NotAccepted) {
                        return true;
                    } else {
                        return false;
                    }
                }
            } catch (UnsupportedEncodingException e) {

            }
        }
        return false;
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
}
