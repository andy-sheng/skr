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
import com.module.home.R;
import com.module.home.musictest.model.Question;
import com.module.home.musictest.model.QuestionOptionArr;

public class QuesetionView extends RelativeLayout {

    ExTextView mQuestionTv;
    TagFlowLayout mOptionsTag;

    public QuesetionView(Context context) {
        super(context);
        init();
    }

    public QuesetionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public QuesetionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.music_question_view, this);

        mQuestionTv = (ExTextView) findViewById(R.id.question_tv);
        mOptionsTag = (TagFlowLayout) findViewById(R.id.options_tag);
    }

    public void setData(Question question) {
        mQuestionTv.setText("Q1:" + question.getQuestionTitle());

        mOptionsTag.setAdapter(new TagAdapter<QuestionOptionArr>(question.getQuestionOptionArr()) {
            @Override
            public View getView(FlowLayout parent, int position, QuestionOptionArr optionArr) {
                ExTextView textView = (ExTextView) LayoutInflater.from(getContext()).inflate(R.layout.option_arr_textview,
                        mOptionsTag, false);
                textView.setText(optionArr.getOptionVal());
                return textView;
            }
        });
    }
}
