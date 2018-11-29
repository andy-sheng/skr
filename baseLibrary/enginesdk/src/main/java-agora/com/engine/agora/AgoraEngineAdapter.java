package com.engine.agora;

import android.content.res.AssetManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.ViewGroup;

import com.common.log.MyLog;
import com.common.utils.U;
import com.engine.Params;
import com.engine.agora.effect.EffectModel;
import com.engine.agora.source.PrivateTextureHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;

import io.agora.rtc.Constants;
import io.agora.rtc.IAudioEffectManager;
import io.agora.rtc.IRtcEngineEventHandlerEx;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.mediaio.MediaIO;
import io.agora.rtc.video.AgoraVideoFrame;
import io.agora.rtc.video.ViEAndroidGLES20;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

public class AgoraEngineAdapter {
    public final static String TAG = "AgoraEngineAdapter";

    static final String APP_ID = "549ef854ebff41e8848dc288025039e7";

    private static class AgoraEngineAdapterHolder {
        private static final AgoraEngineAdapter INSTANCE = new AgoraEngineAdapter();
    }

    private AgoraEngineAdapter() {
        tryCopyAssetsEffect2Sdcard();
    }

    public static final AgoraEngineAdapter getInstance() {
        return AgoraEngineAdapterHolder.INSTANCE;
    }

    private Params mConfig;
    private RtcEngine mRtcEngine;
    private Handler mUiHandler = new Handler();
    private HandlerThread mWorkHandler = new HandlerThread("AgoraAdapterWorkThread");
    private AgoraOutCallback mOutCallback;
    private List<EffectModel> mEffectModels = new ArrayList<>();
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
                mOutCallback.onUserJoined(uid, elapsed);
            }
        }

        @Override
        public void onUserMuteAudio(int uid, boolean muted) {
            super.onUserMuteAudio(uid, muted);
            if (mOutCallback != null) {
                mOutCallback.onUserMuteAudio(uid, muted);
            }
        }

        @Override
        public void onUserOffline(final int uid, int reason) {
            super.onUserOffline(uid, reason);
            if (mOutCallback != null) {
                mOutCallback.onUserOffline(uid, reason);
            }
        }

        @Override
        public void onUserMuteVideo(final int uid, final boolean muted) { // Tutorial Step 10
            super.onUserMuteVideo(uid, muted);
            if (mOutCallback != null) {
                mOutCallback.onUserMuteVideo(uid, muted);
            }
        }

        @Override
        public void onUserEnableVideo(int uid, boolean enabled) {
            super.onUserEnableVideo(uid, enabled);
            if (mOutCallback != null) {
                mOutCallback.onUserEnableVideo(uid, enabled);
            }
        }

        @Override
        public void onVideoSizeChanged(int uid, int width, int height, int rotation) {
            super.onVideoSizeChanged(uid, width, height, rotation);
            if (mOutCallback != null) {
                mOutCallback.onVideoSizeChanged(uid, width, height, rotation);
            }
        }

        @Override
        public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onRejoinChannelSuccess(channel, uid, elapsed);
            if (mOutCallback != null) {
                mOutCallback.onRejoinChannelSuccess(channel, uid, elapsed);
            }
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
            if (mOutCallback != null) {
                mOutCallback.onJoinChannelSuccess(channel, uid, elapsed);
            }
        }

        @Override
        public void onLeaveChannel(RtcStats stats) {
            super.onLeaveChannel(stats);
            if (mOutCallback != null) {
                mOutCallback.onLeaveChannel(stats);
            }
        }

        @Override
        public void onClientRoleChanged(int oldRole, int newRole) {
            super.onClientRoleChanged(oldRole, newRole);
            if (mOutCallback != null) {
                mOutCallback.onClientRoleChanged(oldRole, newRole);
            }
        }

        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) { // Tutorial Step 5
            super.onFirstRemoteVideoDecoded(uid, width, height, elapsed);
            // 一般可以在这里绑定视图
            if (mOutCallback != null) {
                mOutCallback.onFirstRemoteVideoDecoded(uid, width, height, elapsed);
            }
        }

    };

    public void setOutCallback(AgoraOutCallback outCallback) {
        this.mOutCallback = outCallback;
    }

    public void init(Params config) {
        mConfig = config;
    }

    /**
     * 初始化引擎
     */
    void tryInitRtcEngine() {
        if (mRtcEngine == null) {
            synchronized (this) {
                try {
                    if (mRtcEngine == null) {
                        mRtcEngine = RtcEngine.create(U.app(), APP_ID, mCallback);
                        // 模式为广播
                        mRtcEngine.setChannelProfile(mConfig.getChannelProfile());

                        if (mConfig.isEnableVideo()) {
                            // 开启视频
                            mRtcEngine.enableVideo();
                            setVideoEncoderConfiguration();
                        }

                        // 开关视频双流模式。
                        mRtcEngine.enableDualStreamMode(true);
                        if (mConfig.isUseCbEngine()) {
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
            mRtcEngine.stopPreview();
        }
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
        }
        RtcEngine.destroy();
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
        tryInitRtcEngine();
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
    }

    public void setClientRole(boolean isAnchor) {
        tryInitRtcEngine();
        if (isAnchor) {
            mRtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        } else {
            mRtcEngine.setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
        }
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
        tryInitRtcEngine();
        MyLog.d(TAG, "joinChannel" + " token=" + token + " channelId=" + channelId + " extra=" + extra + " uid=" + uid);
        // 一定要设置一个角色
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
        tryInitRtcEngine();
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
        VideoEncoderConfiguration.VideoDimensions dimensions = new VideoEncoderConfiguration.VideoDimensions(mConfig.getLocalVideoWidth(), mConfig.getLocalVideoHeight());
        VideoEncoderConfiguration.FRAME_RATE frameRate = VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_24;
        int bitrate = VideoEncoderConfiguration.STANDARD_BITRATE;
        VideoEncoderConfiguration.ORIENTATION_MODE orientationMode = VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT;

        VideoEncoderConfiguration videoEncoderConfiguration = new VideoEncoderConfiguration(dimensions, frameRate, bitrate, orientationMode);

        mRtcEngine.setVideoEncoderConfiguration(videoEncoderConfiguration);
    }

    public void setLocalVideoRenderer(SurfaceView surfaceView) {
        tryInitRtcEngine();
        surfaceView = tryReplcaceSurfaceView(surfaceView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0));
    }

    private SurfaceView tryReplcaceSurfaceView(SurfaceView surfaceView) {
        if (ViEAndroidGLES20.IsSupported(surfaceView.getContext())) {
            MyLog.w(TAG, "ViEAndroidGLES20.IsSupported=true,Replcace SurfaceView Now");
            // 如果支持，尝试替换掉现有的Surfaceview，因为这个性能更好
            ViewGroup parentView = (ViewGroup) surfaceView.getParent();
            if (parentView != null) {
                ViEAndroidGLES20 newSurfaceView = new ViEAndroidGLES20(surfaceView.getContext());
                int index = parentView.indexOfChild(surfaceView);
                ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
                try {
                    int mSubLayer = (int) U.getReflectUtils().readField(surfaceView, "mSubLayer");
                    U.getReflectUtils().writeField(newSurfaceView, "mSubLayer", mSubLayer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                parentView.removeView(surfaceView);
                parentView.addView(newSurfaceView, index, layoutParams);
                return newSurfaceView;
            }
        }
        return surfaceView;
    }

    /**
     * 绑定远端视图
     * 如果uid传的是非法值，则会缓存view
     * 自动等第一个合法的uid出现时（onJoinUser时）
     * 再bind视图
     * <p>
     * 如果uid是用我们自己的账号体系，那这里是可以提前分配view吧
     *
     * @param uid
     */
    public void setRemoteVideoRenderer(int uid, SurfaceView surfaceView) {
        MyLog.d(TAG, "setRemoteVideoRenderer" + " uid=" + uid + " surfaceView=" + surfaceView);
        tryInitRtcEngine();

        surfaceView = tryReplcaceSurfaceView(surfaceView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid));
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

    public List<EffectModel> getAllEffects() {
        return mEffectModels;
    }

    private void tryCopyAssetsEffect2Sdcard() {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                File file = new File(U.getAppInfoUtils().getMainDir(), "effects");
                U.getFileUtils().copyAssetsToSdcard("effects", file.getPath(), false);
                int id = 0;
                for (File effectFile : file.listFiles()) {
                    EffectModel effectModel = new EffectModel();
                    effectModel.setId(id++);
                    effectModel.setName(effectFile.getName());
                    effectModel.setPath(effectFile.getAbsolutePath());
                    if (!mEffectModels.contains(effectModel)) {
                        mEffectModels.add(effectModel);
                    }
                }
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .subscribe();
    }

    /**
     * 播放音效
     */
    public void playEffects(EffectModel effectModel) {
        if (mRtcEngine != null) {
            IAudioEffectManager manager = mRtcEngine.getAudioEffectManager();
            manager.preloadEffect(effectModel.getId(), effectModel.getPath());
            // 播放一个音效
            manager.playEffect(
                    effectModel.getId(),                         // 要播放的音效 id
                    effectModel.getPath(),         // 播放文件的路径
                    -1,                        // 播放次数，-1 代表无限循环
                    1, // 音调 0.5-2
                    0.0,                       // 改变音效的空间位置，0表示正前方
                    100,                       // 音量，取值 0 ~ 100， 100 代表原始音量
                    true                       // 是否令远端也能听到音效的声音
            );
        }
    }

    /**
     * 外部推送音频帧
     *
     * @param data
     * @param ts
     * @return
     */
    public int pushExternalAudioFrame(byte[] data, long ts) {
        if (!mConfig.isUseCbEngine()) {
            throw new IllegalStateException("usecbengine flag is false");
        }
        tryInitRtcEngine();
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
        if (!mConfig.isUseCbEngine()) {
            throw new IllegalStateException("usecbengine flag is false");
        }
        tryInitRtcEngine();
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
            vf.stride = 100;
            vf.height = 200;
            vf.textureID = textureId;
            vf.syncMode = true;
            vf.eglContext11 = context;
            vf.transform = UNIQUE_MAT;
            vf.rotation = 0;
            boolean pushState = mRtcEngine.pushExternalVideoFrame(vf);
            MyLog.d(TAG, "pushExternalVideoFrame" + " textureId=" + textureId + " pushState:" + pushState);
        }
    }

}
