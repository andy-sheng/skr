package com.mi.liveassistant.attachment;

import org.json.JSONObject;

/**
 * Created by anping on 16-4-21.
 */
public interface Jsonable {
    String toJSONString();

    JSONObject toJSONObject();

    boolean parseJSONString(String jsonStr);
}
