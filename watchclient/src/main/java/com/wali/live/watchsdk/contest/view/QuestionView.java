package com.wali.live.watchsdk.contest.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.log.MyLog;
import com.base.mvp.specific.RxRelativeLayout;
import com.base.utils.CommonUtils;
import com.mi.live.data.push.model.contest.ContestQuestionMsgExt;
import com.mi.live.data.push.model.contest.QuestionInfoModel;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.contest.cache.ContestCurrentCache;
import com.wali.live.watchsdk.contest.presenter.CommitContestAnswerPresenter;
import com.wali.live.watchsdk.contest.presenter.IContestCommitAnswerView;
import com.wali.live.watchsdk.view.TimeCounterView;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by liuyanyan on 2018/1/15.
 */
public class QuestionView extends RxRelativeLayout implements View.OnClickListener, IContestCommitAnswerView {
    private final String TAG = QuestionView.class.getSimpleName() + hashCode();

    public final int COUNT_DOWN_TOTAL_NUM = 10 * 1000;//答题倒计时总时长
    public final int COUNT_DOWN_INTERVAL = 1 * 1000;//倒计时间隔

    public static final int MSG_HIDE_VIEW = 101;

    private ImageView mNotifyIv;
    private TextView mQuestionNameTv;
    private LinearLayout mAnswerContainer;
    private TimeCounterView mTimeCounterView;

    private String mSelectId;
    private boolean mIsOverTime;

    private String mSeq;//题目号
    private long mZuId;//主播id
    private String mRoomId;//房间id

    private CommitContestAnswerPresenter mCommitAnswerPresenter;
    private QuestionView.TimerHandler mTimerHandler = new QuestionView.TimerHandler(new WeakReference<QuestionView>(this));
//    private CountDownTimer mCountDownTimer = new CountDownTimer(COUNT_DOWN_TOTAL_NUM, COUNT_DOWN_INTERVAL) {
//        @Override
//        public void onTick(long millisUntilFinished) {
//            MyLog.w(TAG, "countDownNow mills = " + millisUntilFinished);
//            mCountDownTv.setText(String.valueOf(millisUntilFinished / 1000));
//        }
//
//        @Override
//        public void onFinish() {
//            mCountDownTv.setVisibility(GONE);
//            MyLog.w(TAG, "timer finish");
//            setNotifyTv(0, R.string.contest_time_out, R.drawable.bg_corner_60px_red);
//            mIsOverTime = true;
//            mTimerHandler.sendEmptyMessageDelayed(MSG_HIDE_VIEW, 2_000);
//        }
//    };

    private static class TimerHandler extends Handler {
        private final WeakReference<QuestionView> mView;

        private TimerHandler(WeakReference<QuestionView> mView) {
            this.mView = mView;
        }

        @Override
        public void handleMessage(Message msg) {
            QuestionView questionView = mView.get();
            if (questionView == null) {
                return;
            }
            switch (msg.what) {
                case MSG_HIDE_VIEW:
                    questionView.hideView();
                    break;
                default:
                    break;
            }
        }
    }

    public QuestionView(Context context) {
        super(context);
        init(context);
    }

    public QuestionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public QuestionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.view_summit_question, this);
        initContentView();
        initPresenter();
    }

    private void initContentView() {
        mNotifyIv = (ImageView) findViewById(R.id.notify_view);
//        mCountDownTv = (TextView) findViewById(R.id.count_down_view);
        mTimeCounterView = (TimeCounterView) findViewById(R.id.count_down);
        mTimeCounterView.setOnFinishListener(new TimeCounterView.FinishCallBack() {
            @Override
            public void onFinish() {
                MyLog.w(TAG, "timer finish");
                mIsOverTime = true;
                mTimerHandler.sendEmptyMessageDelayed(MSG_HIDE_VIEW, 4_000);
            }
        });
        mQuestionNameTv = (TextView) findViewById(R.id.question_name);
        mAnswerContainer = (LinearLayout) findViewById(R.id.answer_container);
    }

    private void initPresenter() {
        mCommitAnswerPresenter = new CommitContestAnswerPresenter(this);
    }

    private void showView() {
        MyLog.w(TAG, " showCacheData = " + ContestCurrentCache.getInstance().toString());
        setVisibility(VISIBLE);

//        mCountDownTv.setVisibility(GONE);
        mTimeCounterView.setVisibility(GONE);
        mNotifyIv.setVisibility(GONE);

        if (ContestCurrentCache.getInstance().isContinue()) {
            MyLog.w(TAG, "can continue answer");
            mTimeCounterView.setVisibility(VISIBLE);
            mTimeCounterView.showWaiting();
//            mCountDownTv.setVisibility(VISIBLE);
//            mCountDownTimer.start();
        } else if (ContestCurrentCache.getInstance().isWatchMode()) {
            MyLog.w(TAG, " gameOut watchMode");
            setNotifyIv(R.drawable.youle_live_answer_icon_watch);
            mTimerHandler.sendEmptyMessageDelayed(MSG_HIDE_VIEW, COUNT_DOWN_TOTAL_NUM);
        } else {
            MyLog.w(TAG, " gameOut");
            setNotifyIv(R.drawable.youle_live_answer_icon_out);
            mTimerHandler.sendEmptyMessageDelayed(MSG_HIDE_VIEW, COUNT_DOWN_TOTAL_NUM);
        }
    }

    private void hideView() {
        setVisibility(GONE);

        if (TextUtils.isEmpty(mSelectId) && ContestCurrentCache.getInstance().isContinue()) {
            MyLog.w(TAG, "hideView commitContestAnswer ");
            ContestCurrentCache.getInstance().setContinue(false);
            mCommitAnswerPresenter.commitContestAnswer(mSeq, "", mZuId, mRoomId);
        }
        resetData();
    }

    private void resetData() {
        MyLog.w(TAG, "resetData");
        mIsOverTime = false;
        mSelectId = null;
        mTimerHandler.removeCallbacksAndMessages(null);
    }

    private void setItemsStatus() {
        for (int index = 0; index < mAnswerContainer.getChildCount(); index++) {
            View view = mAnswerContainer.getChildAt(index);
            if (!view.isSelected()) {
                view.findViewById(R.id.content_view).setEnabled(false);
            }
        }
    }

    private void setNotifyIv(int imageRes) {
        mNotifyIv.setVisibility(VISIBLE);
        mNotifyIv.setImageResource(imageRes);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (CommonUtils.isFastDoubleClick()) {
            return;
        }
        if (i == R.id.item_view) {
            if (!ContestCurrentCache.getInstance().isContinue()) {
                //已经丧失答题资格
//                ToastUtils.showToast("已出局");
                MyLog.w(TAG, "onClick out");
            } else if (!TextUtils.isEmpty(mSelectId)) {
                //已经选择过了
                MyLog.w(TAG, "onClick hasChoose");
//                ToastUtils.showToast("已选择过了");
            } else if (mIsOverTime) {
                //是否已经超时
                MyLog.w(TAG, "onClick timeout");
//                ToastUtils.showToast("已超时不能继续答题");
            } else {
                //可以继续答题
                String id = (String) v.getTag();
                mSelectId = id;
                v.setSelected(true);
                setItemsStatus();
                MyLog.w(TAG, "onClick commitContestAnswer id = " + id);
                mCommitAnswerPresenter.commitContestAnswer(mSeq, id, mZuId, mRoomId);
            }
        }
    }

    public void bindContestQuestionData(ContestQuestionMsgExt questionMsgExt) {
        MyLog.w(TAG, "bindContestQuestionData");
        QuestionInfoModel model = questionMsgExt.getQuestionInfoModel();
        bindData(model);
    }

    private void bindData(QuestionInfoModel model) {
        if (null != model) {
            MyLog.w(TAG, "bindData model = " + model.toString());

            mSeq = model.getSeq();
            mQuestionNameTv.setText(String.valueOf(model.getQuestionShowId()) + "." + model.getQuestionContent());
            List<QuestionInfoModel.QuestionInfoItem> answerList = model.getQuestionInfoItems();
            mAnswerContainer.removeAllViews();
            if (null != answerList && answerList.size() > 0) {
                for (QuestionInfoModel.QuestionInfoItem item : answerList) {
                    View itemView = inflate(getContext(), R.layout.item_summit_question_option, null);

                    TextView optionTitleTv = (TextView) itemView.findViewById(R.id.answer_option_tv);
                    optionTitleTv.setText(item.getText());
                    if (ContestCurrentCache.getInstance().isContinue()) {
                        optionTitleTv.setEnabled(true);
                    } else {
                        optionTitleTv.setEnabled(false);
                    }

                    itemView.setTag(item.getId());
                    itemView.setOnClickListener(this);
                    itemView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    mAnswerContainer.addView(itemView);
                }
            }
            showView();
        } else {
            MyLog.w(TAG, "bindData model is null");
            hideView();
        }
    }

    public void initRoomData(long zuId, String roomId) {
        mZuId = zuId;
        mRoomId = roomId;
    }

    public void destroy() {
        super.destroy();
        resetData();
        mTimerHandler.removeCallbacksAndMessages(null);
        mTimeCounterView.stop();
    }
}
