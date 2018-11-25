//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.core.assist;

import android.widget.ImageView;

public enum ViewScaleType {
  FIT_INSIDE,
  CROP;

  private ViewScaleType() {
  }

  public static io.rong.imageloader.core.assist.ViewScaleType fromImageView(ImageView imageView) {
    switch(imageView.getScaleType()) {
      case FIT_CENTER:
      case FIT_XY:
      case FIT_START:
      case FIT_END:
      case CENTER_INSIDE:
        return FIT_INSIDE;
      case MATRIX:
      case CENTER:
      case CENTER_CROP:
      default:
        return CROP;
    }
  }
}
