package com.wali.live.watchsdk.channel.viewmodel;

import java.util.Arrays;
import java.util.HashSet;

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
    public static final int TYPE_ONE_CARD = 10;             // 10:一行一列，不带，现在改成和28样式一致
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
    public static final int TYPE_TOPIC_GRID = 27;           // 27.广场标签，话题结构
    public static final int TYPE_GAME_CARD = 28;            // 28.游戏频道，内嵌头像样式

    public static final int TYPE_PLACEHOLDER_RANK = 29;     //顶部 视图样式 （社区主播势力日榜）
    public static final int TYPE_FOUR_LINE_NAVIGATION = 30; // 30.圈子新增 标签类型 多行四列，icon 和 文本
    public static final int TYPE_THREE_CARD_LEFT_BIG = 31;  // 31.三个元素组成，左大图，右两小图
    public static final int TYPE_THREE_CARD_RIGHT_BIG = 32; // 32.三个元素组成，左两小图，右大图
    public static final int TYPE_FLOAT_HEADER = 33;         // 33.一行一列，header嵌在封面左上角，其他与19类似
    public static final int TYPE_HEAD_LARGE_CARD = 34;      // 34. 二级页顶部的一张大图，中间有两行文字
    public static final int TYPE_VARIABLE_LENGTH_TAG = 35;  // 35.标签 长度随内容变化， 可以折叠

    public static final int TYPE_TWO_LONG_COVER = 38;        // 38.一行两列，4比3比例的视频封面图
    public static final int TYPE_PAGE_HEADER = 39;           // 39.频道页头图

    public static final int TYPE_PLACEHOLDER = 42;

    public static final int TYPE_LIVE_GROUP = 45;       // 45.直播组
    public static final int TYPE_LIVE_OR_LIVE_GROUP = 48; //48.一行两列，直播间和直播间组的随意组合，四种情况

    public static final HashSet<Integer> ALL_CHANNEL_UI_TYPE =
            new HashSet<>(Arrays.asList(
                    TYPE_MAX_FIVE_CIRCLE,
                    TYPE_THREE_PIC,
                    TYPE_FIVE_CIRCLE,
                    TYPE_TWO_LAYER,
                    TYPE_THREE_CIRCLE,
                    TYPE_BANNER,
                    TYPE_TWO_CARD,
                    TYPE_SCROLL_CARD,
                    TYPE_SCROLL_CIRCLE,
                    TYPE_ONE_CARD,
                    TYPE_ONE_CARD_DEFAULT,
                    TYPE_SIX_CARD,
                    TYPE_CONCERN_CARD,
                    TYPE_SPLIT_LINE,
                    TYPE_THREE_CARD,
                    TYPE_ONE_LIST,
                    TYPE_ONE_LIVE_LIST,
                    TYPE_TWO_WIDE_CARD,
                    TYPE_LARGE_CARD,
                    TYPE_NAVIGATE,
                    TYPE_THREE_NEW,
                    TYPE_VIDEO_BANNER,
                    // TYPE_NOTICE_SCROLL,
                    TYPE_THREE_CONCERN_CARD,
                    TYPE_EFFECT_CARD,
                    TYPE_THREE_INNER_CARD,
                    TYPE_TOPIC_GRID,
                    TYPE_GAME_CARD,
                    // TYPE_PLACEHOLDER_RANK,
                    TYPE_FOUR_LINE_NAVIGATION,
                    TYPE_THREE_CARD_LEFT_BIG,
                    TYPE_THREE_CARD_RIGHT_BIG,
                    TYPE_FLOAT_HEADER,
                    TYPE_HEAD_LARGE_CARD,
                    TYPE_VARIABLE_LENGTH_TAG,
                    TYPE_TWO_LONG_COVER,
                    TYPE_PAGE_HEADER,
                    TYPE_PLACEHOLDER,
                    TYPE_LIVE_GROUP,
                    TYPE_LIVE_OR_LIVE_GROUP
            ));
}
