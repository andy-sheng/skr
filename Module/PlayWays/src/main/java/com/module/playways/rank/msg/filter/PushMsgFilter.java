package com.module.playways.rank.msg.filter;

import com.zq.live.proto.Room.ERoomMsgType;
import com.zq.live.proto.Room.RoomMsg;

import java.util.List;

/**
 * 过滤器,按需求添加
 */
public interface PushMsgFilter {

    boolean doFilter(RoomMsg msg);   // true 表示放行,false表示不放行

}
