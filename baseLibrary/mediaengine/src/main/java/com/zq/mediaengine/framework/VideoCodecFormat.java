package com.zq.mediaengine.framework;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;

/**
 * Encode params for video.
 */
public class VideoCodecFormat {

    public static final int ENCODE_SCENE_DEFAULT = 0;
    public static final int ENCODE_SCENE_SHOWSELF = 1;
    public static final int ENCODE_SCENE_GAME = 2;

    public static final int ENCODE_PROFILE_DEFAULT = 0;
    public static final int ENCODE_PROFILE_LOW_POWER = 1;
    public static final int ENCODE_PROFILE_BALANCE = 2;
    public static final int ENCODE_PROFILE_HIGH_PERFORMANCE = 3;

    //MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ;
    public static final int ENCODE_BITRATE_MODE_CQ = 0;
    //MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR
    public static final int ENCODE_BITRATE_MODE_VBR = 1;
    //MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR
    public static final int ENCODE_BITRATE_MODE_CBR = 2;

    /**
     * Pixel format of input video frame, see AVConst.PIX_FMT_XXX
     */
    public int pixFmt;
    /**
     * Video codec id, see AVConst.CODEC_ID_XXX
     */
    public int codecId;
    /**
     * Width of input video frame
     */
    public int width;
    /**
     * Height of input video frame
     */
    public int height;
    /**
     * orientation of current frame, 0/90/180/270 is valid value
     */
    public int orientation;
    /**
     * Video bitrate in bps
     */
    public int bitrate;
    /**
     * Video frame rate
     */
    public float frameRate;
    /**
     * Video encode key frame interval
     */
    public float iFrameInterval;
    /**
     * Video encode profile, see ENCODE_PROFILE_XXX
     */
    public int profile;
    /**
     * Video encode scene, see ENCODE_SCENE_XXX
     */
    public int scene;
    /**
     * Video encode crf value, used in CQ mode
     */
    public int crf;
    /**
     * Is in live streaming mode
     */
    public boolean liveStreaming;
    /**
     * Video encode bitrate mode, see ENCODE_BITRATE_MODE_XXX
     */
    public int bitrateMode;
    /**
     * ptr of AVCodecParameters
     */
    public long avCodecParPtr;

    public VideoCodecFormat(int codecId, int width, int height, int bitrate) {
        this.codecId = codecId;
        this.width = width;
        this.height = height;
        this.orientation = 0;
        this.bitrate = bitrate;
        this.frameRate = 15.0f;
        this.iFrameInterval = 5.0f;
        this.scene = ENCODE_SCENE_SHOWSELF;
        this.profile = ENCODE_PROFILE_LOW_POWER;
        this.pixFmt = AVConst.PIX_FMT_I420;
        this.crf = 23;
        this.liveStreaming = true;
        this.bitrateMode = ENCODE_BITRATE_MODE_VBR;
        this.avCodecParPtr = 0;
    }

    public VideoCodecFormat(VideoCodecFormat format) {
        codecId = format.codecId;
        width = format.width;
        height = format.height;
        orientation = format.orientation;
        bitrate = format.bitrate;
        frameRate = format.frameRate;
        iFrameInterval = format.iFrameInterval;
        scene = format.scene;
        profile = format.profile;
        pixFmt = format.pixFmt;
        crf = format.crf;
        liveStreaming = format.liveStreaming;
        bitrateMode = format.bitrateMode;
        avCodecParPtr = format.avCodecParPtr;
    }

    public MediaFormat toMediaFormat() {
        String mime;
        if (codecId == AVConst.CODEC_ID_AVC) {
            mime = "video/avc";
        } else if (codecId == AVConst.CODEC_ID_HEVC) {
            mime = "video/hevc";
        } else {
            throw new IllegalArgumentException("Only aac supported");
        }

        MediaFormat mediaFormat = MediaFormat.createVideoFormat(mime,
                (width + 15) / 16 * 16, (height + 1) / 2 * 2);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        mediaFormat.setInteger("bitrate-mode", bitrateMode);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE,
                (int) (frameRate + 0.5f));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,
                    (int) (iFrameInterval + 0.5f));
        } else {
            mediaFormat.setFloat(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval);
        }
        // avc profile
        int profile = MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline;
        if (codecId == AVConst.CODEC_ID_AVC) {
            int level = MediaCodecInfo.CodecProfileLevel.AVCLevel31;
            if (width * height > 1280 * 720) {
                level = MediaCodecInfo.CodecProfileLevel.AVCLevel4;
            }
            switch (this.profile) {
                case VideoCodecFormat.ENCODE_PROFILE_HIGH_PERFORMANCE:
                    profile = MediaCodecInfo.CodecProfileLevel.AVCProfileHigh;
                    break;
                case VideoCodecFormat.ENCODE_PROFILE_BALANCE:
                    profile = MediaCodecInfo.CodecProfileLevel.AVCProfileMain;
                    break;
                default:
                    break;
            }
            mediaFormat.setInteger("profile", profile);
            mediaFormat.setInteger("level", level);
        }
        return mediaFormat;
    }
}
