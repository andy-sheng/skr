package com.example.rxretrofit.fastjson;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.JSONSerializable;
import com.alibaba.fastjson.serializer.JSONSerializer;

import java.io.IOException;
import java.lang.reflect.Type;

public enum Sex implements JSONSerializable {

    NONE("0","NONE"),MAN("1","男"),WOMAN("2","女");
    private final String code;
    private final String des;
    private Sex(String code, String des) {
        this.code = code;
        this.des = des;
    }

    public String getCode() {
        return code;
    }

    public String getDes() {
        return des;
    }

    @Override
    public void write(JSONSerializer serializer, Object fieldName, Type fieldType) throws IOException {
        JSONObject object = new JSONObject();
        object.put("code", code);
        object.put("des", des);
        serializer.write(object);
    }
}