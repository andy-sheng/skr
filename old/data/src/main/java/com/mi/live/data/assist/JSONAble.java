package com.mi.live.data.assist;

import org.json.JSONObject;

/**
 * Created by anping on 16-4-21.
 */
public interface JSONAble {
    String toJSONString();

    JSONObject toJSONObject();

    boolean parseJSONString(String jsonStr);
}
