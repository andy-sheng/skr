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

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
        tryBindRemoteViewAutoOnMainThread();
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
    }

    /**
     * 销毁所有
     */
    public void destroy() {
        AgoraEngineAdapter.getInstance().destroy();
        CbEngineAdapter.getInstance().destroy();
        mUserStatusMap.clear();
        mRemoteViewCache.clear();
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

    /**
     * 绑定远端用户的视频view
     * 如果uid传的是0，会自动绑定一个当前没有绑定视图的用户
     * 如果当前都绑定视图，等下一个 onFirstRemoteVideoDecoded 就会绑定消费掉该视图
     *
     * @param uid
     * @param view
     */
    public void bindRemoteView(int uid, TextureView view) {
        MyLog.d(TAG, "bindRemoteView" + " uid=" + uid + " view=" + view);
        if (uid != 0) {
            AgoraEngineAdapter.getInstance().setRemoteVideoRenderer(uid, view);
        } else {
            mRemoteViewCache.add(view);
            tryBindRemoteViewAutoOnMainThread();
        }
    }

    public void bindRemoteView(int uid, SurfaceView view) {
        if (uid != 0) {
            UserStatus userStatus = mUserStatusMap.get(uid);
            if (userStatus != null) {
                adjustViewWH2VideoWH(view, userStatus.getFirstVideoWidth(), userStatus.getFirstVideoHeight());
            }
            AgoraEngineAdapter.getInstance().setRemoteVideoRenderer(uid, view);
        } else {
            mRemoteViewCache.add(view);
            tryBindRemoteViewAutoOnMainThread();
        }
    }

    /**
     * 尝试自动绑定视图
     */
    private void tryBindRemoteViewAutoOnMainThread() {
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
        if (width != 0 && height != 0) {
            // 适应一下视频流的宽和高
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            lp.width = width;
            lp.height = height;
        }
    }
}
