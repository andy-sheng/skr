package com.component.busilib.friends;


import com.common.core.userinfo.model.UserInfoModel;

import java.io.Serializable;
import java.util.List;


public class RecommendModel implements Serializable {
    public static final int TYPE_FRIEND_ROOM = 1;
    public static final int TYPE_RECOMMEND_ROOM = 2;
    public static final int TYPE_FOLLOW_ROOM = 3;
    /**
     * roomInfo : {"inPlayersNum":0,"isOwner":true,"roomID":0,"roomTag":"URT_UNKNOWN","roomType":"RT_UNKNOWN","tagID":0,"totalPlayersNum":0,"userID":0}
     * tagInfo : {"bgColor":"string","introduction":"string","tagID":0,"tagName":"string"}
     * userInfo : {"avatar":"string","nickname":"string","sex":"unknown","userID":0}
     * category : 1
     */

    private SimpleRoomInfo roomInfo;
    private SpecialModel tagInfo;
    private UserInfoModel userInfo;
    private int category;
    private String displayName;
    private String displayURL;
    private String displayAvatar;
    List<PlayUser> playUsers;

    public List<PlayUser> getPlayUsers() {
        return playUsers;
    }

    public void setPlayUsers(List<PlayUser> playUsers) {
        this.playUsers = playUsers;
    }

    public SimpleRoomInfo getRoomInfo() {
        return roomInfo;
    }

    public void setRoomInfo(SimpleRoomInfo roomInfo) {
        this.roomInfo = roomInfo;
    }

    public SpecialModel getTagInfo() {
        return tagInfo;
    }

    public void setTagInfo(SpecialModel tagInfo) {
        this.tagInfo = tagInfo;
    }

    public UserInfoModel getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfoModel userInfo) {
        this.userInfo = userInfo;
    }


    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }


    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayURL() {
        return displayURL;
    }

    public void setDisplayURL(String displayURL) {
        this.displayURL = displayURL;
    }

    public String getDisplayAvatar() {
        return displayAvatar;
    }

    public void setDisplayAvatar(String displayAvatar) {
        this.displayAvatar = displayAvatar;
    }

    @Override
    public String toString() {
        return "RecommendModel{" +
                "roomInfo=" + roomInfo +
                ", tagInfo=" + tagInfo +
                ", userInfo=" + userInfo +
                ", category=" + category +
                ", displayName='" + displayName + '\'' +
                ", displayURL='" + displayURL + '\'' +
                ", displayAvatar='" + displayAvatar + '\'' +
                '}';
    }

    public static class PlayUser implements Serializable {

        /**
         * userID : 2193839
         * isOnline : true
         * userInfo : {"userID":2193839,"nickName":"大娃🙃","avatar":"http://res-static.inframe.mobi/pictures/2193839/3CF44EEE-4BC4-44BA-B812-213AECEB2DE8.jpg","sex":1,"description":"","isSystem":false,"mainLevel":0}
         * isSkrer : false
         * role : 1
         */

        private int userID;
        private boolean isOnline;
        private UserInfoBean userInfo;
        private boolean isSkrer;
        private int role;

        public int getUserID() {
            return userID;
        }

        public void setUserID(int userID) {
            this.userID = userID;
        }

        public boolean isIsOnline() {
            return isOnline;
        }

        public void setIsOnline(boolean isOnline) {
            this.isOnline = isOnline;
        }

        public UserInfoBean getUserInfo() {
            return userInfo;
        }

        public void setUserInfo(UserInfoBean userInfo) {
            this.userInfo = userInfo;
        }

        public boolean isIsSkrer() {
            return isSkrer;
        }

        public void setIsSkrer(boolean isSkrer) {
            this.isSkrer = isSkrer;
        }

        public int getRole() {
            return role;
        }

        public void setRole(int role) {
            this.role = role;
        }

        public static class UserInfoBean implements Serializable {
            /**
             * userID : 2193839
             * nickName : 大娃🙃
             * avatar : http://res-static.inframe.mobi/pictures/2193839/3CF44EEE-4BC4-44BA-B812-213AECEB2DE8.jpg
             * sex : 1
             * description :
             * isSystem : false
             * mainLevel : 0
             */

            private int userID;
            private String nickName;
            private String avatar;
            private int sex;
            private String description;
            private boolean isSystem;
            private int mainLevel;

            public int getUserID() {
                return userID;
            }

            public void setUserID(int userID) {
                this.userID = userID;
            }

            public String getNickName() {
                return nickName;
            }

            public void setNickName(String nickName) {
                this.nickName = nickName;
            }

            public String getAvatar() {
                return avatar;
            }

            public void setAvatar(String avatar) {
                this.avatar = avatar;
            }

            public int getSex() {
                return sex;
            }

            public void setSex(int sex) {
                this.sex = sex;
            }

            public String getDescription() {
                return description;
            }

            public void setDescription(String description) {
                this.description = description;
            }

            public boolean isIsSystem() {
                return isSystem;
            }

            public void setIsSystem(boolean isSystem) {
                this.isSystem = isSystem;
            }

            public int getMainLevel() {
                return mainLevel;
            }

            public void setMainLevel(int mainLevel) {
                this.mainLevel = mainLevel;
            }
        }
    }

}
