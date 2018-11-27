package com.engine.agora;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.ViewGroup;

import com.common.log.MyLog;
import com.common.utils.U;
import com.engine.EngineManager;
import com.engine.agora.source.PrivateTextureHelper;

import java.util.HashSet;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandlerEx;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.mediaio.AgoraSurfaceView;
import io.agora.rtc.mediaio.AgoraTextureView;
import io.agora.rtc.mediaio.MediaIO;
import io.agora.rtc.video.AgoraVideoFrame;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class AgoraEngineAdapter {
    public final static String TAG = "AgoraEngineAdapter";

    static final String APP_ID = "549ef854ebff41e8848dc288025039e7";

    private static class AgoraEngineAdapterHolder {
        private static final AgoraEngineAdapter INSTANCE = new AgoraEngineAdapter();
    }

    private AgoraEngineAdapter() {

    }

    public static final AgoraEngineAdapter getInstance() {
        return AgoraEngineAdapterHolder.INSTANCE;
    }


    private RtcEngine mRtcEngine;
    private Handler mUiHandler = new Handler();
    private HandlerThread mWorkHandler = new HandlerThread("AgoraAdapterWorkThread");
    private AgoraOutCallback mOutCallback;

    /**
     * 所有的回调都在这
     * 注意回调的运行线程，一般都不会在主线程
     */
    IRtcEngineEventHandlerEx mCallback = new AgoraEngineCallbackWithLog(TAG) {
        /**
         * 有用户加入频道
         * @param uid
         * @param elapsed
         */
        @Override
        public void onUserJoined(final int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
            if (mOutCallback != null) {
                mOutCallback.onUserJoined(uid,elapsed);
            }
        }

        @Override
        public void onUserOffline(final int uid, int reason) {
            super.onUserOffline(uid, reason);
            if (mOutCallback != null) {
                mOutCallback.onUserOffline(uid,reason);
            }
        }

        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) { // Tutorial Step 5
            // 一般可以在这里绑定视图
            if (mOutCallback != null) {
                mOutCallback.onFirstRemoteVideoDecoded(uid,width,height,elapsed);
            }
        }

        @Override
        public void onUserMuteVideo(final int uid, final boolean muted) { // Tutorial Step 10
            if (mOutCallback != null) {
                mOutCallback.onUserMuteVideo(uid,muted);
            }
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
            if(mOutCallback!=null){
                mOutCallback.onJoinChannelSuccess(channel,uid,elapsed);
            }
        }
    };

    public void setOutCallback(AgoraOutCallback outCallback) {
        this.mOutCallback = outCallback;
    }
    /**
     * 初始化引擎
     */
    void tryInit() {
        if (mRtcEngine == null) {
            synchronized (this) {
                try {
                    if (mRtcEngine == null) {
                        mRtcEngine = RtcEngine.create(U.app(), APP_ID, mCallback);
                        // 模式为广播
                        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
                        // 开启视频
                        mRtcEngine.enableVideo();
                        // 开关视频双流模式。
                        mRtcEngine.enableDualStreamMode(true);

                        // 音视频自采集
                        mRtcEngine.setExternalAudioSource(
                                true,      // 开启外部音频源
                                44100,     // 采样率，可以有8k，16k，32k，44.1k和48kHz等模式
                                1          // 外部音源的通道数，最多2个
                        );

                        mRtcEngine.setExternalVideoSource(
                                true,      // 是否使用外部视频源
                                false,      // 是否使用texture作为输出
                                true        // true为使用推送模式；false为拉取模式，但目前不支持
                        );
                    }
                } catch (Exception e) {
                    MyLog.e(TAG, Log.getStackTraceString(e));

                    throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
                }
            }
        }
    }

    /**
     * 销毁
     */
    public void destroy() {
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
        }
        mUiHandler.removeCallbacksAndMessages(null);
        mRtcEngine = null;
    }

    /**
     * 开启视频预览。
     * <p>
     * 该方法用于在进入频道前启动本地视频预览。调用该 API 前，必须：
     * <p>
     * 调用 enableVideo 开启视频功能。
     * 调用 setupLocalVideo 设置预览窗口及属性。
     */
    public void startPreview() {
        if (mRtcEngine != null) {
            mRtcEngine.startPreview();
        }
    }

    /**
     * 关闭预览
     */
    public void stopPreview() {
        if (mRtcEngine != null) {
            mRtcEngine.stopPreview();
        }
    }

    /**
     * 设置频道模式为通信
     */
    public void setCommunicationMode() {
        tryInit();
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
        tryInit();
        MyLog.d(TAG,"joinChannel" + " token=" + token + " channelId=" + channelId + " extra=" + extra + " uid=" + uid);
        // 一定要设置一个角色
        mRtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
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
        tryInit();
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
        tryInit();
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
        tryInit();
        SurfaceView surfaceView = RtcEngine.CreateRendererView(U.app());
        surfaceView.setZOrderMediaOverlay(true);
        container.addView(surfaceView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, 0));
    }

    /**
     * 绑定远端视图
     * 如果uid传的是非法值，则会缓存view
     * 自动等第一个合法的uid出现时（onJoinUser时）
     * 再bind视图
     * <p>
     * 如果uid是用我们自己的账号体系，那这里是可以提前分配view吧
     *
     * @param container
     * @param uid
     */
    public void bindRemoteVideo(int uid, ViewGroup container) {
        MyLog.d(TAG, "bindRemoteVideo" + " container=" + container + " uid=" + uid);
        tryInit();
        if (container.getChildCount() >= 1) {
            // 只绑定一个
            return;
        }
        if (uid <= 0) {
            return;
        }
        MyLog.d(TAG, "setupRemoteVideo");

        SurfaceView surfaceView = RtcEngine.CreateRendererView(U.app());
        container.addView(surfaceView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, uid));
        surfaceView.setTag(uid);
    }

    /**
     * 方法设置远端视频渲染器。实时通讯过程中，Agora SDK 通常会启动默认的视频渲染器进行视频渲染。
     * 当需要自定义视频渲染设备时，App 可以先通过 IVideoSink 自定义渲染器，然后调用该方法将视频渲染器加入到 SDK 中
     *
     * @param userId
     */
    public void setRemoteVideoRenderer(int userId, TextureView textureView) {
        PrivateTextureHelper privateTextureHelper = new PrivateTextureHelper(textureView.getContext(), textureView);
        privateTextureHelper.init(null);
        privateTextureHelper.setBufferType(MediaIO.BufferType.BYTE_ARRAY);
        privateTextureHelper.setPixelFormat(MediaIO.PixelFormat.I420);

        mRtcEngine.setRemoteVideoRenderer(userId, privateTextureHelper);
    }

    /**
     * 外部推送音频帧
     *
     * @param data
     * @param ts
     * @return
     */
    public int pushExternalAudioFrame(byte[] data, long ts) {
        tryInit();
        if (mRtcEngine != null) {
            int pushState = mRtcEngine.pushExternalAudioFrame(data, ts);
            return pushState;
        }
        return -1;
    }

    /**
     * 推送视频帧
     *
     * @param textureId
     */
    public final void pushExternalVideoFrame(int textureId) {
        tryInit();
        if (mRtcEngine != null) {
            EGLContext context = ((EGL10) EGLContext.getEGL()).eglGetCurrentContext();
            // 转换矩阵 4x4
            float[] UNIQUE_MAT = {
                    1.0f, 0.0f, 0.0f, 0.0f,
                    0.0f, 1.0f, 0.0f, 0.0f,
                    0.0f, 0.0f, 1.0f, 0.0f,
                    0.0f, 0.0f, 0.0f, 1.0f
            };
            AgoraVideoFrame vf = new AgoraVideoFrame();
            vf.format = AgoraVideoFrame.FORMAT_TEXTURE_2D;
            vf.timeStamp = System.currentTimeMillis();
            vf.stride = 1080;
            vf.height = 1920;
            vf.textureID = textureId;
            vf.syncMode = true;
            vf.eglContext11 = context;
            vf.transform = UNIQUE_MAT;
            boolean pushState = mRtcEngine.pushExternalVideoFrame(vf);
            Log.d(TAG, "pushExternalVideoFrame pushState " + pushState);
        }
    }

}
