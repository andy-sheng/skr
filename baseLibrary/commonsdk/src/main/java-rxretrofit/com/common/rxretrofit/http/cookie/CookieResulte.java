package com.common.rxretrofit.http.cookie;

import android.support.annotation.NonNull;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

/**
 * post請求緩存数据
 */
@Entity(
        indexes = {
                @Index(value = "url DESC", unique = true)
        }
)
public class CookieResulte {
    @Id
    private Long id;
    @NonNull
    private String url; /*url*/
    private String resulte;/*返回结果*/
    private long time; /*时间*/

    @Generated(hash = 703456687)
    public CookieResulte(Long id, @NonNull String url, String resulte, long time) {
        this.id = id;
        this.url = url;
        this.resulte = resulte;
        this.time = time;
    }

    public CookieResulte(@NonNull String url, String resulte, long time) {
        this.url = url;
        this.resulte = resulte;
        this.time = time;
    }

    @Generated(hash = 2104390000)
    public CookieResulte() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getResulte() {
        return this.resulte;
    }

    public void setResulte(String resulte) {
        this.resulte = resulte;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
