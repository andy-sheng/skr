package com.module.home.musictest.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.flowlayout.FlowLayout;
import com.common.flowlayout.TagAdapter;
import com.common.flowlayout.TagFlowLayout;
import com.common.view.ex.ExTextView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.home.R;
import com.module.home.musictest.model.Answer;
import com.module.home.musictest.model.Question;
import com.module.home.musictest.model.QuestionOptionArr;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

public class QuestionView extends RelativeLayout {

    ExTextView mQuestionTv;
    TagFlowLayout mOptionsTag;
    ExTextView mLastTv;
    ExTextView mNextTv;
    ExTextView mCompleteTv;

    OnClickListener mOnClickListener;
    TagAdapter<QuestionOptionArr> mTagAdapter;

    Question mQuestion;    // 问题
    int maxNum;            // 问题最大数
    int current;           // 当前是第几个问题

    public QuestionView(Context context) {
        super(context);
        init();
    }

    public QuestionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public QuestionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.music_question_view, this);

        mQuestionTv = (ExTextView) findViewById(R.id.question_tv);
        mOptionsTag = (TagFlowLayout) findViewById(R.id.options_tag);
        mLastTv = (ExTextView) findViewById(R.id.last_tv);
        mNextTv = (ExTextView) findViewById(R.id.next_tv);
        mCompleteTv = (ExTextView) findViewById(R.id.complete_tv);

        RxView.clicks(mNextTv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        clickNext();
                    }
                });

        RxView.clicks(mLastTv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        clickLast();
                    }
                });

        RxView.clicks(mCompleteTv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        clickComplete();
                    }
                });

    }

    public void setMaxNum(int maxNum) {
        this.maxNum = maxNum;
    }

    public void setData(Question question, int current, Set<Integer> selectedList) {
        this.mQuestion = question;
        this.current = current;

        if (current == 0 && maxNum == 1) {
            mLastTv.setVisibility(GONE);
            mNextTv.setVisibility(GONE);
            mCompleteTv.setVisibility(VISIBLE);
        } else if (current == 0 && maxNum > 1) {
            mLastTv.setVisibility(GONE);
            mNextTv.setVisibility(VISIBLE);
            mCompleteTv.setVisibility(GONE);
        } else if (current > 0 && current < (maxNum - 1)) {
            mLastTv.setVisibility(VISIBLE);
            mNextTv.setVisibility(VISIBLE);
            mCompleteTv.setVisibility(GONE);
        } else if (current == (maxNum - 1)) {
            mLastTv.setVisibility(VISIBLE);
            mNextTv.setVisibility(GONE);
            mCompleteTv.setVisibility(VISIBLE);
        }

        mQuestionTv.setText(question.getQuestionTitle());

        mTagAdapter = new TagAdapter<QuestionOptionArr>(question.getQuestionOptionArr()) {
            @Override
            public View getView(FlowLayout parent, int position, QuestionOptionArr optionArr) {
                TextView textView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.option_arr_textview,
                        mOptionsTag, false);
                textView.setText(optionArr.getOptionVal());
                return textView;
            }
        };

        mOptionsTag.setAdapter(mTagAdapter);
        if (selectedList != null) {
            mTagAdapter.setSelectedList(selectedList);
        }
    }

    public void setListener(OnClickListener listener) {
        this.mOnClickListener = listener;
    }

    private void clickNext() {
        Set<Integer> set = mOptionsTag.getSelectedList();
        List<String> options = new ArrayList<>();
        for (Integer key : set) {
            options.add(mQuestion.getQuestionOptionArr().get(key).getOptionID());
        }
        Answer answer = new Answer();
        answer.setQuestionID(mQuestion.getQuestionID());
        answer.setAnswerIDs(options);

        if (mOnClickListener != null) {
            mOnClickListener.nextQuestion(answer, current + 1, set);
        }
    }

    private void clickLast() {
        if (mOnClickListener != null) {
            mOnClickListener.lastQuestion(current - 1);
        }
    }

    private void clickComplete() {
        Set<Integer> set = mOptionsTag.getSelectedList();
        List<String> options = new ArrayList<>();
        for (Integer key : set) {
            options.add(mQuestion.getQuestionOptionArr().get(key).getOptionID());
        }
        Answer answer = new Answer();
        answer.setQuestionID(mQuestion.getQuestionID());
        answer.setAnswerIDs(options);

        if (mOnClickListener != null) {
            mOnClickListener.onCompletion(answer);
        }
    }


    public interface OnClickListener {
        // 下一题
        void nextQuestion(Answer answer, int next, Set<Integer> selectList);

        // 上一题
        void lastQuestion(int next);

        // 完成
        void onCompletion(Answer answer);
    }
}
