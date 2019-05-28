//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.plugin.location;

import io.rong.imlib.model.UserInfo;

public interface IUserInfoProvider {
    void getUserInfo(String var1, io.rong.imkit.plugin.location.IUserInfoProvider.UserInfoCallback var2);

    public interface UserInfoCallback {
        void onGotUserInfo(UserInfo var1);
    }
}
