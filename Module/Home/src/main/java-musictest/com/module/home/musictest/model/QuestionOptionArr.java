package com.module.home.musictest.model;

import java.io.Serializable;

// 选项
public class QuestionOptionArr implements Serializable {
    /**
     * optionID : g
     * optionVal : R&B
     */

    private String optionID;
    private String optionVal;

    public String getOptionID() {
        return optionID;
    }

    public void setOptionID(String optionID) {
        this.optionID = optionID;
    }

    public String getOptionVal() {
        return optionVal;
    }

    public void setOptionVal(String optionVal) {
        this.optionVal = optionVal;
    }
}
