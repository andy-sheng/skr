package com.engine;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

public class CdnInfo implements Serializable {
    @JSONField(name = "cdnType")
    String cdnType;
    @JSONField(name = "appendix")
    String appendix;
    @JSONField(name = "enableCache")
    boolean enableCache;
    @JSONField(name = "url")
    String url;

    public String getCdnType() {
        return cdnType;
    }

    public void setCdnType(String cdnType) {
        this.cdnType = cdnType;
    }

    public String getAppendix() {
        return appendix;
    }

    public void setAppendix(String appendix) {
        this.appendix = appendix;
    }

    public boolean isEnableCache() {
        return enableCache;
    }

    public void setEnableCache(boolean enableCache) {
        this.enableCache = enableCache;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
