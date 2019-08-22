package com.module.feeds.statistics

enum class FeedPage(val from:Int) {
    RECOMMEND(1),//推荐
    FOLLOW(2),//关注
    COLLECT(3),//收藏
    HOMEPAGE(4),// 个人中心
    SONG_ALBUM_OP(5),// 运营歌单
    SONG_ALBUM_RANK(6),// 排行榜歌单
    SONG_ALBUM_CHANLLENGE(7),// 打榜榜单

    // 从不同页面进详情页
    DETAIL_FROM_RECOMMEND(8),
    DETAIL_FROM_FOLLOW(9),
    DETAIL_FROM_COLLECT(10),
    DETAIL_FROM_HOMEPAGE(11),
    DETAIL_FROM_SONG_ALBUM_OP(12),
    DETAIL_FROM_SONG_ALBUM_RANK(13),
    DETAIL_FROM_SONG_ALBUM_CHANLLENGE(14)

}