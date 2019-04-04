package com.module.playways.rank.prepare.view;


import com.module.playways.rank.msg.event.JoinActionEvent;

public interface IRankMatchingView {
    /**
     * 匹配成功
     */
    void matchRankSucess(JoinActionEvent event);
}
