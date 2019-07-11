package com.module.playways.songmanager.presenter;

import com.common.mvp.RxLifeCyclePresenter;
import com.component.busilib.friends.SpecialModel;
import com.module.playways.songmanager.model.GrabRoomSongModel;

public abstract class BaseExitSongManagePresenter extends RxLifeCyclePresenter {
    public abstract void getPlayBookList();

    public void changeMusicTag(SpecialModel specialModel, int roomID) {
    }

    public abstract void deleteSong(GrabRoomSongModel grabRoomSongModel);

    public void getTagList() {
    }
}
