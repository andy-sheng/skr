//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit;

import io.rong.imkit.RongContext;
import io.rong.imkit.model.GroupUserInfo;
import io.rong.imkit.userInfoCache.IRongCacheListener;
import io.rong.imlib.model.Discussion;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.PublicServiceProfile;
import io.rong.imlib.model.UserInfo;

public class RongUserCacheListener implements IRongCacheListener {
  public RongUserCacheListener() {
  }

  public void onUserInfoUpdated(UserInfo info) {
    if (info != null) {
      RongContext.getInstance().getEventBus().post(info);
    }

  }

  public void onGroupUserInfoUpdated(GroupUserInfo info) {
    if (info != null) {
      RongContext.getInstance().getEventBus().post(info);
    }

  }

  public void onGroupUpdated(Group group) {
    if (group != null) {
      RongContext.getInstance().getEventBus().post(group);
    }

  }

  public void onDiscussionUpdated(Discussion discussion) {
    if (discussion != null) {
      RongContext.getInstance().getEventBus().post(discussion);
    }

  }

  public void onPublicServiceProfileUpdated(PublicServiceProfile profile) {
    if (profile != null) {
      RongContext.getInstance().getEventBus().post(profile);
    }

  }

  public UserInfo getUserInfo(String id) {
    return RongContext.getInstance().getUserInfoProvider() != null ? RongContext.getInstance().getUserInfoProvider().getUserInfo(id) : null;
  }

  public GroupUserInfo getGroupUserInfo(String group, String id) {
    return RongContext.getInstance().getGroupUserInfoProvider() != null ? RongContext.getInstance().getGroupUserInfoProvider().getGroupUserInfo(group, id) : null;
  }

  public Group getGroupInfo(String id) {
    return RongContext.getInstance().getGroupInfoProvider() != null ? RongContext.getInstance().getGroupInfoProvider().getGroupInfo(id) : null;
  }
}
