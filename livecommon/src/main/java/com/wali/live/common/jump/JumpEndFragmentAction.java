package com.wali.live.common.jump;

import android.support.v4.app.FragmentActivity;

import com.mi.live.data.user.User;

/**
 * Created by chengsimin on 16/9/18.
 */
public interface JumpEndFragmentAction {
    void  showEndLiveFragment(FragmentActivity activity, int layoutId, long ownerId, String roomId, long avatarTs, User owner, int viewer, int liveType);
}
