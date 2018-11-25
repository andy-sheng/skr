//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.core;

import android.graphics.Bitmap;
import android.os.Handler;

import io.rong.imageloader.core.assist.LoadedFrom;
import io.rong.imageloader.core.process.BitmapProcessor;
import io.rong.imageloader.utils.L;

final class ProcessAndDisplayImageTask implements Runnable {
  private static final String LOG_POSTPROCESS_IMAGE = "PostProcess image before displaying [%s]";
  private final io.rong.imageloader.core.ImageLoaderEngine engine;
  private final Bitmap bitmap;
  private final io.rong.imageloader.core.ImageLoadingInfo imageLoadingInfo;
  private final Handler handler;

  public ProcessAndDisplayImageTask(io.rong.imageloader.core.ImageLoaderEngine engine, Bitmap bitmap, io.rong.imageloader.core.ImageLoadingInfo imageLoadingInfo, Handler handler) {
    this.engine = engine;
    this.bitmap = bitmap;
    this.imageLoadingInfo = imageLoadingInfo;
    this.handler = handler;
  }

  public void run() {
    L.d("PostProcess image before displaying [%s]", new Object[]{this.imageLoadingInfo.memoryCacheKey});
    BitmapProcessor processor = this.imageLoadingInfo.options.getPostProcessor();
    Bitmap processedBitmap = processor.process(this.bitmap);
    DisplayBitmapTask displayBitmapTask = new DisplayBitmapTask(processedBitmap, this.imageLoadingInfo, this.engine, LoadedFrom.MEMORY_CACHE);
    LoadAndDisplayImageTask.runTask(displayBitmapTask, this.imageLoadingInfo.options.isSyncLoading(), this.handler, this.engine);
  }
}
