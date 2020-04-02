package com.component.person.photo.manager;

import com.component.person.photo.model.PhotoModel;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 个人信息(基础类)
 */
@Entity(
//        indexes = {
//                @Index(value = "localPath", unique = true)
//        }
)
public class ClubPhotoModelDB implements Serializable {
    private static final long serialVersionUID = -5809722578272943997L;
//    @Id
//    private Long id;

    @Id
    private String localPath;// 本地路径

    private Integer status = PhotoModel.Companion.getSTATUS_SUCCESS();

    @Keep
    public ClubPhotoModelDB(String localPath, Integer status) {
        this.localPath = localPath;
        this.status = status;
    }

    @Keep
    public ClubPhotoModelDB() {
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

