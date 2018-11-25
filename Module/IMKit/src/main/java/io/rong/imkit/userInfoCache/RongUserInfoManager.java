//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.userInfoCache;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;

import java.io.File;
import java.util.List;

import io.rong.common.RLog;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.cache.RongCache;
import io.rong.imkit.model.GroupUserInfo;
import io.rong.imkit.userInfoCache.IRongCacheListener;
import io.rong.imkit.userInfoCache.RongConversationInfo;
import io.rong.imkit.utils.StringUtils;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.ResultCallback;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Conversation.PublicServiceType;
import io.rong.imlib.model.Discussion;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.PublicServiceProfile;
import io.rong.imlib.model.UserInfo;

public class RongUserInfoManager implements Callback {
  private static final String TAG = "RongUserInfoManager";
  private static final int USER_CACHE_MAX_COUNT = 256;
  private static final int PUBLIC_ACCOUNT_CACHE_MAX_COUNT = 64;
  private static final int GROUP_CACHE_MAX_COUNT = 128;
  private static final int DISCUSSION_CACHE_MAX_COUNT = 16;
  private static final int EVENT_GET_USER_INFO = 2;
  private static final int EVENT_GET_GROUP_INFO = 3;
  private static final int EVENT_GET_GROUP_USER_INFO = 4;
  private static final int EVENT_GET_DISCUSSION = 5;
  private static final int EVENT_UPDATE_USER_INFO = 7;
  private static final int EVENT_UPDATE_GROUP_USER_INFO = 8;
  private static final int EVENT_UPDATE_GROUP_INFO = 9;
  private static final int EVENT_UPDATE_DISCUSSION = 10;
  private static final int EVENT_LOGOUT = 11;
  private static final int EVENT_CLEAR_CACHE = 12;
  private static final String GROUP_PREFIX = "groups";
  private RongDatabaseDao mRongDatabaseDao;
  private RongCache<String, UserInfo> mUserInfoCache;
  private RongCache<String, GroupUserInfo> mGroupUserInfoCache;
  private RongCache<String, RongConversationInfo> mGroupCache;
  private RongCache<String, RongConversationInfo> mDiscussionCache;
  private RongCache<String, PublicServiceProfile> mPublicServiceProfileCache;
  private RongCache<String, String> mRequestCache;
  private IRongCacheListener mCacheListener;
  private boolean mIsCacheUserInfo;
  private boolean mIsCacheGroupInfo;
  private boolean mIsCacheGroupUserInfo;
  private Handler mWorkHandler;
  private String mAppKey;
  private String mUserId;
  private boolean mInitialized;
  private Context mContext;

  private RongUserInfoManager() {
    this.mIsCacheUserInfo = true;
    this.mIsCacheGroupInfo = true;
    this.mIsCacheGroupUserInfo = true;
    this.mUserInfoCache = new RongCache(256);
    this.mGroupUserInfoCache = new RongCache(256);
    this.mGroupCache = new RongCache(128);
    this.mDiscussionCache = new RongCache(16);
    this.mRequestCache = new RongCache(64);
    this.mPublicServiceProfileCache = new RongCache(64);
    HandlerThread workThread = new HandlerThread("RongUserInfoManager");
    workThread.start();
    this.mWorkHandler = new Handler(workThread.getLooper(), this);
    this.mInitialized = false;
  }

  public void setIsCacheUserInfo(boolean mIsCacheUserInfo) {
    this.mIsCacheUserInfo = mIsCacheUserInfo;
  }

  public void setIsCacheGroupInfo(boolean mIsCacheGroupInfo) {
    this.mIsCacheGroupInfo = mIsCacheGroupInfo;
  }

  public void setIsCacheGroupUserInfo(boolean mIsCacheGroupUserInfo) {
    this.mIsCacheGroupUserInfo = mIsCacheGroupUserInfo;
  }

  public static io.rong.imkit.userInfoCache.RongUserInfoManager getInstance() {
    return io.rong.imkit.userInfoCache.RongUserInfoManager.SingletonHolder.sInstance;
  }

  public boolean handleMessage(Message msg) {
    if (TextUtils.isEmpty(this.mUserId)) {
      if (TextUtils.isEmpty(RongIMClient.getInstance().getCurrentUserId())) {
        RLog.i("RongUserInfoManager", "user hasn't connected, return directly!");
        return true;
      }

      this.mUserId = RongIMClient.getInstance().getCurrentUserId();
      RLog.i("RongUserInfoManager", "userId:" + this.mUserId);
      this.mRongDatabaseDao = new RongDatabaseDao();
      this.mRongDatabaseDao.open(this.mContext, this.mAppKey, this.mUserId);
    } else if (!this.mUserId.equals(RongIMClient.getInstance().getCurrentUserId())) {
      this.clearUserInfoCache();
      RLog.d("RongUserInfoManager", "user changed, old userId = " + this.mUserId + ", current userId = " + RongIMClient.getInstance().getCurrentUserId());
      this.mUserId = RongIMClient.getInstance().getCurrentUserId();
      if (this.mRongDatabaseDao != null) {
        this.mRongDatabaseDao.close();
        this.mRongDatabaseDao.open(this.mContext, this.mAppKey, this.mUserId);
      }
    }

    String userId;
    String groupId;
    Group group;
    GroupUserInfo groupUserInfo;
    Discussion discussion;
    RongConversationInfo conversationInfo;
    RongConversationInfo oldConversationInfo;
    UserInfo userInfo;
    switch(msg.what) {
      case 2:
        userId = (String)msg.obj;
        userInfo = null;
        if (this.mRongDatabaseDao != null) {
          userInfo = this.mRongDatabaseDao.getUserInfo(userId);
        }

        if (userInfo != null && userInfo.getPortraitUri() != null) {
          Uri uri = userInfo.getPortraitUri();
          if (uri.toString().toLowerCase().startsWith("file://")) {
            File file = new File(uri.toString().substring(7));
            if (!file.exists()) {
              userInfo = null;
            }
          } else if (uri.toString().equals("")) {
            userInfo = null;
          }
        }

        if (userInfo == null) {
          if (this.mCacheListener != null) {
            userInfo = this.mCacheListener.getUserInfo(userId);
          }

          if (userInfo != null) {
            this.putUserInfoInDB(userInfo);
          }
        }

        if (userInfo != null) {
          this.putUserInfoInCache(userInfo);
          this.mRequestCache.remove(userId);
          if (this.mCacheListener != null) {
            this.mCacheListener.onUserInfoUpdated(userInfo);
          }
        }
        break;
      case 3:
        groupId = (String)msg.obj;
        group = null;
        String cacheGroupId = "groups" + groupId;
        if (this.mRongDatabaseDao != null) {
          group = this.mRongDatabaseDao.getGroupInfo(groupId);
        }

        if (group != null && group.getPortraitUri() != null) {
          Uri uri = group.getPortraitUri();
          if (uri.toString().toLowerCase().startsWith("file://")) {
            File file = new File(uri.toString().substring(7));
            if (!file.exists()) {
              group = null;
            }
          } else if (uri.toString().equals("")) {
            group = null;
          }
        }

        if (group == null) {
          if (this.mCacheListener != null) {
            group = this.mCacheListener.getGroupInfo(groupId);
          }

          if (group != null && this.mRongDatabaseDao != null) {
            this.mRongDatabaseDao.putGroupInfo(group);
          }
        }

        if (group != null) {
          conversationInfo = new RongConversationInfo(ConversationType.GROUP.getValue() + "", group.getId(), group.getName(), group.getPortraitUri());
          this.mGroupCache.put(groupId, conversationInfo);
          this.mRequestCache.remove(cacheGroupId);
          if (this.mCacheListener != null) {
            this.mCacheListener.onGroupUpdated(group);
          }
        }
        break;
      case 4:
        groupUserInfo = null;
        groupId = StringUtils.getArg1((String)msg.obj);
        userId = StringUtils.getArg2((String)msg.obj);
        if (this.mRongDatabaseDao != null) {
          groupUserInfo = this.mRongDatabaseDao.getGroupUserInfo(groupId, userId);
        }

        if (groupUserInfo == null) {
          if (this.mCacheListener != null) {
            groupUserInfo = this.mCacheListener.getGroupUserInfo(groupId, userId);
          }

          if (groupUserInfo != null && this.mRongDatabaseDao != null) {
            this.mRongDatabaseDao.putGroupUserInfo(groupUserInfo);
          }
        }

        if (groupUserInfo != null) {
          this.mGroupUserInfoCache.put((String)msg.obj, groupUserInfo);
          this.mRequestCache.remove((String)msg.obj);
          if (this.mCacheListener != null) {
            this.mCacheListener.onGroupUserInfoUpdated(groupUserInfo);
          }
        }
        break;
      case 5:
        final String discussionId = (String)msg.obj;
        discussion = null;
        if (this.mRongDatabaseDao != null) {
          discussion = this.mRongDatabaseDao.getDiscussionInfo(discussionId);
        }

        if (discussion != null) {
          conversationInfo = new RongConversationInfo(ConversationType.DISCUSSION.getValue() + "", discussion.getId(), discussion.getName(), (Uri)null);
          this.mDiscussionCache.put(discussionId, conversationInfo);
          if (this.mCacheListener != null) {
            this.mCacheListener.onDiscussionUpdated(discussion);
          }
        } else {
          RongIM.getInstance().getDiscussion(discussionId, new ResultCallback<Discussion>() {
            public void onSuccess(Discussion discussion) {
              if (discussion != null) {
                if (io.rong.imkit.userInfoCache.RongUserInfoManager.this.mRongDatabaseDao != null) {
                  io.rong.imkit.userInfoCache.RongUserInfoManager.this.mRongDatabaseDao.putDiscussionInfo(discussion);
                }

                RongConversationInfo conversationInfo = new RongConversationInfo(ConversationType.DISCUSSION.getValue() + "", discussion.getId(), discussion.getName(), (Uri)null);
                io.rong.imkit.userInfoCache.RongUserInfoManager.this.mDiscussionCache.put(discussionId, conversationInfo);
                if (io.rong.imkit.userInfoCache.RongUserInfoManager.this.mCacheListener != null) {
                  io.rong.imkit.userInfoCache.RongUserInfoManager.this.mCacheListener.onDiscussionUpdated(discussion);
                }
              }

            }

            public void onError(ErrorCode e) {
            }
          });
        }
      case 6:
      default:
        break;
      case 7:
        userInfo = (UserInfo)msg.obj;
        UserInfo oldUserInfo = this.putUserInfoInCache(userInfo);
        if (oldUserInfo == null || oldUserInfo.getName() == null || oldUserInfo.getPortraitUri() == null || userInfo.getName() != null || userInfo.getPortraitUri() != null) {
          this.putUserInfoInDB(userInfo);
          this.mRequestCache.remove(userInfo.getUserId());
          if (this.mCacheListener != null) {
            this.mCacheListener.onUserInfoUpdated(userInfo);
          }
        }
        break;
      case 8:
        groupUserInfo = (GroupUserInfo)msg.obj;
        String key = StringUtils.getKey(groupUserInfo.getGroupId(), groupUserInfo.getUserId());
        GroupUserInfo oldGroupUserInfo = (GroupUserInfo)this.mGroupUserInfoCache.put(key, groupUserInfo);
        if (oldGroupUserInfo == null || oldGroupUserInfo.getNickname() != null && groupUserInfo.getNickname() != null && !oldGroupUserInfo.getNickname().equals(groupUserInfo.getNickname())) {
          this.mRequestCache.remove(key);
          if (this.mRongDatabaseDao != null) {
            this.mRongDatabaseDao.putGroupUserInfo(groupUserInfo);
          }

          if (this.mCacheListener != null) {
            this.mCacheListener.onGroupUserInfoUpdated(groupUserInfo);
          }
        }
        break;
      case 9:
        group = (Group)msg.obj;
        conversationInfo = new RongConversationInfo(ConversationType.GROUP.getValue() + "", group.getId(), group.getName(), group.getPortraitUri());
        oldConversationInfo = (RongConversationInfo)this.mGroupCache.put(conversationInfo.getId(), conversationInfo);
        if (oldConversationInfo == null || oldConversationInfo.getName() == null || oldConversationInfo.getUri() == null || conversationInfo.getName() != null || conversationInfo.getUri() != null) {
          String cachedGroupId = "groups" + group.getId();
          this.mRequestCache.remove(cachedGroupId);
          if (this.mRongDatabaseDao != null) {
            this.mRongDatabaseDao.putGroupInfo(group);
          }

          if (this.mCacheListener != null) {
            this.mCacheListener.onGroupUpdated(group);
          }
        }
        break;
      case 10:
        discussion = (Discussion)msg.obj;
        conversationInfo = new RongConversationInfo(ConversationType.DISCUSSION.getValue() + "", discussion.getId(), discussion.getName(), (Uri)null);
        oldConversationInfo = (RongConversationInfo)this.mDiscussionCache.put(conversationInfo.getId(), conversationInfo);
        if (oldConversationInfo == null || oldConversationInfo.getName() != null && conversationInfo.getName() != null && !oldConversationInfo.getName().equals(conversationInfo.getName())) {
          if (this.mRongDatabaseDao != null) {
            this.mRongDatabaseDao.putDiscussionInfo(discussion);
          }

          if (this.mCacheListener != null) {
            this.mCacheListener.onDiscussionUpdated(discussion);
          }
        }
        break;
      case 11:
        this.clearUserInfoCache();
        this.mInitialized = false;
        this.mUserId = null;
        this.mAppKey = null;
        if (this.mRongDatabaseDao != null) {
          this.mRongDatabaseDao.close();
          this.mRongDatabaseDao = null;
        }
        break;
      case 12:
        this.mRequestCache.clear();
    }

    return false;
  }

  public void init(Context context, String appKey, IRongCacheListener listener) {
    if (TextUtils.isEmpty(appKey)) {
      RLog.e("RongUserInfoManager", "init, appkey is null.");
    } else if (this.mInitialized) {
      RLog.d("RongUserInfoManager", "has been init, no need init again");
    } else {
      this.mContext = context;
      this.mAppKey = appKey;
      this.mCacheListener = listener;
      this.mInitialized = true;
    }
  }

  private void clearUserInfoCache() {
    if (this.mUserInfoCache != null) {
      this.mUserInfoCache.clear();
    }

    if (this.mDiscussionCache != null) {
      this.mDiscussionCache.clear();
    }

    if (this.mGroupCache != null) {
      this.mGroupCache.clear();
    }

    if (this.mGroupUserInfoCache != null) {
      this.mGroupUserInfoCache.clear();
    }

    if (this.mPublicServiceProfileCache != null) {
      this.mPublicServiceProfileCache.clear();
    }

    this.mRequestCache.clear();
  }

  public void uninit() {
    RLog.i("RongUserInfoManager", "uninit");
    this.mWorkHandler.sendEmptyMessage(11);
  }

  private UserInfo putUserInfoInCache(UserInfo info) {
    return this.mUserInfoCache != null ? (UserInfo)this.mUserInfoCache.put(info.getUserId(), info) : null;
  }

  private void insertUserInfoInDB(UserInfo info) {
    if (this.mRongDatabaseDao != null) {
      this.mRongDatabaseDao.insertUserInfo(info);
    }

  }

  private void putUserInfoInDB(UserInfo info) {
    if (this.mRongDatabaseDao != null) {
      this.mRongDatabaseDao.putUserInfo(info);
    }

  }

  public UserInfo getUserInfo(String id) {
    RLog.i("RongUserInfoManager", "getUserInfo : " + id);
    if (TextUtils.isEmpty(id)) {
      return null;
    } else {
      UserInfo info = null;
      if (this.mIsCacheUserInfo) {
        info = (UserInfo)this.mUserInfoCache.get(id);
        if (info == null) {
          String cachedId = (String)this.mRequestCache.get(id);
          if (cachedId != null) {
            return null;
          }

          this.mRequestCache.put(id, id);
          Message message = Message.obtain();
          message.what = 2;
          message.obj = id;
          this.mWorkHandler.sendMessage(message);
          if (!this.mWorkHandler.hasMessages(12)) {
            this.mWorkHandler.sendEmptyMessageDelayed(12, 30000L);
          }
        }
      } else if (this.mCacheListener != null) {
        info = this.mCacheListener.getUserInfo(id);
      }

      return info;
    }
  }

  public List<UserInfo> getAllUserInfo() {
    RLog.i("RongUserInfoManager", "getAllUserInfo");
    if (this.mRongDatabaseDao != null) {
      return this.mRongDatabaseDao.getAllUserInfo();
    } else {
      RLog.i("RongUserInfoManager", "mRongDatabaseDao is null");
      return null;
    }
  }

  public GroupUserInfo getGroupUserInfo(String gId, String id) {
    if (!TextUtils.isEmpty(gId) && !TextUtils.isEmpty(id)) {
      RLog.d("RongUserInfoManager", "getGroupUserInfo : " + gId + ", " + id);
      String key = StringUtils.getKey(gId, id);
      GroupUserInfo info = null;
      if (this.mIsCacheGroupUserInfo) {
        info = (GroupUserInfo)this.mGroupUserInfoCache.get(key);
        if (info == null) {
          String cachedId = (String)this.mRequestCache.get(key);
          if (cachedId != null) {
            return null;
          }

          this.mRequestCache.put(key, key);
          Message message = Message.obtain();
          message.what = 4;
          message.obj = key;
          this.mWorkHandler.sendMessage(message);
          if (!this.mWorkHandler.hasMessages(12)) {
            this.mWorkHandler.sendEmptyMessageDelayed(12, 30000L);
          }
        }
      } else if (this.mCacheListener != null) {
        info = this.mCacheListener.getGroupUserInfo(gId, id);
      }

      return info;
    } else {
      return null;
    }
  }

  public Group getGroupInfo(String id) {
    if (TextUtils.isEmpty(id)) {
      return null;
    } else {
      RLog.i("RongUserInfoManager", "getGroupInfo : " + id);
      Group groupInfo = null;
      if (this.mIsCacheGroupInfo) {
        RongConversationInfo info = (RongConversationInfo)this.mGroupCache.get(id);
        if (info == null) {
          String cachedId = (String)this.mRequestCache.get(id);
          if (cachedId != null) {
            return null;
          }

          this.mRequestCache.put(id, id);
          Message message = Message.obtain();
          message.what = 3;
          message.obj = id;
          this.mWorkHandler.sendMessage(message);
          if (!this.mWorkHandler.hasMessages(12)) {
            this.mWorkHandler.sendEmptyMessageDelayed(12, 30000L);
          }
        } else {
          groupInfo = new Group(info.getId(), info.getName(), info.getUri());
        }
      } else if (this.mCacheListener != null) {
        groupInfo = this.mCacheListener.getGroupInfo(id);
      }

      return groupInfo;
    }
  }

  public Discussion getDiscussionInfo(String id) {
    if (TextUtils.isEmpty(id)) {
      return null;
    } else {
      Discussion discussionInfo = null;
      RongConversationInfo info = (RongConversationInfo)this.mDiscussionCache.get(id);
      if (info == null) {
        Message message = Message.obtain();
        message.what = 5;
        message.obj = id;
        this.mWorkHandler.sendMessage(message);
      } else {
        discussionInfo = new Discussion(info.getId(), info.getName());
      }

      return discussionInfo;
    }
  }

  public PublicServiceProfile getPublicServiceProfile(final PublicServiceType type, final String id) {
    if (type != null && !TextUtils.isEmpty(id)) {
      final String key = StringUtils.getKey(type.getValue() + "", id);
      PublicServiceProfile info = (PublicServiceProfile)this.mPublicServiceProfileCache.get(key);
      if (info == null) {
        this.mWorkHandler.post(new Runnable() {
          public void run() {
            if (RongContext.getInstance() != null && RongContext.getInstance().getPublicServiceProfileProvider() != null) {
              PublicServiceProfile result = RongContext.getInstance().getPublicServiceProfileProvider().getPublicServiceProfile(type, id);
              if (result != null) {
                io.rong.imkit.userInfoCache.RongUserInfoManager.this.mPublicServiceProfileCache.put(key, result);
                if (io.rong.imkit.userInfoCache.RongUserInfoManager.this.mCacheListener != null) {
                  io.rong.imkit.userInfoCache.RongUserInfoManager.this.mCacheListener.onPublicServiceProfileUpdated(result);
                }
              }
            } else {
              RongIM.getInstance().getPublicServiceProfile(type, id, new ResultCallback<PublicServiceProfile>() {
                public void onSuccess(PublicServiceProfile result) {
                  if (result != null) {
                    io.rong.imkit.userInfoCache.RongUserInfoManager.this.mPublicServiceProfileCache.put(key, result);
                    if (io.rong.imkit.userInfoCache.RongUserInfoManager.this.mCacheListener != null) {
                      io.rong.imkit.userInfoCache.RongUserInfoManager.this.mCacheListener.onPublicServiceProfileUpdated(result);
                    }
                  }

                }

                public void onError(ErrorCode e) {
                }
              });
            }

          }
        });
      }

      return info;
    } else {
      return null;
    }
  }

  public void setUserInfo(UserInfo info) {
    if (this.mIsCacheUserInfo) {
      Message message = Message.obtain();
      message.what = 7;
      message.obj = info;
      this.mWorkHandler.sendMessage(message);
    } else if (this.mCacheListener != null) {
      this.mCacheListener.onUserInfoUpdated(info);
    }

  }

  public void setGroupUserInfo(GroupUserInfo info) {
    if (this.mIsCacheGroupUserInfo) {
      Message message = Message.obtain();
      message.what = 8;
      message.obj = info;
      this.mWorkHandler.sendMessage(message);
    } else if (this.mCacheListener != null) {
      this.mCacheListener.onGroupUserInfoUpdated(info);
    }

  }

  public void setGroupInfo(Group group) {
    if (this.mIsCacheGroupInfo) {
      Message message = Message.obtain();
      message.what = 9;
      message.obj = group;
      this.mWorkHandler.sendMessage(message);
    } else if (this.mCacheListener != null) {
      this.mCacheListener.onGroupUpdated(group);
    }

  }

  public void setDiscussionInfo(Discussion discussion) {
    Message message = Message.obtain();
    message.what = 10;
    message.obj = discussion;
    this.mWorkHandler.sendMessage(message);
  }

  public void setPublicServiceProfile(PublicServiceProfile profile) {
    String key = StringUtils.getKey(profile.getConversationType().getValue() + "", profile.getTargetId());
    PublicServiceProfile oldInfo = (PublicServiceProfile)this.mPublicServiceProfileCache.put(key, profile);
    if ((oldInfo == null || oldInfo.getName() != null && profile.getName() != null && !oldInfo.getName().equals(profile.getName()) || oldInfo.getPortraitUri() != null && profile.getPortraitUri() != null && !oldInfo.getPortraitUri().toString().equals(profile.getPortraitUri().toString())) && this.mCacheListener != null) {
      this.mCacheListener.onPublicServiceProfileUpdated(profile);
    }

  }

  private static class SingletonHolder {
    static io.rong.imkit.userInfoCache.RongUserInfoManager sInstance = new io.rong.imkit.userInfoCache.RongUserInfoManager();

    private SingletonHolder() {
    }
  }
}
