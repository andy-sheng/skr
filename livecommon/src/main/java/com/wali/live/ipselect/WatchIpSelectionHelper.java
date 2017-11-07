package com.wali.live.ipselect;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.base.preference.PreferenceUtils;
import com.base.utils.Constants;
import com.mi.live.data.report.keyflow.KeyFlowReportManager;
import com.wali.live.dns.IDnsStatusListener;
import com.wali.live.dns.PreDnsManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangli on 16-11-3.
 *
 * @module 域名解析(拉流)
 */
public class WatchIpSelectionHelper extends BaseIpSelectionHelper {
    private static final String TAG = "WatchIpSelectionHelper";

    private boolean mIsForceDropRate = PreferenceUtils.getSettingBoolean(com.base.global.GlobalData.app(),
            PreferenceUtils.KEY_DEBUG_DROP_BITRATE, false);
    private boolean mIsDropRate = false;

    @Override
    public String getTAG() {
        return TAG;
    }

    public boolean hasStreamUrl() {
        return !TextUtils.isEmpty(mOriginalStreamUrl);
    }

    public WatchIpSelectionHelper(
            @NonNull Context context,
            IDnsStatusListener dnsStatusListener,
            KeyFlowReportManager keyFlowReporter) {
        super(context, dnsStatusListener, keyFlowReporter);
    }

    private boolean needSetHost() {
        return PreDnsManager.INSTANCE.needSetHost(mHost, mProtocol);
    }

    @Override
    protected String generateUrlForGuaranteeIp(String host, String originalStreamUrl) {
        int start = originalStreamUrl.indexOf(host);
        if (start != -1) {
            int end = originalStreamUrl.lastIndexOf(".flv");
            if (start <= end) {
                return originalStreamUrl.substring(start, end) + ".flv";
            }
        }
        return null;
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
    public String generateUrlForIp(
            @NonNull String originalStreamUrl,
            @NonNull String host,
            String selectedIp) {
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
        if (mIsDropRate || mIsForceDropRate) {
            return dropRateVideoUrl(url);
        }
        return url;
    }

    @Override
    protected void incrementFetchGuaranteeIpCnt() {
        super.incrementFetchGuaranteeIpCnt();
        if (Constants.isGooglePlayBuild && mFetchGuaranteeIpCnt == 2) { // Note: 降低码率会延迟到下次generateUrlForIp被调用才生效
            mIsDropRate = true;
        }
    }

    @Override
    public String getStreamHost() {
        return needSetHost() ? mHost : "";
    }
}
