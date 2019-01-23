package com.zq.report.model;

public class ReportModel {

//    TYPE_UNKNOWN	0 未知，不合法
//    TYPE_PIANZI	1 骗子，有欺诈行为
//    TYPE_RUMA	    2 侮辱谩骂
//    TYPE_SEX	    3 色情低俗
//    TYPE_WEIGUI	4 头像、昵称违规
//    TYPE_ZUOBI	5 违规作弊
//    TYPE_LUANCHAN	6 恶意乱唱
//    TYPE_MAOCHONG	7 冒充官方

    private int type;
    private String text;

    ReportModel() {

    }

    public ReportModel(int type, String text) {
        this.type = type;
        this.text = text;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
