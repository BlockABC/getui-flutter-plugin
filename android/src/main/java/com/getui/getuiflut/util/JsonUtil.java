package com.getui.getuiflut.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by denghaofa
 * on 2020-05-08 16:52
 */
public class JsonUtil {
    public static Object parseJson(String json) {
        JSONObject jsonObject = StringUtil.parseJson(json);
        if (null != jsonObject) {
            return parseJson(jsonObject);
        }
        JSONArray jsonArray = StringUtil.parseJsonArray(json);
        if (null != jsonArray) {
            return parseJson(jsonArray);
        }
        return json;
    }

    public static Map<String, Object> parseJson(JSONObject jsonObject) {
        Map<String, Object> map = new HashMap<>();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jsonObject.opt(key);
            if (value instanceof JSONObject) {
                map.put(key, parseJson((JSONObject) value));
                continue;
            }
            if (value instanceof JSONArray) {
                map.put(key, parseJson((JSONArray) value));
                continue;
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> parseJson(JSONArray jsonArray) {
        List<Object> list = new ArrayList<>();
        int length = jsonArray.length();
        for (int index = 0; index < length; index++) {
            Object object = jsonArray.opt(index);
            if (object instanceof JSONObject) {
                list.add(parseJson((JSONObject) object));
                continue;
            }
            if (object instanceof JSONArray) {
                list.add(parseJson((JSONArray) object));
                continue;
            }
            list.add(object);
        }
        return list;
    }
}
