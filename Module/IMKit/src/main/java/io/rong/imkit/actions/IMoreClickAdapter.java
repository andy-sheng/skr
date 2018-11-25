//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.actions;

import android.support.v4.app.Fragment;
import android.view.ViewGroup;

import java.util.List;

import io.rong.imkit.actions.IClickActions;

public interface IMoreClickAdapter {
  void bindView(ViewGroup var1, Fragment var2, List<IClickActions> var3);

  void hideMoreActionLayout();

  void setMoreActionEnable(boolean var1);

  boolean isMoreActionShown();
}
