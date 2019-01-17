package com.module.home.musictest.model;

import java.io.Serializable;
import java.util.List;

// 问题
public class Question implements Serializable {
    /**
     * questionID : 1
     * questionTitle : 最爱唱哪些风格的歌曲呢？
     * questionOptionArr : [{"optionID":"a","optionVal":"流行"},{"optionID":"b","optionVal":"摇滚"},{"optionID":"c","optionVal":"民谣"},{"optionID":"d","optionVal":"嘻哈"},{"optionID":"e","optionVal":"乡村"},{"optionID":"f","optionVal":"民谣"},{"optionID":"g","optionVal":"R&B"},{"optionID":"h","optionVal":"民歌"}]
     */

    private String questionID;
    private String questionTitle;
    private List<QuestionOptionArr> questionOptionArr;

    public String getQuestionID() {
        return questionID;
    }

    public void setQuestionID(String questionID) {
        this.questionID = questionID;
    }

    public String getQuestionTitle() {
        return questionTitle;
    }

    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }

    public List<QuestionOptionArr> getQuestionOptionArr() {
        return questionOptionArr;
    }

    public void setQuestionOptionArr(List<QuestionOptionArr> questionOptionArr) {
        this.questionOptionArr = questionOptionArr;
    }
}
