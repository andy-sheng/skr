//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.utils;

import android.os.Handler;
import android.os.HandlerThread;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import io.rong.common.RLog;
import io.rong.imkit.RongContext;
import io.rong.imlib.model.MessageContent;

public class MessageProviderUserInfoHelper {
  private static final String TAG = "MessageProviderUserInfoHelper";
  private ConcurrentHashMap<MessageContent, List<String>> mMessageIdUserIdsMap = new ConcurrentHashMap();
  private static io.rong.imkit.utils.MessageProviderUserInfoHelper mHelper;
  ArrayList<String> cacheUserIds = new ArrayList();
  HandlerThread mWorkThread = new HandlerThread("MessageProviderUserInfoHelper");
  Handler mUserInfoHandler;

  public static io.rong.imkit.utils.MessageProviderUserInfoHelper getInstance() {
    if (mHelper == null) {
      Class var0 = io.rong.imkit.utils.MessageProviderUserInfoHelper.class;
      synchronized(io.rong.imkit.utils.MessageProviderUserInfoHelper.class) {
        if (mHelper == null) {
          mHelper = new io.rong.imkit.utils.MessageProviderUserInfoHelper();
        }
      }
    }

    return mHelper;
  }

  MessageProviderUserInfoHelper() {
    this.mWorkThread.start();
    this.mUserInfoHandler = new Handler(this.mWorkThread.getLooper());
  }

  synchronized void setCacheUserId(String userId) {
    if (!this.cacheUserIds.contains(userId)) {
      this.cacheUserIds.add(userId);
    }

  }

  synchronized void removeCacheUserId(String userId) {
    if (this.cacheUserIds.contains(userId)) {
      this.cacheUserIds.remove(userId);
    }

  }

  public synchronized boolean isCacheUserId(String userId) {
    return this.cacheUserIds.contains(userId);
  }

  public void registerMessageUserInfo(MessageContent message, String userId) {
    RLog.i("MessageProviderUserInfoHelper", "registerMessageUserInfo userId:" + userId);
    List<String> userIdList = (List)this.mMessageIdUserIdsMap.get(message);
    if (userIdList == null) {
      userIdList = new ArrayList();
      this.mMessageIdUserIdsMap.put(message, userIdList);
    }

    if (!((List)userIdList).contains(userId)) {
      ((List)userIdList).add(userId);
    }

    this.setCacheUserId(userId);
  }

  public void notifyMessageUpdate(final String userId) {
    Iterator messageUserIdsIterator = this.mMessageIdUserIdsMap.entrySet().iterator();
    this.mUserInfoHandler.postDelayed(new Runnable() {
      public void run() {
        io.rong.imkit.utils.MessageProviderUserInfoHelper.this.removeCacheUserId(userId);
      }
    }, 500L);

    while(messageUserIdsIterator.hasNext()) {
      Entry userIdMessageEntry = (Entry)messageUserIdsIterator.next();
      List<String> userIdList = (List)userIdMessageEntry.getValue();
      if (userIdList != null) {
        if (userIdList.contains(userId)) {
          userIdList.remove(userId);
        }

        if (userIdList.isEmpty()) {
          RongContext.getInstance().getEventBus().post(userIdMessageEntry.getKey());
          this.mMessageIdUserIdsMap.remove(userIdMessageEntry.getKey());
          RLog.d("MessageProviderUserInfoHelper", "notifyMessageUpdate --notify--" + userIdMessageEntry.getKey().toString());
        } else {
          RLog.d("MessageProviderUserInfoHelper", "notifyMessageUpdate --wait--" + userId);
        }
      }
    }

  }

  public boolean isRequestGetUserInfo() {
    return !this.mMessageIdUserIdsMap.isEmpty();
  }
}
