package com.engine;

import android.view.SurfaceView;

import com.changba.songstudio.CbEngineAdapter;
import com.engine.agora.AgoraEngineAdapter;
import com.engine.agora.AgoraOutCallback;

import java.util.HashMap;

public class EngineManager implements AgoraOutCallback {

    /**
     * 存储该房间所有用户在引擎中的状态的，
     * key为在引擎中的用户 id
     */
    private HashMap<String,UserStatus> mUserStatusMap = new HashMap<>();

    @Override
    public void onUserJoined(int uid, int elapsed) {
        // 用户加入了
        ensureJoin(uid);
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        // 用户离开房间了
        String key = String.valueOf(uid);
        mUserStatusMap.remove(key);
    }

    @Override
    public void onUserMuteVideo(int uid, boolean muted) {
        UserStatus status = ensureJoin(uid);
        status.setVideoMute(muted);
    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        UserStatus status = ensureJoin(uid);
        status.setFirstVideoDecoded(true);
    }

    private UserStatus ensureJoin(int uid){
        String key = String.valueOf(uid);
        if(!mUserStatusMap.containsKey(key)){
            UserStatus userStatus = new UserStatus(key);
            userStatus.setEnterTs(System.currentTimeMillis());
            mUserStatusMap.put(key,userStatus);
            return userStatus;
        }else{
            return mUserStatusMap.get(key);
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

    public void destroy(){
        AgoraEngineAdapter.getInstance().destroy();
        mUserStatusMap.clear();
    }

    /**
     * 开启唱吧引擎的自采集视频预览
     */
    public void startPreview(SurfaceView surfaceView){

        CbEngineAdapter.getInstance().startPreview(surfaceView);
    }

}
