package com.common.matrix.display;


import com.alibaba.fastjson.JSONObject;

public class MyIssue {
    private int    type;
    private String     tag;
    private String     key;
    private JSONObject content;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public JSONObject getContent() {
        return content;
    }

    public String getDesc() {
        if("io".startsWith(tag)){
            if(type==1){
                return "主线程io";
            }else if(type==2){
                return "buffer太小";
            }
            else if(type==3){
                return "重复读取同一文件";
            }
            else if(type==4){
                return "文件泄露，未关闭";
            }
        }else  if("Trace_EvilMethod".startsWith(tag)){
            return "耗时方法";
        }
        return "";
    }

    public void setContent(JSONObject content) {
        this.content = content;
    }
}
