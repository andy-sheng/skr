//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.model;

public class GroupUserInfo {
    private String nickname;
    private String userId;
    private String groupId;

    public GroupUserInfo(String groupId, String userId, String nickname) {
        this.groupId = groupId;
        this.nickname = nickname;
        this.userId = userId;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getNickname() {
        return this.nickname;
    }

    public String getUserId() {
        return this.userId;
    }
}
