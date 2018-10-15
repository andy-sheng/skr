package com.common.milink;

import com.common.core.channel.HostChannelManager;
import com.mi.milink.sdk.base.Global;

/**
 * Created by zyh on 2017/8/3.
 */

public class MilinkUtils {

    public static String QUA_HEAD = "v1-";

    public static String getQua() {
        // never change the order of these values
        StringBuilder qua = new StringBuilder();
        qua.append(QUA_HEAD);
        qua.append("android");
        qua.append("-");
        qua.append(Global.getClientAppInfo().getAppName());
        qua.append("-");
        qua.append(Global.getClientAppInfo().getVersionName());
        qua.append("-");
        qua.append(Global.getClientAppInfo().getReleaseChannel());
        qua.append("-");
        qua.append(Global.getClientAppInfo().getLanguageCode());
        qua.append("-");
        qua.append(HostChannelManager.getInstance().getChannelId());
        qua.append("-");
        qua.append(Global.getMiLinkVersion());
        return qua.toString().toLowerCase();
    }

}
