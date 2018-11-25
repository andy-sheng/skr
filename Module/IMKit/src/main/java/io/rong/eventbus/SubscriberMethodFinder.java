//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.eventbus;

import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.rong.eventbus.EventBus;
import io.rong.eventbus.EventBusException;
import io.rong.eventbus.ThreadMode;

class SubscriberMethodFinder {
  private static final String ON_EVENT_METHOD_NAME = "onEvent";
  private static final int BRIDGE = 64;
  private static final int SYNTHETIC = 4096;
  private static final int MODIFIERS_IGNORE = 5192;
  private static final Map<String, List<SubscriberMethod>> methodCache = new HashMap();
  private final Map<Class<?>, Class<?>> skipMethodVerificationForClasses = new ConcurrentHashMap();

  SubscriberMethodFinder(List<Class<?>> skipMethodVerificationForClassesList) {
    if (skipMethodVerificationForClassesList != null) {
      Iterator var2 = skipMethodVerificationForClassesList.iterator();

      while(var2.hasNext()) {
        Class<?> clazz = (Class)var2.next();
        this.skipMethodVerificationForClasses.put(clazz, clazz);
      }
    }

  }

  List<SubscriberMethod> findSubscriberMethods(Class<?> subscriberClass) {
    String key = subscriberClass.getName();
    Map var4 = methodCache;
    List<SubscriberMethod> subscriberMethods;
    synchronized(methodCache) {
      subscriberMethods = (List)methodCache.get(key);
    }

    if (subscriberMethods != null) {
      return subscriberMethods;
    } else {
      subscriberMethods = new ArrayList();
      Class<?> clazz = subscriberClass;
      HashSet<String> eventTypesFound = new HashSet();

      for(StringBuilder methodKeyBuilder = new StringBuilder(); clazz != null; clazz = clazz.getSuperclass()) {
        String name = clazz.getName();
        if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("android.")) {
          break;
        }

        Method[] methods = clazz.getDeclaredMethods();
        Method[] var9 = methods;
        int var10 = methods.length;

        for(int var11 = 0; var11 < var10; ++var11) {
          Method method = var9[var11];
          String methodName = method.getName();
          if (methodName.startsWith("onEvent")) {
            int modifiers = method.getModifiers();
            if ((modifiers & 1) != 0 && (modifiers & 5192) == 0) {
              Class<?>[] parameterTypes = method.getParameterTypes();
              if (parameterTypes.length == 1) {
                String modifierString = methodName.substring("onEvent".length());
                ThreadMode threadMode;
                if (modifierString.length() == 0) {
                  threadMode = ThreadMode.PostThread;
                } else if (modifierString.equals("MainThread")) {
                  threadMode = ThreadMode.MainThread;
                } else if (modifierString.equals("BackgroundThread")) {
                  threadMode = ThreadMode.BackgroundThread;
                } else {
                  if (!modifierString.equals("Async")) {
                    if (!this.skipMethodVerificationForClasses.containsKey(clazz)) {
                      throw new EventBusException("Illegal onEvent method, check for typos: " + method);
                    }
                    continue;
                  }

                  threadMode = ThreadMode.Async;
                }

                Class<?> eventType = parameterTypes[0];
                methodKeyBuilder.setLength(0);
                methodKeyBuilder.append(methodName);
                methodKeyBuilder.append('>').append(eventType.getName());
                String methodKey = methodKeyBuilder.toString();
                if (eventTypesFound.add(methodKey)) {
                  subscriberMethods.add(new SubscriberMethod(method, threadMode, eventType));
                }
              }
            } else if (!this.skipMethodVerificationForClasses.containsKey(clazz)) {
              Log.d(EventBus.TAG, "Skipping method (not public, static or abstract): " + clazz + "." + methodName);
            }
          }
        }
      }

      if (subscriberMethods.isEmpty()) {
        throw new EventBusException("Subscriber " + subscriberClass + " has no public methods called " + "onEvent");
      } else {
        Map var25 = methodCache;
        synchronized(methodCache) {
          methodCache.put(key, subscriberMethods);
          return subscriberMethods;
        }
      }
    }
  }

  static void clearCaches() {
    Map var0 = methodCache;
    synchronized(methodCache) {
      methodCache.clear();
    }
  }
}
