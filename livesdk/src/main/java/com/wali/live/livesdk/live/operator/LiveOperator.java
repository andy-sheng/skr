package com.wali.live.livesdk.live.operator;

import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.activity.RxActivity;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.presenter.RxLifeCyclePresenter;
import com.mi.live.data.push.presenter.RoomMessagePresenter;
import com.mi.live.data.repository.RoomMessageRepository;
import com.mi.live.data.repository.datasource.RoomMessageStore;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.engine.streamer.IStreamer;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yangli on 16-9-3.
 *
 * @moudle 直播操作辅助类
 */
public class LiveOperator extends RxLifeCyclePresenter {
    private static final String TAG = "LiveOperator";

    private AudioManager mAudioManager;

    private @Nullable IStreamer mStreamer;
    private RoomMessagePresenter mPullRoomMessagePresenter;

    public LiveOperator() {
    }

    private JSONObject getOptimalKaraOkParams() {
        MyLog.w(TAG, "getOptimalKaraOkParams");
        JSONObject object = new JSONObject();
        try {
            object.put(IStreamer.OPTIMAL_MUSIC_IN_SPEAKER_MODE,
                    PreferenceUtils.getSettingFloat(GlobalData.app(), IStreamer.OPTIMAL_MUSIC_IN_SPEAKER_MODE, 1.0f));
            object.put(IStreamer.OPTIMAL_VOICE_IN_SPEAKER_MODE,
                    PreferenceUtils.getSettingFloat(GlobalData.app(), IStreamer.OPTIMAL_VOICE_IN_SPEAKER_MODE, 1.0f));
            object.put(IStreamer.OPTIMAL_ONLY_VOICE_IN_SPEAKER_MODE,
                    PreferenceUtils.getSettingFloat(GlobalData.app(), IStreamer.OPTIMAL_ONLY_VOICE_IN_SPEAKER_MODE, 1.0f));
            object.put(IStreamer.OPTIMAL_MUSIC_IN_HEADSET_MODE,
                    PreferenceUtils.getSettingFloat(GlobalData.app(), IStreamer.OPTIMAL_MUSIC_IN_HEADSET_MODE, 1.0f));
            object.put(IStreamer.OPTIMAL_VOICE_IN_HEADSET_MODE,
                    PreferenceUtils.getSettingFloat(GlobalData.app(), IStreamer.OPTIMAL_VOICE_IN_HEADSET_MODE, 1.0f));
            object.put(IStreamer.OPTIMAL_INTRINSIC_MUSIC_VOICE_DELAY,
                    PreferenceUtils.getSettingInt(GlobalData.app(), IStreamer.OPTIMAL_INTRINSIC_MUSIC_VOICE_DELAY, 0));
        } catch (JSONException e) {
            MyLog.e(TAG, "getOptimalKaraOkParams failed, exception=" + e);
        }
        return object;
    }

    public void onStartRecord(@NonNull RxActivity rxActivity, @NonNull IStreamer streamer, @NonNull RoomBaseDataModel myRoomData) {
        mStreamer = streamer;
        mStreamer.setOptimalDefaultParams(getOptimalKaraOkParams());
        mStreamer.setVoiceVolume(1.0f); // TODO 参数拉取和存储 YangLi
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) GlobalData.app().getSystemService(Context.AUDIO_SERVICE);
        }
        mStreamer.setHeadsetPlugged(mAudioManager.isWiredHeadsetOn() || mAudioManager.isBluetoothA2dpOn());
        // 对外的不收push了，统一走拉取模式
        if (mPullRoomMessagePresenter == null) {
            mPullRoomMessagePresenter = new RoomMessagePresenter(myRoomData, new RoomMessageRepository(new RoomMessageStore()), rxActivity);
        }
        mPullRoomMessagePresenter.startWork();
    }

    public void onStopRecord() {
        if (mStreamer != null) {
            mStreamer.stopStream();
            mStreamer = null;
        }
        if (mPullRoomMessagePresenter != null) {
            mPullRoomMessagePresenter.stopWork();
            mPullRoomMessagePresenter.destroy();
            mPullRoomMessagePresenter = null;
        }
    }
}
