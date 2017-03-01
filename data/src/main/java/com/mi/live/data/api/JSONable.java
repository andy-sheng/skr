package com.mi.live.data.api;

import org.json.JSONObject;

/**
 * 一个接口, 提供序列化到jsonObject对象
 * Created by yaojian on 16-4-27.
 */
public interface JSONable {
    /**
     * 将数据序列化到一个JSONObject中
     */
    JSONObject serialToJSON();

    /**
     * 由一个JSONObject来序列化
     */
    void serialFromJSON(JSONObject jsonObject);
}
