package model;

/**
 * Created by liuyanyan on 2018/1/16.
 */

public class AwardUser {
    private String nickName;
    private long uuid;
    private long avatar;

    public AwardUser(long uuid, String nickName, long avatar){
        this.uuid = uuid;
        this.nickName = nickName;
        this.avatar = avatar;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public long getAvatar() {
        return avatar;
    }

    public void setAvatar(long avatar) {
        this.avatar = avatar;
    }

    public long getUuid() {
        return uuid;
    }

    public void setUuid(long uuid) {
        this.uuid = uuid;
    }
}
