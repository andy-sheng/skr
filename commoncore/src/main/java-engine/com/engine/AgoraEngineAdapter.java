package com.engine;

import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.common.log.MethodCallLogProxy;
import com.common.log.MyLog;
import com.common.utils.U;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandlerEx;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class AgoraEngineAdapter {
    public final static String TAG = "AgoraEngineAdapter";

    static final String APP_ID = "549ef854ebff41e8848dc288025039e7";

    private static class AgoraEngineAdapterHolder {
        private static final AgoraEngineAdapter INSTANCE = new AgoraEngineAdapter();
    }

    RtcEngine mRtcEngine;

    /**
     * 所有的回调都在这
     */
    IRtcEngineEventHandlerEx mCallback = new AgoraEngineCallbackWithLog(TAG) {

        /**
         * 有用户加入频道
         * @param uid
         * @param elapsed
         */
        @Override
        public void onUserJoined(int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);

        }
    };

    private AgoraEngineAdapter() {
        init();
    }

    public static final AgoraEngineAdapter getInstance() {
        return AgoraEngineAdapterHolder.INSTANCE;
    }

    void init() {
        if (mRtcEngine == null) {
            try {

//                IRtcEngineEventHandlerEx proxy = MethodCallLogProxy.attach(mCallback, TAG);
                mRtcEngine = RtcEngine.create(U.app(), APP_ID, mCallback);
            } catch (Exception e) {
                MyLog.e(TAG, Log.getStackTraceString(e));

                throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
            }
        }
    }

    /**
     * 设置频道模式为通信
     */
    public void setCommunicationMode() {
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
    }

    /**
     * 传入能标识用户角色和权限的 Token。如果安全要求不高，也可以将值设为 null。
     * Token 需要在应用程序的服务器端生成。
     * 传入能标识频道的频道 ID。输入相同频道 ID 的用户会进入同一个频道。
     * <p>
     * 频道内每个用户的 UID 必须是唯一的。如果将 UID 设为 0，系统将自动分配一个 UID。
     * 如果已在频道中，用户必须调用 leaveChannel 方法退出当前频道，才能进入下一个频道。
     */
    public void joinChannel(String token, String channelId, String extra, int uid) {
        mRtcEngine.joinChannel(token, channelId, extra, uid);
    }

    /**
     * 调用 enableVideo 方法打开视频模式。在 Agora SDK 中，
     * 音频功能是默认打开的，因此在加入频道前，或加入频道后，你都可以调用该方法开启视频。
     * <p>
     * 如果在加入频道前打开，则进入频道后直接加入视频通话或直播。
     * 如果在通话或直播过程中打开，则由纯音频切换为视频通话或直播。
     */
    public void enableVideo() {
        mRtcEngine.enableVideo();
    }

    /**
     * 在该方法中，指定你想要的视频编码的分辨率、帧率、码率以及视频编码的方向模式。
     * 详细的视频编码参数定义，参考 setVideoEncoderConfiguration 中的描述。
     * <p>
     * 该方法设置的参数为理想情况下的最大值。当视频引擎因网络等原因无法达到设置的分辨率、帧率或码率值时，会取最接近设置值的最大值。
     * 如果设备的摄像头无法支持定义的视频属性，SDK 会为摄像头自动选择一个合理的分辨率。
     * 该行为对视频编码没有影响，编码时 SDK 仍沿用该方法中定义的分辨率。
     */
    public void setVideoEncoderConfiguration() {

        VideoEncoderConfiguration.VideoDimensions dimensions = new VideoEncoderConfiguration.VideoDimensions(360, 640);
        VideoEncoderConfiguration.FRAME_RATE frameRate = VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_24;
        int bitrate = VideoEncoderConfiguration.STANDARD_BITRATE;
        VideoEncoderConfiguration.ORIENTATION_MODE orientationMode = VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT;

        VideoEncoderConfiguration videoEncoderConfiguration = new VideoEncoderConfiguration(dimensions, frameRate, bitrate, orientationMode);

        mRtcEngine.setVideoEncoderConfiguration(videoEncoderConfiguration);
    }

    /**
     * 绑定本地视图view
     *
     * @param container 容器
     */
    public void bindLocalVideoView(ViewGroup container) {
        SurfaceView surfaceView = RtcEngine.CreateRendererView(U.app());
        surfaceView.setZOrderMediaOverlay(true);
        container.addView(surfaceView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, 0));
    }

    /**
     * 绑定远端视图
     *
     * @param container
     * @param uid
     */
    private void setupRemoteVideo(ViewGroup container, int uid) {
        if (container.getChildCount() >= 1) {
            return;
        }

        SurfaceView surfaceView = RtcEngine.CreateRendererView(U.app());
        container.addView(surfaceView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, uid));
        surfaceView.setTag(uid);
    }

}
