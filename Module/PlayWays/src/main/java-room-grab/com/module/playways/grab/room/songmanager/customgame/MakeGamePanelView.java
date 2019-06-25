package com.module.playways.grab.room.songmanager.customgame;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.log.MyLog;
import com.common.player.IPlayer;
import com.common.player.MyMediaPlayer;
import com.common.player.VideoPlayerAdapter;
import com.common.recorder.MyMediaRecorder;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.upload.UploadCallback;
import com.common.upload.UploadParams;
import com.common.upload.UploadTask;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.countdown.CircleCountDownView;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.songmanager.event.AddCustomGameEvent;
import com.module.playways.grab.room.songmanager.event.BeginRecordCustomGameEvent;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class MakeGamePanelView extends RelativeLayout {

    public final static String TAG = "MakeGamePanelView";

    static final int STATUS_IDLE = 1;
    static final int STATUS_RECORDING = 2;
    static final int STATUS_RECORD_OK = 3;
    static final int STATUS_RECORD_PLAYING = 4;
    ProgressBar mSubmitProgressBar;
    TextView mTitleTv;
    TextView mDescTv;
    TextView mCountDownTv;
    ExImageView mPlayBtn;
    CircleCountDownView mCircleCountDownView;
    TextView mRecordingTipsTv;
    ExTextView mReRecordBtn;
    ExTextView mSubmitBtn;
    ExTextView mTime60Btn;
    ExTextView mTime90Btn;
    ExTextView mTime120Btn;

    int mStatus = STATUS_IDLE;
    long mBeginRecordingTs = 0;
    int mPlayTimeExpect = 60;
    int mRoomID;

    MyMediaRecorder mMyMediaRecorder;

    String mUploadUrl;

    String mMakeAudioFilePath = new File(U.getAppInfoUtils().getMainDir(), "make_game_intro.aac").getPath();

    UploadTask mUploadTask;

    boolean mUploading = false;

    IPlayer mMediaPlayer;

    boolean mDownTouching = false;

    Handler mUiHandler = new Handler();

    public MakeGamePanelView(Context context) {
        super(context);
        init();
    }

    public MakeGamePanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void init() {
        inflate(getContext(), R.layout.make_game_panel_view_layout, this);
        mTitleTv = (TextView) this.findViewById(R.id.title_tv);
        mDescTv = (TextView) this.findViewById(R.id.desc_tv);
        mCountDownTv = (TextView) this.findViewById(R.id.count_down_tv);
        mPlayBtn = (ExImageView) this.findViewById(R.id.play_btn);
        mCircleCountDownView = (CircleCountDownView) this.findViewById(R.id.circle_count_down_view);
        mRecordingTipsTv = (TextView) this.findViewById(R.id.recording_tips_tv);
        mReRecordBtn = (ExTextView) this.findViewById(R.id.re_record_btn);
        mSubmitBtn = (ExTextView) this.findViewById(R.id.submit_btn);
        mTime60Btn = (ExTextView) this.findViewById(R.id.time60_btn);
        mTime90Btn = (ExTextView) this.findViewById(R.id.time90_btn);
        mTime120Btn = (ExTextView) this.findViewById(R.id.time120_btn);
        mSubmitProgressBar = (ProgressBar) this.findViewById(R.id.submit_progress_bar);

        mPlayBtn.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        mDownTouching =true;
                        if (mStatus == STATUS_IDLE) {
                            // 开始录音
                            mStatus = STATUS_RECORDING;
                            mRecordingTipsTv.setText("录音中...");
                            mCircleCountDownView.setVisibility(VISIBLE);
                            mCircleCountDownView.go(0, 15 * 1000);
                            mBeginRecordingTs = System.currentTimeMillis();
                            startCountDown();
                            EventBus.getDefault().post(new BeginRecordCustomGameEvent(true));
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        mDownTouching = false;
                        if (mStatus == STATUS_RECORDING) {
                            cancelCountDown();
                            // 停止录音
                            mCircleCountDownView.setVisibility(GONE);
                            if (System.currentTimeMillis() - mBeginRecordingTs < 5 * 1000) {
                                mStatus = STATUS_IDLE;
                                U.getToastUtil().showShort("至少录5秒钟哦");
                                changeToRecordBegin();
                            } else {
                                // 录制成功
                                mStatus = STATUS_RECORD_OK;
                                changeToRecordOk();
                            }
                            EventBus.getDefault().post(new BeginRecordCustomGameEvent(false));
                        } else if (mStatus == STATUS_RECORD_OK) {
                            mStatus = STATUS_RECORD_PLAYING;
                            mPlayBtn.setImageResource(R.drawable.make_game_zanting);
                            playRecorderRes(true);
                            mRecordingTipsTv.setText("暂停");
                        } else if (mStatus == STATUS_RECORD_PLAYING) {
                            mStatus = STATUS_RECORD_OK;
                            mPlayBtn.setImageResource(R.drawable.make_game_bofang);
                            playRecorderRes(false);
                            mRecordingTipsTv.setText("播放");
                        }
                        break;
                }
                return true;
            }
        });

        mTime60Btn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if(mDownTouching){
                    return;
                }
                mPlayTimeExpect = 60;
                mTime60Btn.setEnabled(false);
                mTime90Btn.setEnabled(true);
                mTime120Btn.setEnabled(true);
            }
        });

        mTime90Btn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if(mDownTouching){
                    return;
                }
                mPlayTimeExpect = 90;
                mTime60Btn.setEnabled(true);
                mTime90Btn.setEnabled(false);
                mTime120Btn.setEnabled(true);
            }
        });

        mTime120Btn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if(mDownTouching){
                    return;
                }
                mPlayTimeExpect = 120;
                mTime60Btn.setEnabled(true);
                mTime90Btn.setEnabled(true);
                mTime120Btn.setEnabled(false);
            }
        });

        mReRecordBtn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if(mDownTouching){
                    return;
                }
                mStatus = STATUS_IDLE;
                changeToRecordBegin();
            }
        });
        mSubmitBtn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if(mDownTouching){
                    return;
                }
                // 调研录音
                uploadAudioRes();
            }
        });
        changeToRecordBegin();
    }

    private void playRecorderRes(boolean play) {
        if (play) {
            if (mMediaPlayer == null) {
                mMediaPlayer = new MyMediaPlayer();
                mMediaPlayer.setCallback(new VideoPlayerAdapter.PlayerCallbackAdapter() {
                    @Override
                    public void onCompletion() {
                        super.onCompletion();
                        mMediaPlayer.reset();
                        mStatus = STATUS_RECORD_OK;
                        mPlayBtn.setImageResource(R.drawable.make_game_bofang);
                        EventBus.getDefault().post(new BeginRecordCustomGameEvent(false));
                    }
                });
            }
            mMediaPlayer.startPlay(mMakeAudioFilePath);
            EventBus.getDefault().post(new BeginRecordCustomGameEvent(true));
        } else {
            EventBus.getDefault().post(new BeginRecordCustomGameEvent(false));
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
            }
        }
    }

    /**
     * 上传音频资源
     */
    private void uploadAudioRes() {
        if (TextUtils.isEmpty(mUploadUrl)) {
            if (!mUploading) {
                mSubmitProgressBar.setVisibility(VISIBLE);
                mUploading = true;
                mUploadTask = UploadParams.newBuilder(mMakeAudioFilePath)
                        .setFileType(UploadParams.FileType.customGame)
                        .startUploadAsync(new UploadCallback() {
                            @Override
                            public void onProgressNotInUiThread(long currentSize, long totalSize) {

                            }

                            @Override
                            public void onSuccessNotInUiThread(String url) {
                                mUploadUrl = url;
                                sendToServer();
                                mUploading = false;
                                mUiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mSubmitProgressBar.setVisibility(GONE);
                                    }
                                });

                            }

                            @Override
                            public void onFailureNotInUiThread(String msg) {
                                mUploading = false;
                                U.getToastUtil().showShort("上传失败");
                                mUiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mSubmitProgressBar.setVisibility(GONE);
                                    }
                                });
                            }
                        });
            }
        } else {
            sendToServer();
        }
    }

    /**
     * 传到服务器
     */
    public void sendToServer() {
        // 上传提交
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", mRoomID);
        map.put("standIntro", mUploadUrl);
        map.put("standIntroEndT", mMyMediaRecorder.getDuration());
        map.put("totalMs", mPlayTimeExpect * 1000);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        GrabRoomServerApi grabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
        if (grabRoomServerApi != null) {
            ApiMethods.subscribe(grabRoomServerApi.addCustomGame(body), new ApiObserver<ApiResult>() {
                @Override
                public void process(ApiResult obj) {
                    mSubmitProgressBar.setVisibility(GONE);
                    if (obj.getErrno() == 0) {
                        U.getToastUtil().showShort("添加成功");
                        EventBus.getDefault().post(new AddCustomGameEvent());
                        // 刷新ui
                        if (mDialogPlus != null) {
                            mDialogPlus.dismiss();
                        }
                    }
                }

                @Override
                public void onNetworkError(ErrorType errorType) {
                    super.onNetworkError(errorType);
                    mSubmitProgressBar.setVisibility(GONE);
                }
            }, new ApiMethods.RequestControl("addCustomGame", ApiMethods.ControlType.CancelThis));
        }
    }

    HandlerTaskTimer mHandlerTaskTimer;

    private void cancelCountDown() {
        if (mHandlerTaskTimer != null) {
            mHandlerTaskTimer.dispose();
        }
        if (mMyMediaRecorder != null) {
            mMyMediaRecorder.stop();
        }
        mCountDownTv.setText("15s");
    }

    private void startCountDown() {
        cancelCountDown();
        mHandlerTaskTimer = HandlerTaskTimer.newBuilder().interval(1000)
                .take(16)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        String t = (16 - integer) + "s";
                        mCountDownTv.setText(t);
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        if (mMyMediaRecorder != null) {
                            mMyMediaRecorder.stop();
                        }
                        changeToRecordOk();
                        EventBus.getDefault().post(new BeginRecordCustomGameEvent(false));
                    }
                });
        if (mMyMediaRecorder == null) {
            mMyMediaRecorder = MyMediaRecorder.newBuilder().build();
        }
        mMyMediaRecorder.start(mMakeAudioFilePath);
    }

    private void changeToRecordOk() {
        mCountDownTv.setVisibility(GONE);
        mReRecordBtn.setVisibility(VISIBLE);
        mSubmitBtn.setVisibility(VISIBLE);
        mDescTv.setVisibility(GONE);
        mTime60Btn.setVisibility(VISIBLE);
        mTime90Btn.setVisibility(VISIBLE);
        mTime120Btn.setVisibility(VISIBLE);
        if (mPlayTimeExpect == 60) {
            mTime60Btn.setEnabled(false);
            mTime90Btn.setEnabled(true);
            mTime120Btn.setEnabled(true);
        }
        mPlayBtn.setImageResource(R.drawable.make_game_bofang);
        mRecordingTipsTv.setText("播放");
        mTitleTv.setText("选择表演时间");
        mSubmitProgressBar.setVisibility(GONE);
    }

    private void changeToRecordBegin() {
        mCountDownTv.setVisibility(VISIBLE);
        mReRecordBtn.setVisibility(GONE);
        mSubmitBtn.setVisibility(GONE);
        mDescTv.setVisibility(VISIBLE);
        mTime60Btn.setVisibility(GONE);
        mTime90Btn.setVisibility(GONE);
        mTime120Btn.setVisibility(GONE);
        mPlayBtn.setImageResource(R.drawable.make_game_luyin);
        mRecordingTipsTv.setText("按住录音");
        mTitleTv.setText("一句话描述游戏规则");
        mSubmitProgressBar.setVisibility(GONE);
    }

    @Override
    protected void onDetachedFromWindow() {
        MyLog.d(TAG,"onDetachedFromWindow" );
        super.onDetachedFromWindow();
        if (mHandlerTaskTimer != null) {
            mHandlerTaskTimer.dispose();
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        if (mMyMediaRecorder != null) {
            mMyMediaRecorder.destroy();
        }
        if (mUploadTask != null) {
            mUploadTask.cancel();
        }
        EventBus.getDefault().post(new BeginRecordCustomGameEvent(false));
    }

    DialogPlus mDialogPlus;

    public void showByDialog(int roomId) {
        this.mRoomID = roomId;
        if (mDialogPlus != null) {
            mDialogPlus.dismiss(false);
        }
        mDialogPlus = DialogPlus.newDialog(getContext())
                .setContentHolder(new ViewHolder(this))
                .setGravity(Gravity.BOTTOM)
                .setCancelable(true)
                .setContentBackgroundResource(com.common.core.R.color.transparent)
                .setOverlayBackgroundResource(com.common.core.R.color.black_trans_80)
                .setExpanded(false)
                .create();
        mDialogPlus.show();
    }

    public void dismiss() {
        if (mDialogPlus != null) {
            mDialogPlus.dismiss();
        }
    }
}
