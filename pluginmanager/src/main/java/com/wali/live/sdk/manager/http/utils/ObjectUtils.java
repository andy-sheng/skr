package com.wali.live.sdk.manager.http.utils;

import com.wali.live.sdk.manager.http.bean.BasicNameValuePair;
import com.wali.live.sdk.manager.http.bean.NameValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ObjectUtils {
    public ObjectUtils() {
    }

    public static List<NameValuePair> mapToPairs(Map<String, String> map) {
        if(map == null) {
            return null;
        } else {
            ArrayList pairs = new ArrayList();
            Set entries = map.entrySet();
            Iterator i$ = entries.iterator();

            while(i$.hasNext()) {
                Entry entry = (Entry)i$.next();
                String key = (String)entry.getKey();
                String value = (String)entry.getValue();
                BasicNameValuePair pair = new BasicNameValuePair(key, value != null?value:"");
                pairs.add(pair);
            }

            return pairs;
        }
    }

    public static Map<String, Object> jsonToMap(JSONObject jsonObj) {
        if(jsonObj == null) {
            return null;
        } else {
            HashMap map = new HashMap();
            Iterator iter = jsonObj.keys();

            while(iter.hasNext()) {
                String key = (String)iter.next();
                Object value = jsonObj.opt(key);
                map.put(key, convertObj(value));
            }

            return map;
        }
    }

    public static Object convertObjectToJson(Object obj) {
        if(obj instanceof List) {
            List jobj1 = (List)obj;
            JSONArray objMap1 = new JSONArray();
            Iterator keys1 = jobj1.iterator();

            while(keys1.hasNext()) {
                Object i$1 = keys1.next();
                objMap1.put(convertObjectToJson(i$1));
            }

            return objMap1;
        } else if(!(obj instanceof Map)) {
            return obj;
        } else {
            JSONObject jobj = new JSONObject();
            Map objMap = (Map)obj;
            Set keys = objMap.keySet();
            Iterator i$ = keys.iterator();

            while(i$.hasNext()) {
                Object key = i$.next();

                try {
                    jobj.put((String)key, convertObjectToJson(objMap.get(key)));
                } catch (JSONException var7) {
                    var7.printStackTrace();
                }
            }

            return jobj;
        }
    }

    public static Map<String, String> listToMap(Map<String, List<String>> listMap) {
        HashMap map = new HashMap();
        if(listMap != null) {
            Set entries = listMap.entrySet();
            Iterator i$ = entries.iterator();

            while(i$.hasNext()) {
                Entry entry = (Entry)i$.next();
                String key = (String)entry.getKey();
                List valueList = (List)entry.getValue();
                if(key != null && valueList != null && valueList.size() > 0) {
                    map.put(key, valueList.get(0));
                }
            }
        }

        return map;
    }

    public static String flattenMap(Map<?, ?> map) {
        if(map == null) {
            return "null";
        } else {
            Set entries = map.entrySet();
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            Iterator i$ = entries.iterator();

            while(i$.hasNext()) {
                Entry entry = (Entry)i$.next();
                Object key = entry.getKey();
                Object value = entry.getValue();
                sb.append("(");
                sb.append(key);
                sb.append(",");
                sb.append(value);
                sb.append("),");
            }

            sb.append("}");
            return sb.toString();
        }
    }

    private static Object convertObj(Object obj) {
        if(obj instanceof JSONObject) {
            return jsonToMap((JSONObject)obj);
        } else if(!(obj instanceof JSONArray)) {
            return obj == JSONObject.NULL?null:obj;
        } else {
            JSONArray array = (JSONArray)obj;
            int size = array.length();
            ArrayList list = new ArrayList();

            for(int i = 0; i < size; ++i) {
                list.add(convertObj(array.opt(i)));
            }

            return list;
        }
    }
}
