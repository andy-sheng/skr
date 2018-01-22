package com.wali.live.watchsdk.contest;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.dialog.MyAlertDialog;
import com.base.event.KeyboardEvent;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.event.MiLinkEvent;
import com.mi.live.data.push.model.contest.ContestAnswerMsgExt;
import com.mi.live.data.push.model.contest.ContestQuestionMsgExt;
import com.mi.live.data.push.model.contest.LastQuestionInfoModel;
import com.mi.live.data.push.model.contest.QuestionInfoModel;
import com.mi.live.data.push.presenter.RoomMessagePresenter;
import com.mi.live.data.query.mapper.RoomDataMapper;
import com.mi.live.data.query.model.EnterRoomInfo;
import com.mi.live.data.repository.RoomMessageRepository;
import com.mi.live.data.repository.datasource.RoomMessageStore;
import com.thornbirds.component.IEventObserver;
import com.thornbirds.component.IParams;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.component.BaseSdkController;
import com.wali.live.component.EmptyController;
import com.wali.live.proto.LiveProto;
import com.wali.live.receiver.NetworkReceiver;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.presenter.LiveCommentPresenter;
import com.wali.live.watchsdk.component.view.LiveCommentView;
import com.wali.live.watchsdk.contest.cache.ContestCurrentCache;
import com.wali.live.watchsdk.contest.cache.ContestGlobalCache;
import com.wali.live.watchsdk.contest.model.AwardUser;
import com.wali.live.watchsdk.contest.model.ContestNoticeModel;
import com.wali.live.watchsdk.contest.presenter.ContestInvitePresenter;
import com.wali.live.watchsdk.contest.presenter.ContestMessagePresenter;
import com.wali.live.watchsdk.contest.presenter.ContestPreparePresenter;
import com.wali.live.watchsdk.contest.presenter.ContestVideoPlayerPresenter;
import com.wali.live.watchsdk.contest.presenter.ContestWatchPresenter;
import com.wali.live.watchsdk.contest.presenter.IContestInviteView;
import com.wali.live.watchsdk.contest.presenter.IContestPrepareView;
import com.wali.live.watchsdk.contest.presenter.IContestWatchView;
import com.wali.live.watchsdk.contest.view.AnswerView;
import com.wali.live.watchsdk.contest.view.ContestFailView;
import com.wali.live.watchsdk.contest.view.ContestInputView;
import com.wali.live.watchsdk.contest.view.ContestLateView;
import com.wali.live.watchsdk.contest.view.ContestNoWinView;
import com.wali.live.watchsdk.contest.view.ContestRevivalRuleView;
import com.wali.live.watchsdk.contest.view.ContestSuccessView;
import com.wali.live.watchsdk.contest.view.ContestWinRevivalRuleView;
import com.wali.live.watchsdk.contest.view.QuestionView;
import com.wali.live.watchsdk.contest.winner.WinerListDialog;
import com.wali.live.watchsdk.eventbus.EventClass;
import com.wali.live.watchsdk.watch.presenter.push.RoomStatusPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomSystemMsgPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomTextMsgPresenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by lan on 2018/1/10.
 */
public class ContestWatchActivity extends ContestComponentActivity implements View.OnClickListener,
        IContestWatchView, IContestPrepareView, IContestInviteView, IEventObserver {
    private static final String EXTRA_ZUID = "extra_zuid";
    private static final String EXTRA_ROOM_ID = "extra_room_id";
    private static final String EXTRA_VIDEO_URL = "extra_video_url";

    // 域名解析、重连相关
    public static final int MSG_RELOAD_VIDEO = 100;             // onInfo开始buffer时，reload数据的标记。
    public static final int MAG_SHOW_SUCCESS_VIEW = 101;

    public static final int PLAYER_KADUN_RELOAD_TIME = 5000;    // 毫秒

    private ViewGroup mMainContainer;

    private View mTouchView;

    private TextView mViewerCntTv;
    private TextView mRevivalCntTv;

    private ImageView mCloseBtn;
    private RelativeLayout mShareContainer;
    private TextView mBarrageBtn;
    private ContestInputView mInputView;
    private ContestRevivalRuleView mRevivalRuleView;
    private ContestWinRevivalRuleView mContestWinRevivalRuleView;
    private ContestNoWinView mNoWinView;

    private View mPlaceholderView;

    private ContestVideoPlayerPresenter mPlayerPresenter;
    private TextureView mPlayerView;
    private final BaseSdkController mController = new BaseSdkController() {
        @Override
        protected String getTAG() {
            return "ContestWatchActivity";
        }
    };

    private QuestionView mQuestionView;
    private AnswerView mAnswerView;

    private ContestSuccessView mContestSuccessView;
    private ContestFailView mContestFailView;
    private ContestLateView mContestLateView;

    private LiveCommentView mCommentView;
    private LiveCommentPresenter mLiveCommentPresenter;

    private Handler mHandler = new MyUIHandler(this);

    //拉取enter live 信息失败后，是否需要再拉取一次
    private boolean mPullEnterLiveInfo = true;
    private boolean mPullRoomInfo = true;
    private boolean mEnterLiveSuccess;
    private long mEnterLiveTime;

    private ContestMessagePresenter mContestMessagePresenter;
    private ContestWatchPresenter mContestWatchPresenter;

    private RoomTextMsgPresenter mRoomTextMsgPresenter;
    private RoomSystemMsgPresenter mRoomSytemMsgPresenter;
    private RoomStatusPresenter mRoomStatusPresenter;

    private LiveRoomChatMsgManager mRoomChatMsgManager;
    private RoomMessagePresenter mPullRoomMessagePresenter;
    private final LiveCommentPresenter mCommentPresenter = new LiveCommentPresenter(new EmptyController());

    private ContestPreparePresenter mContestPreparePresenter;
    private ContestInvitePresenter mContestInvitePresenter;

    private String mContestId;//场次Id
    private long mPerQuestionTotalAnswer;//每题总答题人数

    private LastQuestionInfoModel mLastQuestionInfoModel;
    private MyAlertDialog mQuitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isMIUIV6()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
        setContentView(R.layout.contest_watch_layout);

        initData();
        initHelper();

        initView();
        initPresenter();
    }

    private void initData() {
        Intent data = getIntent();
        if (data == null) {
            MyLog.e(TAG, "intent is null");
            finish();
            return;
        }
        long zuid = data.getLongExtra(EXTRA_ZUID, 0l);
        String roomId = data.getStringExtra(EXTRA_ROOM_ID);
        String videoUrl = data.getStringExtra(EXTRA_VIDEO_URL);

        mMyRoomData.setUid(zuid);
        mMyRoomData.setRoomId(roomId);
        mMyRoomData.setVideoUrl(videoUrl);
    }

    private void initHelper() {
        mRoomChatMsgManager = new LiveRoomChatMsgManager(LiveRoomChatMsgManager.DEFAULT_MAX_SIZE);
    }

    private void initView() {
        initMainView();
        initOtherView();
        initPlayerView();
        initQuestionView();
    }

    private void initMainView() {
        mMainContainer = $(R.id.main_act_container);

        mTouchView = $(R.id.touch_view);
        mTouchView.setOnClickListener(this);

        mShareContainer = $(R.id.share_container);
        mShareContainer.setOnClickListener(this);

        mBarrageBtn = $(R.id.barrage_btn);
        mBarrageBtn.setOnClickListener(this);

        mInputView = $(R.id.input_view);
        mInputView.setInputListener(new ContestInputView.InputListener() {
            @Override
            public void sendBarrage(String msg) {
                mRoomChatMsgManager.sendTextBarrageMessageAsync(msg, mMyRoomData.getRoomId(), mMyRoomData.getUid(), null);
            }
        });

        mRevivalRuleView = $(R.id.revival_rule_view);
        mContestWinRevivalRuleView = $(R.id.revival_win_rule_view);
        mNoWinView = $(R.id.contest_no_win_view);

        mPlaceholderView = $(R.id.placeholder_view);

        mCommentView = $(R.id.comment_rv);
        mCommentView.setSoundEffectsEnabled(false);
        mCommentView.setToken(mRoomChatMsgManager.toString());
        mCommentView.onOrientation(false);
        mCommentView.setPresenter(mCommentPresenter);
        mCommentPresenter.setView(mCommentView.getViewProxy());
        mCommentPresenter.startPresenter();

        mCloseBtn = $(R.id.close_btn);
        mCloseBtn.setOnClickListener(this);
    }

    private void initOtherView() {
        mViewerCntTv = $(R.id.view_tv);
        mRevivalCntTv = $(R.id.revival_cnt_tv);
        mRevivalCntTv.setOnClickListener(this);
        updateRevivalCount();
    }

    private void updateRevivalCount() {
        mRevivalCntTv.setText(getString(R.string.contest_prepare_revival_card) + "x" + ContestGlobalCache.getRevivalNum());
    }

    private void initPlayerView() {
        mPlayerView = $(R.id.video_view);
        mPlayerPresenter = new ContestVideoPlayerPresenter(new BaseSdkController() {
            @Override
            protected String getTAG() {
                return "ContestWatchActivity";
            }
        }, true);
        if (!TextUtils.isEmpty(mMyRoomData.getVideoUrl())) {
            MyLog.d(TAG, "setVideoUrl");
            play(mMyRoomData.getVideoUrl());
        }
        mPlayerPresenter.setView(mPlayerView);
    }

    private void initQuestionView() {
        mQuestionView = $(R.id.question_view);
        mQuestionView.setVisibility(View.GONE);
        mQuestionView.initRoomData(mMyRoomData.getUid(), mMyRoomData.getRoomId());

        mAnswerView = $(R.id.answer_view);
        mAnswerView.setVisibility(View.GONE);

//        TextView showQuestion = $(R.id.test_btn);
//        showQuestion.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                EventBus.getDefault().post(new EventClass.ShowContestView(EventClass.ShowContestView.TYPE_LATE_VIEW, EventClass.ShowContestView.ACTION_SHOW));
//            }
//        });
//
//        TextView showAnswer = $(R.id.test_btn_1);
//        showAnswer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                EventBus.getDefault().post(new EventClass.ShowContestView(EventClass.ShowContestView.TYPE_FAIL_VIEW, EventClass.ShowContestView.ACTION_SHOW));
//            }
//        });
//
//        TextView showAnswer1 = $(R.id.test_btn_2);
//        showAnswer1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                EventBus.getDefault().post(new EventClass.ShowContestView(EventClass.ShowContestView.TYPE_SUCCESS_VIEW, EventClass.ShowContestView.ACTION_SHOW));
//            }
//        });
    }

    private void showContestSuccessView(LastQuestionInfoModel lastQuestionInfoModel) {
        if (mContestSuccessView == null) {
            mContestSuccessView = $(R.id.contest_success_view);
        }
        mContestSuccessView.setVisibility(View.VISIBLE);
        mContestSuccessView.bindData(lastQuestionInfoModel);
    }

    private void hideContestSuccessView() {
        mContestSuccessView.setVisibility(View.GONE);
    }

    private void showContestFailView() {
        if (mContestFailView == null) {
            mContestFailView = $(R.id.contest_fail_view);
        }
        mContestFailView.setVisibility(View.VISIBLE);
    }

    private void hideContestFailView() {
        mContestFailView.setVisibility(View.GONE);
    }

    private void showContestLateView() {
        if (mContestLateView == null) {
            mContestLateView = $(R.id.contest_late_view);
        }
        mContestLateView.setVisibility(View.VISIBLE);
    }

    private void hideContestLateView() {
        mContestLateView.setVisibility(View.GONE);
    }

    private void initPresenter() {
        mContestMessagePresenter = new ContestMessagePresenter(mPlayerPresenter);
        addPushProcessor(mContestMessagePresenter);

        mContestMessagePresenter.setCallBack(new ContestMessagePresenter.IContestCallBack() {
            @Override
            public void onQuestion(final ContestQuestionMsgExt msgExt) {
                MyLog.w(TAG, "showContestView Question = " + msgExt.toString());
//                if (!(mContestQuestionView.getVisibility() == View.VISIBLE)) {
//                    mContestQuestionView.bindContestQuestionData(msgExt);
//                }
//                if (!(mQuestionView.getVisibility() == View.VISIBLE)) {
                mQuestionView.bindContestQuestionData(msgExt);
//                }
            }

            @Override
            public void onAnswer(final ContestAnswerMsgExt msgExt) {
                MyLog.w(TAG, "showContestView Answer = " + msgExt.toString());
//                if (!(mAnswerView.getVisibility() == View.VISIBLE)) {
                mRevivalCntTv.setText(getString(R.string.contest_prepare_revival_card) + "x" + ContestGlobalCache.getRevivalNum());
                if (mQuestionView.getVisibility() == View.VISIBLE) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mAnswerView.bindContestAnswerData(msgExt);
                            QuestionInfoModel model = msgExt.getQuestionInfoModel();
                            boolean isLastQuestion = model.isLastQuestion();
                            mContestId = msgExt.getContestId();
                            mPerQuestionTotalAnswer = msgExt.getAnswerNums();

                            if (isLastQuestion) {
                                mLastQuestionInfoModel = msgExt.getLastQuestionInfoModel();
                            }
                        }
                    }, 10 * 1000);
                } else {
                    mAnswerView.bindContestAnswerData(msgExt);
                    QuestionInfoModel model = msgExt.getQuestionInfoModel();
                    boolean isLastQuestion = model.isLastQuestion();
                    mContestId = msgExt.getContestId();
                    mPerQuestionTotalAnswer = msgExt.getAnswerNums();

                    if (isLastQuestion) {
                        mLastQuestionInfoModel = msgExt.getLastQuestionInfoModel();
                    }
                }
//                }
            }
        });

        mRoomTextMsgPresenter = new RoomTextMsgPresenter(mRoomChatMsgManager);
        addPushProcessor(mRoomTextMsgPresenter);
        mRoomSytemMsgPresenter = new RoomSystemMsgPresenter(mRoomChatMsgManager);
        addPushProcessor(mRoomSytemMsgPresenter);
        mRoomStatusPresenter = new RoomStatusPresenter(mRoomChatMsgManager);
        addPushProcessor(mRoomStatusPresenter);
        mContestWatchPresenter = new ContestWatchPresenter(this);

        mContestPreparePresenter = new ContestPreparePresenter(this);
        mContestInvitePresenter = new ContestInvitePresenter(this);
        enterLive();
    }

    private void enterLive() {
        if (isFinishing()) {
            return;
        }
        mContestWatchPresenter.enterLiveToServer(mMyRoomData.getUid(), mMyRoomData.getRoomId());
    }

    private void queryRoomInfo() {
        if (isFinishing()) {
            return;
        }
        mContestWatchPresenter.pullRoomInfo(mMyRoomData.getUid(), mMyRoomData.getRoomId());
    }

    private void leaveLive() {
        if (mContestWatchPresenter != null) {
            mContestWatchPresenter.leaveLiveToServer(mMyRoomData.getUid(), mMyRoomData.getRoomId());
        }
    }

    private void play(String videoUrl) {
        if (!TextUtils.isEmpty(videoUrl)) {
            mPlayerPresenter.setOriginalStreamUrl(videoUrl);
            mPlayerPresenter.startVideo();
        }
    }

    private void release() {
        mPlayerPresenter.releaseVideo();
        mCommentPresenter.stopPresenter();
        mCommentPresenter.destroy();
        mAnswerView.destroy();
        mQuestionView.destroy();

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mPullRoomMessagePresenter != null) {
            mPullRoomMessagePresenter.stopWork();
            mPullRoomMessagePresenter.destroy();
        }
        MiLinkClientAdapter.getsInstance().setGlobalPushFlag(false);
    }

    @Override
    public boolean isKeyboardResize() {
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        KeyboardUtils.hideKeyboard(this);
        leaveLive();
        ContestCurrentCache.getInstance().clearCache();
        release();
    }

    private void showRevivalRuleView() {
        MyLog.w(TAG, "showRevivalRuleView");
        mRevivalRuleView.show();
    }

    private void showWinRevivalView() {
        MyLog.w(TAG, "showWinRevivalView");
        if (mLastQuestionInfoModel != null) {
            mContestWinRevivalRuleView.bindData(mLastQuestionInfoModel);
            mContestWinRevivalRuleView.show();
        } else {
            showRevivalRuleView();
        }
    }

    private void pullWinnerList() {
        MyLog.w(TAG, "pullWinnerList");
        mContestWatchPresenter.getAwardList(mContestId, mMyRoomData.getRoomId());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.close_btn) {
            showQuitDialog();
        } else if (id == R.id.revival_cnt_tv) {
            showRevivalRuleView();
        } else if (id == R.id.barrage_btn) {
            enterInputMode();
        } else if (id == R.id.touch_view) {
            if (mInputView.isInputMode()) {
                KeyboardUtils.hideKeyboard(this);
                enterBottomMode();
            }
        }
    }

    private void enterInputMode() {
        KeyboardUtils.showKeyboard(this);
        mInputView.showInputView();

        mShareContainer.setVisibility(View.GONE);
        mBarrageBtn.setVisibility(View.GONE);
    }

    private void enterBottomMode() {
        mInputView.hideInputView();
        mShareContainer.setVisibility(View.VISIBLE);
        mBarrageBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (mInputView.isInputMode()) {
            enterBottomMode();
            return;
        }
        if (mRevivalRuleView.isShown()) {
            mRevivalRuleView.hide();
            return;
        }
        if (mNoWinView.isShown()) {
            mNoWinView.hide();
            return;
        }

        if (ContestCurrentCache.getInstance().isContinue()) {
            showQuitDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void showQuitDialog() {
        if (mQuitDialog == null) {
            MyAlertDialog.Builder builder = new MyAlertDialog.Builder(this);
            builder.setTitle(R.string.contest_room_quit_title);
            builder.setMessage(getString(R.string.contest_room_quit_tip, ContestGlobalCache.getBonus()));
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mQuitDialog.dismiss();
                    KeyboardUtils.hideKeyboard(ContestWatchActivity.this);
                    finish();
                }
            });
            builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mQuitDialog.dismiss();
                }
            });
            builder.setAutoDismiss(true);
            mQuitDialog = builder.create();
        }
        mQuitDialog.show();
    }

    private void updateViewerTvByData() {
        mViewerCntTv.setText(getResources().getQuantityString(R.plurals.game_comment_number,
                mMyRoomData.getViewerCnt(), mMyRoomData.getViewerCnt()));
        mContestWatchPresenter.startTimerForViewers(mMyRoomData.getUid(), mMyRoomData.getRoomId());
    }

    private void currentContestEnd(String from) {
        MyLog.w(TAG, "currentContestEnd from=" + from);
        ToastUtils.showToast(R.string.contest_room_end_tip);
        ContestPrepareActivity.open(this);
        finish();
    }

    @Override
    public void processEnterLive(EnterRoomInfo enterRoomInfo) {
        // 进房间处理回调，先判断之前有没有流地址，没有的话，使用房间信息，重新播放一次
        boolean needPlayAgain = TextUtils.isEmpty(mMyRoomData.getVideoUrl());
        RoomDataMapper.fillRoomDataModelByEnterRoomInfo(mMyRoomData, enterRoomInfo);
        MyLog.e(TAG, "processEnterLive:" + enterRoomInfo.getRetCode());
        switch (enterRoomInfo.getRetCode()) {
            case ErrorCode.CODE_SUCCESS: {
                ContestGlobalCache.setRevivalNum(mMyRoomData.getRevivalNum());
                ContestCurrentCache.getInstance().setContinue(mMyRoomData.isAbleContest());

                if (mMyRoomData.isLate() || !mMyRoomData.isAbleContest()) {
                    ContestCurrentCache.getInstance().setWatchMode(true);
                } else {
                    ContestCurrentCache.getInstance().setWatchMode(false);
                }
                ContestCurrentCache.getInstance().setContinue(true);
                MyLog.w(TAG, "ENTER LIVE SUCCESS");
                MiLinkClientAdapter.getsInstance().setGlobalPushFlag(true);
                MyLog.e(TAG, "processEnterLive:" + enterRoomInfo.getRetCode() + " mVideoUrl=" + mMyRoomData.getVideoUrl());
                updateViewerTvByData();
                mEnterLiveSuccess = true;
                mEnterLiveTime = System.currentTimeMillis();
                if (mPullRoomMessagePresenter == null) {
                    mPullRoomMessagePresenter = new RoomMessagePresenter(mMyRoomData,
                            new RoomMessageRepository(new RoomMessageStore()), this);
                }
                mPullRoomMessagePresenter.startWork();

                if (mMyRoomData.isLate()) {
                    if (ContestCurrentCache.getInstance().isNeedShowLateView(mMyRoomData.getContestId())) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                EventBus.getDefault().post(new EventClass.ShowContestView(EventClass.ShowContestView.TYPE_LATE_VIEW, EventClass.ShowContestView.ACTION_SHOW));
                            }
                        });
                    }
                }

                if (ContestGlobalCache.needGetNoticeAgain(mMyRoomData.getRoomId())) {
                    mContestPreparePresenter.getContestNotice();
                }
                if (ContestGlobalCache.needGetCodeAgain()) {
                    mContestInvitePresenter.getInviteCode();
                }

                if (needPlayAgain) {
                    play(mMyRoomData.getVideoUrl());
                }
            }
            break;
            case ErrorCode.CODE_ROOM_NOT_EXIST:
                // 房间不存在有可能是房间隔离限制导致的,所以也要进行断流
                //TODO ticket 增加购票人数信息
                MyLog.w(TAG, "processEnterLive but room not exit");
                currentContestEnd("processEnterLive");
                break;
            case ErrorCode.CODE_SERVER_ERROR:
            case ErrorCode.CODE_DB_ERROR:
                MyLog.w(TAG, "processEnterLive DB error or server error");
                ToastUtils.showToast(R.string.live_network_error);
                break;
            case ErrorCode.CODE_ROOM_ZUID_SLEEP:
                MyLog.w(TAG, "processEnterLive but" + getString(R.string.pause_tip));
                break;
            case ErrorCode.CODE_SERVER_RESPONSE_ERROR_CODE_NO_PERMISSION_TO_ENTER_ROOM:
                MyLog.w(TAG, "processEnterLive but" + getString(R.string.have_been_kicked));
                break;
            case ErrorCode.CODE_ROOM_NOT_INVITE:
                MyLog.w(TAG, "processEnterLive but private room have no permission");
                break;
            case ErrorCode.CODE_ROOM_INVITE_CNT_OVER:
                MyLog.w(TAG, "processEnterLive but" + getString(R.string.personalroom_cnt_over));
                break;
            default:
                MyLog.w(TAG, "processEnterLive default");
                //第一次拉取enter live 信息失败后，再拉取一次
                if (mPullEnterLiveInfo) {
                    mPullEnterLiveInfo = false;
                    enterLive();
                } else {
                    ToastUtils.showToast(R.string.live_network_error);
                }
                break;
        }
    }

    @Override
    public void processRoomInfo(LiveProto.RoomInfoRsp rsp) {
        if (rsp != null) {
            switch (rsp.getRetCode()) {
                case ErrorCode.CODE_SUCCESS:
                    MyLog.w(TAG, " processRoomInfo success");
                    if (!TextUtils.isEmpty(rsp.getDownStreamUrl())) {
                        mMyRoomData.setVideoUrl(rsp.getDownStreamUrl());
                    }
                    if (!TextUtils.isEmpty(rsp.getShareUrl())) {
                        mMyRoomData.setShareUrl(rsp.getShareUrl());
                    }
                    if (rsp.hasContestInfo()) {
                        if (rsp.getContestInfo().hasAbleContest()) {
                            MyLog.w(TAG, "rsp.getContestInfo().hasAbleContest() = " + rsp.getContestInfo().getAbleContest());
                            mMyRoomData.setAbleContest(rsp.getContestInfo().getAbleContest());
                            ContestCurrentCache.getInstance().setContinue(mMyRoomData.isAbleContest());
                        }
                        if (rsp.getContestInfo().hasRevivalNum()) {
                            MyLog.w(TAG, "rsp.getContestInfo().getRevivalNum()=" + rsp.getContestInfo().getRevivalNum());
                            mMyRoomData.setRevivalNum(rsp.getContestInfo().getRevivalNum());
                            ContestGlobalCache.setRevivalNum(mMyRoomData.getRevivalNum());
                        }
                    }
                    break;
                case ErrorCode.CODE_ROOM_NOT_EXIST:
                    MyLog.w(TAG, " processRoomInfo room not exit");
                    break;
                default:
                    MyLog.w(TAG, "processRoomInfo default branch errorCode fail");
                    break;
            }
        }
    }

    @Override
    public void processViewerNum(int num) {
        MyLog.d(TAG, "processViewerNum num=" + num + " mMyRoomData.getViewerCnt()=" + mMyRoomData.getViewerCnt());
        if (num > 0) {
            mMyRoomData.setViewerCnt(num);
            mViewerCntTv.setText(getResources().getQuantityString(R.plurals.game_comment_number, num, num));
        }
    }

    @Override
    public void showAwardListView(List<AwardUser> userList) {
        MyLog.w(TAG, "showAwardListView");
        if (null != userList) {
            if (mLastQuestionInfoModel != null) {
                if (ContestCurrentCache.getInstance().isSuccess()) {
                    ContestCurrentCache.getInstance().setSuccess(false);
                    userList.add(new AwardUser(MyUserInfoManager.getInstance().getUuid(),
                            MyUserInfoManager.getInstance().getNickname(),
                            MyUserInfoManager.getInstance().getAvatar()));
                }
                new WinerListDialog(ContestWatchActivity.this, R.style.PKDialog)
                        .show(userList, mLastQuestionInfoModel.getMyBonus(), mLastQuestionInfoModel.getWinNum());
            } else {
                MyLog.w(TAG, "lastQuestionInfo is null");
            }
        }
    }

    @Override
    public void getInviteCodeSuccess(String code) {
        mRevivalRuleView.updateCode();
        updateRevivalCount();
    }

    @Override
    public void setInviteCodeSuccess(int revivalNum) {
        //nothing to do
    }

    @Override
    public void setInviteCodeFailure(int errCode) {
        //nothing to do
    }

    @Override
    public void setContestNotice(ContestNoticeModel model) {
        if (mQuitDialog != null) {
            mQuitDialog.setMessage(getString(R.string.contest_room_quit_tip, ContestGlobalCache.getBonus()));
        }
        updateRevivalCount();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(KeyboardEvent event) {
        MyLog.w(TAG, "KeyboardEvent eventType=" + event.eventType);
        switch (event.eventType) {
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_HIDDEN:
                if (mPlaceholderView != null) {
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mPlaceholderView.getLayoutParams();
                    layoutParams.height = 0;
                    mPlaceholderView.setLayoutParams(layoutParams);
                }
                break;
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE:
                if (mPlaceholderView != null) {
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mPlaceholderView.getLayoutParams();
                    layoutParams.height = (int) event.obj1;
                    mPlaceholderView.setLayoutParams(layoutParams);
                }
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MiLinkEvent.StatusLogined event) {
        if (event != null) {
            enterLive();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.ShowContestView event) {
        if (event != null) {
            if (event.type == EventClass.ShowContestView.TYPE_SUCCESS_VIEW) {
                MyLog.w(TAG, "onEvent successView");
                if (event.action == EventClass.ShowContestView.ACTION_SHOW) {
                    mHandler.sendEmptyMessageDelayed(MAG_SHOW_SUCCESS_VIEW, 2_000);
                } else {
                    //成功页面结束之后再放烟花
                    EventBus.getDefault().post(new EventClass.ShowContestView(EventClass.ShowContestView.TYPE_AWARD_VIEW, EventClass.ShowContestView.ACTION_SHOW));
                    hideContestSuccessView();
                }
            } else if (event.type == EventClass.ShowContestView.TYPE_FAIL_VIEW) {
                MyLog.w(TAG, "onEvent failView");
                if (event.action == EventClass.ShowContestView.ACTION_SHOW) {
                    showContestFailView();
                } else {
                    hideContestFailView();
                }
            } else if (event.type == EventClass.ShowContestView.TYPE_LATE_VIEW) {
                MyLog.w(TAG, "onEvent lateView");
                if (event.action == EventClass.ShowContestView.ACTION_SHOW) {
                    showContestLateView();
                } else {
                    hideContestLateView();
                }
            } else if (event.type == EventClass.ShowContestView.TYPE_AWARD_VIEW) {
                MyLog.w(TAG, "onEvent awardView");
                if (event.action == EventClass.ShowContestView.ACTION_SHOW) {
                    pullWinnerList();
                }
            } else if (event.type == EventClass.ShowContestView.TYPE_INVITE_SHARE_VIEW) {
                if (event.action == EventClass.ShowContestView.ACTION_SHOW) {
                    showRevivalRuleView();
                }
            } else if (event.type == EventClass.ShowContestView.TYPE_WIN_SHARE_VIEW) {
                if (event.action == EventClass.ShowContestView.ACTION_SHOW) {
                    showWinRevivalView();
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(com.wali.live.event.EventClass.NetWorkChangeEvent event) {
        if (null != event) {
            NetworkReceiver.NetState netCode = event.getNetState();
            if (netCode != NetworkReceiver.NetState.NET_NO) {
                MyLog.v(TAG, "onNetStateChanged netCode = " + netCode);
                if (!isFinishing()) {
                    // TODO: 2018/1/13 流量4g网络转化暂时去掉  请参考WatchActivity
                    queryRoomInfo();
                }
            }
        }
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        switch (event) {


        }
        return false;
    }

    private static class MyUIHandler extends Handler {
        private final WeakReference<ContestWatchActivity> mWeakRef;

        public MyUIHandler(ContestWatchActivity wrapperView) {
            super(Looper.getMainLooper());
            mWeakRef = new WeakReference(wrapperView);
        }

        @Override
        public void handleMessage(Message msg) {
            ContestWatchActivity activity = mWeakRef.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                case MSG_RELOAD_VIDEO:
                    MyLog.w(activity.TAG, "MSG_RELOAD_VIDEO");
//                    activity.startReconnect();
                    break;
                case MAG_SHOW_SUCCESS_VIEW:
                    activity.showContestSuccessView(activity.mLastQuestionInfoModel);
                    break;
                default:
                    break;
            }
        }
    }

    public static void open(BaseActivity activity, long zuid, String roomId, String videoUrl) {
        if (MiLinkClientAdapter.getsInstance().isTouristMode()) {
            ToastUtils.showToast(R.string.contest_login_tip);
            return;
        }
        Intent intent = new Intent(activity, ContestWatchActivity.class);
        intent.putExtra(EXTRA_ZUID, zuid);
        intent.putExtra(EXTRA_ROOM_ID, roomId);
        if (!TextUtils.isEmpty(videoUrl)) {
            intent.putExtra(EXTRA_VIDEO_URL, videoUrl);
        }
        activity.startActivity(intent);
    }
}
