package com.module.playways.room.msg.filter;

/**
 * 过滤器,按需求添加
 */
public interface PushMsgFilter<M> {

    boolean doFilter(M msg);   // true 表示放行,false表示不放行

}
