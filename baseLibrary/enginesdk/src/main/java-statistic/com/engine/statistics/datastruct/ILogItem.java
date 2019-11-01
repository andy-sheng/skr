package com.engine.statistics.datastruct;


import org.json.JSONObject;

public interface ILogItem
{
    String toString();
    JSONObject toJSONObject();
    String getKey(); //the key is for json-structure requirement

}