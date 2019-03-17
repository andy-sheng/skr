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

    public static void genJoinGameKouling(int inviterId, int gameId) {
        StringBuilder sb = new StringBuilder();
        sb.append("复制整段信息，打开【撕歌Skr】查看。");
        sb.append("我开了个房间，一起来撕歌吧。");
//        String info = String.format("inframeskr://room/grabjoin?owner=%d&gameId=%d&ts=%s", inviterId, gameId, System.currentTimeMillis());
        String info = String.format("inframeskr://game/grabmatch?tagId=2");
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
                String scheme = new String(bytes, "utf-8");
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
}
