package com.module.playways.rank.prepare.view;


import com.module.playways.rank.prepare.model.GrabCurGameStateModel;
import com.module.playways.rank.prepare.model.MatchIconModel;

import java.util.List;

public interface IGrabMatchingView {
    /**
     * 匹配成功
     */
    void matchSucess(GrabCurGameStateModel t);
}
