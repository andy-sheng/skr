package com.module.home.musictest.view;

import com.module.home.musictest.model.Question;

import java.util.List;

public interface IQuestionView {

    void loadQuestionsData(List<Question> questionList);

    void loadQuestionsDataFail();

    void onComplete();
}
