//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.userInfoCache;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import io.rong.common.RLog;
import io.rong.imkit.model.GroupUserInfo;
import io.rong.imlib.model.Discussion;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.UserInfo;

class RongDatabaseDao {
  private static final String TAG = "RongDatabaseDao";
  private RongUserCacheDatabaseHelper rongUserCacheDatabaseHelper;
  private SQLiteDatabase db;
  private final String usersTable = "users";
  private final String groupUsersTable = "group_users";
  private final String groupsTable = "groups";
  private final String discussionsTable = "discussions";

  RongDatabaseDao() {
  }

  void open(Context context, String appKey, String currentUserId) {
    RongUserCacheDatabaseHelper.setDbPath(context, appKey, currentUserId);

    try {
      this.rongUserCacheDatabaseHelper = new RongUserCacheDatabaseHelper(context);
      this.db = this.rongUserCacheDatabaseHelper.getWritableDatabase();
    } catch (SQLiteException var5) {
      RLog.e("RongDatabaseDao", "SQLiteException occur");
      var5.printStackTrace();
    }

  }

  void close() {
    if (this.db != null) {
      this.db.close();
      this.db = null;
    }

  }

  protected void finalize() throws Throwable {
    if (this.db != null) {
      this.db.close();
    }

    super.finalize();
  }

  UserInfo getUserInfo(String userId) {
    if (userId == null) {
      RLog.w("RongDatabaseDao", "getUserInfo userId is invalid");
      return null;
    } else if (this.db == null) {
      RLog.w("RongDatabaseDao", "getUserInfo db is invalid");
      return null;
    } else {
      Cursor c = null;
      UserInfo info = null;

      try {
        c = this.db.query("users", (String[])null, "id = ?", new String[]{userId}, (String)null, (String)null, (String)null);
        if (c != null && c.moveToFirst()) {
          String id = c.getString(c.getColumnIndex("id"));
          String name = c.getString(c.getColumnIndex("name"));
          String portrait = c.getString(c.getColumnIndex("portrait"));
          info = new UserInfo(id, name, Uri.parse(portrait));
        }
      } finally {
        if (c != null) {
          c.close();
        }

      }

      return info;
    }
  }

  List<UserInfo> getAllUserInfo() {
    if (this.db == null) {
      RLog.w("RongDatabaseDao", "getUserInfo db is invalid");
      return null;
    } else {
      Cursor c = null;
      ArrayList userInfoList = new ArrayList();

      try {
        c = this.db.query("users", (String[])null, (String)null, (String[])null, (String)null, (String)null, (String)null);
        if (c != null) {
          while(c.moveToNext()) {
            String id = c.getString(c.getColumnIndex("id"));
            String name = c.getString(c.getColumnIndex("name"));
            String portrait = c.getString(c.getColumnIndex("portrait"));
            UserInfo info = new UserInfo(id, name, Uri.parse(portrait));
            userInfoList.add(info);
          }
        }
      } finally {
        if (c != null) {
          c.close();
        }

      }

      return userInfoList;
    }
  }

  synchronized void insertUserInfo(UserInfo userInfo) {
    if (userInfo != null && userInfo.getUserId() != null) {
      if (this.db == null) {
        RLog.w("RongDatabaseDao", "insertUserInfo db is invalid");
      } else {
        ContentValues cv = new ContentValues();
        cv.put("id", userInfo.getUserId());
        cv.put("name", userInfo.getName());
        cv.put("portrait", userInfo.getPortraitUri() + "");
        this.db.insert("users", (String)null, cv);
      }
    } else {
      RLog.w("RongDatabaseDao", "insertUserInfo userId is invalid");
    }
  }

  synchronized void updateUserInfo(UserInfo userInfo) {
    if (userInfo != null && userInfo.getUserId() != null) {
      if (this.db == null) {
        RLog.w("RongDatabaseDao", "updateUserInfo db is invalid");
      } else {
        ContentValues cv = new ContentValues();
        cv.put("id", userInfo.getUserId());
        cv.put("name", userInfo.getName());
        cv.put("portrait", userInfo.getPortraitUri() + "");
        this.db.update("users", cv, "id = ?", new String[]{userInfo.getUserId()});
      }
    } else {
      RLog.w("RongDatabaseDao", "updateUserInfo userId is invalid");
    }
  }

  synchronized void putUserInfo(UserInfo userInfo) {
    if (userInfo != null && !TextUtils.isEmpty(userInfo.getUserId())) {
      if (this.db == null) {
        RLog.w("RongDatabaseDao", "putUserInfo db is invalid");
      } else {
        try {
          this.db.execSQL("replace into users (id, name, portrait) values (?, ?, ?)", new String[]{userInfo.getUserId(), userInfo.getName(), userInfo.getPortraitUri() + ""});
        } catch (SQLException var3) {
          RLog.e("RongDatabaseDao", "putUserInfo DB if full");
        }

      }
    } else {
      RLog.w("RongDatabaseDao", "putUserInfo userId is invalid");
    }
  }

  GroupUserInfo getGroupUserInfo(String groupId, String userId) {
    if (userId != null && groupId != null) {
      if (this.db == null) {
        RLog.w("RongDatabaseDao", "getGroupUserInfo db is invalid");
        return null;
      } else {
        Cursor c = null;
        GroupUserInfo info = null;

        try {
          c = this.db.query("group_users", (String[])null, "group_id = ? and user_id = ?", new String[]{groupId, userId}, (String)null, (String)null, (String)null);
          if (c != null && c.moveToFirst()) {
            String gId = c.getString(c.getColumnIndex("group_id"));
            String uId = c.getString(c.getColumnIndex("user_id"));
            String nickname = c.getString(c.getColumnIndex("nickname"));
            info = new GroupUserInfo(gId, uId, nickname);
          }
        } finally {
          if (c != null) {
            c.close();
          }

        }

        return info;
      }
    } else {
      RLog.w("RongDatabaseDao", "getGroupUserInfo parameter is invalid");
      return null;
    }
  }

  synchronized void insertGroupUserInfo(GroupUserInfo userInfo) {
    if (userInfo != null && userInfo.getGroupId() != null && userInfo.getUserId() != null) {
      if (this.db == null) {
        RLog.w("RongDatabaseDao", "insertGroupUserInfo db is invalid");
      } else {
        ContentValues cv = new ContentValues();
        cv.put("group_id", userInfo.getGroupId());
        cv.put("user_id", userInfo.getUserId());
        cv.put("nickname", userInfo.getNickname());
        this.db.insert("group_users", (String)null, cv);
      }
    } else {
      RLog.w("RongDatabaseDao", "insertGroupUserInfo parameter is invalid");
    }
  }

  synchronized void updateGroupUserInfo(GroupUserInfo userInfo) {
    if (userInfo != null && userInfo.getGroupId() != null && userInfo.getUserId() != null) {
      if (this.db == null) {
        RLog.w("RongDatabaseDao", "updateGroupUserInfo db is invalid");
      } else {
        ContentValues cv = new ContentValues();
        cv.put("group_id", userInfo.getGroupId());
        cv.put("user_id", userInfo.getUserId());
        cv.put("nickname", userInfo.getNickname());
        this.db.update("group_users", cv, "group_id=? and user_id=?", new String[]{userInfo.getGroupId(), userInfo.getUserId()});
      }
    } else {
      RLog.w("RongDatabaseDao", "updateGroupUserInfo parameter is invalid");
    }
  }

  synchronized void putGroupUserInfo(GroupUserInfo userInfo) {
    if (userInfo != null && userInfo.getGroupId() != null && userInfo.getUserId() != null) {
      if (this.db == null) {
        RLog.w("RongDatabaseDao", "putGroupUserInfo db is invalid");
      } else {
        this.db.execSQL("delete from group_users where group_id=? and user_id=?", new String[]{userInfo.getGroupId(), userInfo.getUserId()});
        this.db.execSQL("insert into group_users (group_id, user_id, nickname) values (?, ?, ?)", new String[]{userInfo.getGroupId(), userInfo.getUserId(), userInfo.getNickname()});
      }
    } else {
      RLog.w("RongDatabaseDao", "putGroupUserInfo parameter is invalid");
    }
  }

  Group getGroupInfo(String groupId) {
    if (groupId == null) {
      RLog.w("RongDatabaseDao", "getGroupInfo parameter is invalid");
      return null;
    } else if (this.db == null) {
      RLog.w("RongDatabaseDao", "getGroupInfo db is invalid");
      return null;
    } else {
      Cursor c = null;
      Group group = null;

      try {
        c = this.db.query("groups", (String[])null, "id = ?", new String[]{groupId}, (String)null, (String)null, (String)null);
        if (c != null && c.moveToFirst()) {
          String id = c.getString(c.getColumnIndex("id"));
          String name = c.getString(c.getColumnIndex("name"));
          String portrait = c.getString(c.getColumnIndex("portrait"));
          group = new Group(id, name, Uri.parse(portrait));
        }
      } finally {
        if (c != null) {
          c.close();
        }

      }

      return group;
    }
  }

  synchronized void insertGroupInfo(Group group) {
    if (group != null && group.getId() != null) {
      if (this.db == null) {
        RLog.w("RongDatabaseDao", "insertGroupInfo db is invalid");
      } else {
        ContentValues cv = new ContentValues();
        cv.put("id", group.getId());
        cv.put("name", group.getName());
        cv.put("portrait", group.getPortraitUri() + "");
        this.db.insert("groups", (String)null, cv);
      }
    } else {
      RLog.w("RongDatabaseDao", "insertGroupInfo parameter is invalid");
    }
  }

  synchronized void updateGroupInfo(Group group) {
    if (group != null && group.getId() != null) {
      if (this.db == null) {
        RLog.w("RongDatabaseDao", "updateGroupInfo db is invalid");
      } else {
        ContentValues cv = new ContentValues();
        cv.put("id", group.getId());
        cv.put("name", group.getName());
        cv.put("portrait", group.getPortraitUri() + "");
        this.db.update("groups", cv, "id = ?", new String[]{group.getId()});
      }
    } else {
      RLog.w("RongDatabaseDao", "updateGroupInfo parameter is invalid");
    }
  }

  synchronized void putGroupInfo(Group group) {
    if (group != null && group.getId() != null) {
      if (this.db == null) {
        RLog.w("RongDatabaseDao", "putGroupInfo db is invalid");
      } else {
        this.db.execSQL("replace into groups (id, name, portrait) values (?, ?, ?)", new String[]{group.getId(), group.getName(), group.getPortraitUri() == null ? "" : group.getPortraitUri().toString()});
      }
    } else {
      RLog.w("RongDatabaseDao", "putGroupInfo parameter is invalid");
    }
  }

  Discussion getDiscussionInfo(String discussionId) {
    if (discussionId == null) {
      RLog.w("RongDatabaseDao", "getDiscussionInfo parameter is invalid");
      return null;
    } else if (this.db == null) {
      RLog.w("RongDatabaseDao", "getDiscussionInfo db is invalid");
      return null;
    } else {
      Cursor c = null;
      Discussion discussion = null;

      try {
        c = this.db.query("discussions", (String[])null, "id = ?", new String[]{discussionId}, (String)null, (String)null, (String)null);
        if (c != null && c.moveToFirst()) {
          String id = c.getString(c.getColumnIndex("id"));
          String name = c.getString(c.getColumnIndex("name"));
          discussion = new Discussion(id, name);
        }
      } finally {
        if (c != null) {
          c.close();
        }

      }

      return discussion;
    }
  }

  synchronized void insertDiscussionInfo(Discussion discussion) {
    if (discussion != null && discussion.getId() != null) {
      if (this.db == null) {
        RLog.w("RongDatabaseDao", "insertDiscussionInfo db is invalid");
      } else {
        ContentValues cv = new ContentValues();
        cv.put("id", discussion.getId());
        cv.put("name", discussion.getName());
        cv.put("portrait", "");
        this.db.insert("discussions", (String)null, cv);
      }
    } else {
      RLog.w("RongDatabaseDao", "insertDiscussionInfo parameter is invalid");
    }
  }

  synchronized void updateDiscussionInfo(Discussion discussion) {
    if (discussion != null && discussion.getId() != null) {
      if (this.db == null) {
        RLog.w("RongDatabaseDao", "updateDiscussionInfo db is invalid");
      } else {
        ContentValues cv = new ContentValues();
        cv.put("id", discussion.getId());
        cv.put("name", discussion.getName());
        cv.put("portrait", "");
        this.db.update("discussions", cv, "id = ?", new String[]{discussion.getId()});
      }
    } else {
      RLog.w("RongDatabaseDao", "updateDiscussionInfo parameter is invalid");
    }
  }

  synchronized void putDiscussionInfo(Discussion discussion) {
    if (discussion != null && discussion.getId() != null) {
      if (this.db == null) {
        RLog.w("RongDatabaseDao", "putDiscussionInfo db is invalid");
      } else {
        this.db.execSQL("replace into discussions (id, name, portrait) values (?, ?, ?)", new String[]{discussion.getId(), discussion.getName(), ""});
      }
    } else {
      RLog.w("RongDatabaseDao", "putDiscussionInfo parameter is invalid");
    }
  }
}
