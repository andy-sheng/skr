package com.module.playways.room.msg.filter;

import com.zq.live.proto.Room.RoomMsg;

/**
 * 过滤器,按需求添加
 */
public interface PushMsgFilter<T> {

    boolean doFilter(T msg);   // true 表示放行,false表示不放行

}
