//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.manager;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.io.File;

import io.rong.common.RLog;
import io.rong.imkit.R;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imlib.IRongCallback.ISendMessageCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.TypingMessage.TypingMessageManager;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.message.VoiceMessage;

public class AudioRecordManager implements Callback {
    private static final String TAG = "AudioRecordManager";
    private int RECORD_INTERVAL;
    private IAudioState mCurAudioState;
    private View mRootView;
    private Context mContext;
    private ConversationType mConversationType;
    private String mTargetId;
    private Handler mHandler;
    private AudioManager mAudioManager;
    private MediaRecorder mMediaRecorder;
    private Uri mAudioPath;
    private long smStartRecTime;
    private OnAudioFocusChangeListener mAfChangeListener;
    private PopupWindow mRecordWindow;
    private ImageView mStateIV;
    private TextView mStateTV;
    private TextView mTimerTV;
    IAudioState idleState;
    IAudioState recordState;
    IAudioState sendingState;
    IAudioState cancelState;
    IAudioState timerState;

    public static io.rong.imkit.manager.AudioRecordManager getInstance() {
        return io.rong.imkit.manager.AudioRecordManager.SingletonHolder.sInstance;
    }

    @TargetApi(21)
    private AudioRecordManager() {
        this.RECORD_INTERVAL = 60;
        this.idleState = new io.rong.imkit.manager.AudioRecordManager.IdleState();
        this.recordState = new io.rong.imkit.manager.AudioRecordManager.RecordState();
        this.sendingState = new io.rong.imkit.manager.AudioRecordManager.SendingState();
        this.cancelState = new io.rong.imkit.manager.AudioRecordManager.CancelState();
        this.timerState = new io.rong.imkit.manager.AudioRecordManager.TimerState();
        RLog.d("AudioRecordManager", "AudioRecordManager");
        if (VERSION.SDK_INT < 21) {
            try {
                TelephonyManager manager = (TelephonyManager) RongContext.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
                manager.listen(new PhoneStateListener() {
                    public void onCallStateChanged(int state, String incomingNumber) {
                        switch (state) {
                            case 1:
                                io.rong.imkit.manager.AudioRecordManager.this.sendEmptyMessage(6);
                            case 0:
                            case 2:
                            default:
                                super.onCallStateChanged(state, incomingNumber);
                        }
                    }
                }, 32);
            } catch (SecurityException var2) {
                var2.printStackTrace();
            }
        }

        this.mCurAudioState = this.idleState;
        this.idleState.enter();
    }

    public final boolean handleMessage(Message msg) {
        RLog.i("AudioRecordManager", "handleMessage " + msg.what);
        AudioStateMessage m;
        switch (msg.what) {
            case 2:
                this.sendEmptyMessage(2);
                break;
            case 7:
                m = AudioStateMessage.obtain();
                m.what = msg.what;
                m.obj = msg.obj;
                this.sendMessage(m);
                break;
            case 8:
                m = AudioStateMessage.obtain();
                m.what = 7;
                m.obj = msg.obj;
                this.sendMessage(m);
        }

        return false;
    }

    private void initView(View root) {
        this.mHandler = new Handler(root.getHandler().getLooper(), this);
        LayoutInflater inflater = LayoutInflater.from(root.getContext());
        View view = inflater.inflate(R.layout.rc_wi_vo_popup, (ViewGroup) null);
        this.mStateIV = (ImageView) view.findViewById(R.id.rc_audio_state_image);
        this.mStateTV = (TextView) view.findViewById(R.id.rc_audio_state_text);
        this.mTimerTV = (TextView) view.findViewById(R.id.rc_audio_timer);
        this.mRecordWindow = new PopupWindow(view, -1, -1);
        this.mRecordWindow.showAtLocation(root, 17, 0, 0);
        this.mRecordWindow.setFocusable(true);
        this.mRecordWindow.setOutsideTouchable(false);
        this.mRecordWindow.setTouchable(false);
    }

    private void setTimeoutView(int counter) {
        if (counter > 0) {
            if (this.mRecordWindow != null) {
                this.mStateIV.setVisibility(View.GONE);
                this.mStateTV.setVisibility(View.VISIBLE);
                this.mStateTV.setText(R.string.rc_voice_rec);
                this.mStateTV.setBackgroundResource(R.color.black_trans_30);
                this.mTimerTV.setText(String.format("%s", counter));
                this.mTimerTV.setVisibility(View.VISIBLE);
            }
        } else if (this.mRecordWindow != null) {
            this.mStateIV.setVisibility(View.VISIBLE);
            this.mStateIV.setImageResource(R.drawable.rc_ic_volume_wraning);
            this.mStateTV.setText(R.string.rc_voice_too_long);
            this.mStateTV.setBackgroundResource(R.color.black_trans_30);
            this.mTimerTV.setVisibility(View.GONE);
        }

    }

    private void setRecordingView() {
        RLog.d("AudioRecordManager", "setRecordingView");
        if (this.mRecordWindow != null) {
            this.mStateIV.setVisibility(View.VISIBLE);
            this.mStateIV.setImageResource(R.drawable.rc_ic_volume_1);
            this.mStateTV.setVisibility(View.VISIBLE);
            this.mStateTV.setText(R.string.rc_voice_rec);
            this.mStateTV.setBackgroundResource(R.color.black_trans_30);
            this.mTimerTV.setVisibility(View.GONE);
        }

    }

    private void setCancelView() {
        RLog.d("AudioRecordManager", "setCancelView");
        if (this.mRecordWindow != null) {
            this.mTimerTV.setVisibility(View.GONE);
            this.mStateIV.setVisibility(View.VISIBLE);
            this.mStateIV.setImageResource(R.drawable.rc_ic_volume_cancel);
            this.mStateTV.setVisibility(View.VISIBLE);
            this.mStateTV.setText(R.string.rc_voice_cancel);
            this.mStateTV.setBackgroundResource(R.drawable.rc_corner_voice_style);
        }

    }

    private void destroyView() {
        RLog.d("AudioRecordManager", "destroyView");
        if (this.mRecordWindow != null) {
            this.mHandler.removeMessages(7);
            this.mHandler.removeMessages(8);
            this.mHandler.removeMessages(2);
            this.mRecordWindow.dismiss();
            this.mRecordWindow = null;
            this.mStateIV = null;
            this.mStateTV = null;
            this.mTimerTV = null;
            this.mHandler = null;
            this.mContext = null;
            this.mRootView = null;
        }

    }

    public void setMaxVoiceDuration(int maxVoiceDuration) {
        this.RECORD_INTERVAL = maxVoiceDuration;
    }

    public int getMaxVoiceDuration() {
        return this.RECORD_INTERVAL;
    }

    public void startRecord(View rootView, ConversationType conversationType, String targetId) {
        this.mRootView = rootView;
        this.mContext = rootView.getContext().getApplicationContext();
        this.mConversationType = conversationType;
        this.mTargetId = targetId;
        this.mAudioManager = (AudioManager) this.mContext.getSystemService(Context.AUDIO_SERVICE);
        if (rootView != null) {
            if (this.mAfChangeListener != null) {
                this.mAudioManager.abandonAudioFocus(this.mAfChangeListener);
                this.mAfChangeListener = null;
            }

            this.mAfChangeListener = new OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    RLog.d("AudioRecordManager", "OnAudioFocusChangeListener " + focusChange);
                    if (focusChange == -1) {
                        io.rong.imkit.manager.AudioRecordManager.this.mAudioManager.abandonAudioFocus(io.rong.imkit.manager.AudioRecordManager.this.mAfChangeListener);
                        io.rong.imkit.manager.AudioRecordManager.this.mAfChangeListener = null;
                        io.rong.imkit.manager.AudioRecordManager.this.sendEmptyMessage(6);
                    }

                }
            };
            this.sendEmptyMessage(1);
            if (TypingMessageManager.getInstance().isShowMessageTyping()) {
                RongIMClient.getInstance().sendTypingStatus(conversationType, targetId, "RC:VcMsg");
            }

        }
    }

    public void willCancelRecord() {
        this.sendEmptyMessage(3);
    }

    public void continueRecord() {
        this.sendEmptyMessage(4);
    }

    public void stopRecord() {
        this.sendEmptyMessage(5);
    }

    public void destroyRecord() {
        AudioStateMessage msg = new AudioStateMessage();
        msg.obj = true;
        msg.what = 5;
        this.sendMessage(msg);
    }

    void sendMessage(AudioStateMessage message) {
        this.mCurAudioState.handleMessage(message);
    }

    void sendEmptyMessage(int event) {
        AudioStateMessage message = AudioStateMessage.obtain();
        message.what = event;
        this.mCurAudioState.handleMessage(message);
    }

    private void startRec() {
        RLog.d("AudioRecordManager", "startRec");

        try {
            this.muteAudioFocus(this.mAudioManager, true);
            this.mAudioManager.setMode(0);
            this.mMediaRecorder = new MediaRecorder();

            try {
                Resources resources = this.mContext.getResources();
                int bps = resources.getInteger(resources.getIdentifier("rc_audio_encoding_bit_rate", "integer", this.mContext.getPackageName()));
                this.mMediaRecorder.setAudioSamplingRate(8000);
                this.mMediaRecorder.setAudioEncodingBitRate(bps);
            } catch (NotFoundException var3) {
                var3.printStackTrace();
            }

            this.mMediaRecorder.setAudioChannels(1);
            this.mMediaRecorder.setAudioSource(1);
            this.mMediaRecorder.setOutputFormat(3);
            this.mMediaRecorder.setAudioEncoder(1);
            this.mAudioPath = Uri.fromFile(new File(this.mContext.getCacheDir(), System.currentTimeMillis() + "temp.voice"));
            this.mMediaRecorder.setOutputFile(this.mAudioPath.getPath());
            this.mMediaRecorder.prepare();
            this.mMediaRecorder.start();
            Message message = Message.obtain();
            message.what = 7;
            message.obj = 10;
            this.mHandler.sendMessageDelayed(message, (long) (this.RECORD_INTERVAL * 1000 - 10000));
        } catch (Exception var4) {
            var4.printStackTrace();
        }

    }

    private boolean checkAudioTimeLength() {
        long delta = SystemClock.elapsedRealtime() - this.smStartRecTime;
        return delta < 1000L;
    }

    private void stopRec() {
        RLog.d("AudioRecordManager", "stopRec");

        try {
            this.muteAudioFocus(this.mAudioManager, false);
            if (this.mMediaRecorder != null) {
                this.mMediaRecorder.stop();
                this.mMediaRecorder.release();
                this.mMediaRecorder = null;
            }
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    private void deleteAudioFile() {
        RLog.d("AudioRecordManager", "deleteAudioFile");
        if (this.mAudioPath != null) {
            File file = new File(this.mAudioPath.getPath());
            if (file.exists()) {
                file.delete();
            }
        }

    }

    private void sendAudioFile() {
        RLog.d("AudioRecordManager", "sendAudioFile path = " + this.mAudioPath);
        if (this.mAudioPath != null) {
            File file = new File(this.mAudioPath.getPath());
            if (!file.exists() || file.length() == 0L) {
                RLog.e("AudioRecordManager", "sendAudioFile fail cause of file length 0 or audio permission denied");
                return;
            }

            int duration = (int) (SystemClock.elapsedRealtime() - this.smStartRecTime) / 1000;
            VoiceMessage voiceMessage = VoiceMessage.obtain(this.mAudioPath, duration > this.RECORD_INTERVAL ? this.RECORD_INTERVAL : duration);
            RongIM.getInstance().sendMessage(io.rong.imlib.model.Message.obtain(this.mTargetId, this.mConversationType, voiceMessage), (String) null, (String) null, new ISendMessageCallback() {
                public void onAttached(io.rong.imlib.model.Message message) {
                }

                public void onSuccess(io.rong.imlib.model.Message message) {
                }

                public void onError(io.rong.imlib.model.Message message, ErrorCode errorCode) {
                }
            });
        }

    }

    private void audioDBChanged() {
        if (this.mMediaRecorder != null) {
            int db = this.mMediaRecorder.getMaxAmplitude() / 600;
            switch (db / 5) {
                case 0:
                    this.mStateIV.setImageResource(R.drawable.rc_ic_volume_1);
                    break;
                case 1:
                    this.mStateIV.setImageResource(R.drawable.rc_ic_volume_2);
                    break;
                case 2:
                    this.mStateIV.setImageResource(R.drawable.rc_ic_volume_3);
                    break;
                case 3:
                    this.mStateIV.setImageResource(R.drawable.rc_ic_volume_4);
                    break;
                case 4:
                    this.mStateIV.setImageResource(R.drawable.rc_ic_volume_5);
                    break;
                case 5:
                    this.mStateIV.setImageResource(R.drawable.rc_ic_volume_6);
                    break;
                case 6:
                    this.mStateIV.setImageResource(R.drawable.rc_ic_volume_7);
                    break;
                default:
                    this.mStateIV.setImageResource(R.drawable.rc_ic_volume_8);
            }
        }

    }

    private void muteAudioFocus(AudioManager audioManager, boolean bMute) {
        if (VERSION.SDK_INT < 8) {
            RLog.d("AudioRecordManager", "muteAudioFocus Android 2.1 and below can not stop music");
        } else {
            if (bMute) {
                audioManager.requestAudioFocus(this.mAfChangeListener, 3, 2);
            } else {
                audioManager.abandonAudioFocus(this.mAfChangeListener);
                this.mAfChangeListener = null;
            }

        }
    }

    class TimerState extends IAudioState {
        TimerState() {
        }

        void handleMessage(AudioStateMessage msg) {
            RLog.d("AudioRecordManager", this.getClass().getSimpleName() + " handleMessage : " + msg.what);
            switch (msg.what) {
                case 3:
                    io.rong.imkit.manager.AudioRecordManager.this.setCancelView();
                    io.rong.imkit.manager.AudioRecordManager.this.mCurAudioState = io.rong.imkit.manager.AudioRecordManager.this.cancelState;
                case 4:
                default:
                    break;
                case 5:
                    io.rong.imkit.manager.AudioRecordManager.this.mHandler.postDelayed(new Runnable() {
                        public void run() {
                            io.rong.imkit.manager.AudioRecordManager.this.stopRec();
                            io.rong.imkit.manager.AudioRecordManager.this.sendAudioFile();
                            io.rong.imkit.manager.AudioRecordManager.this.destroyView();
                        }
                    }, 500L);
                    io.rong.imkit.manager.AudioRecordManager.this.mCurAudioState = io.rong.imkit.manager.AudioRecordManager.this.idleState;
                    io.rong.imkit.manager.AudioRecordManager.this.idleState.enter();
                    break;
                case 6:
                    io.rong.imkit.manager.AudioRecordManager.this.stopRec();
                    io.rong.imkit.manager.AudioRecordManager.this.destroyView();
                    io.rong.imkit.manager.AudioRecordManager.this.deleteAudioFile();
                    io.rong.imkit.manager.AudioRecordManager.this.mCurAudioState = io.rong.imkit.manager.AudioRecordManager.this.idleState;
                    io.rong.imkit.manager.AudioRecordManager.this.idleState.enter();
                    break;
                case 7:
                    int counter = (Integer) msg.obj;
                    if (counter >= 0) {
                        Message message = Message.obtain();
                        message.what = 8;
                        message.obj = counter - 1;
                        io.rong.imkit.manager.AudioRecordManager.this.mHandler.sendMessageDelayed(message, 1000L);
                        io.rong.imkit.manager.AudioRecordManager.this.setTimeoutView(counter);
                    } else {
                        io.rong.imkit.manager.AudioRecordManager.this.mHandler.postDelayed(new Runnable() {
                            public void run() {
                                io.rong.imkit.manager.AudioRecordManager.this.stopRec();
                                io.rong.imkit.manager.AudioRecordManager.this.sendAudioFile();
                                io.rong.imkit.manager.AudioRecordManager.this.destroyView();
                            }
                        }, 500L);
                        io.rong.imkit.manager.AudioRecordManager.this.mCurAudioState = io.rong.imkit.manager.AudioRecordManager.this.idleState;
                    }
            }

        }
    }

    class CancelState extends IAudioState {
        CancelState() {
        }

        void handleMessage(AudioStateMessage msg) {
            RLog.d("AudioRecordManager", this.getClass().getSimpleName() + " handleMessage : " + msg.what);
            switch (msg.what) {
                case 1:
                case 2:
                case 3:
                default:
                    break;
                case 4:
                    io.rong.imkit.manager.AudioRecordManager.this.setRecordingView();
                    io.rong.imkit.manager.AudioRecordManager.this.mCurAudioState = io.rong.imkit.manager.AudioRecordManager.this.recordState;
                    io.rong.imkit.manager.AudioRecordManager.this.sendEmptyMessage(2);
                    break;
                case 5:
                case 6:
                    io.rong.imkit.manager.AudioRecordManager.this.stopRec();
                    io.rong.imkit.manager.AudioRecordManager.this.destroyView();
                    io.rong.imkit.manager.AudioRecordManager.this.deleteAudioFile();
                    io.rong.imkit.manager.AudioRecordManager.this.mCurAudioState = io.rong.imkit.manager.AudioRecordManager.this.idleState;
                    io.rong.imkit.manager.AudioRecordManager.this.idleState.enter();
                    break;
                case 7:
                    int counter = (Integer) msg.obj;
                    if (counter > 0) {
                        Message message = Message.obtain();
                        message.what = 8;
                        message.obj = counter - 1;
                        io.rong.imkit.manager.AudioRecordManager.this.mHandler.sendMessageDelayed(message, 1000L);
                    } else {
                        io.rong.imkit.manager.AudioRecordManager.this.mHandler.postDelayed(new Runnable() {
                            public void run() {
                                io.rong.imkit.manager.AudioRecordManager.this.stopRec();
                                io.rong.imkit.manager.AudioRecordManager.this.sendAudioFile();
                                io.rong.imkit.manager.AudioRecordManager.this.destroyView();
                            }
                        }, 500L);
                        io.rong.imkit.manager.AudioRecordManager.this.mCurAudioState = io.rong.imkit.manager.AudioRecordManager.this.idleState;
                        io.rong.imkit.manager.AudioRecordManager.this.idleState.enter();
                    }
            }

        }
    }

    class SendingState extends IAudioState {
        SendingState() {
        }

        void handleMessage(AudioStateMessage message) {
            RLog.d("AudioRecordManager", "SendingState handleMessage " + message.what);
            switch (message.what) {
                case 9:
                    io.rong.imkit.manager.AudioRecordManager.this.stopRec();
                    if ((Boolean) message.obj) {
                        io.rong.imkit.manager.AudioRecordManager.this.sendAudioFile();
                    }

                    io.rong.imkit.manager.AudioRecordManager.this.destroyView();
                    io.rong.imkit.manager.AudioRecordManager.this.mCurAudioState = io.rong.imkit.manager.AudioRecordManager.this.idleState;
                default:
            }
        }
    }

    class RecordState extends IAudioState {
        RecordState() {
        }

        void handleMessage(AudioStateMessage msg) {
            RLog.d("AudioRecordManager", this.getClass().getSimpleName() + " handleMessage : " + msg.what);
            switch (msg.what) {
                case 2:
                    io.rong.imkit.manager.AudioRecordManager.this.audioDBChanged();
                    io.rong.imkit.manager.AudioRecordManager.this.mHandler.sendEmptyMessageDelayed(2, 150L);
                    break;
                case 3:
                    io.rong.imkit.manager.AudioRecordManager.this.setCancelView();
                    io.rong.imkit.manager.AudioRecordManager.this.mCurAudioState = io.rong.imkit.manager.AudioRecordManager.this.cancelState;
                case 4:
                default:
                    break;
                case 5:
                    final boolean checked = io.rong.imkit.manager.AudioRecordManager.this.checkAudioTimeLength();
                    boolean activityFinished = false;
                    if (msg.obj != null) {
                        activityFinished = (Boolean) msg.obj;
                    }

                    if (checked && !activityFinished) {
                        io.rong.imkit.manager.AudioRecordManager.this.mStateIV.setImageResource(R.drawable.rc_ic_volume_wraning);
                        io.rong.imkit.manager.AudioRecordManager.this.mStateTV.setText(R.string.rc_voice_short);
                        io.rong.imkit.manager.AudioRecordManager.this.mHandler.removeMessages(2);
                    }

                    if (!activityFinished && io.rong.imkit.manager.AudioRecordManager.this.mHandler != null) {
                        io.rong.imkit.manager.AudioRecordManager.this.mHandler.postDelayed(new Runnable() {
                            public void run() {
                                AudioStateMessage message = AudioStateMessage.obtain();
                                message.what = 9;
                                message.obj = !checked;
                                io.rong.imkit.manager.AudioRecordManager.this.sendMessage(message);
                            }
                        }, 500L);
                        io.rong.imkit.manager.AudioRecordManager.this.mCurAudioState = io.rong.imkit.manager.AudioRecordManager.this.sendingState;
                    } else {
                        io.rong.imkit.manager.AudioRecordManager.this.stopRec();
                        if (!checked && activityFinished) {
                            io.rong.imkit.manager.AudioRecordManager.this.sendAudioFile();
                        }

                        io.rong.imkit.manager.AudioRecordManager.this.destroyView();
                        io.rong.imkit.manager.AudioRecordManager.this.mCurAudioState = io.rong.imkit.manager.AudioRecordManager.this.idleState;
                    }
                    break;
                case 6:
                    io.rong.imkit.manager.AudioRecordManager.this.stopRec();
                    io.rong.imkit.manager.AudioRecordManager.this.destroyView();
                    io.rong.imkit.manager.AudioRecordManager.this.deleteAudioFile();
                    io.rong.imkit.manager.AudioRecordManager.this.mCurAudioState = io.rong.imkit.manager.AudioRecordManager.this.idleState;
                    io.rong.imkit.manager.AudioRecordManager.this.idleState.enter();
                    break;
                case 7:
                    int counter = (Integer) msg.obj;
                    io.rong.imkit.manager.AudioRecordManager.this.setTimeoutView(counter);
                    io.rong.imkit.manager.AudioRecordManager.this.mCurAudioState = io.rong.imkit.manager.AudioRecordManager.this.timerState;
                    if (counter >= 0) {
                        Message message = Message.obtain();
                        message.what = 8;
                        message.obj = counter - 1;
                        io.rong.imkit.manager.AudioRecordManager.this.mHandler.sendMessageDelayed(message, 1000L);
                    } else {
                        io.rong.imkit.manager.AudioRecordManager.this.mHandler.postDelayed(new Runnable() {
                            public void run() {
                                io.rong.imkit.manager.AudioRecordManager.this.stopRec();
                                io.rong.imkit.manager.AudioRecordManager.this.sendAudioFile();
                                io.rong.imkit.manager.AudioRecordManager.this.destroyView();
                            }
                        }, 500L);
                        io.rong.imkit.manager.AudioRecordManager.this.mCurAudioState = io.rong.imkit.manager.AudioRecordManager.this.idleState;
                    }
            }

        }
    }

    class IdleState extends IAudioState {
        public IdleState() {
            RLog.d("AudioRecordManager", "IdleState");
        }

        void enter() {
            super.enter();
            if (io.rong.imkit.manager.AudioRecordManager.this.mHandler != null) {
                io.rong.imkit.manager.AudioRecordManager.this.mHandler.removeMessages(7);
                io.rong.imkit.manager.AudioRecordManager.this.mHandler.removeMessages(8);
                io.rong.imkit.manager.AudioRecordManager.this.mHandler.removeMessages(2);
            }

        }

        void handleMessage(AudioStateMessage msg) {
            RLog.d("AudioRecordManager", "IdleState handleMessage : " + msg.what);
            switch (msg.what) {
                case 1:
                    io.rong.imkit.manager.AudioRecordManager.this.initView(io.rong.imkit.manager.AudioRecordManager.this.mRootView);
                    io.rong.imkit.manager.AudioRecordManager.this.setRecordingView();
                    io.rong.imkit.manager.AudioRecordManager.this.startRec();
                    io.rong.imkit.manager.AudioRecordManager.this.smStartRecTime = SystemClock.elapsedRealtime();
                    io.rong.imkit.manager.AudioRecordManager.this.mCurAudioState = io.rong.imkit.manager.AudioRecordManager.this.recordState;
                    io.rong.imkit.manager.AudioRecordManager.this.sendEmptyMessage(2);
                default:
            }
        }
    }

    static class SingletonHolder {
        static io.rong.imkit.manager.AudioRecordManager sInstance = new io.rong.imkit.manager.AudioRecordManager();

        SingletonHolder() {
        }
    }
}
