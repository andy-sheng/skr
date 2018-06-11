package com.wali.live.watchsdk.channel.list.model;

import com.wali.live.proto.LiveShowProto;

public class ChannelShow {
    long channelId;
    String channelName;
    boolean hasChild;
    int uiType;//客户端UI展示类型, 默认=1,分类=2,三列=3,二列=4,动态页样式:5,网页:6
    boolean autoFresh;
    long freshInterval;
    String url;//类型是6的时候必须
    int flag;//控制位,第1位表示是否有搜索框
    String iconUrl;//频道图标地址[子频道]
    ChannelStyle channelStyle;//频道主题风格

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public boolean isHasChild() {
        return hasChild;
    }

    public void setHasChild(boolean hasChild) {
        this.hasChild = hasChild;
    }

    public int getUiType() {
        return uiType;
    }

    public void setUiType(int uiType) {
        this.uiType = uiType;
    }

    public boolean isAutoFresh() {
        return autoFresh;
    }

    public void setAutoFresh(boolean autoFresh) {
        this.autoFresh = autoFresh;
    }

    public long getFreshInterval() {
        return freshInterval;
    }

    public void setFreshInterval(long freshInterval) {
        this.freshInterval = freshInterval;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public ChannelStyle getChannelStyle() {
        return channelStyle;
    }

    public void setChannelStyle(ChannelStyle channelStyle) {
        this.channelStyle = channelStyle;
    }

    public static ChannelShow parseFromPb(LiveShowProto.ChannelShow cs) {
        ChannelShow channelShow = new ChannelShow();
        channelShow.setChannelId(cs.getCId());
        channelShow.setChannelName(cs.getCName());
        channelShow.setHasChild(cs.getHasChild());
        channelShow.setUiType(cs.getUiType());
        channelShow.setAutoFresh(cs.getFresh().getIsAuto());
        channelShow.setFreshInterval(cs.getFresh().getInterval());
        channelShow.setUrl(cs.getUrl());
        channelShow.setFlag(cs.getFlag());
        channelShow.setIconUrl(cs.getIconUrl());

        if (cs.hasStyle()) {
            ChannelStyle style = new ChannelStyle();
            style.setBgColor(cs.getStyle().getBgColor());
            style.setHignLightColor(cs.getStyle().getHighlightColor());
            style.setNormalColor(cs.getStyle().getNormalColor());
            style.setStatusBarColor(cs.getStyle().getStatusBarColor());
            channelShow.setChannelStyle(style);
        }

        return channelShow;
    }

    static class ChannelStyle {
        String bgColor;//导航栏底色，十六进制颜色码, 如 #FFFFFF = 白色
        String hignLightColor;//频道高亮选中的颜色，十六进制颜色码, 如 #FFFFFF = 白色
        String normalColor;//频道正常的颜色，十六进制颜色码, 如 #FFFFFF = 白色
        String statusBarColor;//系统状态栏字体颜色，十六进制颜色码, 如 #FFFFFF = 白色

        public String getBgColor() {
            return bgColor;
        }

        public void setBgColor(String bgColor) {
            this.bgColor = bgColor;
        }

        public String getHignLightColor() {
            return hignLightColor;
        }

        public void setHignLightColor(String hignLightColor) {
            this.hignLightColor = hignLightColor;
        }

        public String getNormalColor() {
            return normalColor;
        }

        public void setNormalColor(String normalColor) {
            this.normalColor = normalColor;
        }

        public String getStatusBarColor() {
            return statusBarColor;
        }

        public void setStatusBarColor(String statusBarColor) {
            this.statusBarColor = statusBarColor;
        }
    }

}
