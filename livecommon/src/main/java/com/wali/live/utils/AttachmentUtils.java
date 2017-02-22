package com.wali.live.utils;

import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.preference.PreferenceUtils;
import com.wali.live.common.MessageType;

public class AttachmentUtils {

    private final static long MIN_ATTACHMENT_BASE_ID = 10240;

    public synchronized static long generateAttachmentId() {
        final long preferenceBaseId = PreferenceUtils.getSettingLong(
                GlobalData.app(),
                PreferenceUtils.PREF_KEY_ATTACHMENT_BASE_ID,
                MIN_ATTACHMENT_BASE_ID);
        long baseId = Math.max(System.currentTimeMillis(), preferenceBaseId) + 1;
        PreferenceUtils.setSettingLong(GlobalData.app(),
                PreferenceUtils.PREF_KEY_ATTACHMENT_BASE_ID, baseId);
        return baseId;
    }

    public static String getMimeType(int messageType, String fileName) {
        String extension;
        int dotIndex = -1;
        if (!TextUtils.isEmpty(fileName)) {
            dotIndex = fileName.lastIndexOf('.');
            if (dotIndex < 0) {
                dotIndex = fileName.lastIndexOf("+");// 因为diskCache把逗号变成了+号
            }
        }
        if (dotIndex < 0)
            extension = "";
        else
            extension = fileName.substring(dotIndex + 1);
        String type = "";
        switch (messageType) {
            case MessageType.IMAGE:
                type = FileUtils.getFileType(fileName);
                if (TextUtils.isEmpty(type)) {
                    type = extension;
                }
                return "image/" + type;
            case MessageType.AUDIO:
                type = FileUtils.getFileType(fileName);
                if (TextUtils.isEmpty(type)) {
                    type = extension;
                }
                return "audio/" + type;
            case MessageType.VIDEO:
                type = FileUtils.getFileType(fileName);
                if (TextUtils.isEmpty(type)) {
                    type = extension;
                }
                return "video/" + type;
        }

        return "";
    }
}
