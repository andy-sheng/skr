package com.common.matrix.display;

public class MethodInfo {
    int no;
    int type;
    String method;

    public MethodInfo(int no, int type, String method) {
        this.no = no;
        this.type = type;
        this.method = method;
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
