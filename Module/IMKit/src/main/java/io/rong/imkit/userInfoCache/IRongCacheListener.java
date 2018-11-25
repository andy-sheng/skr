//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.userInfoCache;

import io.rong.imkit.model.GroupUserInfo;
import io.rong.imlib.model.Discussion;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.PublicServiceProfile;
import io.rong.imlib.model.UserInfo;

public interface IRongCacheListener {
  void onUserInfoUpdated(UserInfo var1);

  void onGroupUserInfoUpdated(GroupUserInfo var1);

  void onGroupUpdated(Group var1);

  void onDiscussionUpdated(Discussion var1);

  void onPublicServiceProfileUpdated(PublicServiceProfile var1);

  UserInfo getUserInfo(String var1);

  GroupUserInfo getGroupUserInfo(String var1, String var2);

  Group getGroupInfo(String var1);
}
