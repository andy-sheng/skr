package com.mi.live.data.user;

import com.wali.live.proto.UserProto;

import java.io.Serializable;

/**
 * Created by anping on 17/2/21.
 */

public class Medal implements Serializable {
    private String picId;

    public Medal(UserProto.Medal medal) {
        picId = medal.getPicId();
    }

    public Medal() {

    }

    public String getPicId() {
        return picId;
    }

    public void setPicId(String picId) {
        this.picId = picId;
    }
}
