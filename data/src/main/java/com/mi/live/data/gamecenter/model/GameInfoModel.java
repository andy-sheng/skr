package com.mi.live.data.gamecenter.model;

import com.wali.live.proto.GameCenterProto;

import java.util.ArrayList;
import java.util.List;

public class GameInfoModel {

    long gameId; // 游戏id
    String gameName; // 游戏名称
    String iconUrl; // 游戏图标
    long fileSize; //游戏大小
    int gameType; //游戏类型
    int rank; //游戏排名
    double score; //游戏评分
    long followNum; //关注人数
    boolean isFollow; //是否关注
    String packageName; //包名
    String packageUrl; // 下载地址
    long packageSize; // 包大小
    String introTitle; //游戏简介标题
    String intro; //游戏简介

    Developer mDeveloper = null; // 开发商

    List<GameVideo> mGameVideoList = new ArrayList<>(); // 游戏视频
    List<ScreenShot> mScreenShotList = new ArrayList<>(); // 游戏截图
    List<GameTag> mGameTagList = new ArrayList<>(); // 游戏标签


    public long getGameId() {
        return gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public long getFileSize() {
        return fileSize;
    }

    public int getGameType() {
        return gameType;
    }

    public int getRank() {
        return rank;
    }

    public double getScore() {
        return score;
    }

    public long getFollowNum() {
        return followNum;
    }

    public boolean isFollow() {
        return isFollow;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getPackageUrl() {
        return packageUrl;
    }

    public long getPackageSize() {
        return packageSize;
    }

    public String getIntroTitle() {
        return introTitle;
    }

    public String getIntro() {
        return intro;
    }

    public Developer getDeveloper() {
        return mDeveloper;
    }

    public List<GameVideo> getGameVideoList() {
        return mGameVideoList;
    }

    public List<ScreenShot> getScreenShotList() {
        return mScreenShotList;
    }

    public List<GameTag> getGameTagList() {
        return mGameTagList;
    }

    public void parse(GameCenterProto.GameInfo gameInfo) {
        if (gameInfo != null) {
            GameCenterProto.GameBaseInfo gameBaseInfo = gameInfo.getGameBaseInfo();
            if (gameBaseInfo != null) {
                gameId = gameBaseInfo.getGameId();
                gameName = gameBaseInfo.getGameName();
                iconUrl = gameBaseInfo.getIconUrl();
                fileSize = gameBaseInfo.getFileSize();
                gameType = gameBaseInfo.getGameType();
                rank = gameBaseInfo.getRank();
                score = gameBaseInfo.getScore();
                followNum = gameBaseInfo.getFollowNum();
                isFollow = gameBaseInfo.getIsFollow();
                packageName = gameBaseInfo.getPackageName();
                packageUrl = gameBaseInfo.getPackageUrl();
                packageSize = gameBaseInfo.getPackageSize();
                introTitle = gameBaseInfo.getIntroTitle();
                intro = gameBaseInfo.getIntro();
            }

            GameCenterProto.Developer developer = gameInfo.getDeveloper();
            if (developer != null) {
                mDeveloper = new Developer();
                mDeveloper.developerId = developer.getDeveloperId();
                mDeveloper.developerName = developer.getDeveloperName();
                mDeveloper.iconUrl = developer.getIconUrl();
            }

            List<GameCenterProto.GameVideo> videoList = gameInfo.getGameVideosList();
            for (GameCenterProto.GameVideo gm : videoList) {
                GameVideo gameVideo = new GameVideo();
                gameVideo.parse(gm);
                mGameVideoList.add(gameVideo);
            }

            List<GameCenterProto.ScreenShot> screenShotsList = gameInfo.getScreenShotsList();
            for (GameCenterProto.ScreenShot ss : screenShotsList) {
                ScreenShot screenShot = new ScreenShot();
                screenShot.parse(ss);
                mScreenShotList.add(screenShot);
            }

            List<GameCenterProto.GameTag> gameTagList = gameInfo.getGameTagsList();
            for (GameCenterProto.GameTag gt : gameTagList) {
                GameTag gameTag = new GameTag();
                gameTag.parse(gt);
                mGameTagList.add(gameTag);
            }
        }
    }


    public static class GameVideo {
        List<VideoBaseInfo> videoInfoList = new ArrayList<>();

        String screenUrl; //封面
        int duration; //时长
        long playCnt; //播放数

        public void parse(GameCenterProto.GameVideo gameVideo) {
            screenUrl = gameVideo.getScreenUrl();
            duration = gameVideo.getDuration();
            playCnt = gameVideo.getPlayCnt();
            for (GameCenterProto.VideoBaseInfo vbi : gameVideo.getVideoBaseInfoList()) {
                VideoBaseInfo videoBaseInfo = new VideoBaseInfo();
                videoBaseInfo.parse(vbi);
                videoInfoList.add(videoBaseInfo);
            }
        }

        public String getScreenUrl() {
            return screenUrl;
        }

        public int getDuration() {
            return duration;
        }

        public long getPlayCnt() {
            return playCnt;
        }

        public static class VideoBaseInfo {
            int width;
            int height;
            String videoUrl;
            long videoSize;

            public void parse(GameCenterProto.VideoBaseInfo vbi) {
                width = vbi.getWidth();
                height = vbi.getHeight();
                videoUrl = vbi.getVideoUrl();
                videoSize = vbi.getVideoSize();
            }
        }
    }

    public static class ScreenShot {
        String picId;
        String picUrl;
        int picType; //扩展留用

        public void parse(GameCenterProto.ScreenShot screenShot) {
            picId = screenShot.getPicId();
            picUrl = screenShot.getPicUrl();
            picType = screenShot.getPicType();
        }

        public String getPicId() {
            return picId;
        }

        public String getPicUrl() {
            return picUrl;
        }

        public int getPicType() {
            return picType;
        }
    }

    public static class GameTag {
        int tagId;
        String tagName;
        int tagType; //扩展留用
        String actUrl;

        public void parse(GameCenterProto.GameTag gameTag) {
            tagId = gameTag.getTagId();
            tagName = gameTag.getTagName();
            tagType = gameTag.getTagType();
            actUrl = gameTag.getActUrl();
        }

        public int getTagId() {
            return tagId;
        }

        public String getTagName() {
            return tagName;
        }

        public int getTagType() {
            return tagType;
        }

        public String getActUrl() {
            return actUrl;
        }
    }


    public static class Developer {
        int developerId;
        String developerName;
        int iconUrl;

        public int getDeveloperId() {
            return developerId;
        }

        public String getDeveloperName() {
            return developerName;
        }

        public int getIconUrl() {
            return iconUrl;
        }
    }
}
