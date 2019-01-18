package com.module.home.musictest.model;

import java.io.Serializable;
import java.util.List;

// 问题
public class Question implements Serializable {

    /**
     * questionID : 1
     * questionTitle : Q1:最爱唱哪些风格的歌呢（支持多选）
     * questionOptionArr : [{"optionID":"a","optionVal":"流行"},{"optionID":"b","optionVal":"民歌"},{"optionID":"c","optionVal":"民谣"},{"optionID":"d","optionVal":"嘻哈"},{"optionID":"e","optionVal":"R&B"},{"optionID":"f","optionVal":"摇滚"}]
     * selectType : 2
     * minSelectNum : 1
     * maxSelectNum : 3
     */

    private String questionID;
    private String questionTitle;
    private int selectType;
    private int minSelectNum;
    private int maxSelectNum;
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

    public int getSelectType() {
        return selectType;
    }

    public void setSelectType(int selectType) {
        this.selectType = selectType;
    }

    public int getMinSelectNum() {
        return minSelectNum;
    }

    public void setMinSelectNum(int minSelectNum) {
        this.minSelectNum = minSelectNum;
    }

    public int getMaxSelectNum() {
        return maxSelectNum;
    }

    public void setMaxSelectNum(int maxSelectNum) {
        this.maxSelectNum = maxSelectNum;
    }

    public List<QuestionOptionArr> getQuestionOptionArr() {
        return questionOptionArr;
    }

    public void setQuestionOptionArr(List<QuestionOptionArr> questionOptionArr) {
        this.questionOptionArr = questionOptionArr;
    }
}
