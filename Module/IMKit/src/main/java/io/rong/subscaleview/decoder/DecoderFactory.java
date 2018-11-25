//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.subscaleview.decoder;

import java.lang.reflect.InvocationTargetException;

public interface DecoderFactory<T> {
  T make() throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException;
}
