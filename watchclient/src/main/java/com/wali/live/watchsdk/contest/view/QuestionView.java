package com.wali.live.watchsdk.contest.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.base.log.MyLog;
import com.base.mvp.specific.RxRelativeLayout;
import com.base.utils.CommonUtils;
import com.mi.live.data.push.model.contest.ContestQuestionMsgExt;
import com.mi.live.data.push.model.contest.QuestionInfoModel;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.contest.cache.ContestCurrentCache;
import com.wali.live.watchsdk.contest.media.ContestMediaHelper;
import com.wali.live.watchsdk.contest.presenter.CommitContestAnswerPresenter;
import com.wali.live.watchsdk.contest.presenter.IContestCommitAnswerView;
import com.wali.live.watchsdk.view.CountDownView;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by liuyanyan on 2018/1/15.
 */
public class QuestionView extends RxRelativeLayout implements View.OnClickListener, IContestCommitAnswerView {
    private final String TAG = QuestionView.class.getSimpleName() + hashCode();

    public static final int MSG_HIDE_VIEW = 101;
    public static final int MSG_HIDE_NOTIFY_VIEW = 102;

    private ImageView mNotifyIv;
    private TextView mQuestionNameTv;
    private LinearLayout mAnswerContainer;
    private CountDownView mTimeCounterView;
    private View mAnimContainer;

    private String mSelectId;
    private boolean mIsOverTime;

    private String mSeq;//题目号
    private long mZuId;//主播id
    private String mRoomId;//房间id

    private ContestMediaHelper mMediaHelper;
    private CommitContestAnswerPresenter mCommitAnswerPresenter;
    private QuestionView.TimerHandler mTimerHandler = new QuestionView.TimerHandler(new WeakReference<QuestionView>(this));

    private Animation mShowAnimation;
    private Animation mHideAnimation;
    private Animation mContentShowAnimation;
    private ValueAnimator mClickAnimator;

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
                case MSG_HIDE_NOTIFY_VIEW:
                    questionView.mNotifyIv.setVisibility(GONE);
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
        mTimeCounterView = (CountDownView) findViewById(R.id.count_down);
        mTimeCounterView.setOnFinishListener(new CountDownView.FinishCallBack() {
            @Override
            public void onFinish() {
                MyLog.w(TAG, "timer finish");
                mIsOverTime = true;
                mTimerHandler.sendEmptyMessageDelayed(MSG_HIDE_VIEW, 4_000);
            }
        });
        mQuestionNameTv = (TextView) findViewById(R.id.question_name);
        mAnswerContainer = (LinearLayout) findViewById(R.id.answer_container);

        mAnimContainer = findViewById(R.id.anim_container);
    }

    private void initPresenter() {
        mCommitAnswerPresenter = new CommitContestAnswerPresenter(this);
        mMediaHelper = new ContestMediaHelper(getContext());
    }

    private void showViewAnimation() {
        setVisibility(VISIBLE);
        if (mShowAnimation == null) {
            mShowAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.contest_view_show_anim);
        }
        mAnimContainer.clearAnimation();
        mAnimContainer.startAnimation(mShowAnimation);

        mTimerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, " showQuestionName anim");
                mContentShowAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.contest_view_alpha_in);
                mQuestionNameTv.clearAnimation();
                mQuestionNameTv.setVisibility(VISIBLE);
                mQuestionNameTv.startAnimation(mContentShowAnimation);
            }
        }, 190);

        for (int index = 0; index < mAnswerContainer.getChildCount(); index++) {
            final View view = mAnswerContainer.getChildAt(index);
            mTimerHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MyLog.w(TAG, " showQuestionItem anim");
                    mContentShowAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.contest_view_alpha_in);
                    view.setVisibility(VISIBLE);
                    view.clearAnimation();
                    view.startAnimation(mContentShowAnimation);
                }
            }, 390 + index * 200);
        }
    }

    private void hideViewAnimation() {
        if (mHideAnimation == null) {
            mHideAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.contest_view_hide_anim);
            mHideAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    setVisibility(GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
        mAnimContainer.clearAnimation();
        mAnimContainer.startAnimation(mHideAnimation);
    }

    private void showView() {
        MyLog.w(TAG, " showCacheData = " + ContestCurrentCache.getInstance().toString());
        showViewAnimation();

        mTimeCounterView.setVisibility(GONE);
        mNotifyIv.setVisibility(GONE);
        mMediaHelper.playRawSource(R.raw.contest_begin_tip);

        mTimeCounterView.setVisibility(VISIBLE);
        mTimeCounterView.showWaiting();
    }

    private void hideView() {
        MyLog.w(TAG, "hideView");
        hideViewAnimation();

        if (TextUtils.isEmpty(mSelectId) && ContestCurrentCache.getInstance().isContinue()) {
            MyLog.w(TAG, "hideView commitContestAnswer ");
            ContestCurrentCache.getInstance().setContinue(false);
            mCommitAnswerPresenter.commitContestAnswer(mSeq, "", mZuId, mRoomId);
        }
    }

    private void resetData() {
        MyLog.w(TAG, "resetData");
        mIsOverTime = false;
        mSelectId = null;
        mTimerHandler.removeCallbacksAndMessages(null);

        if (mClickAnimator != null) {
            mClickAnimator.removeAllUpdateListeners();
            mClickAnimator.cancel();
            mClickAnimator = null;
        }
    }

    private void setItemsStatus() {
        for (int index = 0; index < mAnswerContainer.getChildCount(); index++) {
            View view = mAnswerContainer.getChildAt(index);
            if (!view.isSelected()) {
                view.findViewById(R.id.content_view).setEnabled(false);
            }
            view.setEnabled(false);
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
            if (!TextUtils.isEmpty(mSelectId)) {
                //已经选择过了
                MyLog.w(TAG, "onClick hasChoose");
//                ToastUtils.showToast("已选择过了");
            } else if (mIsOverTime) {
                //是否已经超时
                MyLog.w(TAG, "onClick timeout");
//                ToastUtils.showToast("已超时不能继续答题");
            } else if (ContestCurrentCache.getInstance().isWatchMode()) {
                MyLog.w(TAG, " onclick watchMode");
                setNotifyIv(R.drawable.youle_live_answer_icon_watch);
                mTimerHandler.sendEmptyMessageDelayed(MSG_HIDE_NOTIFY_VIEW, 2 * 1000);
            } else if (!ContestCurrentCache.getInstance().isContinue()) {
                MyLog.w(TAG, " onclick out");
                setNotifyIv(R.drawable.youle_live_answer_icon_out);
                mTimerHandler.sendEmptyMessageDelayed(MSG_HIDE_NOTIFY_VIEW, 2 * 1000);
                //已经丧失答题资格
            } else {
                //可以继续答题
                String id = (String) v.getTag();
                mSelectId = id;
                v.setSelected(true);
                setItemsStatus();

                showClickAnimation(v);
                MyLog.w(TAG, "onClick commitContestAnswer id = " + id);
                mCommitAnswerPresenter.commitContestAnswer(mSeq, id, mZuId, mRoomId);
            }
        }
    }

    private void showClickAnimation(final View v) {
        v.clearAnimation();
        mClickAnimator = ValueAnimator.ofFloat(1.0f, 1.05f, 1.0f);
        mClickAnimator.setDuration(400);
        mClickAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float curValue = (float) valueAnimator.getAnimatedValue();
                v.setScaleX(curValue);
                v.setScaleY(curValue);
            }
        });
        mClickAnimator.start();
    }

    public void bindContestQuestionData(ContestQuestionMsgExt questionMsgExt) {
        MyLog.w(TAG, "bindContestQuestionData");
        resetData();
        QuestionInfoModel model = questionMsgExt.getQuestionInfoModel();
        bindData(model);
    }

    private void bindData(QuestionInfoModel model) {
        if (null != model) {
            MyLog.w(TAG, "bindData model = " + model.toString());

            mSeq = model.getSeq();
            mQuestionNameTv.setText(String.valueOf(model.getQuestionShowId()) + "." + model.getQuestionContent());
            mQuestionNameTv.setVisibility(INVISIBLE);

            List<QuestionInfoModel.QuestionInfoItem> answerList = model.getQuestionInfoItems();
            mAnswerContainer.removeAllViews();
            if (null != answerList && answerList.size() > 0) {
                for (QuestionInfoModel.QuestionInfoItem item : answerList) {
                    View itemView = inflate(getContext(), R.layout.item_summit_question_option, null);

                    TextView optionTitleTv = (TextView) itemView.findViewById(R.id.answer_option_tv);
                    optionTitleTv.setText(item.getText());
                    if (ContestCurrentCache.getInstance().isContinue()) {
                        itemView.findViewById(R.id.content_view).setEnabled(true);
                        optionTitleTv.setEnabled(true);
                    } else {
                        itemView.findViewById(R.id.content_view).setEnabled(false);
                        optionTitleTv.setEnabled(false);
                    }

                    itemView.setVisibility(INVISIBLE);
                    itemView.setTag(item.getId());
                    itemView.setOnClickListener(this);
                    itemView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
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
        MyLog.w(TAG, " destroy");
        mTimerHandler.removeCallbacksAndMessages(null);
        mTimeCounterView.stop();
        if (mMediaHelper != null) {
            mMediaHelper.destroy();
        }
        if (mShowAnimation != null) {
            mShowAnimation.cancel();
        }
        if (mHideAnimation != null) {
            mHideAnimation.cancel();
        }
        if (mContentShowAnimation != null) {
            mContentShowAnimation.cancel();
        }
    }
}
