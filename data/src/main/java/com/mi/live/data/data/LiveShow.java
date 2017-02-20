package com.mi.live.data.data;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.mi.live.data.api.JSONable;
import com.wali.live.proto.LiveShowProto;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by linjinbin on 16/2/19.
 * 直播秀列表的数据结构
 */
public class LiveShow implements Serializable, JSONable {
    private final static String TAG = "LiveShow";
    public static final String KEY_EXPOSE_LIVE_ID = "livepv_%s";
    public static final String KEY_CLICK_LIVE_ID = "liveclick_%s";
    static final long serialVersionUID = 4209360273858925922L;
    public static final int LIVETYPE_PUBLIC = 0;
    public static final int LIVETYPE_PRIVATE = 1;
    public static final int LIVETYPE_BACKPLAY = 2;
    public static final int LIVETYPE_PK = 3;
    /*口令直播*/
    public static final int LIVETYPE_TOKEN = 4;
    /*门票直播*/
    public static final int LIVETYPE_TICKET = 5;

    public static final int LIVETYPE_VR = 7;

    private String liveId;      //直播id

    private String location;    //地点
    private int viewerCnt;      //关注数
    private String url;         //直播地址
    public boolean hasReplayVideo;   //回放地址
    private String tagName;     //标签名字
    private UserShow userShow;  //userShow
    private long fromChannelId; //来源入口
    private int liveType = 0;   //直播类型 , 0公开  1私密 2回放 3pk 4口令
    private String liveTitle;   //直播标题
    public String shareUrl;     //分享地址

    private String exposeTag;   //曝光打点tag
    private boolean isReported; //是否曝光打点
    private String exposeKey;
    //注意：这个目前只用于liveshow直接跳转打点了，新频道模板使用schemeactivity，在跳转的地方打点
    private String clickKey;

    private String coverUrl;//直播封面
    private long startTime;

    /*pk*/
    private String pkLiveId;
    private UserShow pkUser;
    /**
     * 暂存一次直播/回放的密码
     */
    private String password;

    public int appType = -1; //0=小米直播app, 1=无人机, 2=导播台, 3=游戏, 4=一直播

    public LiveShow() {
        userShow = new UserShow();
    }

    public LiveShow(LiveShowProto.LiveShow protoLive) {
        parse(protoLive);
    }

    //建议大家的PB转换成展示模型类都统一写成模型类的loadFromPB的模式
    public static LiveShow loadFromPB(LiveShowProto.SingleBackShow live) {
        LiveShow show = new LiveShow();
        if (live != null) {
            LiveShowProto.UserShow user = live.getUser();
            LiveShowProto.BackInfo back = live.getBack();
            show.setUid(user.getUId());
            show.setNickname(user.getNickname());
            show.setAvatar(user.getAvatar());
            show.setLiveId(back.getBaId());
            show.setViewerCnt(back.getViewerCnt());
            show.setUrl(back.getUrl());
            show.setLiveTitle(back.getBaTitle());
            show.shareUrl = back.getShareUrl();
            show.setCoverUrl(back.getCoverUrl());
            show.setLiveType(LiveShow.LIVETYPE_BACKPLAY);
        }

        return show;
    }

    public void parse(LiveShowProto.LiveShow protoLive) {
        this.liveId = protoLive.getLiId();
        this.userShow = new UserShow(protoLive.getUser());
        this.liveType = protoLive.getLiType();
        this.location = protoLive.getLocation();
        this.viewerCnt = protoLive.getViewerCnt();
        this.url = protoLive.getUrl();
        this.liveTitle = protoLive.getLiTitle();
        shareUrl = protoLive.getShareUrl();
        coverUrl = protoLive.getCoverUrl();
        startTime = protoLive.getStartTime();

        exposeTag = protoLive.getTag();
        generateExpose();

        if (protoLive.getPkInfo() != null) {
            parse(protoLive.getPkInfo());
        }

        appType = protoLive.getAppType();
    }

    private void generateExpose() {
        if (userShow != null) {
            exposeKey = String.format(KEY_EXPOSE_LIVE_ID, exposeTag);
            clickKey = String.format(KEY_CLICK_LIVE_ID, exposeTag);
        }
    }

    public void parse(LiveShowProto.PKLive protoPkLive) {
        this.pkLiveId = protoPkLive.getLiveId();
        this.pkUser = new UserShow(protoPkLive.getUser());
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof LiveShow)) {
            MyLog.d("LiveShow", "LiveShow compare result different　object");
            return false;
        }
        LiveShow liveShow = (LiveShow) object;
        if (getUid() != liveShow.getUid()
                || !this.liveId.equalsIgnoreCase(liveShow.liveId)
                ) {
            MyLog.d("LiveShow", "LiveShow compare result different　LiveShow");
            return false;
        }
        return true;
    }

    public String getLiveId() {
        return liveId;
    }

    public void setLiveId(String liveId) {
        this.liveId = liveId;
    }

    public long getUid() {
        return this.userShow.uid;
    }

    public void setUid(long uid) {
        this.userShow.uid = uid;
    }

    public String getNickname() {
        return this.userShow.nickname;
    }

    public void setNickname(String nickname) {
        this.userShow.nickname = nickname;
    }

    public long getAvatar() {
        return this.userShow.avatar;
    }

    public void setAvatar(long avatar) {
        this.userShow.avatar = avatar;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getViewerCnt() {
        return viewerCnt;
    }

    public void setViewerCnt(int viewerCnt) {
        this.viewerCnt = viewerCnt;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagName() {
        return tagName;
    }

    public int getLevel() {
        return this.userShow.level;
    }

    public int getCertType() {
        return this.userShow.certType;
    }

    public int getLiveType() {
        return liveType;
    }

    public void setLiveType(int type) {
        liveType = type;
    }

    public void setLiveTitle(String title) {
        liveTitle = title;
    }

    public String getLiveTitle() {
        return liveTitle;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setUserShow(UserShow show) {
        if (show != null) {
            userShow = show;
        }
    }

    public UserShow getUserShow() {
        return userShow;
    }

    public void setFromChannelId(long id) {
        fromChannelId = id;
    }

    public long getFromChannelId() {
        return fromChannelId;
    }

    public void setShareUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            shareUrl = url;
        }
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public String getExposeTag() {
        return exposeTag;
    }

    public void setExposeTag(String tag) {
        this.exposeTag = tag;
        generateExpose();
    }

    public boolean isReported() {
        return isReported;
    }

    public void setReported(boolean reported) {
        isReported = reported;
    }

    public String getExposeKey() {
        return exposeKey;
    }

    public String getClickKey() {
        return clickKey;
    }

    public void setPkLiveId(String id) {
        if (!TextUtils.isEmpty(id)) {
            pkLiveId = id;
        }
    }

    public String getPkLiveId() {
        return pkLiveId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getPkNickname() {
        return pkUser != null ? pkUser.nickname : "";
    }

    public long getPkAvatarTs() {
        return pkUser != null ? pkUser.avatar : 0;
    }

    public long getPkUid() {
        return pkUser != null ? pkUser.uid : 0;
    }

    public void setPkUserShow(UserShow userShow) {
        if (userShow != null) {
            pkUser = userShow;
        }
    }

    public UserShow getPkUserShow() {
        return pkUser;
    }

    public BackShowListData toBackShowData() {
        BackShowListData backShowData = new BackShowListData();
        backShowData.uId = getUid();
        backShowData.userNickname = this.getNickname();
        backShowData.avatar = getAvatar();
        backShowData.baId = this.liveId;
        backShowData.viewerCnt = this.viewerCnt;
        backShowData.url = this.url;
        backShowData.startTime = 0;
        backShowData.endTime = 0;
        backShowData.liveTitle = this.liveTitle;
        backShowData.shareUrl = this.shareUrl;
        backShowData.startTime = this.startTime;
        backShowData.coverUrl = this.coverUrl;
        backShowData.addr = getLocation();
        return backShowData;
    }

    //JSONable interface funcs begins **********************************

    @Override
    public JSONObject serialToJSON() {
        JSONObject result = new JSONObject();

        try {
            result.put("liveshow_liveid", this.getLiveId());
            result.put("liveshow_location", this.getLocation());
            result.put("liveshow_viewercnt", this.getViewerCnt());
            result.put("liveshow_url", this.getUrl());
            result.put("liveshow_tagname", this.getTagName());
            if (getUserShow() != null) {
                UserShow userShow = this.getUserShow();
                JSONObject tmp = userShow.serialToJSON();
                if (tmp != null) {
                    result.put("liveshow_usershow", tmp);
                }
            }
            result.put("liveshow_fromchannelid", this.getFromChannelId());
            result.put("liveshow_livetype", this.getLiveType());
            result.put("liveshow_livetitle", this.getLiveTitle());
            result.put("liveshow_shareurl", this.getShareUrl());
            result.put("liveshow_starttime", this.getStartTime());
            result.put("liveshow_pkliveid", this.getPkLiveId());
            if (getPkUserShow() != null) {
                UserShow userShow = getPkUserShow();
                JSONObject tmp = userShow.serialToJSON();
                if (tmp != null) {
                    result.put("liveshow_pkusershow", tmp);
                }
            }
        } catch (JSONException e) {
            MyLog.e(TAG, e);
        }
        return result;
    }

    @Override
    public void serialFromJSON(JSONObject jsonObject) {
        if (jsonObject == null) {
            return;
        }

        try {
            this.setLiveId(jsonObject.optString("liveshow_liveid", ""));
            this.setLocation(jsonObject.optString("liveshow_location", ""));
            this.setViewerCnt(jsonObject.optInt("liveshow_viewercnt", 0));
            this.setUrl(jsonObject.optString("liveshow_url", ""));
            this.setTagName(jsonObject.optString("liveshow_tagname", ""));
            Object obj1 = jsonObject.get("liveshow_usershow");
            if (obj1 != null && obj1 instanceof JSONObject) {
                UserShow userShow = new UserShow();
                userShow.serialFromJSON((JSONObject) obj1);
                this.setUserShow(userShow);
            } else {
                this.setUserShow(null);
            }
            this.setFromChannelId(jsonObject.optLong("liveshow_fromchannelid", 0));
            this.setLiveType(jsonObject.getInt("liveshow_livetype"));
            this.setLiveTitle(jsonObject.optString("liveshow_livetitle", ""));
            this.setShareUrl(jsonObject.optString("liveshow_shareurl", ""));
            this.setStartTime(jsonObject.getLong("liveshow_starttime"));
            this.setPkLiveId(jsonObject.optString("liveshow_pkliveid", ""));
            Object obj2 = jsonObject.get("liveshow_pkusershow");
            if (obj2 != null && obj2 instanceof JSONObject) {
                UserShow userShow = new UserShow();
                userShow.serialFromJSON((JSONObject) obj2);
                this.setPkUserShow(userShow);
            } else {
                this.setPkUserShow(null);
            }
        } catch (JSONException e) {
            MyLog.e(TAG, e);
        }
    }

    //JSONable interface funcs ends ************************************

    public static class UserShow implements Serializable, JSONable {
        static final long serialVersionUID = 4209361273858985328L;
        public long uid;           //用户id
        public String nickname;    //昵称
        public long avatar;        //头像时间戳
        public int level;          //等级
        public int certType;       //认证

        public UserShow() {
        }

        public UserShow(LiveShowProto.UserShow protoUser) {
            this.uid = protoUser.getUId();
            this.nickname = protoUser.getNickname();
            this.avatar = protoUser.getAvatar();
            this.level = protoUser.getLevel();
            this.certType = protoUser.getCertType();
        }


        @Override
        public JSONObject serialToJSON() {
            JSONObject result = new JSONObject();

            try {
                result.put("usershow_uid", this.uid);
                result.put("usershow_nickname", this.nickname);
                result.put("usershow_avatar", this.avatar);
                result.put("usershow_level", this.level);
                result.put("usershow_certtype", this.certType);
            } catch (JSONException e) {
                MyLog.e(TAG, e);
            }

            return result;
        }

        @Override
        public void serialFromJSON(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }

            this.uid = jsonObject.optLong("usershow_uid", 0);
            this.nickname = jsonObject.optString("usershow_nickname", "");
            this.avatar = jsonObject.optLong("usershow_avatar", 0);
            this.level = jsonObject.optInt("usershow_level", 0);
            this.certType = jsonObject.optInt("usershow_certtype", 0);
        }
    }
}
