package com.module.home.musictest.model;

import java.io.Serializable;
import java.util.List;

public class Answer implements Serializable {
    /**
     * questionID : 1
     * answerIDs : ["b","e"]
     */

    private String questionID;
    private List<String> answerIDs;

    public String getQuestionID() {
        return questionID;
    }

    public void setQuestionID(String questionID) {
        this.questionID = questionID;
    }

    public List<String> getAnswerIDs() {
        return answerIDs;
    }

    public void setAnswerIDs(List<String> answerIDs) {
        this.answerIDs = answerIDs;
    }
}
