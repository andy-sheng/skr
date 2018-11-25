//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.subscaleview.decoder;

import android.graphics.Bitmap.Config;
import android.support.annotation.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import io.rong.subscaleview.decoder.DecoderFactory;

public class CompatDecoderFactory<T> implements DecoderFactory<T> {
  private final Class<? extends T> clazz;
  private final Config bitmapConfig;

  public CompatDecoderFactory(@NonNull Class<? extends T> clazz) {
    this(clazz, (Config)null);
  }

  public CompatDecoderFactory(@NonNull Class<? extends T> clazz, Config bitmapConfig) {
    this.clazz = clazz;
    this.bitmapConfig = bitmapConfig;
  }

  public T make() throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
    if (this.bitmapConfig == null) {
      return this.clazz.newInstance();
    } else {
      Constructor<? extends T> ctor = this.clazz.getConstructor(Config.class);
      return ctor.newInstance(this.bitmapConfig);
    }
  }
}
