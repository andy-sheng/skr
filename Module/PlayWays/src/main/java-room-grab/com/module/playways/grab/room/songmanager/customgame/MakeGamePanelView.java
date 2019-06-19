package com.module.playways.grab.room.songmanager.customgame;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.countdown.CircleCountDownView;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class MakeGamePanelView extends RelativeLayout {

    public final static String TAG = "MakeGamePanelView";

    static final int STATUS_IDLE = 1;
    static final int STATUS_RECORDING = 2;
    static final int STATUS_RECORD_OK = 3;

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

        mPlayBtn.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mStatus == STATUS_IDLE) {
                            // 开始录音
                            mStatus = STATUS_RECORDING;
                            mRecordingTipsTv.setText("录音中...");
                            mCircleCountDownView.setVisibility(VISIBLE);
                            mCircleCountDownView.go(0, 15 * 1000);
                            mBeginRecordingTs = System.currentTimeMillis();
                            startCountDown();
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        if (mStatus == STATUS_RECORDING) {
                            cancelCountDown();
                            // 停止录音
                            mCircleCountDownView.setVisibility(GONE);
                            if (System.currentTimeMillis() - mBeginRecordingTs < 5 * 1000) {
                                mStatus = STATUS_IDLE;
                                U.getToastUtil().showShort("至少录5秒钟哦");
                            } else {
                                // 录制成功
                                mStatus = STATUS_RECORD_OK;
                                changeToRecordOk();
                            }
                        }
                        break;
                }
                return true;
            }
        });

        mTime60Btn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                mPlayTimeExpect = 60;
                mTime60Btn.setEnabled(false);
                mTime90Btn.setEnabled(true);
                mTime120Btn.setEnabled(true);
            }
        });

        mTime90Btn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                mPlayTimeExpect = 90;
                mTime60Btn.setEnabled(true);
                mTime90Btn.setEnabled(false);
                mTime120Btn.setEnabled(true);
            }
        });

        mTime120Btn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                mPlayTimeExpect = 120;
                mTime60Btn.setEnabled(true);
                mTime90Btn.setEnabled(true);
                mTime120Btn.setEnabled(false);
            }
        });

        mReRecordBtn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                mStatus = STATUS_IDLE;
                changeToRecordBegin();
            }
        });
        mSubmitBtn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // 调研录音
                // 上传提交
                HashMap<String, Object> map = new HashMap<>();
                map.put("roomID", mRoomID);
                map.put("standIntro", "http://www.baidu.com");
                map.put("standIntroEndT", 20);
                map.put("totalMs", mPlayTimeExpect * 1000);

                RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

                GrabRoomServerApi grabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
                if (grabRoomServerApi != null) {
                    ApiMethods.subscribe(grabRoomServerApi.addCustomGame(body), new ApiObserver<ApiResult>() {
                        @Override
                        public void process(ApiResult obj) {
                            if(obj.getErrno()==0){
                                 U.getToastUtil().showShort("添加成功");
                                 // 刷新ui
                                if (mDialogPlus != null) {
                                    mDialogPlus.dismiss();
                                }
                            }
                        }
                    });
                }
            }
        });
        changeToRecordBegin();
    }

    HandlerTaskTimer mHandlerTaskTimer;

    private void cancelCountDown() {
        if (mHandlerTaskTimer != null) {
            mHandlerTaskTimer.dispose();
        }
        mCountDownTv.setText("15s");
    }

    private void startCountDown() {
        cancelCountDown();
        mHandlerTaskTimer = HandlerTaskTimer.newBuilder().interval(1000)
                .take(15)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        String t = (15 - integer) + "s";
                        mCountDownTv.setText(t);
                    }
                });
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
        mRecordingTipsTv.setText("安装录音");
        mTitleTv.setText("一句话描述游戏规则");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelCountDown();
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
}
