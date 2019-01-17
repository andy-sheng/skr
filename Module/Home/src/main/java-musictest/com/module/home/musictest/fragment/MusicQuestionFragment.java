package com.module.home.musictest.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.flowlayout.TagFlowLayout;
import com.common.view.ex.ExTextView;
import com.common.view.titlebar.CommonTitleBar;
import com.module.home.R;
import com.module.home.musictest.model.Question;
import com.module.home.musictest.presenter.MusicTestPresenter;
import com.module.home.musictest.view.IQuestionView;
import com.module.home.musictest.view.QuesetionView;

import java.util.List;

// 音乐问题页面
public class MusicQuestionFragment extends BaseFragment implements IQuestionView {

    CommonTitleBar mTitlebar;
    RelativeLayout mQuestionArea;

    MusicTestPresenter mMusicTestPresenter;

    List<Question> mQuestions;

    @Override
    public int initView() {
        return R.layout.music_question_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mQuestionArea = (RelativeLayout) mRootView.findViewById(R.id.question_area);

        mMusicTestPresenter = new MusicTestPresenter(this);
        addPresent(mMusicTestPresenter);

        mMusicTestPresenter.getQuestionList();
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void loadQuestionsData(List<Question> questionList) {
        this.mQuestions = questionList;

        QuesetionView quesetionView = new QuesetionView(getContext());
        quesetionView.setData(questionList.get(0));
        mQuestionArea.addView(quesetionView);
    }
}
