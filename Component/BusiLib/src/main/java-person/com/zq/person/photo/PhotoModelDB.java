package com.zq.person.photo;

import android.support.annotation.NonNull;

import com.zq.person.model.PhotoModel;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;

import java.io.Serializable;

/**
 * 个人信息(基础类)
 */
@Entity(
        indexes = {
                @Index(value = "localPath", unique = true)
        }
)
public class PhotoModelDB implements Serializable {
    private static final long serialVersionUID = -5809722578272943999L;
    @Id
    private Long id;

    @Index(unique = true)
    private String localPath;// 本地路径

    private Integer status = PhotoModel.STATUS_SUCCESS;

    @Generated(hash = 1386382573)
    public PhotoModelDB(Long id, String localPath, Integer status) {
        this.id = id;
        this.localPath = localPath;
        this.status = status;
    }

    @Generated(hash = 507245802)
    public PhotoModelDB() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLocalPath() {
        return this.localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

}

