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
                        mRtcEngine = RtcEngine.create(U.app(), APP_ID, mCallback);
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

    private void initRtcEngineInner() {
        if (mConfig.isEnableAudio()) {
            //该方法需要在 joinChannel 之前设置好，joinChannel 后设置不生效。
            mRtcEngine.enableAudio();
            mRtcEngine.setAudioProfile(Constants.AudioProfile.getValue(mConfig.getAudioProfile())
                    , Constants.AudioScenario.getValue(mConfig.getAudioScenario()));

            enableAudioQualityIndication(mConfig.isEnableAudioQualityIndication());
            enableAudioVolumeIndication(mConfig.getVolumeIndicationInterval(), mConfig.getVolumeIndicationSmooth());
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
            // 开启视频
            mRtcEngine.enableVideo();
            // Agora 建议在 enableVideo 前调用该方法，可以加快首帧出图的时间。
            //  所有设置的参数均为理想情况下的最大值
            setVideoEncoderConfiguration();
        } else {
            mRtcEngine.disableVideo();
        }

        // 开关视频双流模式。对端能选择接收大流还是小流
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

        mUiHandler.removeCallbacksAndMessages(null);
        if (destroyAll) {
            //该方法为同步调用。在等待 RtcEngine 对象资源释放后再返回。APP 不应该在 SDK 产生的回调中调用该接口，否则由于 SDK 要等待回调返回才能回收相关的对象资源，会造成死锁。
            RtcEngine.destroy();
            mRtcEngine = null;
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
    public void joinChannel(String token, String channelId, String extra, int uid) {
        tryInitRtcEngine();
        MyLog.d(TAG, "joinChannel" + " token=" + token + " channelId=" + channelId + " extra=" + extra + " uid=" + uid);
        // 一定要设置一个角色
        mRtcEngine.joinChannel(token, channelId, extra, uid);
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
        mRtcEngine.enableLocalVideo(enable);
    }

    /**
     * 调用该方法时，SDK 不再发送本地视频流，但摄像头仍然处于工作状态。
     * 相比于 enableLocalVideo (false) 用于控制本地视频流发送的方法，该方法响应速度更快。
     * 该方法不影响本地视频流获取，没有禁用摄像头
     *
     * @param muted
     */
    public void muteLocalVideoStream(boolean muted) {
        mRtcEngine.muteLocalVideoStream(muted);
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
        mRtcEngine.muteRemoteVideoStream(uid, muted);
    }

    /**
     * 你不想看其他人的了，其他人还想互相看
     * @param muted
     */
    public void muteAllRemoteVideoStreams(boolean muted) {
        mRtcEngine.muteAllRemoteVideoStreams(muted);
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
        mRtcEngine.enableLocalAudio(enable);
    }

    /**
     * 两个方法的区别是
     * enableLocalAudio：开启或关闭本地语音采集及处理
     * muteLocalAudioStream：停止或继续发送本地音频流
     *
     * @param muted
     */
    public void muteLocalAudioStream(boolean muted) {
        mRtcEngine.muteLocalAudioStream(muted);
    }

    /**
     * 接收/停止接收指定音频流。
     *
     * @param muted
     */
    public void muteRemoteAudioStream(int uid, boolean muted) {
        mRtcEngine.muteRemoteAudioStream(uid, muted);
    }

    /**
     * 接收/停止接收所有音频流。
     * 适用于 A 在唱歌，B C 能互相聊天，但不能打扰到 A 的场景
     */
    public void muteAllRemoteAudioStreams(boolean muted) {
        mRtcEngine.muteAllRemoteAudioStreams(muted);
    }

    /**
     * 录音音量，可在 0~400 范围内进行调节
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
        mRtcEngine.adjustRecordingSignalVolume(volume);
    }

    /**
     * 播放音量，可在 0~400 范围内进行调节
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
        mRtcEngine.adjustPlaybackSignalVolume(volume);
    }

    /**
     * 启用音量回调提示
     * 一旦启用，onAudioQuality 将被定期触发
     */
    public void enableAudioQualityIndication(boolean enable) {
        mRtcEngine.enableAudioQualityIndication(enable);
    }

    /**
     * 启用说话者音量提示
     *
     * @param interval 建议大于 200ms
     * @param smooth   [0,10] 建议3
     */
    public void enableAudioVolumeIndication(int interval, int smooth) {
        mRtcEngine.enableAudioVolumeIndication(interval, smooth);
    }

    /*音频基础结束*/

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
            if (!effectModel.hasPreload()) {
                manager.preloadEffect(effectModel.getId(), effectModel.getPath());
                effectModel.setHasPreload(true);
            }
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
    /*自定义推流流相关结束*/
}
