//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.manager;

import android.net.Uri;

public interface IAudioPlayListener {
  void onStart(Uri var1);

  void onStop(Uri var1);

  void onComplete(Uri var1);
}
