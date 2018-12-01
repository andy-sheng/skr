package com.engine;

import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.changba.songstudio.CbEngineAdapter;
import com.common.log.MyLog;
import com.common.utils.U;
import com.engine.agora.AgoraEngineAdapter;
import com.engine.agora.AgoraOutCallback;
import com.engine.agora.effect.EffectModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;

/**
 * 关于音视频引擎的都放在这个类里
 */
public class EngineManager implements AgoraOutCallback {

    public final static String TAG = "EngineManager";

    private Params mConfig;
    /**
     * 存储该房间所有用户在引擎中的状态的，
     * key为在引擎中的用户 id
     */
    private HashMap<Integer, UserStatus> mUserStatusMap = new HashMap<>();
    private HashSet<View> mRemoteViewCache = new HashSet<>();
    private Handler mUiHandler = new Handler();

    @Override
    public void onUserJoined(int uid, int elapsed) {
        // 用户加入了
        UserStatus userStatus = ensureJoin(uid);
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_JOIN, userStatus));
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        // 用户离开
        UserStatus userStatus = mUserStatusMap.remove(uid);
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_LEAVE, userStatus));
    }

    @Override
    public void onUserMuteVideo(int uid, boolean muted) {
        UserStatus status = ensureJoin(uid);
        status.setVideoMute(muted);
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_MUTE_VIDEO, status));
    }

    @Override
    public void onUserMuteAudio(int uid, boolean muted) {
        UserStatus status = ensureJoin(uid);
        status.setAudioMute(muted);
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_MUTE_AUDIO, status));
    }

    @Override
    public void onUserEnableVideo(int uid, boolean enabled) {
        UserStatus status = ensureJoin(uid);
        status.setEnableVideo(enabled);
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_VIDEO_ENABLE, status));
    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        UserStatus status = ensureJoin(uid);
        status.setFirstVideoDecoded(true);
        status.setFirstVideoWidth(width);
        status.setFirstVideoHeight(height);
        tryBindRemoteViewAutoOnMainThread("onFirstRemoteVideoDecoded");
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_FIRST_VIDEO_DECODED, status));
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        UserStatus userStatus = ensureJoin(uid);
        userStatus.setIsSelf(true);
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_JOIN, userStatus));
    }

    @Override
    public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
        UserStatus userStatus = ensureJoin(uid);
        userStatus.setIsSelf(true);
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_REJOIN, userStatus));
    }

    @Override
    public void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {

    }

    @Override
    public void onClientRoleChanged(int oldRole, int newRole) {
        // 只有切换时才会触发
    }

    @Override
    public void onVideoSizeChanged(int uid, int width, int height, int rotation) {

    }

    private UserStatus ensureJoin(int uid) {
        if (!mUserStatusMap.containsKey(uid)) {
            UserStatus userStatus = new UserStatus(uid);
            userStatus.setEnterTs(System.currentTimeMillis());
            mUserStatusMap.put(uid, userStatus);
            return userStatus;
        } else {
            return mUserStatusMap.get(uid);
        }
    }

    private static class EngineManagerHolder {
        private static final EngineManager INSTANCE = new EngineManager();
    }

    private EngineManager() {
        AgoraEngineAdapter.getInstance().setOutCallback(this);
    }

    public static final EngineManager getInstance() {
        return EngineManagerHolder.INSTANCE;
    }

    public void init(Params params) {
        destroy();
        mConfig = params;
        AgoraEngineAdapter.getInstance().init(mConfig);
        CbEngineAdapter.getInstance().init(mConfig);
    }

    public Params getParams() {
        return mConfig;
    }

    /**
     * 销毁所有
     */
    public void destroy() {
        AgoraEngineAdapter.getInstance().destroy(true);
        CbEngineAdapter.getInstance().destroy();
        mUserStatusMap.clear();
        mRemoteViewCache.clear();
        mUiHandler.removeCallbacksAndMessages(null);
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_ENGINE_DESTROY,null));
    }

    /**
     * 开启唱吧引擎的自采集视频预览
     * 这个view也是之后的本地view
     */
    public void startPreview(SurfaceView surfaceView) {
        if (mConfig.isUseCbEngine()) {
            CbEngineAdapter.getInstance().startPreview(surfaceView);
        } else {
            // agora引擎好像加入房间后，预览才有效果
            AgoraEngineAdapter.getInstance().setLocalVideoRenderer(surfaceView);
            AgoraEngineAdapter.getInstance().startPreview();
        }
    }

    /**
     * 开启唱吧引擎的自采集视频预览
     */
    public void stopPreview() {
        if (mConfig.isUseCbEngine()) {
            CbEngineAdapter.getInstance().stopPreview();
        } else {
            AgoraEngineAdapter.getInstance().stopPreview();
        }
    }

    public void startRecord() {
        if (mConfig.isUseCbEngine()) {
            CbEngineAdapter.getInstance().startRecord();
        } else {
            U.getToastUtil().showShort("mConfig.isUseCbEngine is false ，cancel");
        }
    }

    /**
     * 加入agora的房间
     *
     * @param roomid
     * @param userId
     * @param isAnchor 是否以主播的身份
     *                 不是主播只看不能说
     */
    public void joinRoom(String roomid, int userId, boolean isAnchor) {
        if (userId <= 0) {
            userId = 0;
        }
        if (mConfig.getChannelProfile() == Params.CHANNEL_TYPE_LIVE_BROADCASTING) {
            if (isAnchor) {
                AgoraEngineAdapter.getInstance().setClientRole(isAnchor);
            } else {
                AgoraEngineAdapter.getInstance().setClientRole(isAnchor);
            }
        }
        AgoraEngineAdapter.getInstance().joinChannel(null, roomid, "Extra Optional Data", userId);
    }

    public void setClientRole(boolean isAnchor) {
        AgoraEngineAdapter.getInstance().setClientRole(isAnchor);
    }

    /**
     * 绑定远端用户的视频view
     * 如果uid传的是0，会自动绑定一个当前没有绑定视图的用户
     * 如果当前都绑定视图，等下一个 onFirstRemoteVideoDecoded 就会绑定消费掉该视图
     *
     * @param uid
     * @param view
     */
    public void bindRemoteView(final int uid, final TextureView view) {
        MyLog.d(TAG, "bindRemoteView" + " uid=" + uid + " view=" + view);
        if (uid != 0) {
            final UserStatus userStatus = mUserStatusMap.get(uid);
            if (userStatus != null) {
                adjustViewWH2VideoWH(view, userStatus.getFirstVideoWidth(), userStatus.getFirstVideoHeight());
            }
            if (Looper.getMainLooper() == Looper.myLooper()) {
                userStatus.setView(view);
                AgoraEngineAdapter.getInstance().setRemoteVideoRenderer(uid, view);
            } else {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        userStatus.setView(view);
                        AgoraEngineAdapter.getInstance().setRemoteVideoRenderer(uid, view);
                    }
                });
            }
        } else {
            mRemoteViewCache.add(view);
            tryBindRemoteViewAutoOnMainThread("bindRemoteView");
        }
    }

    public void bindRemoteView(final int uid, final SurfaceView view) {
        MyLog.d(TAG, "bindRemoteView" + " uid=" + uid + " view=" + view);
        if (uid != 0) {
            final UserStatus userStatus = mUserStatusMap.get(uid);
            if (userStatus != null) {
                adjustViewWH2VideoWH(view, userStatus.getFirstVideoWidth(), userStatus.getFirstVideoHeight());
            }
            if (Looper.getMainLooper() == Looper.myLooper()) {
                userStatus.setView(view);
                AgoraEngineAdapter.getInstance().setRemoteVideoRenderer(uid, view);
            } else {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        userStatus.setView(view);
                        AgoraEngineAdapter.getInstance().setRemoteVideoRenderer(uid, view);
                    }
                });
            }
        } else {
            mRemoteViewCache.add(view);
            tryBindRemoteViewAutoOnMainThread("bindRemoteView");
        }
    }

    /**
     * 尝试自动绑定视图
     */
    private void tryBindRemoteViewAutoOnMainThread(String from) {
        MyLog.d(TAG, "tryBindRemoteViewAutoOnMainThread" + " from=" + from);
        if (Looper.myLooper() == Looper.getMainLooper()) {
            tryBindRemoteViewAuto();
        } else {
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    tryBindRemoteViewAuto();
                }
            });
        }

        return;
    }

    private void tryBindRemoteViewAuto() {
        // 判断当前有没有未绑定的
        List<View> canRemoveViews = new ArrayList<>();
        for (View view : mRemoteViewCache) {
            for (int key : mUserStatusMap.keySet()) {
                UserStatus userStatus = mUserStatusMap.get(key);
                if (!userStatus.isSelf()
                        && !userStatus.hasBindView()
                        && !userStatus.isVideoMute()
                        && userStatus.isFirstVideoDecoded()
                        ) {
                    // 这个用户有资格消费一个 surfaceview
                    if (view instanceof TextureView) {
                        canRemoveViews.add(view);
                        userStatus.setView(view);
                        adjustViewWH2VideoWH(view, userStatus.getFirstVideoWidth(), userStatus.getFirstVideoHeight());
                        AgoraEngineAdapter.getInstance().setRemoteVideoRenderer(userStatus.getUserId(), (TextureView) view);
                        break;
                    } else if (view instanceof SurfaceView) {
                        canRemoveViews.add(view);
                        userStatus.setView(view);
                        adjustViewWH2VideoWH(view, userStatus.getFirstVideoWidth(), userStatus.getFirstVideoHeight());
                        AgoraEngineAdapter.getInstance().setRemoteVideoRenderer(userStatus.getUserId(), (SurfaceView) view);
                        break;
                    }
                }
            }
        }
        for (View view : canRemoveViews) {
            mRemoteViewCache.remove(view);
        }
    }

    /**
     * 矫正view的宽高和视频一致
     *
     * @param view
     * @param width
     * @param height
     */
    private void adjustViewWH2VideoWH(View view, int width, int height) {
        MyLog.d(TAG, "adjustViewWH2VideoWH" + " view=" + view + " width=" + width + " height=" + height);
        if (width != 0 && height != 0) {
            // 适应一下视频流的宽和高
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            lp.width = width;
            lp.height = height;
        }
    }

    /**
     * 播放音效
     */
    public void playEffects(EffectModel effectModel) {
        AgoraEngineAdapter.getInstance().playEffects(effectModel);
    }

    public List<EffectModel> getAllEffects() {
        return AgoraEngineAdapter.getInstance().getAllEffects();
    }


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
        MyLog.d(TAG,"setLocalVoicePitch" + " pitch=" + pitch);
        mConfig.setLocalVoicePitch(pitch);
        AgoraEngineAdapter.getInstance().setLocalVoicePitch(pitch);
    }

    /**
     * 设置本地语音音效均衡
     *
     * @param bandFrequency 频谱子带索引，取值范围是 [0-9]，分别代表 10 个频带，对应的中心频率是 [31，62，125，250，500，1k，2k，4k，8k，16k] Hz
     * @param bandGain      每个 band 的增益，单位是 dB，每一个值的范围是 [-15，15]，默认值为 0
     */
    public void setLocalVoiceEqualization(int bandFrequency, int bandGain) {
        AgoraEngineAdapter.getInstance().setLocalVoiceEqualization(bandFrequency, bandGain);
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
        AgoraEngineAdapter.getInstance().setLocalVoiceReverb(reverbKey, value);
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
    public void startAudioMixing(String filePath, boolean loopback, boolean replace, int cycle) {
        AgoraEngineAdapter.getInstance().startAudioMixing(filePath, loopback, replace, cycle);
    }

    /**
     * 停止播放音乐文件及混音。
     * 请在频道内调用该方法。
     */
    public void stopAudioMixing() {
        AgoraEngineAdapter.getInstance().stopAudioMixing();
    }

    /**
     * 暂停播放音乐文件及混音
     */
    public void pauseAudioMixing() {
        AgoraEngineAdapter.getInstance().pauseAudioMixing();
    }

    /**
     * 继续播放混音
     */
    public void resumeAudioMixing() {
        AgoraEngineAdapter.getInstance().resumeAudioMixing();
    }

    /**
     * 调节混音音量大小
     *
     * @param volume 1-100 默认100
     */
    public void adjustAudioMixingVolume(int volume) {
        AgoraEngineAdapter.getInstance().adjustAudioMixingVolume(volume);
    }

    /**
     * @return 获取伴奏时长，单位ms
     */
    public int getAudioMixingDuration() {
        return AgoraEngineAdapter.getInstance().getAudioMixingDuration();
    }

    /**
     * @return 获取混音当前播放位置 ms
     */
    public int getAudioMixingCurrentPosition() {
        return AgoraEngineAdapter.getInstance().getAudioMixingCurrentPosition();
    }

    /**
     * 拖动混音进度条
     *
     * @param posMs
     */
    public void setAudioMixingPosition(int posMs) {
        AgoraEngineAdapter.getInstance().setAudioMixingPosition(posMs);
    }

    /*音频高级扩展结束*/
}
