//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.core.listener;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import io.rong.imageloader.core.ImageLoader;

public class PauseOnScrollListener implements OnScrollListener {
  private ImageLoader imageLoader;
  private final boolean pauseOnScroll;
  private final boolean pauseOnFling;
  private final OnScrollListener externalListener;

  public PauseOnScrollListener(ImageLoader imageLoader, boolean pauseOnScroll, boolean pauseOnFling) {
    this(imageLoader, pauseOnScroll, pauseOnFling, (OnScrollListener)null);
  }

  public PauseOnScrollListener(ImageLoader imageLoader, boolean pauseOnScroll, boolean pauseOnFling, OnScrollListener customListener) {
    this.imageLoader = imageLoader;
    this.pauseOnScroll = pauseOnScroll;
    this.pauseOnFling = pauseOnFling;
    this.externalListener = customListener;
  }

  public void onScrollStateChanged(AbsListView view, int scrollState) {
    switch(scrollState) {
      case 0:
        this.imageLoader.resume();
        break;
      case 1:
        if (this.pauseOnScroll) {
          this.imageLoader.pause();
        }
        break;
      case 2:
        if (this.pauseOnFling) {
          this.imageLoader.pause();
        }
    }

    if (this.externalListener != null) {
      this.externalListener.onScrollStateChanged(view, scrollState);
    }

  }

  public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    if (this.externalListener != null) {
      this.externalListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
    }

  }
}
