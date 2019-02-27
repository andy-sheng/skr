package com.module.playways.rank.prepare.view;


import com.module.playways.rank.msg.event.JoinActionEvent;
import com.module.playways.rank.prepare.model.MatchIconModel;
import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.module.playways.rank.song.model.SongModel;

import java.util.List;

public interface IMatchingView {
    /**
     * 匹配成功
     */
    void matchSucess(JoinActionEvent event);
}
