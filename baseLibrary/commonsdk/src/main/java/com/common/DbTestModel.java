package com.common;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Transient;

import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;


@Entity(indexes = {
        @Index(value = "text, date DESC", unique = true)
})
public class DbTestModel {
    @Id(autoincrement = true)
    private Long id;

    @NotNull
    @Property(nameInDb = "dbtext")
    private String text;

    private Date date;
    @Generated
    private String bbb;

    // 不进数据库
    @Transient
    private int aa;

@Keep
public DbTestModel(Long id, @NotNull String text, Date date, String bbb) {
    this.id = id;
    this.text = text;
    this.date = date;
    this.bbb = bbb;
}

@Keep
public DbTestModel() {
}

public Long getId() {
    return this.id;
}

public void setId(Long id) {
    this.id = id;
}

public String getText() {
    return this.text;
}

public void setText(String text) {
    this.text = text;
}

public Date getDate() {
    return this.date;
}

public void setDate(Date date) {
    this.date = date;
}

public String getBbb() {
    return this.bbb;
}

public void setBbb(String bbb) {
    this.bbb = bbb;
}

}
