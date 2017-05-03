package com.base.ipselect;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangli on 16-11-3.
 *
 * @module 域名解析(拉流)
 */
public class WatchIpSelectionHelper extends BaseIpSelectionHelper {
    private static final String TAG = "WatchIpSelectionHelper";

    private boolean mIsDropRate = false;

    public WatchIpSelectionHelper(Context context, IDnsStatusListener dnsStatusListener) {
        super(context, dnsStatusListener);
    }

    @Override
    public String getTAG() {
        return TAG;
    }

    private boolean needSetHost() {
        return PreDnsManager.INSTANCE.needSetHost(mHost, mProtocol);
    }

    private String dropRateVideoUrl(String videoUrl) {
        int index = videoUrl.lastIndexOf(".flv");
        if (index >= 0 && !videoUrl.contains("_400.flv")) {
            return videoUrl.substring(0, index) + "_400" + videoUrl.substring(index);
        }
        return videoUrl;
    }

    @Override
    protected List<String> generatePortList() {
        List<String> portList = super.generatePortList();
        if (mProtocol.equals("http")) {
            if (portList == null) {
                portList = new ArrayList<>();
                portList.add("80");
            } else if (portList.isEmpty()) {
                portList.add("80");
            }
        }
        return portList;
    }

    @Override
    public String generateUrlForIp(String originalStreamUrl, String host, String selectedIp) {
        String url = originalStreamUrl;
        int start = originalStreamUrl.indexOf(host);
        if (start != -1) {
            if (!TextUtils.isEmpty(selectedIp)) { // 替换IP
                if (needSetHost()) { // 若needSetHost为true，则不带域名，否则需要带
                    url = originalStreamUrl.substring(0, start) + selectedIp +
                            originalStreamUrl.substring(start + host.length());
                } else {
                    url = originalStreamUrl.substring(0, start) + selectedIp +
                            "/" + mHost +
                            originalStreamUrl.substring(start + host.length());
                }
            } else { // 原始域名，加入端口
                List<String> portList = generatePortList();
                if (portList != null && !portList.isEmpty()) {
                    url = originalStreamUrl.substring(0, start) + host +
                            ":" + portList.get(0) +
                            originalStreamUrl.substring(start + host.length());
                }
            }
        }
        if (mIsDropRate) {
            return dropRateVideoUrl(url);
        }
        return url;
    }

    @Override
    public String getStreamHost() {
        return needSetHost() ? mHost : "";
    }
}
