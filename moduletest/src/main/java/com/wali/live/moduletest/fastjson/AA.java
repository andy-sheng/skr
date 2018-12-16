package com.wali.live.moduletest.fastjson;

public class AA {
    String a;
    String b;
    CC cc;

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    public CC getCc() {
        return cc;
    }

    public void setCc(CC cc) {
        this.cc = cc;
    }

    @Override
    public String toString() {
        return "AA{" +
                "a='" + a + '\'' +
                ", b='" + b + '\'' +
                ", cc=" + cc +
                '}';
    }
}
