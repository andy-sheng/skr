package com.wali.live.watchsdk.endlive;

import android.os.Bundle;

/**
 * Created by jiyangli on 16-7-6.
 */
public interface IEndLiveModel {
    void initData(Bundle bundle);
    long getOwnerId();
    long getAvaTarTs();
    long getUuid();
    long getZuid();
    int getViewerCount();
    int getOwnerCertType();
    String getRoomId();
    String getNickName();
    boolean isFocused();
    int getLiveType();
}
