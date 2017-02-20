package com.wali.live.ipselect;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.wali.live.dns.IDnsStatusListener;

/**
 * Created by yangli on 16-11-21.
 *
 * @module 域名解析(Feeds)
 */
public class FeedsIpSelectionHelper extends WatchIpSelectionHelper {
    private static final String TAG = "FeedsIpSelectionHelper";

    @Override
    public String getTAG() {
        return TAG;
    }

    public FeedsIpSelectionHelper(
            @NonNull Context context,
            IDnsStatusListener dnsStatusListener) {
        super(context, dnsStatusListener, null);
    }

    @Override
    public void setOriginalStreamUrl(String originalStreamUrl) {
        super.setOriginalStreamUrl(originalStreamUrl);
        if (!TextUtils.isEmpty(originalStreamUrl) &&
                !originalStreamUrl.equals(mOriginalStreamUrl)) { // 本地URI
            mOriginalStreamUrl = originalStreamUrl;
            mProtocol = "";
            mHost = "";
            onNewStreamUrl(false); // 重置内部状态
        }
    }
}
