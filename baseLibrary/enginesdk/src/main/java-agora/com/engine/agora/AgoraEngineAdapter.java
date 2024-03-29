package com.engine.agora;

import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.ViewGroup;

import com.common.log.MyLog;
import com.common.utils.U;
import com.engine.Params;
import com.engine.agora.effect.EffectModel;
import com.engine.agora.source.PrivateTextureHelper;
import com.engine.arccloud.AcrCloudManager;
import com.engine.arccloud.AcrRecognizeListener;
import com.engine.arccloud.RecognizeConfig;
import com.engine.effect.IFAudioEffectEngine;
import com.engine.effect.ITbAgcProcessor;
import com.engine.effect.ITbEffectProcessor;
import com.engine.score.ICbScoreProcessor;
import com.engine.score.Score2Callback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;

import io.agora.rtc.Constants;
import io.agora.rtc.IAudioEffectManager;
import io.agora.rtc.IAudioFrameObserver;
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
    public final String TAG = "AgoraEngineAdapter";

    static final String APP_ID;

    static {
        if (U.getChannelUtils().isStaging()) {
            APP_ID = "f23bd32ce6484113b02d14bd878e694c";
        } else {
            APP_ID = "2cceda2dbb2d46d28cab627f30c1a6f7";
        }
    }

    private static class AgoraEngineAdapterHolder {
        private static final AgoraEngineAdapter INSTANCE = new AgoraEngineAdapter();
    }

    private AgoraEngineAdapter() {
        tryCopyAssetsEffect2Sdcard();
    }

    public static final AgoraEngineAdapter getInstance() {
        return AgoraEngineAdapterHolder.INSTANCE;
    }

    static final boolean DEBUG = false && MyLog.isDebugLogOpen();
    static final boolean SCORE_DEBUG = false && MyLog.isDebugLogOpen();

    private Params mConfig;
    private RtcEngine mRtcEngine;
    private Handler mUiHandler = new Handler();
    private AgoraOutCallback mOutCallback;
    private List<EffectModel> mEffectModels = new ArrayList<>();
    private AcrCloudManager mArcCloudManager;// ArcClound的打分和识别
    private ITbEffectProcessor mTbEffectProcessor = new ITbEffectProcessor();// 提供的音效处理类
    private ITbAgcProcessor mITbAgcProcessor = new ITbAgcProcessor();// 提供的Agc处理算法
    private IFAudioEffectEngine mCbEffectProcessor = new IFAudioEffectEngine();// 唱吧提供的音效处理类
    private ICbScoreProcessor mICbScoreProcessor = new ICbScoreProcessor();// 唱吧打分系统

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
            initWhenInChannel();
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

        @Override
        public void onAudioMixingFinished() {
            super.onAudioMixingFinished();
            if (mOutCallback != null) {
                mOutCallback.onAudioMixingFinished();
            }
        }


        // 说话者音量提示
        @Override
        public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
            super.onAudioVolumeIndication(speakers, totalVolume);
            if (mOutCallback != null) {
                mOutCallback.onAudioVolumeIndication(speakers, totalVolume);
            }
        }

        @Override
        public void onAudioRouteChanged(int routing) {
            super.onAudioRouteChanged(routing);
            if (mOutCallback != null) {
                mOutCallback.onAudioRouteChanged(routing);
            }
        }

        @Override
        public void onWarning(int warn) {
            super.onWarning(warn);
            if (mOutCallback != null) {
                mOutCallback.onWarning(warn);
            }
        }

        @Override
        public void onError(int error) {
            super.onError(error);
            if (mOutCallback != null) {
                mOutCallback.onError(error);
            }
        }
    };

    public void setOutCallback(AgoraOutCallback outCallback) {
        this.mOutCallback = outCallback;
    }

    public void init(Params config) {
        Params oldConfig = mConfig;
        mConfig = config;
        if (oldConfig != null && mRtcEngine != null) {
            if (mConfig.getChannelProfile() != oldConfig.getChannelProfile()) {
                // 模式不一样了，必须销毁
                RtcEngine.destroy();
                destroy(true);
            } else {
                // 可以继续使用
                initRtcEngineInner();
            }
        }
    }

    /**
     * 初始化引擎
     */
    private void tryInitRtcEngine() {
        if (mRtcEngine == null) {
            synchronized (this) {
                try {
                    if (mRtcEngine == null) {
                        MyLog.d(TAG, "InitRtcEngine");
                        mRtcEngine = RtcEngine.create(U.app(), APP_ID, mCallback);
                        //mRtcEngine.setParameters("{\"rtc.log_filter\": 65535}");

                        mRtcEngine.setLogFile(U.getAppInfoUtils().getSubDirPath("logs") + "agorasdk.log");
                        // 模式为广播,必须在加入频道前调用
                        // 如果想要切换模式，则需要先调用 destroy 销毁当前引擎，然后使用 create 创建一个新的引擎后，再调用该方法设置新的频道模式
                        mRtcEngine.setChannelProfile(mConfig.getChannelProfile());
                        initRtcEngineInner();
                    }
                } catch (Exception e) {
                    MyLog.e(TAG, Log.getStackTraceString(e));
                    throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
                }
            }
        }
    }

    /**
     * 初始化参数
     */
    private void initRtcEngineInner() {
        if (mConfig.isEnableAudio()) {
            MyLog.d(TAG, "initRtcEngineInner enableAudio");
            //该方法需要在 joinChannel 之前设置好，joinChannel 后设置不生效。
            if (mRtcEngine == null) {
                tryInitRtcEngine();
            }
            mRtcEngine.enableAudio();

            int a = 3, b = 4;
            /**
             * 如果b==3 ，onRecordFrame 里是人声，但是stopMixMusic后数据会有问题
             * 如果b==4 ，onRecordFrame 是伴奏+人声
             */
            switch (mConfig.getScene()) {
                case rank:
                    b = 3;
                    mRtcEngine.setParameters("{\"che.audio.enable.aec\":true }");
                    break;
                case grab:
                    b = 3;
                    break;
                case voice:
                    b = 3;
                    break;
                case audiotest:
                    b = 3;
                    mRtcEngine.setParameters("{\"che.audio.enable.aec\":true }");
                    break;
            }
            mRtcEngine.setAudioProfile(a, b);

            enableAudioQualityIndication(mConfig.isEnableAudioQualityIndication());
            enableAudioVolumeIndication(mConfig.getVolumeIndicationInterval(), mConfig.getVolumeIndicationSmooth());

            // 设置onRecordFrame回调的数据
            setRecordingAudioFrameParameters(44100, 2, Constants.RAW_AUDIO_FRAME_OP_MODE_READ_WRITE, 1024);
            setPlaybackAudioFrameParameters(44100, 2, Constants.RAW_AUDIO_FRAME_OP_MODE_READ_WRITE, 1024);
        } else {
            mRtcEngine.disableAudio();
        }
        /**
         * 调用 enableVideo 方法打开视频模式。在 Agora SDK 中，
         * 音频功能是默认打开的，因此在加入频道前，或加入频道后，你都可以调用该方法开启视频。
         * <p>
         * 如果在加入频道前打开，则进入频道后直接加入视频通话或直播。
         * 如果在通话或直播过程中打开，则由纯音频切换为视频通话或直播。
         * <p>
         * 是一个比较大的涵盖，是否启动视频功能，不启用连对端的流都收不到，与 enableLocalVideo() 等要区分
         */
        if (mConfig.isEnableVideo()) {
            // 注意！！！！Agora 建议在 enableVideo 前调用该方法，可以加快首帧出图的时间。
            //  所有设置的参数均为理想情况下的最大值
            setVideoEncoderConfiguration();
            mRtcEngine.setCameraAutoFocusFaceModeEnabled(mConfig.isCameraAutoFocusFaceModeEnabled());
            // 开启视频
            mRtcEngine.enableVideo();
            // 开关视频双流模式。对端能选择接收大流还是小流
            mRtcEngine.enableDualStreamMode(true);
        } else {
            mRtcEngine.disableVideo();
        }

        // 发送方设置
        // 网络较差时，只发送音频流
        mRtcEngine.setLocalPublishFallbackOption(Constants.STREAM_FALLBACK_OPTION_AUDIO_ONLY);

        // 接收远端视频的配置
        // 弱网环境下先尝试接收小流；若当前网络环境无法显示视频，则只接受音频
        mRtcEngine.setRemoteSubscribeFallbackOption(Constants.STREAM_FALLBACK_OPTION_AUDIO_ONLY);

        if (mConfig.isUseExternalAudio()) {
            // 音视频自采集
            mRtcEngine.setExternalAudioSource(
                    true,      // 开启外部音频源
                    44100,     // 采样率，可以有8k，16k，32k，44.1k和48kHz等模式
                    1          // 外部音源的通道数，最多2个
            );
        }

        if (mConfig.isUseExternalVideo()) {
            mRtcEngine.setExternalVideoSource(
                    true,      // 是否使用外部视频源
                    false,      // 是否使用texture作为输出
                    true        // true为使用推送模式；false为拉取模式，但目前不支持
            );
        }
        mTbEffectProcessor.init();
        mITbAgcProcessor.init();
    }

    /**
     * 一些必须在频道内才能出事
     */
    private void initWhenInChannel() {
        // 初始化各个音量
        adjustRecordingSignalVolume(mConfig.getRecordingSignalVolume());
        adjustPlaybackSignalVolume(mConfig.getPlaybackSignalVolume());
        adjustAudioMixingVolume(mConfig.getAudioMixingPlayoutVolume());
    }

    /**
     * 离开房间
     */
    public void leaveChannel() {
        MyLog.d(TAG, "leaveChannel");
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
        }
    }

    /**
     * 不是做模式切换一般不用销毁所有
     */
    public void destroy(boolean destroyAll) {
        if (mRtcEngine != null) {
            mRtcEngine.stopPreview();
        }
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
        }
        if (mRtcEngine != null) {
            mRtcEngine.getAudioEffectManager().stopAllEffects();
        }
        if (mArcCloudManager != null) {
            mArcCloudManager.destroy();
            mArcCloudManager = null;
        }
        mUiHandler.removeCallbacksAndMessages(null);
        if (destroyAll) {
            //该方法为同步调用。在等待 RtcEngine 对象资源释放后再返回。APP 不应该在 SDK 产生的回调中调用该接口，否则由于 SDK 要等待回调返回才能回收相关的对象资源，会造成死锁。
            RtcEngine.destroy();
            mRtcEngine = null;
        }
        if (mTbEffectProcessor != null) {
            mTbEffectProcessor.destroyEffectProcessor();
        }
        if (mICbScoreProcessor != null) {
            mICbScoreProcessor.destroy();
        }
        if (mITbAgcProcessor != null) {
            mITbAgcProcessor.destroyAgcProcessor();
        }
    }

    /**
     * 设置直播场景下的用户角色。
     * 在加入频道前，用户需要通过本方法设置观众（默认）或主播模式。在加入频道后，用户可以通过本方法切换用户模式。
     *
     * @param isAnchor
     */
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
    public int joinChannel(String token, String channelId, String extra, int uid) {
        tryInitRtcEngine();
        MyLog.d(TAG, "joinChannel" + " token=" + token + " channelId=" + channelId + " extra=" + extra + " uid=" + uid);
        // 一定要设置一个角色
        String t = null;
        if (!TextUtils.isEmpty(token)) {
            t = token;
        }
        int retCode = mRtcEngine.joinChannel(t, channelId, extra, uid);
        return retCode;
    }

    /*视频渲染相关开始*/

    /**
     * 在该方法中，指定你想要的视频编码的分辨率、帧率、码率以及视频编码的方向模式。
     * 详细的视频编码参数定义，参考 setVideoEncoderConfiguration 中的描述。
     * <p>
     * 该方法设置的参数为理想情况下的最大值。当视频引擎因网络等原因无法达到设置的分辨率、帧率或码率值时，会取最接近设置值的最大值。
     * 如果设备的摄像头无法支持定义的视频属性，SDK 会为摄像头自动选择一个合理的分辨率。
     * 该行为对视频编码没有影响，编码时 SDK 仍沿用该方法中定义的分辨率。
     * 如果用户加入频道后不需要重新设置视频编码属性，则 Agora 建议在 enableVideo 前调用该方法，可以加快首帧出图的时间。
     */
    public void setVideoEncoderConfiguration() {
        // 想在房间内动态改变VideoEncoderConfiguration ，直接修改属性，然后set
        VideoEncoderConfiguration.VideoDimensions dimensions = new VideoEncoderConfiguration.VideoDimensions(mConfig.getLocalVideoWidth(), mConfig.getLocalVideoHeight());
        VideoEncoderConfiguration.FRAME_RATE frameRate = mConfig.getRateFps();
        int bitrate = mConfig.getBitrate();
        VideoEncoderConfiguration.ORIENTATION_MODE orientationMode = mConfig.getOrientationMode();
        VideoEncoderConfiguration videoEncoderConfiguration = new VideoEncoderConfiguration(dimensions, frameRate, bitrate, orientationMode);
        mRtcEngine.setVideoEncoderConfiguration(videoEncoderConfiguration);
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
        mRtcEngine.startPreview();
    }

    /**
     * 关闭预览
     */
    public void stopPreview() {
        mRtcEngine.stopPreview();
    }

    /**
     * 该方法设置本地视频镜像，须在开启本地预览前设置。如果在开启预览后设置，需要重新开启预览才能生效
     *
     * @param mode 0：默认镜像模式，即由 SDK 决定镜像模式
     *             1：启用镜像模式
     *             2：关闭镜像模式
     */
    public void setLocalVideoMirrorMode(int mode) {
        mRtcEngine.setLocalVideoMirrorMode(mode);
    }

    /**
     * 切换前/后摄像头
     */
    public void switchCamera() {
        mRtcEngine.switchCamera();
    }

    /**
     * 是否支持闪光灯常亮
     *
     * @return
     */
    public boolean isCameraTorchSupported() {
        return mRtcEngine.isCameraTorchSupported();
    }

    /**
     * 是否打开闪光灯
     *
     * @param on true：打开
     *           false：关闭
     */
    public void setCameraTorchOn(boolean on) {
        mRtcEngine.setCameraTorchOn(on);
    }

    /**
     * 还有两个方法
     * isCameraFocusSupported 是否支持对焦
     * isCameraAutoFocusFaceModeSupported 是否支持手动对焦
     * 手动对焦
     *
     * @param x
     * @param y
     */
    public void setCameraFocusPositionInPreview(float x, float y) {
        mRtcEngine.setCameraFocusPositionInPreview(x, y);
    }

    /**
     * 设置本地视频显示模式
     *
     * @param mode
     */
    public void setLocalRenderMode(int mode) {
        if (mRtcEngine != null) {
            /**
             * RENDER_MODE_HIDDEN(1)：优先保证视窗被填满。视频尺寸等比缩放，直至整个视窗被视频填满。如果视频长宽与显示窗口不同，多出的视频将被截掉
             * RENDER_MODE_FIT(2)：优先保证视频内容全部显示。视频尺寸等比缩放，直至视频窗口的一边与视窗边框对齐。如果视频长宽与显示窗口不同，视窗上未被填满的区域将被涂黑
             */
            mRtcEngine.setLocalRenderMode(mode);
        }
    }

    public void setRemoteRenderMode(int uid, int mode) {
        if (mRtcEngine != null) {
            /**
             * RENDER_MODE_HIDDEN(1)：优先保证视窗被填满。视频尺寸等比缩放，直至整个视窗被视频填满。如果视频长宽与显示窗口不同，多出的视频将被截掉
             * RENDER_MODE_FIT(2)：优先保证视频内容全部显示。视频尺寸等比缩放，直至视频窗口的一边与视窗边框对齐。如果视频长宽与显示窗口不同，视窗上未被填满的区域将被涂黑
             */
            mRtcEngine.setRemoteRenderMode(uid, mode);
        }
    }

    public void setLocalVideoRenderer(SurfaceView surfaceView) {
        tryInitRtcEngine();
        surfaceView = tryReplcaceSurfaceView(surfaceView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0));
    }

    public void setLocalVideoRenderer(TextureView textureView) {
        PrivateTextureHelper privateTextureHelper = new PrivateTextureHelper(textureView.getContext(), textureView);
        privateTextureHelper.init(null);
        privateTextureHelper.setBufferType(MediaIO.BufferType.BYTE_ARRAY);
        privateTextureHelper.setPixelFormat(MediaIO.PixelFormat.I420);
        mRtcEngine.setLocalVideoRenderer(privateTextureHelper);
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
        //退出频道后，SDK 会把远程用户的绑定关系清除掉。
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
     * 该方法禁用/启用本地视频功能。该方法用于只看不发的视频场景。
     * 请在 enableVideo 后调用该方法，否则该方法可能无法正常使用。
     * 调用 enableVideo 后，本地视频默认开启。使用该方法可以开启或关闭本地视频，且不影响接收远端视频。
     *
     * @param enable
     */
    public void enableLocalVideo(boolean enable) {
        if (mRtcEngine != null) {
            mRtcEngine.enableLocalVideo(enable);
        }
    }

    /**
     * 调用该方法时，SDK 不再发送本地视频流，但摄像头仍然处于工作状态。
     * 相比于 enableLocalVideo (false) 用于控制本地视频流发送的方法，该方法响应速度更快。
     * 该方法不影响本地视频流获取，没有禁用摄像头
     *
     * @param muted
     */
    public void muteLocalVideoStream(boolean muted) {
        if (mRtcEngine != null) {
            mRtcEngine.muteLocalVideoStream(muted);
        }
    }

    /**
     * 接收/停止接收指定视频流
     * 如果之前有调用过 muteAllRemoteVideoStreams (true) 停止接收所有远端视频流，
     * 在调用本 API 之前请确保你已调用 muteAllRemoteVideoStreams (false)。 muteAllRemoteVideoStreams 是全局控制，
     * muteRemoteVideoStream 是精细控制。
     *
     * @param uid
     * @param muted
     */
    public void muteRemoteVideoStream(int uid, boolean muted) {
        if (mRtcEngine != null) {
            mRtcEngine.muteRemoteVideoStream(uid, muted);
        }
    }

    /**
     * 你不想看其他人的了，但其他人还能互相看
     *
     * @param muted
     */
    public void muteAllRemoteVideoStreams(boolean muted) {
        if (mRtcEngine != null) {
            mRtcEngine.muteAllRemoteVideoStreams(muted);
        }
    }

    /*视频渲染相关结束*/

    /*音频基础相关开始*/

    /**
     * 加入频道后
     * 它的语音功能默认是开启的。该方法可以关闭或重新开启本地语音功能，停止或重新开始本地音频采集及处理。
     * 该方法不影响接收或播放远端音频流，适用于只听不发的用户场景。
     * 回调 onMicrophoneEnabled
     */
    public void enableLocalAudio(boolean enable) {
        if (mRtcEngine != null) {
            mRtcEngine.enableLocalAudio(enable);
        }
    }

    /**
     * 两个方法的区别是
     * enableLocalAudio：开启或关闭本地语音采集及处理
     * muteLocalAudioStream：停止或继续发送本地音频流
     *
     * @param muted
     */
    public void muteLocalAudioStream(boolean muted) {
        MyLog.d(TAG, "muteLocalAudioStream" + " muted=" + muted);
        if (mRtcEngine != null) {
            mRtcEngine.muteLocalAudioStream(muted);
        }
    }

    /**
     * 接收/停止接收指定音频流。
     *
     * @param muted
     */
    public void muteRemoteAudioStream(int uid, boolean muted) {
        if (mRtcEngine != null) {
            mRtcEngine.muteRemoteAudioStream(uid, muted);
        }
    }

    /**
     * 接收/停止接收所有音频流。
     * 适用于 A 在唱歌，B C 能互相聊天，但不能打扰到 A 的场景
     */
    public void muteAllRemoteAudioStreams(boolean muted) {
        if (mRtcEngine != null) {
            mRtcEngine.muteAllRemoteAudioStreams(muted);
        }
    }

    /**
     * 录音音量，可在 0~400 范围内进行调节 默认100
     *
     * @param volume
     */
    public void adjustRecordingSignalVolume(int volume) {
        if (volume < 0) {
            volume = 0;
        }
        if (volume > 400) {
            volume = 400;
        }
        if (mRtcEngine != null) {
            mRtcEngine.adjustRecordingSignalVolume(volume);
        }
    }

    /**
     * 播放音量，可在 0~400 范围内进行调节 默认100
     *
     * @param volume
     */
    public void adjustPlaybackSignalVolume(int volume) {
        if (volume < 0) {
            volume = 0;
        }
        if (volume > 400) {
            volume = 400;
        }
        if(mRtcEngine!=null){
            mRtcEngine.adjustPlaybackSignalVolume(volume);
        }
    }

    /**
     * 启用音量回调提示
     * 一旦启用，onAudioQuality 将被定期触发
     */
    public void enableAudioQualityIndication(boolean enable) {
        if (mRtcEngine != null) {
            mRtcEngine.enableAudioQualityIndication(enable);
        }
    }

    /**
     * 启用说话者音量提示
     *
     * @param interval 建议大于 200ms
     * @param smooth   [0,10] 建议3
     */
    public void enableAudioVolumeIndication(int interval, int smooth) {
        if (mRtcEngine != null) {
            mRtcEngine.enableAudioVolumeIndication(interval, smooth, true);
        }
    }

    /**
     * 启用/关闭扬声器播放。 该方法设置是否将语音路由设到扬声器（外放）。 你可以在 setDefaultAudioRouteToSpeakerphone 方法中查看默认的语音路由。
     * <p>
     * 参数
     * enabled	是否将音频路由到外放：
     * true：切换到外放
     * false：切换到听筒
     * 注解
     * 请确保在调用此方法前已调用过 joinChannel 方法。
     * 调用该方法后，SDK 将返回 onAudioRouteChanged 回调提示状态已更改。
     * 使用耳机的时候调用该方法不会生效。
     * 直播模式下默认是外放
     *
     * @param fromSpeaker
     */
    public void setEnableSpeakerphone(boolean fromSpeaker) {
        MyLog.d(TAG, "setEnableSpeakerphone" + " fromSpeaker=" + fromSpeaker);
        if (mRtcEngine != null) {
            mRtcEngine.setEnableSpeakerphone(fromSpeaker);
        }
    }

    /**
     * 是否是扬声器播放
     *
     * @return
     */
    public boolean isSpeakerphoneEnabled() {
        return mRtcEngine.isSpeakerphoneEnabled();
    }

    /**
     * 开启或者关闭🎧耳返
     * 默认关闭
     */
    public void enableInEarMonitoring(boolean enable) {
        MyLog.d(TAG, "enableInEarMonitoring" + " enable=" + enable);
        if (mRtcEngine != null) {
            mRtcEngine.enableInEarMonitoring(enable);
        }
    }

    /**
     * 设定耳返音量
     *
     * @param volume 默认100
     */
    public void setInEarMonitoringVolume(int volume) {
        if (mRtcEngine != null) {
            mRtcEngine.setInEarMonitoringVolume(volume);
        }
    }

    /**
     * @param sampleRate     指定 onRecordFrame 中返回数据的采样率，可设置为 8000，16000，32000，44100 或 48000。
     * @param channel        指定 onRecordFrame 中返回数据的通道数，可设置为 1 或 2：
     *                       * 1：单声道
     *                       * 2：双声道
     * @param mode           指定 onRecordFrame 的使用模式：
     *                       RAW_AUDIO_FRAME_OP_MODE_READ_ONLY：只读模式，用户仅从 AudioFrame 获取原始音频数据。例如：若用户通过 Agora SDK 采集数据，自己进行 RTMP 推流，则可以选择该模式。
     *                       RAW_AUDIO_FRAME_OP_MODE_WRITE_ONLY：只写模式，用户替换 AudioFrame 中的数据以供 Agora SDK 编码传输。例如：若用户自行采集数据，可选择该模式。
     *                       RAW_AUDIO_FRAME_OP_MODE_READ_WRITE：读写模式，用户从 AudioFrame 获取并修改数据，并返回给 Aogra SDK 进行编码传输。例如：若用户自己有音效处理模块，且想要根据实际需要对数据进行前处理 (例如变声)，则可以选择该模式。
     * @param samplesPerCall 指定 onRecordFrame 中返回数据的采样点数，如 RTMP 推流应用中通常为 1024。 SamplesPerCall = (int)(SampleRate × sampleInterval)，其中：sample ≥ 0.01，单位为秒。
     */
    public int setRecordingAudioFrameParameters(int sampleRate,
                                                int channel,
                                                int mode,
                                                int samplesPerCall) {
        if (mRtcEngine != null) {
            return mRtcEngine.setRecordingAudioFrameParameters(sampleRate, channel, mode, samplesPerCall);
        }
        return 0;
    }

    /**
     * @param sampleRate     指定 onPlaybackFrame 中返回数据的采样率，可设置为 8000，16000，32000，44100 或 48000
     * @param channel        指定 onPlaybackFrame 中返回数据的通道数，可设置为 1 或 2：
     *                       1：单声道
     *                       2：双声道
     * @param mode           指定 onPlaybackFrame 的使用模式：
     *                       RAW_AUDIO_FRAME_OP_MODE_READ_ONLY：只读模式，用户仅从 AudioFrame 获取原始音频数据，不作任何修改。例如：若用户通过 Agora SDK 采集数据，自己进行 RTMP 推流，则可以选择该模式。
     *                       RAW_AUDIO_FRAME_OP_MODE_WRITE_ONLY：只写模式，用户替换 AudioFrame 中的数据。例如：若用户自行采集数据，可选择该模式。
     *                       RAW_AUDIO_FRAME_OP_MODE_READ_WRITE：读写模式，用户从 AudioFrame 获取数据、修改。例如：若用户自己有音效处理模块，且想要根据实际需要对数据进行后处理 (例如变声)，则可以选择该模式。
     * @param samplesPerCall 指定 onPlaybackFrame 中返回数据的采样点数，如 RTMP 推流应用中通常为 1024。 SamplesPerCall = (int)(SampleRate × sampleInterval)，其中：sample ≥ 0.01，单位为秒。
     */
    public int setPlaybackAudioFrameParameters(int sampleRate,
                                               int channel,
                                               int mode,
                                               int samplesPerCall) {
        return mRtcEngine.setPlaybackAudioFrameParameters(sampleRate, channel, mode, samplesPerCall);
    }

    /*音频基础结束*/

    /*音频高级扩展开始*/

    /**
     * 设置本地语音音调。
     * <p>
     * 该方法改变本地说话人声音的音调。
     * 可以在 [0.5, 2.0] 范围内设置。取值越小，则音调越低。默认值为 1.0，表示不需要修改音调。
     *
     * @param pitch
     */
    public void setLocalVoicePitch(double pitch) {
        if (mRtcEngine != null) {
            mRtcEngine.setLocalVoicePitch(pitch);
        }
    }

    /**
     * 设置本地语音音效均衡
     *
     * @param bandFrequency 频谱子带索引，取值范围是 [0-9]，分别代表 10 个频带，对应的中心频率是 [31，62，125，250，500，1k，2k，4k，8k，16k] Hz
     * @param bandGain      每个 band 的增益，单位是 dB，每一个值的范围是 [-15，15]，默认值为 0
     */
    public void setLocalVoiceEqualization(int bandFrequency, int bandGain) {
        if (mRtcEngine == null) {
            return;
        }
        mRtcEngine.setLocalVoiceEqualization(bandFrequency, bandGain);
    }

    /**
     * 设置本地音效混响。
     *
     * @param reverbKey 混响音效 Key。该方法共有 5 个混响音效 Key，分别如 value 栏列出。
     * @param value     AUDIO_REVERB_DRY_LEVEL(0)：原始声音强度，即所谓的 dry signal，取值范围 [-20, 10]，单位为 dB
     *                  AUDIO_REVERB_WET_LEVEL(1)：早期反射信号强度，即所谓的 wet signal，取值范围 [-20, 10]，单位为 dB
     *                  AUDIO_REVERB_ROOM_SIZE(2)：所需混响效果的房间尺寸，一般房间越大，混响越强，取值范围 [0, 100]，单位为 dB
     *                  AUDIO_REVERB_WET_DELAY(3)：Wet signal 的初始延迟长度，取值范围 [0, 200]，单位为毫秒
     *                  AUDIO_REVERB_STRENGTH(4)：混响持续的强度，取值范围为 [0, 100]
     */
    public void setLocalVoiceReverb(int reverbKey, int value) {
        if (mRtcEngine == null) {
            return;
        }
        mRtcEngine.setLocalVoiceReverb(reverbKey, value);
    }

    /**
     * 开始播放音乐文件及混音。
     * 播放伴奏结束后，会收到 onAudioMixingFinished 回调
     *
     * @param filePath 指定需要混音的本地或在线音频文件的绝对路径。支持d的音频格式包括：mp3、mp4、m4a、aac、3gp、mkv、wav 及 flac。详见 Supported Media Formats。
     *                 如果用户提供的目录以 /assets/ 开头，则去 assets 里面查找该文件
     *                 如果用户提供的目录不是以 /assets/ 开头，一律认为是在绝对路径里查找该文件
     * @param loopback true：只有本地可以听到混音或替换后的音频流
     *                 false：本地和对方都可以听到混音或替换后的音频流
     * @param replace  true：只推动设置的本地音频文件或者线上音频文件，不传输麦克风收录的音频
     *                 false：音频文件内容将会和麦克风采集的音频流进行混音
     * @param cycle    指定音频文件循环播放的次数：
     *                 正整数：循环的次数
     *                 -1：无限循环
     */
    public void startAudioMixing(String filePath, String melPath, boolean loopback, boolean replace, int cycle) {
        if (mRtcEngine == null) {
            return;
        }
        mICbScoreProcessor.init();
        mRtcEngine.startAudioMixing(filePath, loopback, replace, cycle);


        if (mDebugScoreIS != null) {
            try {
                mDebugScoreIS.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mDebugScoreIS = null;
        }
    }

    /**
     * 停止播放音乐文件及混音。
     * 请在频道内调用该方法。
     */
    public void stopAudioMixing() {
        if (mRtcEngine == null) {
            return;
        }
        mRtcEngine.stopAudioMixing();
    }

    /**
     * 暂停播放音乐文件及混音
     */
    public void pauseAudioMixing() {
        if (mRtcEngine == null) {
            return;
        }
        mRtcEngine.pauseAudioMixing();
    }

    /**
     * 继续播放混音
     */
    public void resumeAudioMixing() {
        if (mRtcEngine == null) {
            return;
        }
        mRtcEngine.resumeAudioMixing();
    }

    /**
     * 调节混音音量大小
     * 频道内调用
     *
     * @param volume 1-100 默认100
     */
    public void adjustAudioMixingVolume(int volume) {
        if (mRtcEngine == null) {
            return;
        }
        mRtcEngine.adjustAudioMixingVolume(volume);
    }

    /**
     * @return 获取伴奏时长，单位ms
     */
    public int getAudioMixingDuration() {
        if (mRtcEngine == null) {
            return 0;
        }
        return mRtcEngine.getAudioMixingDuration();
    }

    /**
     * @return 获取混音当前播放位置 ms
     */
    public int getAudioMixingCurrentPosition() {
        if (mRtcEngine == null) {
            return 0;
        }
        return mRtcEngine.getAudioMixingCurrentPosition();
    }

    /**
     * 拖动混音进度条
     *
     * @param posMs
     */
    public void setAudioMixingPosition(int posMs) {
        if (mRtcEngine == null) {
            return;
        }
        mRtcEngine.setAudioMixingPosition(posMs);
    }

    FileInputStream mDebugScoreIS;

    int mLogtag = 0;

    public void setIFAudioEffectEngine(final Params.AudioEffect styleEnum) {
        MyLog.d(TAG, "setIFAudioEffectEngine" + " styleEnum=" + styleEnum);
        tryInitRtcEngine();
//        if (styleEnum == null) {
//            mRtcEngine.registerAudioFrameObserver(null);
//        } else {
        // 注册这玩意怎么会导致没有声音,return false 就会丢弃
        mRtcEngine.registerAudioFrameObserver(new IAudioFrameObserver() {
            /**
             * 10ms 回调一次 , 里面不能做耗时操作，不然声音会有问题
             * @param samples
             * @param numOfSamples
             * @param bytesPerSample
             * @param channels
             * @param samplesPerSec
             * @return
             */
            @Override
            public boolean onRecordFrame(byte[] samples, // 2048
                                         int numOfSamples, // 512
                                         int bytesPerSample,// 2
                                         int channels,// 2
                                         int samplesPerSec//44100
            ) {
                //TODO 考虑用户如果开伴奏了，就不走音效和AGC了，只走下打分
                if (samples == null) {
                    return false;
                }
                if (++mLogtag % 500 == 0) {
                    //  案例这个日志应该5秒一次
                    MyLog.d(TAG, "onRecordFrame" + " samples=" + samples + " numOfSamples=" + numOfSamples + " bytesPerSample=" + bytesPerSample + " channels=" + channels + " samplesPerSec=" + samplesPerSec);
                }
                if (DEBUG) {
                    MyLog.d(TAG, "step0:" + testIn(samples));
                }
                long accTs = 0;
                if (mConfig != null && mConfig.isMixMusicPlaying() && mConfig.isLrcHasStart()) {
                    accTs = mConfig.getCurrentMusicTs() + mConfig.getMixMusicBeginOffset() + (System.currentTimeMillis() - mConfig.getRecordCurrentMusicTsTs());
                }
                if (DEBUG) {
                    MyLog.d(TAG, "step1:" + testIn(samples));
                }
                if (SCORE_DEBUG) {
                    if (mDebugScoreIS == null) {
                        try {
                            if (mConfig.getStyleEnum() == Params.AudioEffect.none) {
                                MyLog.d(TAG, "读取none");
                                mDebugScoreIS = new FileInputStream(new File(Environment.getExternalStorageDirectory(), "none.pcm"));
                            } else if (mConfig.getStyleEnum() == Params.AudioEffect.kongling) {
                                MyLog.d(TAG, "读取dangnigudan");
                                mDebugScoreIS = new FileInputStream(new File(Environment.getExternalStorageDirectory(), "dangnigudan.pcm"));
                            } else {
                                MyLog.d(TAG, "读取tongzhuodeni");
                                mDebugScoreIS = new FileInputStream(new File(Environment.getExternalStorageDirectory(), "tongzhuodeni.pcm"));
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    if (accTs > 0) {
                        // 取本地数据，本地数据为44100 单声道
                        byte[] data = new byte[1024];
                        try {
                            if (mDebugScoreIS.read(data) != -1) {
                                mICbScoreProcessor.process(data, data.length, 1, samplesPerSec, accTs, mConfig.getMidiPath());
                            }
                            if (mArcCloudManager != null) {
                                if (mConfig != null) {
                                    mArcCloudManager.putPool(data, samplesPerSec, 1);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    mICbScoreProcessor.process(samples, samples.length, channels, samplesPerSec, accTs, mConfig.getMidiPath());
                    if (mArcCloudManager != null) {
                        if (mConfig != null) {
                            if (mConfig.isMixMusicPlaying() && mConfig.isLrcHasStart()) {
                                mArcCloudManager.putPool(samples, samplesPerSec, channels);
                            } else if (mConfig.isGrabSingNoAcc()) {
                                // 一唱到底清唱模式，acr打分
                                mArcCloudManager.putPool(samples, samplesPerSec, channels);
                            }
                        }
                    }
                }

                if (DEBUG) {
                    MyLog.d(TAG, "step2:" + testIn(samples));
                }
                if (!mConfig.isMixMusicPlaying()) {
                    // 不播放音乐才走这些音效，否则不走
                    if (styleEnum == Params.AudioEffect.ktv) {
                        mTbEffectProcessor.process(2, samples, samples.length, channels, samplesPerSec);
                    } else if (styleEnum == Params.AudioEffect.rock) {
                        mTbEffectProcessor.process(1, samples, samples.length, channels, samplesPerSec);
                    } else if (styleEnum == Params.AudioEffect.liuxing) {
                        mCbEffectProcessor.process(8, samples, samples.length, channels, samplesPerSec);
                    } else if (styleEnum == Params.AudioEffect.kongling) {
                        mCbEffectProcessor.process(1, samples, samples.length, channels, samplesPerSec);
                    } else {
                        mCbEffectProcessor.destroy();
                    }
                    if (DEBUG) {
                        MyLog.d(TAG, "step3:" + testIn(samples));
                    }
                    //TODO 是不是可以考虑AGC 后再给打分
                    // 针对不同场景，处理agc
                    switch (mConfig.getScene()) {
                        case grab:
                            // 只有单人清唱才走天宝的agc
                            if (mConfig.isGrabSingNoAcc()) {
                                mITbAgcProcessor.processV1(samples, samples.length, channels, samplesPerSec);
                            }
                            break;
                        case voice:
                            break;
                        case rank:
                        case audiotest:
                            if (accTs > 0) {
                                mITbAgcProcessor.processV1(samples, samples.length, channels, samplesPerSec);
                            }
                            break;
                    }

                    if (DEBUG) {
                        MyLog.d(TAG, "step4:" + testIn(samples));
                    }
                }
                if (!TextUtils.isEmpty(mConfig.getRecordingForDebugSavePath())) {
                    if (mOutCallback != null) {
                        mOutCallback.onRecordingBuffer(samples);
                    }
                }
                return true;
            }

            @Override
            public boolean onPlaybackFrame(byte[] samples,
                                           int numOfSamples,
                                           int bytesPerSample,
                                           int channels,
                                           int samplesPerSec) {
                return true;
            }
        });
    }

    int testIn(byte[] samples) {
        int a = 0;
        for (int i = 0; i < samples.length; i++) {
            a += samples[i];
        }
        return a;
    }

    public int getScoreV1() {
        return mICbScoreProcessor.getScoreV1();
    }

    public void getScoreV2(int lineNum, Score2Callback callback) {
        mICbScoreProcessor.getScoreV2(lineNum, callback);
    }

    /**
     * 开始客户端录音。
     * <p>
     * Agora SDK 支持通话过程中在客户端进行录音。该方法录制频道内所有用户的音频，并生成一个包含所有用户声音的录音文件，录音文件格式可以为：
     * <p>
     * .wav：文件大，音质保真度高
     * .aac：文件小，有一定的音质保真度损失
     * 请确保 App 里指定的目录存在且可写。该接口需在加入频道之后调用。如果调用 leaveChannel 时还在录音，录音会自动停止。
     */
    public void startAudioRecording(String saveAudioForAiFilePath, int audioRecordingQualityHigh) {
        if (mRtcEngine == null) {
            return;
        }
        mRtcEngine.startAudioRecording(saveAudioForAiFilePath, audioRecordingQualityHigh);
    }

    /**
     * 停止客户端录音。
     * <p>
     * 该方法停止录音。该接口需要在 leaveChannel 之前调用，不然会在调用 leaveChannel 时自动停止。
     */
    public void stopAudioRecording() {
        if (mRtcEngine == null) {
            return;
        }
        mRtcEngine.stopAudioRecording();
    }

    /*音频高级扩展结束*/

    /*音频特效相关开始*/
    public List<EffectModel> getAllEffects() {
        return mEffectModels;
    }

    /**
     * 尝试拷贝assets到sdcard中
     */
    private void tryCopyAssetsEffect2Sdcard() {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                File file = new File(U.getAppInfoUtils().getMainDir(), "effects");
                U.getFileUtils().copyAssetsToSdcard("effects", file.getPath(), false);
                int id = 0;
                if (file != null && file.listFiles() != null) {
                    for (File effectFile : file.listFiles()) {
                        EffectModel effectModel = new EffectModel();
                        effectModel.setId(id++);
                        effectModel.setName(effectFile.getName());
                        effectModel.setPath(effectFile.getAbsolutePath());
                        if (!mEffectModels.contains(effectModel)) {
                            mEffectModels.add(effectModel);
                        }
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
            if (!effectModel.hasPreload()) {
                manager.preloadEffect(effectModel.getId(), effectModel.getPath());
                effectModel.setHasPreload(true);
            }
            // 播放一个音效
            manager.playEffect(
                    effectModel.getId(),                         // 要播放的音效 id
                    effectModel.getPath(),         // 播放文件的路径
                    0,                        // 播放次数，-1 代表无限循环 ，0代表1次
                    1, // 音调 0.5-2
                    0.0,                       // 改变音效的空间位置，0表示正前方
                    100,                       // 音量，取值 0 ~ 100， 100 代表原始音量
                    true                       // 是否令远端也能听到音效的声音
            );
        }
    }
    /*音频特效相关结束*/

    /*自定义推流流相关开始*/

    /**
     * 外部推送音频帧
     *
     * @param data
     * @param ts
     * @return
     */
    public int pushExternalAudioFrame(byte[] data, long ts) {
        if (!mConfig.isUseExternalAudio()) {
            throw new IllegalStateException("useExternalAudio flag is false");
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
        if (!mConfig.isUseExternalVideo()) {
            throw new IllegalStateException("useExternalVideo flag is false");
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
    /*自定义推流流相关结束*/

    /*其他高级选项开始*/

    /**
     * 该方法每次只能增加一路旁路推流地址。若需推送多路流，则需多次调用该方法。
     *
     * @param url                推流地址，格式为 RTMP
     * @param transcodingEnabled 是否转码
     *                           true：转码。转码是指在旁路推流时对音视频流进行转码处理后，再推送到其他 RTMP 服务器。
     *                           多适用于频道内有多个主播，需要进行混流、合图的场景
     *                           false：不转码
     */
    public void addPublishStreamUrl(String url, boolean transcodingEnabled) {
        mRtcEngine.addPublishStreamUrl(url, transcodingEnabled);
    }

    public void removePublishStreamUrl(String url) {
        mRtcEngine.removePublishStreamUrl(url);
    }

    /**
     * 设置视频优化选项（仅适用于直播）
     *
     * @param preferFrameRateOverImageQuality true：画质和流畅度里，优先保证流畅度
     *                                        false：画质和流畅度里，优先保证画质 (默认)
     */
    public void setVideoQualityParameters(boolean preferFrameRateOverImageQuality) {
        mRtcEngine.setVideoQualityParameters(preferFrameRateOverImageQuality);
    }

    public void setLogLevel(boolean debug) {
        if (debug) {
            mRtcEngine.setLogFilter(Constants.LOG_FILTER_DEBUG);
        } else {
            mRtcEngine.setLogFilter(Constants.LOG_FILTER_WARNING);
        }
    }

    /**
     * 添加水印
     * 发布或者订阅的音视频流回退选项的设定
     * 设置接受大流还是小流
     * 导入在线流媒体流
     * 给通话评分
     * 等等 需要时再添加
     */
    /*其他高级选项结束*/

    /*打分相关开始*/
    private void tryInitArcManager() {
        if (mArcCloudManager == null) {
            synchronized (this) {
                if (mArcCloudManager == null) {
                    mArcCloudManager = new AcrCloudManager();
                }
            }
        }
    }

    public void startRecognize(RecognizeConfig recognizeConfig) {
        tryInitArcManager();
        mArcCloudManager.startRecognize(recognizeConfig);
    }

    public void setRecognizeListener(AcrRecognizeListener recognizeConfig) {
        if (mArcCloudManager != null) {
            mArcCloudManager.setRecognizeListener(recognizeConfig);
        }
    }

    public void stopRecognize() {
        if (mArcCloudManager != null) {
            mArcCloudManager.stopRecognize();
        }
    }

    public void recognizeInManualMode(int lineNo) {
        if (mArcCloudManager != null) {
            mArcCloudManager.recognizeInManualMode(lineNo);
        }
    }


    /*打分相关结束*/
}
