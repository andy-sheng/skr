package com.example.rxretrofit.fastjson;

import java.io.Serializable;

public class Student implements Serializable {

    private Long id;

    private String name;

    private Sex sex;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

}
