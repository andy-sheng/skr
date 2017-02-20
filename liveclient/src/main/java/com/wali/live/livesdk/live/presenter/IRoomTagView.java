package com.wali.live.livesdk.live.presenter;


import com.wali.live.livesdk.live.viewmodel.RoomTag;

import java.util.List;

/**
 * Created by lan on 16/12/16.
 */
public interface IRoomTagView {
    void showTagList(List<RoomTag> roomTags, int type);
    void hideTag();
}
