package com.sheandsoul.v1update.dto;

import java.util.HashMap;
import java.util.Map;

public class TranslationWrapper {

    private Map<String, Object> data = new HashMap<>();

    // Holds text fields that need to be translated
    private Map<String, String> uiText = new HashMap<>();

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public Map<String, String> getUiText() {
        return uiText;
    }

    public void setUiText(Map<String, String> uiText) {
        this.uiText = uiText;
    }

    // Convenience methods
    public void addData(String key, Object value) {
        this.data.put(key, value);
    }

    public void addUiText(String key, String value) {
        this.uiText.put(key, value);
    }

}
