package com.wali.live.livesdk.live.common;

import android.text.TextUtils;

import com.mi.live.data.assist.Attachment;

/**
 * Created by lizhigang on 15/4/15.
 */
public class MessageType {
    //message 带的消息类型, 先根据message 带的消息类型确定, 如果没有带再根据message body的mimeType确定消息类型
    //1 text 2 img 3 audio 4 video 5 magic emotion
    public static final int TYPE_TEXT = 1;

    public static final int TYPE_IMAGE = 2;

    public static final int TYPE_AUDIO = 3;

    public static final int TYPE_VIDEO = 4;

    public static final int TYPE_MAGIC_EMOTION = 5;

    // 平文本，始终应该是最小的。
    public static final int PLAIN_TEXT = 1;

    public static final int IMAGE = 2;

    public static final int AUDIO = 3;

    public static final int VIDEO = 4;

    public static final int IMAGE_GIF = 5;

    public static final int OFFLINE_FILE = 54;

    public static final int OTHERS = 100;

    public static final int UNKNOWN = 101;

    public static final int MUSIC = 102;

    public static final int SUB_NONE = 0;

    public static final int SUB_IMAGE_PICK = 1;

    public static final int SUB_IMAGE_TAKE = 2;

    public static final int CALL_LOG = 6;

    public static final int AUDIO_SPX = 10;

    public static final int WEIBO_SPX = 11;

    public static final int MAGIC_EMOTION = 12;

    public static boolean isImage(int type) {
        return type == MessageType.IMAGE || type == IMAGE_GIF;
    }

    public static boolean isAudio(int type) {
        return type == AUDIO;
    }

    public static boolean isVideo(int type) {
        return type == VIDEO;
    }

    public static int getMessageType(String mimeType) {
        if (!TextUtils.isEmpty(mimeType)) {
            if (Attachment.isAudioMimeType(mimeType)) {
                return MessageType.AUDIO;
            } else if (Attachment.isImageMimeType(mimeType)) {
                return MessageType.IMAGE;
            } else if (Attachment.isVideoMimeType(mimeType)) {
                return MessageType.VIDEO;
            }
        }
        return MessageType.PLAIN_TEXT;
    }

    public static int getMessageTypeFromProto(int type) {
        switch (type) {
            case TYPE_TEXT:
                return PLAIN_TEXT;
            case TYPE_IMAGE:
                return IMAGE;
            case TYPE_AUDIO:
                return AUDIO;
            case TYPE_VIDEO:
                return VIDEO;
            case TYPE_MAGIC_EMOTION:
                return MAGIC_EMOTION;
            default:
                return 0;
        }
    }

    public static int getMessageTypeToProto(int type) {
        switch (type) {
            case PLAIN_TEXT:
                return TYPE_TEXT;
            case IMAGE:
                return TYPE_IMAGE;
            case AUDIO:
                return TYPE_AUDIO;
            case VIDEO:
                return TYPE_VIDEO;
            case MAGIC_EMOTION:
                return TYPE_MAGIC_EMOTION;
            default:
                return 0;
        }
    }
}
