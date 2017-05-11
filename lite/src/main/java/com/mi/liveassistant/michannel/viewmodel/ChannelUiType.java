package com.mi.liveassistant.michannel.viewmodel;

/**
 * Created by lan on 16/8/2.
 *
 * @module 频道
 * @description 频道模板ui类型
 */
public class ChannelUiType {
    /*与服务器模板保持一致*/
    public static final int TYPE_MAX_FIVE_CIRCLE = 1;       // 1:第一种模版，包含四个元素排在一行，每个元素有名字，按钮图，跳转的uri->扩展成最多显示5个的模板
    public static final int TYPE_THREE_PIC = 2;             // 2:一行三个元素，图片是大图
    public static final int TYPE_FIVE_CIRCLE = 3;           // 3:一行五个圆圈元素的模版
    public static final int TYPE_TWO_LAYER = 4;             // 4:两行，第一行两个元素，第二行一个元素
    public static final int TYPE_THREE_CIRCLE = 5;          // 5:一行，三个圆圈，中间的大
    public static final int TYPE_BANNER = 6;                // 6:banner
    public static final int TYPE_TWO_CARD = 7;              // 7:一行两列
    // 下面两个滚动的视图类型改为固定的视图
    public static final int TYPE_SCROLL_CARD = 8;           // 8:一行多列，方图显示
    public static final int TYPE_SCROLL_CIRCLE = 9;         // 9:一行多列，圆图显示
    public static final int TYPE_ONE_CARD = 10;             // 10:一行一列，不带
    public static final int TYPE_ONE_CARD_DEFAULT = 11;     // 11:一行一列，默认类型，带头像栏目区域
    public static final int TYPE_SIX_CARD = 12;             // 11:3*3，六个元素，一大五小，含标题，更多
    public static final int TYPE_CONCERN_CARD = 13;         // 13:一行一列，显示头像，关注关系
    public static final int TYPE_SPLIT_LINE = 14;           // 14:分割线
    public static final int TYPE_THREE_CARD = 15;           // 15:三张图，第一段文字一行显示
    public static final int TYPE_ONE_LIST = 16;             // 16:一行一列，左图右文，带头像
    public static final int TYPE_ONE_LIVE_LIST = 17;        // 17:一行一列，左图右文，一行配文，一行展示观看人次+ 发布时间
    public static final int TYPE_TWO_WIDE_CARD = 18;        // 18:一行两列，上图下文，图为宽图
    public static final int TYPE_LARGE_CARD = 19;           // 19:一行一列，一行配文嵌入图片，右上角显示直播状态
    public static final int TYPE_NAVIGATE = 20;             // 20:导航栏
    public static final int TYPE_THREE_NEW = 21;            // 21:一行三列，最新内嵌样式
    public static final int TYPE_VIDEO_BANNER = 22;         // 22:一行一列大图，大图通栏，一行文字内嵌，直播视频播放
    public static final int TYPE_NOTICE_SCROLL = 23;        // 23:横条滚动预告
    public static final int TYPE_THREE_CONCERN_CARD = 24;   // 24.一行三列，包含粉丝数，显示关注按钮
    public static final int TYPE_EFFECT_CARD = 25;          // 25.带有滚动效果的头条模板
    public static final int TYPE_THREE_INNER_CARD = 26;     // 26.一行三列，图下显示2行文本，与15的区别是文案内嵌
    public static final int TYPE_TOPIC_GRID = 27;           // 27.广场标签，三列结构

    public static final int TYPE_PLACEHOLDER = 28;

}
