//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

import io.rong.imkit.utilities.LangUtils;

public class RongBaseNoActionbarActivity extends FragmentActivity {
  public RongBaseNoActionbarActivity() {
  }

  protected void attachBaseContext(Context newBase) {
    Context context = LangUtils.getConfigurationContext(newBase);
    super.attachBaseContext(context);
  }
}
