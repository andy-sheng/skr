//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.photoview;

import android.annotation.TargetApi;
import android.os.Build.VERSION;
import android.view.View;

public class Compat {
  private static final int SIXTY_FPS_INTERVAL = 16;

  public Compat() {
  }

  public static void postOnAnimation(View view, Runnable runnable) {
    if (VERSION.SDK_INT >= 16) {
      postOnAnimationJellyBean(view, runnable);
    } else {
      view.postDelayed(runnable, 16L);
    }

  }

  @TargetApi(16)
  private static void postOnAnimationJellyBean(View view, Runnable runnable) {
    view.postOnAnimation(runnable);
  }

  public static int getPointerIndex(int action) {
    return VERSION.SDK_INT >= 11 ? getPointerIndexHoneyComb(action) : getPointerIndexEclair(action);
  }

  @TargetApi(5)
  private static int getPointerIndexEclair(int action) {
    return (action & '\uff00') >> 8;
  }

  @TargetApi(11)
  private static int getPointerIndexHoneyComb(int action) {
    return (action & '\uff00') >> 8;
  }
}
