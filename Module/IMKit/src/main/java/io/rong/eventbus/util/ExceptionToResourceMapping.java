//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.eventbus.util;

import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import io.rong.eventbus.EventBus;

public class ExceptionToResourceMapping {
  public final Map<Class<? extends Throwable>, Integer> throwableToMsgIdMap = new HashMap();

  public ExceptionToResourceMapping() {
  }

  public Integer mapThrowable(Throwable throwable) {
    Throwable throwableToCheck = throwable;
    int depthToGo = 20;

    do {
      Integer resId = this.mapThrowableFlat(throwableToCheck);
      if (resId != null) {
        return resId;
      }

      throwableToCheck = throwableToCheck.getCause();
      --depthToGo;
    } while(depthToGo > 0 && throwableToCheck != throwable && throwableToCheck != null);

    Log.d(EventBus.TAG, "No specific message ressource ID found for " + throwable);
    return null;
  }

  protected Integer mapThrowableFlat(Throwable throwable) {
    Class<? extends Throwable> throwableClass = throwable.getClass();
    Integer resId = (Integer)this.throwableToMsgIdMap.get(throwableClass);
    if (resId == null) {
      Class<? extends Throwable> closestClass = null;
      Set<Entry<Class<? extends Throwable>, Integer>> mappings = this.throwableToMsgIdMap.entrySet();
      Iterator var6 = mappings.iterator();

      while(true) {
        Entry mapping;
        Class candidate;
        do {
          do {
            if (!var6.hasNext()) {
              return resId;
            }

            mapping = (Entry)var6.next();
            candidate = (Class)mapping.getKey();
          } while(!candidate.isAssignableFrom(throwableClass));
        } while(closestClass != null && !closestClass.isAssignableFrom(candidate));

        closestClass = candidate;
        resId = (Integer)mapping.getValue();
      }
    } else {
      return resId;
    }
  }

  public io.rong.eventbus.util.ExceptionToResourceMapping addMapping(Class<? extends Throwable> clazz, int msgId) {
    this.throwableToMsgIdMap.put(clazz, msgId);
    return this;
  }
}
