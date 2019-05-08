package com.zq.mediaengine.util;

import android.util.Log;

import com.zq.mediaengine.framework.CredtpModel;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @hide
 */

public class CredtpWrapper {
    private static String TAG = "CredtpWrapper";

    static {
        LibraryLoader.load();
    }

    private static CredtpWrapper sInstance;

    public static CredtpWrapper getInstance() {
        if (sInstance == null) {
            synchronized (CredtpWrapper.class) {
                if (sInstance == null) {
                    sInstance = new CredtpWrapper();
                }
            }
        }
        return sInstance;

    }

    private List<CredtpModel> mCredtpList;
    private Map<Integer, CredtpModel> mCredtpMaps;
    private Map<Integer, String> mCredtpInfos;

    private CredtpWrapper() {
        mCredtpList = getContentList();
        mCredtpMaps = new HashMap<>();
        mCredtpInfos = new HashMap<>();
        int length = mCredtpList.size();
        if (mCredtpList != null) {
            for (int i = 0; i < length; i++) {
                CredtpModel filterShaderModel = mCredtpList.get(i);
                mCredtpMaps.put(filterShaderModel.getType(), filterShaderModel);
            }
        }
    }

    public String getCredtpByType(final int type) {
        byte[] result = new byte[0];
        if (mCredtpInfos.containsKey(type)) {
            return mCredtpInfos.get(type);
        }

        if (mCredtpMaps.containsKey(type)) {
            CredtpModel model = mCredtpMaps.get(type);

            byte[] resultByte = Hex.decodeHex(model.getBody().toCharArray());
            try {
                result = CredtpUtil.decrypt(resultByte,
                        model.getKey().getBytes(CredtpUtil.STRINGENCODE));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String decryptInfo = new String(result);
            addDecryptInfo(type, decryptInfo);
            return decryptInfo;
        } else {
            Log.w(TAG, "do not have the filter shader:" + String.valueOf(type));
        }
        return null;
    }

    private void addDecryptInfo(int type, String decryptInfo) {
        mCredtpInfos.put(type, decryptInfo);
    }

    private native List<CredtpModel> getContentList();
}
