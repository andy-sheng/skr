package com.mi.liveassistant.attachment;

import android.text.TextUtils;

import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.common.preference.PreferenceKeys;
import com.mi.liveassistant.common.preference.PreferenceUtils;
import com.mi.liveassistant.proto.CommonProto;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by lan on 15-11-18.
 */
public class Attachment implements Serializable, Jsonable {
    private static final String TAG = Attachment.class.getSimpleName();
    public static final String PLAIN_TEXT_MIME_TYPE = "text/plain";
    public static final String IMAGE_JPEG_MIME_TYPE = "image/jpeg";

    //-----att to json 字段－－－
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_TEXT = "text";
    public static final String FIELD_URL = "url";
    public static final String FIELD_DURATION = "duration";
    public static final String FIELD_SIZE = "size";
    public static final String FIELD_WIDTH = "width";
    public static final String FIELD_HEIGTH = "height";
    public static final String FIELD_LOCAL_PATH = "localPath";
    public static final String FIELD_MIME_TYPE = "mimeType";
    public static final String FIELD_FILE_NAME = "filename";
    public static final String FIELD_ATT_ID = "attId";
    public static final String FIELD_RESOURCE_ID = "resourceId";
    public static final String FIELD_OBJECT_KEY = "objectKey";
    public static final String FIELD_BUKET_NAME = "buketName";
    public static final String FIELD_IS_ORIGINAL = "isOriginal"; //针对图片来说，发送图片和原图很关键，比如发送图片不是原图则在看大图的时候需要显示下载原图
    public static final String FIELD_MD5 = "md5";

    //---------------------------------------


    public static final int AUTH_TYPE_DEFAULT = 0;
    public static final int AUTH_TYPE_AVATAR = 1;
    public static final int AUTH_TYPE_FEED_BACK = 2;//直播.日志
    public static final int AUTH_TYPE_PIC = 3;
    public static final int AUTH_TYPE_ANIMATION = 4;
    public static final int AUTH_TYPE_USER_PIC = 5;//直播.用户图片
    public static final int AUTH_TYPE_USER_ID = 6;//直播.用户审核资料
    public static final int AUTH_TYPE_USER_VIDEO = 7;//直播视频
    public static final int AUTH_TYPE_USER_WALLPAPER = 8;//直播.用户壁纸

    public static final int TYPE_TEXT = 1;
    public static final int TYPE_IMAGE = 2;
    public static final int TYPE_IMAGE_IC_CARD = 3;
    public static final int TYPE_IMAGE_OTHER_CARD = 4;

    private int type;
    private int cardType;
    public int authType = Attachment.AUTH_TYPE_DEFAULT;


    private String text;
    public String url;
    private int duration;
    private int size; //卧槽　这是搞啥，fileSize　和它什么关系
    private long extType;
    private byte[] extData;
    public int width;
    public int height;

    // 以下是用于上传视频
    public String localPath;
    public String mimeType;
    public String filename;
    public long fileSize;
    public long attId;
    public String resourceId;
    public String objectKey;
    public String bucketName;
    public boolean isOriginal = true;
    public int fromSoucre; //指定是哪个界面上传的att，便于发出事件区分
    public String md5; // 文件的md5


    public Attachment() {
        mimeType = PLAIN_TEXT_MIME_TYPE;
        attId = generateAttachmentId();
    }

    public Attachment(CommonProto.Attachment protoAttachment) {
        parse(protoAttachment);
        attId = generateAttachmentId();
    }

    public void parse(CommonProto.Attachment protoAttachment) {
        type = protoAttachment.getType();
        text = protoAttachment.getText();
        url = protoAttachment.getUrl();
        duration = protoAttachment.getDuration();
        size = protoAttachment.getSize();
        extType = protoAttachment.getExttype();
        extData = protoAttachment.getExtdata().toByteArray();
        width = protoAttachment.getWidth();
        height = protoAttachment.getHeight();
    }

    public CommonProto.Attachment.Builder build() {
        CommonProto.Attachment.Builder builder = CommonProto.Attachment.newBuilder();
        builder.setType(type);
        switch (type) {
            case TYPE_TEXT:
                builder.setText(text);
                break;
            case TYPE_IMAGE:
                builder.setUrl(url);
                builder.setWidth(width);
                builder.setHeight(height);
                break;
            default:
                MyLog.d(TAG, "unknown type = " + type);
                break;
        }
        return builder;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getIdentificationCardType() {
        return cardType;
    }

    public void setIdentificationCardType(int type) {
        this.cardType = type;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getExtType() {
        return extType;
    }

    public void setExtType(long extType) {
        this.extType = extType;
    }

    public byte[] getExtData() {
        return extData;
    }

    public void setExtData(byte[] extData) {
        this.extData = extData;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getAttId() {
        return attId;
    }

    public void setAttId(long attId) {
        this.attId = attId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    // resourceId为空，此时，需要上传
    public boolean needUpload() {
        return TextUtils.isEmpty(resourceId);
    }

    public boolean isLocalPathEmpty() {
        return TextUtils.isEmpty(localPath);
    }

    public String getSuffixFromLocalPath() {
        return getSuffixFromFilePath(localPath);
    }

    public boolean isOriginal() {
        return isOriginal;
    }

    public void setIsOriginal(boolean isOriginal) {
        this.isOriginal = isOriginal;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public static Attachment getTextAttachment(String text) {
        Attachment attachment = new Attachment();
        attachment.setType(TYPE_TEXT);
        attachment.setText(text);
        return attachment;
    }

    public static Attachment getImageAttachment(String url, int width, int height) {
        Attachment attachment = new Attachment();
        attachment.setType(TYPE_IMAGE);
        attachment.setUrl(url);
        attachment.setWidth(width);
        attachment.setHeight(height);
        return attachment;
    }

    public static Attachment getImageTestAttachment() {
        Attachment attachment = new Attachment();
        attachment.setType(TYPE_IMAGE);
        attachment.setUrl("http://photocdn.sohu.com/20151111/Img426047671.jpg");
        return attachment;
    }


    public static boolean isTextMimeType(String mimeType) {
        return !TextUtils.isEmpty(mimeType) ? mimeType.toLowerCase().trim().startsWith("text/")
                : false;
    }

    public static boolean isVideoMimeType(String mimeType) {
        return !TextUtils.isEmpty(mimeType) ? mimeType.toLowerCase().trim().startsWith("video/")
                : false;
    }

    public static boolean isAudioMimeType(String mimeType) {
        return !TextUtils.isEmpty(mimeType) ? mimeType.toLowerCase().trim().startsWith("audio/")
                : false;
    }

    public static boolean isImageMimeType(String mimeType) {
        return !TextUtils.isEmpty(mimeType) ? mimeType.toLowerCase().trim().startsWith("image/")
                : false;
    }


    public static boolean isGifMimeType(String mimeType) {
        return !TextUtils.isEmpty(mimeType) ? mimeType.toLowerCase().trim().startsWith("image/gif")
                : false;
    }

    @Override
    public String toJSONString() {
        return toJSONObject().toString();
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(FIELD_TYPE, type);
            if (!TextUtils.isEmpty(text)) {
                jsonObject.put(FIELD_TEXT, text);
            }
            if (!TextUtils.isEmpty(url)) {
                jsonObject.put(FIELD_URL, url);
            }
            jsonObject.put(FIELD_DURATION, duration);
            jsonObject.put(FIELD_SIZE, size);
            jsonObject.put(FIELD_WIDTH, width);
            jsonObject.put(FIELD_HEIGTH, height);
            if (!TextUtils.isEmpty(localPath)) {
                jsonObject.put(FIELD_LOCAL_PATH, localPath);
            }
            if (!TextUtils.isEmpty(mimeType)) {
                jsonObject.put(FIELD_MIME_TYPE, mimeType);
            }
            if (!TextUtils.isEmpty(filename)) {
                jsonObject.put(FIELD_FILE_NAME, filename);
            }
            jsonObject.put(FIELD_SIZE, fileSize == 0 ? getSize() : fileSize);
            jsonObject.put(FIELD_ATT_ID, attId);
            if (!TextUtils.isEmpty(resourceId)) {
                jsonObject.put(FIELD_RESOURCE_ID, resourceId);
            }
            if (!TextUtils.isEmpty(objectKey)) {
                jsonObject.put(FIELD_OBJECT_KEY, objectKey);
            }
            if (!TextUtils.isEmpty(bucketName)) {
                jsonObject.put(FIELD_BUKET_NAME, bucketName);
            }

            jsonObject.put(FIELD_IS_ORIGINAL, isOriginal);

            if (!TextUtils.isEmpty(md5)) {
                jsonObject.put(FIELD_MD5, md5);
            }
        } catch (Exception e) {
            MyLog.e(e);
        }
        return jsonObject;
    }


    @Override
    public boolean parseJSONString(String jsonStr) {
        if (!TextUtils.isEmpty(jsonStr)) {
            try {
                JSONObject result = new JSONObject(jsonStr);
                type = result.optInt(FIELD_TYPE);
                text = result.optString(FIELD_TEXT, "");
                url = result.optString(FIELD_URL, "");
                duration = result.optInt(FIELD_DURATION);
                size = result.optInt(FIELD_SIZE);
                width = result.optInt(FIELD_WIDTH);
                height = result.optInt(FIELD_HEIGTH);
                localPath = result.optString(FIELD_LOCAL_PATH);
                mimeType = result.optString(FIELD_MIME_TYPE);
                filename = result.optString(FIELD_FILE_NAME);
                attId = result.optLong(FIELD_ATT_ID);
                resourceId = result.optString(FIELD_RESOURCE_ID);
                objectKey = result.optString(FIELD_OBJECT_KEY);
                bucketName = result.optString(FIELD_BUKET_NAME);
                isOriginal = result.optBoolean(FIELD_IS_ORIGINAL);
                md5 = result.optString(FIELD_MD5);
                return true;
            } catch (Exception e) {
                MyLog.e(e);
            }
        }
        return false;
    }


    private String middlePicAppendType = "@style@560"; //默认中图大小，但是可以修改
    private String smallPicAppendType = "@style@160"; //默认小图大小，但是可以修改

    public String getSmallPicUrl() {
        return url + smallPicAppendType;
    }

    public String getMiddlePicUrl() {
        return url + middlePicAppendType;
    }

    public String getOriginUrl() {
        return url;
//        return url+"@style@original";//url 指向的是真原图，而@style@original 是被压缩成webp 的图
    }


    public void setMiddlePicAppendType(String middlePicAppendType) {
        this.middlePicAppendType = middlePicAppendType;
    }

    public void setSmallPicAppendType(String smallPicAppendType) {
        this.smallPicAppendType = smallPicAppendType;
    }

    public synchronized static long generateAttachmentId() {
        final long preferenceBaseId = PreferenceUtils.getSettingLong(
                PreferenceKeys.PREF_KEY_ATTACHMENT_BASE_ID,
                10240);
        long baseId = Math.max(System.currentTimeMillis(), preferenceBaseId) + 1;
        PreferenceUtils.setSettingLong(PreferenceKeys.PREF_KEY_ATTACHMENT_BASE_ID, baseId);
        return baseId;
    }

    public static String getSuffixFromFilePath(String path) {
        String extension = "";
        if (!TextUtils.isEmpty(path)) {
            int indexOfSlash = path.lastIndexOf('/');
            int indexOfDot = path.lastIndexOf('.');
            if (indexOfDot > indexOfSlash) {
                extension = path.substring(indexOfDot + 1);
            }
        }
        return extension;
    }
}